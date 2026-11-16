package ec.edu.nurseflow.novedades.dto;
import ec.edu.nurseflow.novedades.TipoNovedad;
import jakarta.validation.constraints.*;
public record NovedadRequest(@NotNull Long turnoId, @NotNull TipoNovedad tipo,
    @NotBlank @Size(max=3000) String descripcion) {}
