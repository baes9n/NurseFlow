package ec.edu.nurseflow.sistema;

import ec.edu.nurseflow.sistema.dto.EstadoSistemaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sistema")
@Tag(name = "Sistema", description = "Comprobaciones de la primera etapa")
public class EstadoSistemaController {
    private final EstadoSistemaService service;

    public EstadoSistemaController(EstadoSistemaService service) {
        this.service = service;
    }

    @GetMapping("/estado")
    @Operation(summary = "Comprobar API y conexion real con PostgreSQL")
    public EstadoSistemaResponse estado() {
        return service.comprobar();
    }
}
