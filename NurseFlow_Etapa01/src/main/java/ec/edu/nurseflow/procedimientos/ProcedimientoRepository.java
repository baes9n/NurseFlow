package ec.edu.nurseflow.procedimientos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface ProcedimientoRepository extends JpaRepository<Procedimiento, Long>, JpaSpecificationExecutor<Procedimiento> {}
