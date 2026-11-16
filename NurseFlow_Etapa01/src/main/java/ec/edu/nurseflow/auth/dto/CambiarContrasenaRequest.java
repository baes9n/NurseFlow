package ec.edu.nurseflow.auth.dto;
import jakarta.validation.constraints.*;
public record CambiarContrasenaRequest(@NotBlank String actual,
    @NotBlank @Size(min=12, max=100) String nueva) {}
