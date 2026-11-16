package ec.edu.nurseflow.actividades;

import ec.edu.nurseflow.actividades.dto.*;
import ec.edu.nurseflow.auth.AuthService;
import ec.edu.nurseflow.common.PaginaRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/actividades")
@Tag(name="Actividades", description="Control y trazabilidad de actividades de enfermeria")
public class ActividadController {
    private final ActividadService service;
    private final AuthService auth;
    public ActividadController(ActividadService service, AuthService auth) { this.service=service; this.auth=auth; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Crear actividad en una asignacion activa con fecha limite dentro del turno")
    public ActividadRespuesta crear(@Valid @RequestBody ActividadRequest req, @AuthenticationPrincipal Jwt jwt) {
        return service.crear(req,auth.actual(jwt));
    }
    @GetMapping
    @Operation(summary="Consultar actividades con filtros combinados y paginacion en base de datos")
    public PaginaRespuesta<ActividadRespuesta> listar(
        @RequestParam(required=false) Long turnoId,
        @RequestParam(required=false) Long profesionalId,
        @RequestParam(required=false) Long pacienteId,
        @RequestParam(required=false) EstadoActividad estado,
        @RequestParam(required=false) Boolean vencidas,
        @RequestParam(defaultValue="") String texto,
        @RequestParam(required=false) Instant desde,
        @RequestParam(required=false) Instant hasta,
        @RequestParam(defaultValue="0") int pagina,
        @RequestParam(defaultValue="10") int tamano,
        @RequestParam(defaultValue="fechaLimite") String ordenarPor,
        @RequestParam(defaultValue="asc") String direccion,
        @AuthenticationPrincipal Jwt jwt) {
        return service.listar(turnoId,profesionalId,pacienteId,estado,vencidas,texto,desde,hasta,
            pagina,tamano,ordenarPor,direccion,auth.actual(jwt));
    }
    @GetMapping("/{id}")
    public ActividadRespuesta obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.obtener(id,auth.actual(jwt));
    }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ActividadRespuesta editar(@PathVariable Long id, @Valid @RequestBody EditarActividadRequest req,
            @AuthenticationPrincipal Jwt jwt) { return service.editar(id,req,auth.actual(jwt)); }
    @PatchMapping("/{id}/iniciar")
    public ActividadRespuesta iniciar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.iniciar(id,auth.actual(jwt));
    }
    @PatchMapping("/{id}/completar")
    public ActividadRespuesta completar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.completar(id,auth.actual(jwt));
    }
    @PatchMapping("/{id}/cancelar") @PreAuthorize("hasRole('ADMIN')")
    public ActividadRespuesta cancelar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.cancelar(id,auth.actual(jwt));
    }
    @PatchMapping("/{id}/corregir") @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Correccion excepcional de un registro cerrado o finalizado, con motivo y auditoria")
    public ActividadRespuesta corregir(@PathVariable Long id, @Valid @RequestBody CorreccionActividadRequest req,
            @AuthenticationPrincipal Jwt jwt) { return service.corregir(id,req,auth.actual(jwt)); }
}
