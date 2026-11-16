package ec.edu.nurseflow.actividades.dto;
import ec.edu.nurseflow.actividades.*;
import java.time.Instant;

public record ActividadRespuesta(Long id, Long asignacionId, Long turnoId, Long profesionalId,
    Long pacienteId, String titulo, String descripcion, Instant fechaLimite, EstadoActividad estado,
    boolean vencida, Instant completadoEn, Long completadoPorId, Instant creadoEn, String profesionalNombre) {
    public static ActividadRespuesta de(Actividad a, Instant ahora) {
        return new ActividadRespuesta(a.getId(), a.getAsignacion().getId(),
            a.getAsignacion().getTurno().getId(), a.getAsignacion().getProfesional().getId(),
            a.getPaciente() == null ? null : a.getPaciente().getId(), a.getTitulo(), a.getDescripcion(),
            a.getFechaLimite(), a.getEstado(), ReglasActividad.vencida(a.getEstado(),a.getFechaLimite(),ahora),
            a.getCompletadoEn(), a.getCompletadoPor() == null ? null : a.getCompletadoPor().getId(),
            a.getCreadoEn(), a.getAsignacion().getProfesional().getNombres()+" "+a.getAsignacion().getProfesional().getApellidos());
    }
}
