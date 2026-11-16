package ec.edu.nurseflow.procedimientos.dto;
import ec.edu.nurseflow.procedimientos.Procedimiento;
import java.time.Instant;
public record ProcedimientoRespuesta(Long id, Long actividadId, Long profesionalId,
    String detalle, String observaciones, Instant realizadoEn) {
    public static ProcedimientoRespuesta de(Procedimiento p) {
        return new ProcedimientoRespuesta(p.getId(),p.getActividad().getId(),p.getProfesional().getId(),
            p.getDetalle(),p.getObservaciones(),p.getRealizadoEn());
    }
}
