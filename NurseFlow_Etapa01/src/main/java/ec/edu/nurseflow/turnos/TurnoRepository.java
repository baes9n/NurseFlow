package ec.edu.nurseflow.turnos;

import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface TurnoRepository extends JpaRepository<Turno, Long>, JpaSpecificationExecutor<Turno> {
    // Bloquear la fila del turno serializa cambios de horario y nuevas asignaciones.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Turno t where t.id = :id")
    Optional<Turno> bloquear(@Param("id") Long id);
}
