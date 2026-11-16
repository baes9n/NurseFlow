package ec.edu.nurseflow.turnos;

import ec.edu.nurseflow.auth.AuthService;
import ec.edu.nurseflow.common.PaginaRespuesta;
import ec.edu.nurseflow.turnos.dto.*;
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
@RequestMapping("/api/v1/turnos")
@Tag(name="Turnos", description="Programacion, control y cambios auditados de turnos")
public class TurnoController {
    private final TurnoService service;
    private final AuthService auth;
    public TurnoController(TurnoService service, AuthService auth) {
        this.service=service; this.auth=auth;
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Crear turno con horario valido")
    public TurnoRespuesta crear(@Valid @RequestBody TurnoRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return service.crear(req, auth.actual(jwt));
    }
    @GetMapping
    @Operation(summary="Listar con filtro de area, estado, periodo y paginacion SQL; enfermeria ve sus turnos")
    public PaginaRespuesta<TurnoRespuesta> listar(
        @RequestParam(defaultValue="") String area,
        @RequestParam(required=false) EstadoTurno estado,
        @RequestParam(required=false) Instant desde,
        @RequestParam(required=false) Instant hasta,
        @RequestParam(defaultValue="0") int pagina,
        @RequestParam(defaultValue="10") int tamano,
        @RequestParam(defaultValue="inicio") String ordenarPor,
        @RequestParam(defaultValue="asc") String direccion,
        @AuthenticationPrincipal Jwt jwt) {
        return service.listar(area, estado, desde, hasta, pagina, tamano,
            ordenarPor, direccion, auth.actual(jwt));
    }
    @GetMapping("/{id}")
    public TurnoRespuesta obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.obtener(id, auth.actual(jwt));
    }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Actualizar turno programado; vuelve a validar todas sus asignaciones")
    public TurnoRespuesta editar(@PathVariable Long id, @Valid @RequestBody TurnoRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return service.editar(id, req, auth.actual(jwt));
    }
    @PatchMapping("/{id}/iniciar") @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Pasar un turno programado a EN_CURSO")
    public TurnoRespuesta iniciar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.iniciar(id, auth.actual(jwt));
    }
    @PatchMapping("/{id}/cerrar") @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Cerrar un turno solo si no quedan actividades pendientes")
    public TurnoRespuesta cerrar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.cerrar(id, auth.actual(jwt));
    }
    @PatchMapping("/{id}/cancelar") @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Cancelar turno y sus asignaciones en una transaccion")
    public TurnoRespuesta cancelar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.cancelar(id, auth.actual(jwt));
    }
}
