package ec.edu.nurseflow.profesionales.dto;

import jakarta.validation.constraints.*;
public record CrearProfesionalRequest(
    @NotBlank @Email @Size(max=160) String correo,
    @NotBlank @Size(min=12, max=100) String contrasena,
    @NotBlank @Size(max=120) String nombres,
    @NotBlank @Size(max=120) String apellidos,
    @Size(max=40) String numeroLicencia
) {}
