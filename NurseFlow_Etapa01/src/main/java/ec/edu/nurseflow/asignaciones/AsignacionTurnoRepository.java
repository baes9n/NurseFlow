package ec.edu.nurseflow.asignaciones;

import ec.edu.nurseflow.turnos.EstadoTurno;
import java.time.Instant;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

public interface AsignacionTurnoRepository extends JpaRepository<AsignacionTurno, Long>,
        JpaSpecificationExecutor<AsignacionTurno> {

    Optional<AsignacionTurno> findByTurnoIdAndProfesionalId(Long turnoId, Long profesionalId);
    List<AsignacionTurno> findByTurnoIdAndEstado(Long turnoId, EstadoAsignacion estado);

    @Query("select distinct a.profesional.id from AsignacionTurno a " +
        "where a.turno.id = :turnoId and a.estado = :estado order by a.profesional.id")
    List<Long> idsProfesionalesActivosPorTurno(@Param("turnoId") Long turnoId,
        @Param("estado") EstadoAsignacion estado);

    default List<Long> idsProfesionalesActivosPorTurno(Long id) {
        return idsProfesionalesActivosPorTurno(id, EstadoAsignacion.ACTIVA);
    }

    @Query("select count(a) from AsignacionTurno a " +
        "where a.profesional.id = :profesionalId and a.estado = :estadoAsignacion " +
        "and a.turno.estado <> :estadoCancelado " +
        "and a.turno.inicio < :fin and a.turno.fin > :inicio " +
        "and (:excluirTurnoId is null or a.turno.id <> :excluirTurnoId)")
    long contarSolapamientos(@Param("profesionalId") Long profesionalId,
        @Param("inicio") Instant inicio, @Param("fin") Instant fin,
        @Param("excluirTurnoId") Long excluirTurnoId,
        @Param("estadoAsignacion") EstadoAsignacion estadoAsignacion,
        @Param("estadoCancelado") EstadoTurno estadoCancelado);

    @Query("select case when count(a) > 0 then true else false end " +
        "from AsignacionTurno a where a.turno.id = :turnoId " +
        "and a.profesional.usuario.id = :usuarioId and a.estado = :estado")
    boolean tieneAsignacionActiva(@Param("turnoId") Long turnoId,
        @Param("usuarioId") Long usuarioId, @Param("estado") EstadoAsignacion estado);

    @Query("select case when count(a) > 0 then true else false end " +
        "from AsignacionTurno a where a.profesional.id = :profesionalId " +
        "and a.estado = :estadoAsignacion and a.turno.estado <> :estadoCancelado")
    boolean tieneAsignacionActivaProfesional(@Param("profesionalId") Long profesionalId,
        @Param("estadoAsignacion") EstadoAsignacion estadoAsignacion,
        @Param("estadoCancelado") EstadoTurno estadoCancelado);
}
