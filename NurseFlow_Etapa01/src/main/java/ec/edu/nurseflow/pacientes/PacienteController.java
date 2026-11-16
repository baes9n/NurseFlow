package ec.edu.nurseflow.pacientes;

import ec.edu.nurseflow.auth.AuthService;
import ec.edu.nurseflow.common.PaginaRespuesta;
import ec.edu.nurseflow.pacientes.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pacientes")
@Tag(name="Pacientes", description="Datos basicos ficticios de pacientes, sin historias clinicas")
public class PacienteController {
    private final PacienteService service;
    private final AuthService auth;
    public PacienteController(PacienteService service, AuthService auth) {
        this.service=service; this.auth=auth;
    }
    @PostMapping @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.CREATED)
    public PacienteRespuesta crear(@Valid @RequestBody PacienteRequest req, @AuthenticationPrincipal Jwt jwt) {
        return service.crear(req, auth.actual(jwt));
    }
    @GetMapping
    @Operation(summary="Buscar pacientes con filtros combinados y paginacion desde PostgreSQL")
    public PaginaRespuesta<PacienteRespuesta> listar(@RequestParam(defaultValue="") String buscar,
            @RequestParam(required=false) Boolean activo,
            @RequestParam(defaultValue="0") int pagina, @RequestParam(defaultValue="10") int tamano,
            @RequestParam(defaultValue="id") String ordenarPor,
            @RequestParam(defaultValue="asc") String direccion) {
        return service.listar(buscar, activo, pagina, tamano, ordenarPor, direccion);
    }
    @GetMapping("/{id}")
    public PacienteRespuesta obtener(@PathVariable Long id) { return service.obtener(id); }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public PacienteRespuesta editar(@PathVariable Long id, @Valid @RequestBody PacienteRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return service.editar(id, req, auth.actual(jwt));
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary="Baja logica sin eliminar informacion historica")
    public void desactivar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.desactivar(id, auth.actual(jwt));
    }
}
