package ec.edu.nurseflow.turnos;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class IntervalosTurnoTest {
    private static Instant hora(int h) {
        return Instant.parse("2026-11-16T" + String.format("%02d", h) + ":00:00Z");
    }

    @Test
    void rechazaSuperposicionParcial() {
        assertTrue(IntervalosTurno.seSuperponen(hora(7), hora(13), hora(11), hora(17)));
    }

    @Test
    void permiteTurnosConsecutivos() {
        assertFalse(IntervalosTurno.seSuperponen(hora(7), hora(13), hora(13), hora(19)));
    }

    @Test
    void detectaTurnoContenido() {
        assertTrue(IntervalosTurno.seSuperponen(hora(7), hora(19), hora(9), hora(10)));
    }

    @Test
    void rechazaIntervalosInvalidos() {
        assertThrows(IllegalArgumentException.class,
            () -> IntervalosTurno.seSuperponen(hora(13), hora(7), hora(7), hora(19)));
    }
}
