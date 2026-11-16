package ec.edu.nurseflow.reportes;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
class ReporteServiceTest {
    @Test void aceptaPeriodoValido(){assertDoesNotThrow(()->ReporteService.validar(
        Instant.parse("2026-11-01T00:00:00Z"),Instant.parse("2026-12-01T00:00:00Z")));}
    @Test void rechazaPeriodoInvertido(){assertThrows(IllegalArgumentException.class,()->ReporteService.validar(
        Instant.parse("2026-12-01T00:00:00Z"),Instant.parse("2026-11-01T00:00:00Z")));}
    @Test void rechazaPeriodoExtenso(){assertThrows(IllegalArgumentException.class,()->ReporteService.validar(
        Instant.parse("2025-01-01T00:00:00Z"),Instant.parse("2026-12-01T00:00:00Z")));}
}
