package ec.edu.nurseflow.turnos;

import java.time.Instant;
import java.util.Objects;

/**
 * Intervalos semiabiertos [inicio, fin).
 * Dos turnos consecutivos que se tocan en el extremo NO se superponen.
 * Este calculo se integrara con bloqueo transaccional en Etapa 03.
 */
public final class IntervalosTurno {
    private IntervalosTurno() {}

    public static boolean seSuperponen(Instant inicioA, Instant finA,
                                       Instant inicioB, Instant finB) {
        validar(inicioA, finA);
        validar(inicioB, finB);
        return inicioA.isBefore(finB) && inicioB.isBefore(finA);
    }

    private static void validar(Instant inicio, Instant fin) {
        Objects.requireNonNull(inicio, "Inicio obligatorio");
        Objects.requireNonNull(fin, "Fin obligatorio");
        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException("La hora final debe ser posterior a la inicial");
        }
    }
}
