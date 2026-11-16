package ec.edu.nurseflow.procedimientos;

import ec.edu.nurseflow.actividades.*;
import ec.edu.nurseflow.asignaciones.*;
import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.common.*;
import ec.edu.nurseflow.procedimientos.dto.*;
import ec.edu.nurseflow.turnos.*;
import ec.edu.nurseflow.usuarios.*;
import java.time.Instant;
import jakarta.persistence.EntityManager;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcedimientoService {
    private final ProcedimientoRepository procedimientos;
    private final ActividadRepository actividades;
    private final TurnoRepository turnos;
    private final AuditoriaService auditoria;
    private final EntityManager entityManager;
    public ProcedimientoService(ProcedimientoRepository procedimientos, ActividadRepository actividades,
            TurnoRepository turnos, AuditoriaService auditoria, EntityManager entityManager) {
        this.procedimientos=procedimientos;this.actividades=actividades;
        this.turnos=turnos;this.auditoria=auditoria;this.entityManager=entityManager;
    }
    @Transactional
    public ProcedimientoRespuesta registrar(ProcedimientoRequest req, Usuario actor) {
        Actividad preliminar=actividades.findById(req.actividadId())
            .orElseThrow(()->new NoEncontradoException("Actividad no encontrada"));
        // Mismo orden: TURNO -> ACTIVIDAD. Evita carrera con completar o cerrar.
        Turno turno=turnos.bloquear(preliminar.getAsignacion().getTurno().getId())
            .orElseThrow(()->new NoEncontradoException("Turno no encontrado"));
        Actividad actividad=actividades.bloquear(req.actividadId())
            .orElseThrow(()->new NoEncontradoException("Actividad no encontrada"));
        entityManager.refresh(actividad);
        if (turno.getEstado()==EstadoTurno.CERRADO || turno.getEstado()==EstadoTurno.CANCELADO)
            throw new ConflictoException("No se pueden registrar procedimientos de un turno cerrado");
        if (actividad.getAsignacion().getEstado()!=EstadoAsignacion.ACTIVA)
            throw new ConflictoException("La asignacion no esta activa");
        if (actividad.getEstado()!=EstadoActividad.EN_PROGRESO)
            throw new ConflictoException("El procedimiento exige una actividad EN_PROGRESO");
        if (!actividad.getAsignacion().getProfesional().getUsuario().getId().equals(actor.getId()))
            throw new AccessDeniedException("Solo el profesional responsable registra el procedimiento");
        Procedimiento p=new Procedimiento();
        p.setActividad(actividad);p.setProfesional(actividad.getAsignacion().getProfesional());
        p.setDetalle(req.detalle().strip());p.setObservaciones(req.observaciones()==null?null:req.observaciones().strip());
        p.setRealizadoEn(Instant.now());
        procedimientos.saveAndFlush(p);
        auditoria.registrar(actor,"REGISTRAR_PROCEDIMIENTO","Procedimiento",p.getId(),
            "Actividad " + actividad.getId());
        return ProcedimientoRespuesta.de(p);
    }
    @Transactional(readOnly=true)
    public PaginaRespuesta<ProcedimientoRespuesta> listar(Long actividadId, Long turnoId, Long profesionalId,
            int pagina, int tamano, String ordenarPor, String direccion, Usuario actor) {
        Pageable pageable=Paginacion.crear(pagina,tamano,ordenarPor,direccion,Set.of("id","realizadoEn"));
        Specification<Procedimiento> spec=(root,q,cb)->cb.conjunction();
        if (actividadId!=null) spec=spec.and((root,q,cb)->cb.equal(root.get("actividad").get("id"),actividadId));
        if (turnoId!=null) spec=spec.and((root,q,cb)->cb.equal(root.get("actividad").get("asignacion").get("turno").get("id"),turnoId));
        if (profesionalId!=null) spec=spec.and((root,q,cb)->cb.equal(root.get("profesional").get("id"),profesionalId));
        if (actor.getRol()==RolUsuario.ENFERMERIA)
            spec=spec.and((root,q,cb)->cb.equal(root.get("actividad").get("asignacion").get("profesional").get("usuario").get("id"),actor.getId()));
        return PaginaRespuesta.desde(procedimientos.findAll(spec,pageable).map(ProcedimientoRespuesta::de));
    }
    @Transactional(readOnly=true)
    public ProcedimientoRespuesta obtener(Long id, Usuario actor) {
        Procedimiento p=procedimientos.findById(id)
            .orElseThrow(()->new NoEncontradoException("Procedimiento no encontrado"));
        if (actor.getRol()==RolUsuario.ENFERMERIA &&
            !p.getActividad().getAsignacion().getProfesional().getUsuario().getId().equals(actor.getId()))
            throw new AccessDeniedException("Procedimiento ajeno");
        return ProcedimientoRespuesta.de(p);
    }
}
