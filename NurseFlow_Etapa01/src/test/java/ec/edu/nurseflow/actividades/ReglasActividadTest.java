package ec.edu.nurseflow.actividades;

import static org.junit.jupiter.api.Assertions.*;
import ec.edu.nurseflow.common.ConflictoException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ReglasActividadTest {
    @Test void permiteIniciarYCompletar() {
        assertDoesNotThrow(() -> ReglasActividad.validarTransicion(
            EstadoActividad.PENDIENTE,EstadoActividad.EN_PROGRESO));
        assertDoesNotThrow(() -> ReglasActividad.validarTransicion(
            EstadoActividad.EN_PROGRESO,EstadoActividad.COMPLETADA));
    }
    @Test void completadasSonInmutables() {
        assertThrows(ConflictoException.class, () -> ReglasActividad.validarTransicion(
            EstadoActividad.COMPLETADA,EstadoActividad.EN_PROGRESO));
        assertThrows(ConflictoException.class, () -> ReglasActividad.validarTransicion(
            EstadoActividad.CANCELADA,EstadoActividad.COMPLETADA));
        assertThrows(ConflictoException.class, () -> ReglasActividad.validarTransicion(
            EstadoActividad.EN_PROGRESO,EstadoActividad.PENDIENTE));
    }
    @Test void vencimientoSoloParaAbiertasYPlazoSuperado() {
        Instant ahora=Instant.parse("2026-11-16T12:00:00Z");
        Instant ayer=ahora.minusSeconds(60);
        assertTrue(ReglasActividad.vencida(EstadoActividad.PENDIENTE,ayer,ahora));
        assertTrue(ReglasActividad.vencida(EstadoActividad.EN_PROGRESO,ayer,ahora));
        assertFalse(ReglasActividad.vencida(EstadoActividad.COMPLETADA,ayer,ahora));
        assertFalse(ReglasActividad.vencida(EstadoActividad.PENDIENTE,ahora,ahora));
    }
    @Test void fechaLimiteDebeEstarDentroDelTurno() {
        Instant inicio=Instant.parse("2026-11-16T07:00:00Z");
        Instant fin=Instant.parse("2026-11-16T13:00:00Z");
        assertDoesNotThrow(()->ReglasActividad.validarLimite(fin,inicio,fin));
        assertThrows(IllegalArgumentException.class,()->ReglasActividad.validarLimite(
            inicio.minusSeconds(1),inicio,fin));
    }
}
