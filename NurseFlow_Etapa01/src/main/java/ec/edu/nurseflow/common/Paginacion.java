package ec.edu.nurseflow.common;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Paginacion {
    private Paginacion() {}
    public static Pageable crear(int pagina, int tamano, String ordenarPor, String direccion, Set<String> permitidos) {
        if (pagina < 0 || tamano < 1 || tamano > 100) {
            throw new IllegalArgumentException("pagina >= 0 y tamano entre 1 y 100");
        }
        if (!permitidos.contains(ordenarPor)) throw new IllegalArgumentException("Campo de ordenamiento no permitido");
        if (!direccion.equalsIgnoreCase("asc") && !direccion.equalsIgnoreCase("desc"))
            throw new IllegalArgumentException("direccion debe ser asc o desc");
        Sort sort = Sort.by(Sort.Direction.fromString(direccion), ordenarPor);
        if (!ordenarPor.equals("id")) sort = sort.and(Sort.by(Sort.Direction.fromString(direccion), "id"));
        return PageRequest.of(pagina, tamano, sort);
    }
}
