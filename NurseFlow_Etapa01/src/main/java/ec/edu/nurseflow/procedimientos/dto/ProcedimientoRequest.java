package ec.edu.nurseflow.procedimientos.dto;
import jakarta.validation.constraints.*;
public record ProcedimientoRequest(@NotNull Long actividadId,
    @NotBlank @Size(max=3000) String detalle, @Size(max=3000) String observaciones) {}
