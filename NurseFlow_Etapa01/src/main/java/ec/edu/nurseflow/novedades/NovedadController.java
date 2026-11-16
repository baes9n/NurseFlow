package ec.edu.nurseflow.novedades;
import ec.edu.nurseflow.auth.AuthService;
import ec.edu.nurseflow.common.PaginaRespuesta;
import ec.edu.nurseflow.novedades.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/novedades")
@Tag(name="Novedades", description="Historial de incidencias y observaciones de turno")
public class NovedadController {
    private final NovedadService service; private final AuthService auth;
    public NovedadController(NovedadService service,AuthService auth){this.service=service;this.auth=auth;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public NovedadRespuesta registrar(@Valid @RequestBody NovedadRequest req,@AuthenticationPrincipal Jwt jwt){
        return service.registrar(req,auth.actual(jwt));
    }
    @GetMapping
    public PaginaRespuesta<NovedadRespuesta> listar(@RequestParam(required=false) Long turnoId,
        @RequestParam(required=false) TipoNovedad tipo,
        @RequestParam(defaultValue="0") int pagina, @RequestParam(defaultValue="10") int tamano,
        @RequestParam(defaultValue="registradaEn") String ordenarPor,
        @RequestParam(defaultValue="desc") String direccion,@AuthenticationPrincipal Jwt jwt){
        return service.listar(turnoId,tipo,pagina,tamano,ordenarPor,direccion,auth.actual(jwt));
    }
    @GetMapping("/{id}")
    public NovedadRespuesta obtener(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt){
        return service.obtener(id,auth.actual(jwt));
    }
}
