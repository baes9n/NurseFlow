package ec.edu.nurseflow.profesionales.dto;

import jakarta.validation.constraints.*;
public record EditarProfesionalRequest(
    @NotBlank @Size(max=120) String nombres,
    @NotBlank @Size(max=120) String apellidos,
    @Size(max=40) String numeroLicencia
) {}
