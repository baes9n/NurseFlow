package ec.edu.nurseflow.procedimientos;
import ec.edu.nurseflow.auth.AuthService;
import ec.edu.nurseflow.common.PaginaRespuesta;
import ec.edu.nurseflow.procedimientos.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/procedimientos")
@Tag(name="Procedimientos", description="Registro inmutable de procedimientos del profesional responsable")
public class ProcedimientoController {
    private final ProcedimientoService service; private final AuthService auth;
    public ProcedimientoController(ProcedimientoService service,AuthService auth){this.service=service;this.auth=auth;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ENFERMERIA')")
    public ProcedimientoRespuesta registrar(@Valid @RequestBody ProcedimientoRequest req,
            @AuthenticationPrincipal Jwt jwt){return service.registrar(req,auth.actual(jwt));}
    @GetMapping
    public PaginaRespuesta<ProcedimientoRespuesta> listar(
        @RequestParam(required=false) Long actividadId,
        @RequestParam(required=false) Long turnoId,
        @RequestParam(required=false) Long profesionalId,
        @RequestParam(defaultValue="0") int pagina,
        @RequestParam(defaultValue="10") int tamano,
        @RequestParam(defaultValue="realizadoEn") String ordenarPor,
        @RequestParam(defaultValue="desc") String direccion,
        @AuthenticationPrincipal Jwt jwt){return service.listar(actividadId,turnoId,profesionalId,
            pagina,tamano,ordenarPor,direccion,auth.actual(jwt));}
    @GetMapping("/{id}")
    public ProcedimientoRespuesta obtener(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt){
        return service.obtener(id,auth.actual(jwt));}
}
