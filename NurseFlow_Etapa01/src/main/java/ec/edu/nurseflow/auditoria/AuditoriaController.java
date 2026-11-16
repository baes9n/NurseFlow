package ec.edu.nurseflow.auditoria;

import ec.edu.nurseflow.auditoria.dto.AuditoriaRespuesta;
import ec.edu.nurseflow.common.PaginaRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auditorias")
@Tag(name = "Auditoria", description = "Operaciones importantes registradas por el backend")
@PreAuthorize("hasRole('ADMIN')")
public class AuditoriaController {
    private final AuditoriaConsultaService servicio;

    public AuditoriaController(AuditoriaConsultaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Historial auditable filtrado por usuario, accion y entidad")
    public PaginaRespuesta<AuditoriaRespuesta> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String entidad,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            @RequestParam(defaultValue = "creadaEn") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direccion) {
        return servicio.listar(usuarioId, accion, entidad, pagina, tamano, ordenarPor, direccion);
    }
}
