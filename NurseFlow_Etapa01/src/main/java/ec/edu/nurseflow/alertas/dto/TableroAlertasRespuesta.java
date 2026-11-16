package ec.edu.nurseflow.alertas.dto;
import java.time.Instant;
import java.util.List;
public record TableroAlertasRespuesta(Instant generadoEn, Instant umbralProximas,
    long vencidas, long proximas, List<AlertaRespuesta> alertas, boolean hayMas) {}
