package ec.edu.nurseflow.profesionales.dto;
import jakarta.validation.constraints.NotNull;
public record EstadoProfesionalRequest(@NotNull Boolean activo) {}
