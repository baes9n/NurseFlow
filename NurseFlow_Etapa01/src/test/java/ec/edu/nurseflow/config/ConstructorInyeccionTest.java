package ec.edu.nurseflow.config;

import ec.edu.nurseflow.alertas.AlertaService;
import ec.edu.nurseflow.reportes.ReporteService;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstructorInyeccionTest {
    @Test
    void alertaServiceTieneConstructorElegidoPorSpring() {
        validarConstructorInyectable(AlertaService.class);
    }

    @Test
    void reporteServiceTieneConstructorElegidoPorSpring() {
        validarConstructorInyectable(ReporteService.class);
    }

    private void validarConstructorInyectable(Class<?> tipo) {
        Constructor<?>[] constructores = tipo.getDeclaredConstructors();
        long anotados = Arrays.stream(constructores)
            .filter(c -> c.isAnnotationPresent(Autowired.class))
            .count();
        assertEquals(1L, anotados, tipo.getSimpleName() + " debe tener un constructor @Autowired");
        assertTrue(Arrays.stream(constructores)
            .anyMatch(c -> c.isAnnotationPresent(Autowired.class) && Modifier.isPublic(c.getModifiers())),
            tipo.getSimpleName() + " debe exponer un constructor publico inyectable");
    }
}
