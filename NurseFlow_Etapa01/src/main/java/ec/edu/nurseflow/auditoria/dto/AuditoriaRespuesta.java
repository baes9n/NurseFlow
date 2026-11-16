package ec.edu.nurseflow.auditoria.dto;
import ec.edu.nurseflow.auditoria.Auditoria;
import java.time.Instant;
public record AuditoriaRespuesta(Long id, String usuario, String accion, String entidad,
    Long registroId, String detalle, Instant fechaHora) {
    public static AuditoriaRespuesta de(Auditoria a) {
        return new AuditoriaRespuesta(a.getId(), a.getUsuario().getCorreo(), a.getAccion(),
            a.getEntidad(), a.getRegistroId(), a.getDetalle(), a.getCreadaEn());
    }
}
