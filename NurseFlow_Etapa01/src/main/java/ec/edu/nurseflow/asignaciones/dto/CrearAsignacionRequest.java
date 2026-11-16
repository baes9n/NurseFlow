package ec.edu.nurseflow.asignaciones.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearAsignacionRequest(
    @NotNull @Positive Long turnoId,
    @NotNull @Positive Long profesionalId
) {}
