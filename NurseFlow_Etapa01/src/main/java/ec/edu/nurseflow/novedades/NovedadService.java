package ec.edu.nurseflow.novedades;

import ec.edu.nurseflow.asignaciones.*;
import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.common.*;
import ec.edu.nurseflow.novedades.dto.*;
import ec.edu.nurseflow.turnos.*;
import ec.edu.nurseflow.usuarios.*;
import java.time.Instant;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NovedadService {
    private final NovedadRepository novedades;
    private final TurnoRepository turnos;
    private final AsignacionTurnoRepository asignaciones;
    private final AuditoriaService auditoria;
    public NovedadService(NovedadRepository novedades,TurnoRepository turnos,
            AsignacionTurnoRepository asignaciones,AuditoriaService auditoria) {
        this.novedades=novedades;this.turnos=turnos;
        this.asignaciones=asignaciones;this.auditoria=auditoria;
    }
    @Transactional
    public NovedadRespuesta registrar(NovedadRequest req,Usuario actor) {
        Turno turno=turnos.bloquear(req.turnoId())
            .orElseThrow(()->new NoEncontradoException("Turno no encontrado"));
        if (turno.getEstado()==EstadoTurno.CERRADO || turno.getEstado()==EstadoTurno.CANCELADO)
            throw new ConflictoException("No se registran novedades en turnos cerrados");
        verificarAcceso(turno.getId(),actor);
        Novedad n=new Novedad();n.setTurno(turno);n.setRegistradoPor(actor);
        n.setTipo(req.tipo());n.setDescripcion(req.descripcion().strip());n.setRegistradaEn(Instant.now());
        novedades.saveAndFlush(n);
        auditoria.registrar(actor,"REGISTRAR_NOVEDAD","Novedad",n.getId(),"Turno " + turno.getId());
        return NovedadRespuesta.de(n);
    }
    @Transactional(readOnly=true)
    public PaginaRespuesta<NovedadRespuesta> listar(Long turnoId,TipoNovedad tipo,int pagina,
            int tamano,String ordenarPor,String direccion,Usuario actor) {
        Pageable pageable=Paginacion.crear(pagina,tamano,ordenarPor,direccion,Set.of("id","registradaEn","tipo"));
        Specification<Novedad> spec=(root,q,cb)->cb.conjunction();
        if (turnoId!=null) spec=spec.and((root,q,cb)->cb.equal(root.get("turno").get("id"),turnoId));
        if (tipo!=null) spec=spec.and((root,q,cb)->cb.equal(root.get("tipo"),tipo));
        if (actor.getRol()==RolUsuario.ENFERMERIA) {
            spec=spec.and((root,q,cb)->{
                var sq=q.subquery(Long.class);
                var a=sq.from(AsignacionTurno.class);
                sq.select(a.get("id"));
                sq.where(cb.equal(a.get("turno").get("id"),root.get("turno").get("id")),
                    cb.equal(a.get("profesional").get("usuario").get("id"),actor.getId()),
                    cb.equal(a.get("estado"),EstadoAsignacion.ACTIVA));
                return cb.exists(sq);
            });
        }
        return PaginaRespuesta.desde(novedades.findAll(spec,pageable).map(NovedadRespuesta::de));
    }
    @Transactional(readOnly=true)
    public NovedadRespuesta obtener(Long id,Usuario actor) {
        Novedad n=novedades.findById(id)
            .orElseThrow(()->new NoEncontradoException("Novedad no encontrada"));
        verificarAcceso(n.getTurno().getId(),actor);
        return NovedadRespuesta.de(n);
    }
    private void verificarAcceso(Long turnoId, Usuario actor) {
        if (actor.getRol()==RolUsuario.ENFERMERIA &&
            !asignaciones.tieneAsignacionActiva(turnoId,actor.getId(),EstadoAsignacion.ACTIVA))
            throw new AccessDeniedException("Novedad de turno ajeno");
    }
}
