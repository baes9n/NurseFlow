package ec.edu.nurseflow.alertas;

import ec.edu.nurseflow.alertas.dto.TableroAlertasRespuesta;
import ec.edu.nurseflow.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alertas")
@Tag(name="Alertas operativas", description="Actividades abiertas vencidas o por vencer en las proximas 24 horas")
public class AlertaController {
    private final AlertaService alertas;
    private final AuthService auth;
    public AlertaController(AlertaService alertas, AuthService auth) { this.alertas=alertas; this.auth=auth; }
    @GetMapping
    @Operation(summary="Alertas por usuario; el personal solo ve las actividades de sus propias asignaciones")
    public TableroAlertasRespuesta consultar(@AuthenticationPrincipal Jwt jwt) {
        return alertas.consultar(auth.actual(jwt));
    }
}
