package ec.edu.nurseflow.actividades;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ActividadRepository extends JpaRepository<Actividad, Long>, JpaSpecificationExecutor<Actividad> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Actividad a where a.id = :id")
    Optional<Actividad> bloquear(@Param("id") Long id);

    boolean existsByAsignacionId(Long asignacionId);
    boolean existsByAsignacionTurnoId(Long turnoId);

    @Query("select count(a) from Actividad a where a.asignacion.turno.id = :turnoId " +
        "and a.estado in :estados")
    long contarPorEstados(@Param("turnoId") Long turnoId,
        @Param("estados") Collection<EstadoActividad> estados);

    default long contarNoFinalizadas(Long turnoId) {
        return contarPorEstados(turnoId,List.of(EstadoActividad.PENDIENTE, EstadoActividad.EN_PROGRESO));
    }
}
