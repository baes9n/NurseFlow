package ec.edu.nurseflow.auditoria;

import ec.edu.nurseflow.usuarios.Usuario;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {
    private final AuditoriaRepository repository;
    public AuditoriaService(AuditoriaRepository repository) { this.repository = repository; }
    public void registrar(Usuario actor, String accion, String entidad, Long registroId, String detalle) {
        Auditoria a = new Auditoria();
        a.setUsuario(actor); a.setAccion(accion); a.setEntidad(entidad);
        a.setRegistroId(registroId); a.setDetalle(detalle);
        a.setCreadaEn(Instant.now());
        repository.save(a);
    }
}
