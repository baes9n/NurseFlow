package ec.edu.nurseflow.profesionales;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfesionalRepository extends JpaRepository<Profesional, Long>, JpaSpecificationExecutor<Profesional> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Profesional p where p.id = :id")
    Optional<Profesional> bloquear(@Param("id") Long id);

    boolean existsByNumeroLicencia(String numeroLicencia);
    boolean existsByNumeroLicenciaAndIdNot(String numeroLicencia, Long id);
}
