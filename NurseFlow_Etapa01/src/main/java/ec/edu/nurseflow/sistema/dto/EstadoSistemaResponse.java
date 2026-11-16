package ec.edu.nurseflow.sistema.dto;

import java.time.Instant;

public record EstadoSistemaResponse(
    String aplicacion,
    String estado,
    String baseDatos,
    Instant fechaHora
) {}
