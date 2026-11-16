package ec.edu.nurseflow.actividades.dto;
import jakarta.validation.constraints.*;

public record CorreccionActividadRequest(
    @NotBlank @Size(max=160) String titulo,
    @Size(max=3000) String descripcion,
    @NotBlank @Size(min=10,max=500) String motivo
) {}
