package ec.edu.nurseflow.turnos;

import ec.edu.nurseflow.asignaciones.*;
import ec.edu.nurseflow.actividades.ActividadRepository;
import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.common.*;
import ec.edu.nurseflow.profesionales.*;
import ec.edu.nurseflow.turnos.dto.*;
import ec.edu.nurseflow.usuarios.*;
import jakarta.persistence.criteria.*;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TurnoService {
    private final TurnoRepository turnos;
    private final AsignacionTurnoRepository asignaciones;
    private final ProfesionalRepository profesionales;
    private final AuditoriaService auditoria;
    private final ActividadRepository actividades;

    public TurnoService(TurnoRepository turnos, AsignacionTurnoRepository asignaciones,
            ProfesionalRepository profesionales, AuditoriaService auditoria, ActividadRepository actividades) {
        this.turnos=turnos; this.asignaciones=asignaciones;
        this.profesionales=profesionales; this.auditoria=auditoria; this.actividades=actividades;
    }

    @Transactional
    public TurnoRespuesta crear(TurnoRequest req, Usuario actor) {
        validar(req);
        Turno turno = new Turno();
        turno.setArea(req.area().strip()); turno.setInicio(req.inicio()); turno.setFin(req.fin());
        turno.setEstado(EstadoTurno.PROGRAMADO); turno.setCreadoEn(Instant.now());
        turnos.saveAndFlush(turno);
        auditoria.registrar(actor, "CREAR_TURNO", "Turno", turno.getId(), "Turno programado");
        return TurnoRespuesta.de(turno);
    }

    @Transactional(readOnly=true)
    public TurnoRespuesta obtener(Long id, Usuario actor) {
        Turno t = turnos.findById(id)
            .orElseThrow(() -> new NoEncontradoException("Turno no encontrado"));
        verificarVisibilidad(t.getId(), actor);
        return TurnoRespuesta.de(t);
    }

    @Transactional(readOnly=true)
    public PaginaRespuesta<TurnoRespuesta> listar(String area, EstadoTurno estado,
            Instant desde, Instant hasta, int pagina, int tamano,
            String ordenarPor, String direccion, Usuario actor) {
        if (desde != null && hasta != null && !desde.isBefore(hasta))
            throw new IllegalArgumentException("desde debe ser anterior a hasta");
        Pageable page = Paginacion.crear(pagina, tamano, ordenarPor, direccion,
            Set.of("id", "inicio", "fin", "area", "estado"));
        Specification<Turno> spec = (root, q, cb) -> cb.conjunction();
        if (area != null && !area.isBlank()) {
            String busqueda = "%" + area.strip().toLowerCase(Locale.ROOT) + "%";
            spec=spec.and((root,q,cb) -> cb.like(cb.lower(root.get("area")), busqueda));
        }
        if (estado != null) spec=spec.and((root,q,cb) -> cb.equal(root.get("estado"), estado));
        // En el intervalo solicitado se muestran turnos que se solapan con [desde, hasta).
        if (desde != null) spec=spec.and((root,q,cb) -> cb.greaterThan(root.get("fin"), desde));
        if (hasta != null) spec=spec.and((root,q,cb) -> cb.lessThan(root.get("inicio"), hasta));
        if (actor.getRol() == RolUsuario.ENFERMERIA) {
            spec=spec.and((root,q,cb) -> {
                Subquery<Long> sq = q.subquery(Long.class);
                Root<AsignacionTurno> a = sq.from(AsignacionTurno.class);
                sq.select(a.get("id"));
                sq.where(cb.equal(a.get("turno").get("id"), root.get("id")),
                    cb.equal(a.get("profesional").get("usuario").get("id"), actor.getId()),
                    cb.equal(a.get("estado"), EstadoAsignacion.ACTIVA));
                return cb.exists(sq);
            });
        }
        return PaginaRespuesta.desde(turnos.findAll(spec, page).map(TurnoRespuesta::de));
    }

    @Transactional
    public TurnoRespuesta editar(Long id, TurnoRequest req, Usuario actor) {
        validar(req);
        // Orden de bloqueo compartido: TURNO -> PROFESIONALES (IDs ascendentes).
        Turno t = bloquear(id);
        if (t.getEstado() != EstadoTurno.PROGRAMADO)
            throw new ConflictoException("Solo se pueden editar turnos programados");
        if (actividades.existsByAsignacionTurnoId(id))
            throw new ConflictoException("No se cambia el horario de un turno con actividades registradas");
        List<Long> ids = asignaciones.idsProfesionalesActivosPorTurno(id);
        for (Long profesionalId : ids) {
            profesionales.bloquear(profesionalId)
                .orElseThrow(() -> new NoEncontradoException("Profesional no encontrado"));
            if (asignaciones.contarSolapamientos(profesionalId, req.inicio(), req.fin(), id,
                    EstadoAsignacion.ACTIVA, EstadoTurno.CANCELADO) > 0) {
                throw new ConflictoException("El cambio se superpone con otro turno de un profesional asignado: " + profesionalId);
            }
        }
        t.setArea(req.area().strip()); t.setInicio(req.inicio()); t.setFin(req.fin());
        turnos.saveAndFlush(t);
        auditoria.registrar(actor, "EDITAR_TURNO", "Turno", id, "Horario o area actualizados");
        return TurnoRespuesta.de(t);
    }

    @Transactional
    public TurnoRespuesta cancelar(Long id, Usuario actor) {
        Turno t = bloquear(id);
        if (t.getEstado() != EstadoTurno.PROGRAMADO)
            throw new ConflictoException("Solo se pueden cancelar turnos programados");
        if (actividades.existsByAsignacionTurnoId(id))
            throw new ConflictoException("No se cancela un turno con actividades registradas; cierrelo tras resolverlas");
        // Se cancelan las asignaciones en la MISMA transaccion; no quedan filas parciales.
        List<AsignacionTurno> activas = asignaciones.findByTurnoIdAndEstado(id, EstadoAsignacion.ACTIVA);
        for (AsignacionTurno a : activas) a.setEstado(EstadoAsignacion.CANCELADA);
        asignaciones.saveAll(activas);
        t.setEstado(EstadoTurno.CANCELADO);
        turnos.saveAndFlush(t);
        auditoria.registrar(actor, "CANCELAR_TURNO", "Turno", id,
            "Canceladas " + activas.size() + " asignaciones activas");
        return TurnoRespuesta.de(t);
    }

    @Transactional
    public TurnoRespuesta iniciar(Long id, Usuario actor) {
        Turno t=bloquear(id);
        if (t.getEstado()!=EstadoTurno.PROGRAMADO)
            throw new ConflictoException("Solo se inician turnos programados");
        t.setEstado(EstadoTurno.EN_CURSO);
        turnos.save(t);
        auditoria.registrar(actor,"INICIAR_TURNO","Turno",id,"Turno en curso");
        return TurnoRespuesta.de(t);
    }

    @Transactional
    public TurnoRespuesta cerrar(Long id, Usuario actor) {
        Turno t=bloquear(id);
        if (t.getEstado()!=EstadoTurno.EN_CURSO)
            throw new ConflictoException("Solo se cierran turnos en curso");
        long sinResolver=actividades.contarNoFinalizadas(id);
        if (sinResolver>0)
            throw new ConflictoException("No se puede cerrar el turno: "+sinResolver+" actividades sin resolver");
        t.setEstado(EstadoTurno.CERRADO);
        turnos.save(t);
        auditoria.registrar(actor,"CERRAR_TURNO","Turno",id,"Turno cerrado sin actividades pendientes");
        return TurnoRespuesta.de(t);
    }

    private void validar(TurnoRequest req) {
        if (req.inicio() == null || req.fin() == null || !req.inicio().isBefore(req.fin()))
            throw new IllegalArgumentException("El fin debe ser posterior al inicio");
    }
    private Turno bloquear(Long id) {
        return turnos.bloquear(id)
            .orElseThrow(() -> new NoEncontradoException("Turno no encontrado"));
    }
    private void verificarVisibilidad(Long id, Usuario actor) {
        if (actor.getRol() != RolUsuario.ADMIN &&
                !asignaciones.tieneAsignacionActiva(id, actor.getId(), EstadoAsignacion.ACTIVA))
            throw new org.springframework.security.access.AccessDeniedException("Turno ajeno");
    }
}
