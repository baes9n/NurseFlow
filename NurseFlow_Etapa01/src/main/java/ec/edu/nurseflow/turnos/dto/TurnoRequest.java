package ec.edu.nurseflow.turnos.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record TurnoRequest(
    @NotBlank @Size(max = 80) String area,
    @NotNull Instant inicio,
    @NotNull Instant fin
) {}
