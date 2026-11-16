package ec.edu.nurseflow.auth;

import ec.edu.nurseflow.auth.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name="Autenticacion", description="Inicio de sesion, revocacion de tokens y usuario actual")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/login")
    @Operation(summary="Iniciar sesion con correo y contrasena")
    public TokenRespuesta login(@Valid @RequestBody LoginRequest data) { return service.login(data); }

    @GetMapping("/yo")
    @Operation(summary="Consultar usuario de la sesion")
    public UsuarioRespuesta yo(@AuthenticationPrincipal Jwt jwt) {
        return UsuarioRespuesta.de(service.actual(jwt));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary="Revocar JWT actual en el servidor")
    public void logout(@AuthenticationPrincipal Jwt jwt) { service.logout(jwt); }

    @PatchMapping("/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary="Cambiar contrasena y revocar todos los tokens anteriores")
    public void contrasena(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CambiarContrasenaRequest req) {
        service.cambiarContrasena(jwt, req);
    }
}
