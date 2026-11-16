package ec.edu.nurseflow.novedades.dto;
import ec.edu.nurseflow.novedades.*;
import java.time.Instant;
public record NovedadRespuesta(Long id, Long turnoId, Long registradoPorId,
    TipoNovedad tipo,String descripcion,Instant registradaEn) {
    public static NovedadRespuesta de(Novedad n) {
        return new NovedadRespuesta(n.getId(),n.getTurno().getId(),n.getRegistradoPor().getId(),
            n.getTipo(),n.getDescripcion(),n.getRegistradaEn());
    }
}
