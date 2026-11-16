package ec.edu.nurseflow.common;

import java.time.Instant;
import java.util.Map;

public record ApiError(
    Instant fechaHora,
    int codigo,
    String error,
    String mensaje,
    String ruta,
    Map<String, String> campos
) {}
