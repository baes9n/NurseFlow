package ec.edu.nurseflow.auditoria;

import ec.edu.nurseflow.auditoria.dto.AuditoriaRespuesta;
import ec.edu.nurseflow.common.PaginaRespuesta;
import ec.edu.nurseflow.common.Paginacion;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaConsultaService {
    private final AuditoriaRepository repo;

    public AuditoriaConsultaService(AuditoriaRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<AuditoriaRespuesta> listar(
            Long usuarioId, String accion, String entidad, int pagina, int tamano) {
        return listar(usuarioId, accion, entidad, pagina, tamano, "creadaEn", "desc");
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<AuditoriaRespuesta> listar(Long usuarioId, String accion, String entidad,
            int pagina, int tamano, String ordenarPor, String direccion) {
        Specification<Auditoria> spec = (root, query, cb) -> cb.conjunction();
        if (usuarioId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("usuario").get("id"), usuarioId));
        }
        if (accion != null && !accion.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("accion"), accion));
        }
        if (entidad != null && !entidad.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entidad"), entidad));
        }
        // La conversion se ejecuta con una transaccion activa para las relaciones JPA.
        return PaginaRespuesta.desde(repo.findAll(spec,
            Paginacion.crear(pagina, tamano, ordenarPor, direccion,
                Set.of("creadaEn", "accion", "entidad", "registroId", "usuario.correo")))
            .map(AuditoriaRespuesta::de));
    }
}
