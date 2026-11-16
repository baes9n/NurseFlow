package ec.edu.nurseflow.asignaciones.dto;

import ec.edu.nurseflow.asignaciones.*;
import java.time.Instant;

public record AsignacionRespuesta(Long id, Long turnoId, Long profesionalId,
        EstadoAsignacion estado, Instant asignadoEn,
        String profesionalNombre, String turnoArea, Instant turnoInicio) {
    public static AsignacionRespuesta de(AsignacionTurno a) {
        return new AsignacionRespuesta(a.getId(), a.getTurno().getId(),
            a.getProfesional().getId(), a.getEstado(), a.getAsignadoEn(),
            a.getProfesional().getNombres()+" "+a.getProfesional().getApellidos(),
            a.getTurno().getArea(), a.getTurno().getInicio());
    }
}
