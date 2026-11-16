package ec.edu.nurseflow.pacientes.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PacienteRequest(
    @NotBlank @Pattern(regexp="[A-Za-z0-9-]{3,40}", message="Codigo: 3 a 40 letras, numeros o guiones") String codigo,
    @NotBlank @Size(max=120) String nombres,
    @NotBlank @Size(max=120) String apellidos,
    @PastOrPresent LocalDate fechaNacimiento
) {}
