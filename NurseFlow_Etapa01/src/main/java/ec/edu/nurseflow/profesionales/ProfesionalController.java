package ec.edu.nurseflow.profesionales;

import ec.edu.nurseflow.auth.AuthService;
import ec.edu.nurseflow.common.PaginaRespuesta;
import ec.edu.nurseflow.profesionales.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profesionales")
@Tag(name="Profesionales", description="Administracion del personal de enfermeria")
public class ProfesionalController {
    private final ProfesionalService service;
    private final AuthService auth;
    public ProfesionalController(ProfesionalService service, AuthService auth) {
        this.service=service; this.auth=auth;
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Registrar enfermeria con cuenta vinculada (administrador)")
    public ProfesionalRespuesta crear(@Valid @RequestBody CrearProfesionalRequest req, @AuthenticationPrincipal Jwt jwt) {
        return service.crear(req, auth.actual(jwt));
    }

    @GetMapping
    @Operation(summary="Listar personal con busqueda, filtro, orden y paginacion SQL")
    public PaginaRespuesta<ProfesionalRespuesta> listar(
        @RequestParam(defaultValue="") String buscar, @RequestParam(required=false) Boolean activo,
        @RequestParam(defaultValue="0") int pagina, @RequestParam(defaultValue="10") int tamano,
        @RequestParam(defaultValue="id") String ordenarPor,
        @RequestParam(defaultValue="asc") String direccion) {
        return service.listar(buscar, activo, pagina, tamano, ordenarPor, direccion);
    }
    @GetMapping("/{id}")
    public ProfesionalRespuesta obtener(@PathVariable Long id) { return service.obtener(id); }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ProfesionalRespuesta editar(@PathVariable Long id, @Valid @RequestBody EditarProfesionalRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return service.editar(id, req, auth.actual(jwt));
    }
    @PatchMapping("/{id}/estado") @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Activar/desactivar cuenta y perfil, sin borrar historial")
    public ProfesionalRespuesta cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoProfesionalRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return service.cambiarEstado(id, req.activo(), auth.actual(jwt));
    }
}
