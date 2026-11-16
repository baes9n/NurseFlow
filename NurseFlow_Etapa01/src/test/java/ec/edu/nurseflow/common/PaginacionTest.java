package ec.edu.nurseflow.common;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PaginacionTest {
    @Test void ordenamientoProtegido() {
        assertThrows(IllegalArgumentException.class,
            () -> Paginacion.crear(0,10,"passwordHash","asc",Set.of("id")));
    }
    @Test void tamanosControlados() {
        assertThrows(IllegalArgumentException.class,
            () -> Paginacion.crear(0,200,"id","asc",Set.of("id")));
        assertEquals(20, Paginacion.crear(1,20,"id","desc",Set.of("id")).getPageSize());
    }
}
