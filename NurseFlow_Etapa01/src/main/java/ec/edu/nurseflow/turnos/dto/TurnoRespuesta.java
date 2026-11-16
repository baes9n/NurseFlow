package ec.edu.nurseflow.turnos.dto;

import ec.edu.nurseflow.turnos.*;
import java.time.Instant;

public record TurnoRespuesta(Long id, String area, Instant inicio, Instant fin,
        EstadoTurno estado, Instant creadoEn) {
    public static TurnoRespuesta de(Turno turno) {
        return new TurnoRespuesta(turno.getId(), turno.getArea(), turno.getInicio(),
            turno.getFin(), turno.getEstado(), turno.getCreadoEn());
    }
}
