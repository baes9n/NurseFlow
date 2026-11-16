package ec.edu.nurseflow.actividades.dto;
import jakarta.validation.constraints.*;
import java.time.Instant;

public record EditarActividadRequest(
    Long pacienteId,
    @NotBlank @Size(max=160) String titulo,
    @Size(max=3000) String descripcion,
    @NotNull Instant fechaLimite
) {}
