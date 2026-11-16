package ec.edu.nurseflow.alertas.dto;
import java.time.Instant;
public record AlertaRespuesta(Long actividadId, String titulo, String estado, Instant fechaLimite,
    Long turnoId, Long profesionalId, String profesional, String prioridad) {}
