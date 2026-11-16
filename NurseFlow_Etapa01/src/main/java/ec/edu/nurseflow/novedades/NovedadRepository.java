package ec.edu.nurseflow.novedades;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface NovedadRepository extends JpaRepository<Novedad, Long>, JpaSpecificationExecutor<Novedad> {}
