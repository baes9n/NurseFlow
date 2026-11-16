package ec.edu.nurseflow.actividades;

import ec.edu.nurseflow.common.ConflictoException;
import java.time.Instant;

/** Reglas sin persistencia: tambien se comprueban con pruebas unitarias. */
public final class ReglasActividad {
    private ReglasActividad() {}
    public static void validarTransicion(EstadoActividad actual, EstadoActividad destino) {
        if (actual == destino) throw new ConflictoException("La actividad ya se encuentra en ese estado");
        if (actual == EstadoActividad.COMPLETADA || actual == EstadoActividad.CANCELADA)
            throw new ConflictoException("Una actividad finalizada no admite cambios de estado");
        if (!(actual == EstadoActividad.PENDIENTE &&
                (destino == EstadoActividad.EN_PROGRESO || destino == EstadoActividad.COMPLETADA || destino == EstadoActividad.CANCELADA)
                || actual == EstadoActividad.EN_PROGRESO &&
                (destino == EstadoActividad.COMPLETADA || destino == EstadoActividad.CANCELADA)))
            throw new ConflictoException("Transicion de estado no permitida");
    }
    public static void validarLimite(Instant limite, Instant inicio, Instant fin) {
        if (limite == null || limite.isBefore(inicio) || limite.isAfter(fin))
            throw new IllegalArgumentException("La fecha limite debe estar dentro del horario del turno");
    }
    public static boolean vencida(EstadoActividad estado, Instant limite, Instant ahora) {
        return (estado == EstadoActividad.PENDIENTE || estado == EstadoActividad.EN_PROGRESO)
            && limite.isBefore(ahora);
    }
}
