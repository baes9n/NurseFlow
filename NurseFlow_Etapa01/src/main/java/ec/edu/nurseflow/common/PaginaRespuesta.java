package ec.edu.nurseflow.common;

import java.util.List;
import org.springframework.data.domain.Page;

public record PaginaRespuesta<T>(List<T> contenido, long totalElementos,
        int totalPaginas, int pagina, int tamano) {
    public static <T> PaginaRespuesta<T> desde(Page<T> page) {
        return new PaginaRespuesta<>(page.getContent(), page.getTotalElements(),
            page.getTotalPages(), page.getNumber(), page.getSize());
    }
}
