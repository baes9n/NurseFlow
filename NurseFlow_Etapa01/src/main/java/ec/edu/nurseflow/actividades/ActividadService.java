package ec.edu.nurseflow.actividades;

import ec.edu.nurseflow.actividades.dto.*;
import ec.edu.nurseflow.asignaciones.*;
import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.common.*;
import ec.edu.nurseflow.pacientes.*;
import ec.edu.nurseflow.turnos.*;
import ec.edu.nurseflow.usuarios.*;
import jakarta.persistence.criteria.*;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActividadService {
    private final ActividadRepository actividades;
    private final AsignacionTurnoRepository asignaciones;
    private final PacienteRepository pacientes;
    private final TurnoRepository turnos;
    private final AuditoriaService auditoria;
    private final EntityManager entityManager;
    public ActividadService(ActividadRepository actividades, AsignacionTurnoRepository asignaciones,
            PacienteRepository pacientes, TurnoRepository turnos, AuditoriaService auditoria, EntityManager entityManager) {
        this.actividades=actividades; this.asignaciones=asignaciones; this.pacientes=pacientes;
        this.turnos=turnos; this.auditoria=auditoria; this.entityManager=entityManager;
    }

    @Transactional
    public ActividadRespuesta crear(ActividadRequest req, Usuario actor) {
        AsignacionTurno preliminar = obtenerAsignacion(req.asignacionId());
        // El turno se bloquea antes de crear/cambiar actividades y antes de cancelar asignaciones.
        Turno t = bloquearTurno(preliminar.getTurno().getId());
        AsignacionTurno a = obtenerAsignacion(req.asignacionId());
        if (t.getEstado() != EstadoTurno.PROGRAMADO && t.getEstado() != EstadoTurno.EN_CURSO)
            throw new ConflictoException("No se pueden crear actividades en un turno cerrado o cancelado");
        if (a.getEstado()!=EstadoAsignacion.ACTIVA)
            throw new ConflictoException("La asignacion no esta activa");
        if (!Boolean.TRUE.equals(a.getProfesional().getActivo()))
            throw new ConflictoException("El profesional no esta activo");
        ReglasActividad.validarLimite(req.fechaLimite(), t.getInicio(), t.getFin());
        Actividad nueva = new Actividad();
        nueva.setAsignacion(a); nueva.setPaciente(obtenerPaciente(req.pacienteId()));
        nueva.setTitulo(req.titulo().strip()); nueva.setDescripcion(normalizar(req.descripcion()));
        nueva.setFechaLimite(req.fechaLimite()); nueva.setEstado(EstadoActividad.PENDIENTE);
        nueva.setCreadoEn(Instant.now());
        actividades.saveAndFlush(nueva);
        auditoria.registrar(actor, "CREAR_ACTIVIDAD", "Actividad", nueva.getId(),
            "Asignacion " + a.getId() + ", turno " + t.getId());
        return ActividadRespuesta.de(nueva, Instant.now());
    }

    @Transactional(readOnly=true)
    public ActividadRespuesta obtener(Long id, Usuario actor) {
        Actividad a = actividades.findById(id)
            .orElseThrow(() -> new NoEncontradoException("Actividad no encontrada"));
        comprobarAcceso(a,actor);
        return ActividadRespuesta.de(a, Instant.now());
    }

    @Transactional(readOnly=true)
    public PaginaRespuesta<ActividadRespuesta> listar(Long turnoId, Long profesionalId, Long pacienteId,
            EstadoActividad estado, Boolean vencidas, String texto, Instant desde, Instant hasta,
            int pagina, int tamano, String ordenarPor, String direccion, Usuario actor) {
        if (desde != null && hasta != null && desde.isAfter(hasta))
            throw new IllegalArgumentException("desde no puede ser posterior a hasta");
        Pageable page = Paginacion.crear(pagina, tamano, ordenarPor, direccion,
            Set.of("id", "fechaLimite", "estado", "titulo", "creadoEn", "asignacion.profesional.apellidos"));
        Specification<Actividad> spec = (root,q,cb) -> cb.conjunction();
        if (turnoId!=null) spec=spec.and((root,q,cb) -> cb.equal(root.get("asignacion").get("turno").get("id"),turnoId));
        if (profesionalId!=null) spec=spec.and((root,q,cb) -> cb.equal(root.get("asignacion").get("profesional").get("id"),profesionalId));
        if (pacienteId!=null) spec=spec.and((root,q,cb) -> cb.equal(root.get("paciente").get("id"),pacienteId));
        if (estado!=null) spec=spec.and((root,q,cb) -> cb.equal(root.get("estado"),estado));
        if (texto!=null && !texto.isBlank()) {
            String pattern="%"+texto.strip().toLowerCase(Locale.ROOT)+"%";
            spec=spec.and((root,q,cb) -> cb.like(cb.lower(root.get("titulo")),pattern));
        }
        if (desde!=null) spec=spec.and((root,q,cb) -> cb.greaterThanOrEqualTo(root.get("fechaLimite"),desde));
        if (hasta!=null) spec=spec.and((root,q,cb) -> cb.lessThanOrEqualTo(root.get("fechaLimite"),hasta));
        if (vencidas!=null) {
            Instant ahora=Instant.now();
            spec=spec.and((root,q,cb) -> {
                Predicate sinCompletar=root.get("estado").in(EstadoActividad.PENDIENTE, EstadoActividad.EN_PROGRESO);
                Predicate expirado=cb.lessThan(root.get("fechaLimite"),ahora);
                Predicate vencida=cb.and(sinCompletar,expirado);
                return vencidas ? vencida : cb.not(vencida);
            });
        }
        if (actor.getRol()==RolUsuario.ENFERMERIA)
            spec=spec.and((root,q,cb)->cb.equal(root.get("asignacion").get("profesional").get("usuario").get("id"),actor.getId()));
        Instant ahora=Instant.now();
        return PaginaRespuesta.desde(actividades.findAll(spec,page).map(a->ActividadRespuesta.de(a,ahora)));
    }

    @Transactional
    public ActividadRespuesta editar(Long id, EditarActividadRequest req, Usuario actor) {
        Actividad a = bloquearActividadYTurno(id);
        if (a.getEstado()!=EstadoActividad.PENDIENTE)
            throw new ConflictoException("Solo se editan actividades pendientes; use correccion administrativa si esta finalizada");
        comprobarOperable(a);
        Turno t = a.getAsignacion().getTurno();
        ReglasActividad.validarLimite(req.fechaLimite(),t.getInicio(),t.getFin());
        a.setPaciente(obtenerPaciente(req.pacienteId()));
        a.setTitulo(req.titulo().strip()); a.setDescripcion(normalizar(req.descripcion()));
        a.setFechaLimite(req.fechaLimite());
        actividades.save(a);
        auditoria.registrar(actor,"EDITAR_ACTIVIDAD","Actividad",id,"Edicion antes de iniciar");
        return ActividadRespuesta.de(a,Instant.now());
    }

    @Transactional
    public ActividadRespuesta iniciar(Long id, Usuario actor) {
        Actividad a = bloquearActividadYTurno(id);
        comprobarAcceso(a,actor);
        comprobarOperable(a);
        ReglasActividad.validarTransicion(a.getEstado(),EstadoActividad.EN_PROGRESO);
        a.setEstado(EstadoActividad.EN_PROGRESO);
        actividades.save(a);
        auditoria.registrar(actor,"INICIAR_ACTIVIDAD","Actividad",id,"Actividad en progreso");
        return ActividadRespuesta.de(a,Instant.now());
    }

    @Transactional
    public ActividadRespuesta completar(Long id, Usuario actor) {
        Actividad a = bloquearActividadYTurno(id);
        comprobarAcceso(a,actor);
        comprobarOperable(a);
        ReglasActividad.validarTransicion(a.getEstado(),EstadoActividad.COMPLETADA);
        a.setEstado(EstadoActividad.COMPLETADA);
        a.setCompletadoEn(Instant.now()); a.setCompletadoPor(actor);
        actividades.saveAndFlush(a);  // la restriccion SQL comprueba usuario y fecha
        auditoria.registrar(actor,"COMPLETAR_ACTIVIDAD","Actividad",id,
            "Completada por usuario " + actor.getId());
        return ActividadRespuesta.de(a,Instant.now());
    }

    @Transactional
    public ActividadRespuesta cancelar(Long id, Usuario actor) {
        Actividad a = bloquearActividadYTurno(id);
        comprobarOperable(a);
        ReglasActividad.validarTransicion(a.getEstado(),EstadoActividad.CANCELADA);
        a.setEstado(EstadoActividad.CANCELADA);
        actividades.save(a);
        auditoria.registrar(actor,"CANCELAR_ACTIVIDAD","Actividad",id,"Cancelacion administrativa");
        return ActividadRespuesta.de(a,Instant.now());
    }

    @Transactional
    public ActividadRespuesta corregir(Long id, CorreccionActividadRequest req, Usuario actor) {
        Actividad a = bloquearActividadYTurno(id);
        // Excepcion explicita, solo ADMIN, con motivo y trazabilidad (incluso turno cerrado).
        String anterior="titulo="+a.getTitulo()+", descripcion="+a.getDescripcion();
        a.setTitulo(req.titulo().strip()); a.setDescripcion(normalizar(req.descripcion()));
        actividades.save(a);
        auditoria.registrar(actor,"CORREGIR_ACTIVIDAD","Actividad",id,
            "Motivo: "+req.motivo().strip()+"; antes: "+limitar(anterior,1000)+
            "; despues: "+limitar("titulo="+a.getTitulo()+", descripcion="+a.getDescripcion(),1000));
        return ActividadRespuesta.de(a,Instant.now());
    }

    private Actividad bloquearActividadYTurno(Long id) {
        Actividad preliminar=actividades.findById(id)
            .orElseThrow(()->new NoEncontradoException("Actividad no encontrada"));
        bloquearTurno(preliminar.getAsignacion().getTurno().getId());
        Actividad bloqueada=actividades.bloquear(id)
            .orElseThrow(()->new NoEncontradoException("Actividad no encontrada"));
        entityManager.refresh(bloqueada);
        return bloqueada;
    }
    private void comprobarOperable(Actividad a) {
        if (a.getAsignacion().getEstado()!=EstadoAsignacion.ACTIVA)
            throw new ConflictoException("La asignacion esta cancelada");
        EstadoTurno e=a.getAsignacion().getTurno().getEstado();
        if (e==EstadoTurno.CERRADO || e==EstadoTurno.CANCELADO)
            throw new ConflictoException("El turno esta cerrado o cancelado");
    }
    private void comprobarAcceso(Actividad a, Usuario actor) {
        if (actor.getRol()==RolUsuario.ENFERMERIA &&
                !a.getAsignacion().getProfesional().getUsuario().getId().equals(actor.getId()))
            throw new AccessDeniedException("Actividad ajena");
    }
    private AsignacionTurno obtenerAsignacion(Long id) {
        return asignaciones.findById(id)
            .orElseThrow(()->new NoEncontradoException("Asignacion no encontrada"));
    }
    private Turno bloquearTurno(Long id) {
        return turnos.bloquear(id)
            .orElseThrow(()->new NoEncontradoException("Turno no encontrado"));
    }
    private Paciente obtenerPaciente(Long id) {
        if (id==null) return null;
        Paciente p=pacientes.findById(id)
            .orElseThrow(()->new NoEncontradoException("Paciente no encontrado"));
        if (!Boolean.TRUE.equals(p.getActivo()))
            throw new ConflictoException("No se puede asociar un paciente inactivo");
        return p;
    }
    private String normalizar(String s) { return s==null || s.isBlank() ? null : s.strip(); }
    private String limitar(String s, int length) { return s.length()<=length?s:s.substring(0,length); }
}
