package ec.edu.nurseflow.asignaciones;

import ec.edu.nurseflow.asignaciones.dto.*;
import ec.edu.nurseflow.actividades.ActividadRepository;
import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.common.*;
import ec.edu.nurseflow.profesionales.*;
import ec.edu.nurseflow.turnos.*;
import ec.edu.nurseflow.usuarios.*;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsignacionTurnoService {
    private final AsignacionTurnoRepository asignaciones;
    private final TurnoRepository turnos;
    private final ProfesionalRepository profesionales;
    private final AuditoriaService auditoria;
    private final EntityManager entityManager;
    private final ActividadRepository actividades;

    public AsignacionTurnoService(AsignacionTurnoRepository asignaciones, TurnoRepository turnos,
            ProfesionalRepository profesionales, AuditoriaService auditoria, EntityManager entityManager, ActividadRepository actividades) {
        this.asignaciones=asignaciones; this.turnos=turnos;
        this.profesionales=profesionales; this.auditoria=auditoria;
        this.entityManager=entityManager; this.actividades=actividades;
    }

    @Transactional
    public AsignacionRespuesta crear(CrearAsignacionRequest req, Usuario actor) {
        // Las operaciones concurrentes bloquean filas en el mismo orden:
        // primero TURNO, luego PROFESIONAL. Ninguna asignacion incompatible se confirma.
        Turno t = turnos.bloquear(req.turnoId())
            .orElseThrow(() -> new NoEncontradoException("Turno no encontrado"));
        if (t.getEstado() != EstadoTurno.PROGRAMADO)
            throw new ConflictoException("No se puede asignar personal a un turno que no este programado");
        Profesional p = profesionales.bloquear(req.profesionalId())
            .orElseThrow(() -> new NoEncontradoException("Profesional no encontrado"));
        if (!Boolean.TRUE.equals(p.getActivo()) || !Boolean.TRUE.equals(p.getUsuario().getActivo()))
            throw new ConflictoException("El profesional esta inactivo");

        Optional<AsignacionTurno> existente = asignaciones.findByTurnoIdAndProfesionalId(t.getId(), p.getId());
        if (existente.isPresent() && existente.get().getEstado() == EstadoAsignacion.ACTIVA)
            throw new ConflictoException("El profesional ya tiene una asignacion activa en este turno");
        long conflictos = asignaciones.contarSolapamientos(p.getId(), t.getInicio(), t.getFin(), t.getId(),
            EstadoAsignacion.ACTIVA, EstadoTurno.CANCELADO);
        if (conflictos > 0) throw new ConflictoException("El profesional tiene otro turno superpuesto");

        AsignacionTurno a = existente.orElseGet(AsignacionTurno::new);
        a.setTurno(t); a.setProfesional(p);
        a.setEstado(EstadoAsignacion.ACTIVA); a.setAsignadoEn(Instant.now());
        asignaciones.saveAndFlush(a);
        auditoria.registrar(actor, existente.isPresent() ? "REACTIVAR_ASIGNACION" : "CREAR_ASIGNACION",
            "AsignacionTurno", a.getId(), "Turno " + t.getId() + ", profesional " + p.getId());
        return AsignacionRespuesta.de(a);
    }

    @Transactional(readOnly=true)
    public AsignacionRespuesta obtener(Long id, Usuario actor) {
        AsignacionTurno a = asignaciones.findById(id)
            .orElseThrow(() -> new NoEncontradoException("Asignacion no encontrada"));
        comprobarAcceso(a, actor);
        return AsignacionRespuesta.de(a);
    }

    @Transactional(readOnly=true)
    public PaginaRespuesta<AsignacionRespuesta> listar(Long turnoId, Long profesionalId,
            EstadoAsignacion estado, int pagina, int tamano, String ordenarPor,
            String direccion, Usuario actor) {
        return listar(turnoId, profesionalId, estado, "", pagina, tamano, ordenarPor, direccion, actor);
    }

    @Transactional(readOnly=true)
    public PaginaRespuesta<AsignacionRespuesta> listar(Long turnoId, Long profesionalId,
            EstadoAsignacion estado, String buscar, int pagina, int tamano, String ordenarPor,
            String direccion, Usuario actor) {
        Pageable page = Paginacion.crear(pagina, tamano, ordenarPor, direccion,
            Set.of("id", "asignadoEn", "estado"));
        Specification<AsignacionTurno> spec = (root,q,cb) -> cb.conjunction();
        if (turnoId != null) spec=spec.and((root,q,cb) -> cb.equal(root.get("turno").get("id"), turnoId));
        if (profesionalId != null) spec=spec.and((root,q,cb) -> cb.equal(root.get("profesional").get("id"), profesionalId));
        if (estado != null) spec=spec.and((root,q,cb) -> cb.equal(root.get("estado"), estado));
        if (buscar != null && !buscar.isBlank()) {
            String criterio = "%" + buscar.strip().toLowerCase(Locale.ROOT) + "%";
            spec=spec.and((root,q,cb) -> {
                var profesional=root.join("profesional");
                var turno=root.join("turno");
                var filtros=new ArrayList<jakarta.persistence.criteria.Predicate>();
                filtros.add(cb.like(cb.lower(profesional.get("nombres")), criterio));
                filtros.add(cb.like(cb.lower(profesional.get("apellidos")), criterio));
                filtros.add(cb.like(cb.lower(turno.get("area")), criterio));
                if (buscar.strip().matches("[0-9]{1,15}")) {
                    Long id=Long.parseLong(buscar.strip());
                    filtros.add(cb.equal(root.get("id"),id));
                    filtros.add(cb.equal(turno.get("id"),id));
                }
                return cb.or(filtros.toArray(new jakarta.persistence.criteria.Predicate[0]));
            });
        }
        if (actor.getRol() == RolUsuario.ENFERMERIA) {
            spec=spec.and((root,q,cb) -> cb.equal(root.get("profesional").get("usuario").get("id"), actor.getId()));
        }
        return PaginaRespuesta.desde(asignaciones.findAll(spec, page).map(AsignacionRespuesta::de));
    }

    @Transactional
    public void cancelar(Long id, Usuario actor) {
        AsignacionTurno a = asignaciones.findById(id)
            .orElseThrow(() -> new NoEncontradoException("Asignacion no encontrada"));
        // Bloquear el turno antes que el profesional evita carreras con editar/cancelar turno.
        Turno t = turnos.bloquear(a.getTurno().getId())
            .orElseThrow(() -> new NoEncontradoException("Turno no encontrado"));
        profesionales.bloquear(a.getProfesional().getId())
            .orElseThrow(() -> new NoEncontradoException("Profesional no encontrado"));
        entityManager.refresh(a); // leer estado confirmado tras adquirir bloqueos
        if (t.getEstado() != EstadoTurno.PROGRAMADO)
            throw new ConflictoException("Solo se pueden retirar asignaciones de turnos programados");
        if (a.getEstado() != EstadoAsignacion.ACTIVA)
            throw new ConflictoException("La asignacion ya esta cancelada");
        if (actividades.existsByAsignacionId(id))
            throw new ConflictoException("No se retira una asignacion con actividades registradas");
        a.setEstado(EstadoAsignacion.CANCELADA);
        asignaciones.save(a);
        auditoria.registrar(actor, "CANCELAR_ASIGNACION", "AsignacionTurno", id,
            "Turno " + t.getId() + ", profesional " + a.getProfesional().getId());
    }

    private void comprobarAcceso(AsignacionTurno a, Usuario actor) {
        if (actor.getRol() == RolUsuario.ENFERMERIA &&
                !a.getProfesional().getUsuario().getId().equals(actor.getId()))
            throw new AccessDeniedException("Asignacion ajena");
    }
}
