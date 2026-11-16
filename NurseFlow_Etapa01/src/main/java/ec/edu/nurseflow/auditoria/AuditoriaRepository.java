package ec.edu.nurseflow.auditoria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long>, JpaSpecificationExecutor<Auditoria> {
    // Evita consultas N+1 y carga el usuario necesario para construir el DTO.
    // Es una relacion ManyToOne, segura para paginar junto al registro principal.
    @Override
    @EntityGraph(attributePaths = "usuario")
    Page<Auditoria> findAll(Specification<Auditoria> spec, Pageable pageable);
}
