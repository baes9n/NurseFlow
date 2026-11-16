package ec.edu.nurseflow.reportes.dto;
import java.time.Instant;
import java.util.List;
public record ReportePeriodoRespuesta(Instant desde, Instant hasta, Instant generadoEn,
    Long profesionalId, long total, long pendientes, long enProgreso, long completadas,
    long canceladas, long vencidas, List<ResumenProfesional> profesionales) {}
