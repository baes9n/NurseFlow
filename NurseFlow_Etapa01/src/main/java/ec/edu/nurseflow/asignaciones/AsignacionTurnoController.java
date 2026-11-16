package ec.edu.nurseflow.asignaciones;

import ec.edu.nurseflow.asignaciones.dto.*;
import ec.edu.nurseflow.auth.AuthService;
import ec.edu.nurseflow.common.PaginaRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/asignaciones")
@Tag(name="Asignaciones", description="Asignacion sin superposiciones, con control transaccional")
public class AsignacionTurnoController {
    private final AsignacionTurnoService service;
    private final AuthService auth;
    public AsignacionTurnoController(AsignacionTurnoService service, AuthService auth) {
        this.service=service; this.auth=auth;
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Asignar profesional, detectar duplicados y rechazar turnos superpuestos")
    public AsignacionRespuesta crear(@Valid @RequestBody CrearAsignacionRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return service.crear(req, auth.actual(jwt));
    }
    @GetMapping
    @Operation(summary="Listar asignaciones; enfermeria solo puede consultar las propias")
    public PaginaRespuesta<AsignacionRespuesta> listar(
        @RequestParam(required=false) Long turnoId,
        @RequestParam(required=false) Long profesionalId,
        @RequestParam(required=false) EstadoAsignacion estado,
        @RequestParam(defaultValue="") String buscar,
        @RequestParam(defaultValue="0") int pagina,
        @RequestParam(defaultValue="10") int tamano,
        @RequestParam(defaultValue="id") String ordenarPor,
        @RequestParam(defaultValue="asc") String direccion,
        @AuthenticationPrincipal Jwt jwt) {
        return service.listar(turnoId, profesionalId, estado, buscar, pagina, tamano,
            ordenarPor, direccion, auth.actual(jwt));
    }
    @GetMapping("/{id}")
    public AsignacionRespuesta obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.obtener(id, auth.actual(jwt));
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Retirar asignacion sin borrar historial (baja logica)")
    public void cancelar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.cancelar(id, auth.actual(jwt));
    }
}
