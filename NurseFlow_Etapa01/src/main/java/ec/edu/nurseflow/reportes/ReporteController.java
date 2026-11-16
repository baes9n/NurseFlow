package ec.edu.nurseflow.reportes;

import ec.edu.nurseflow.auth.AuthService;
import ec.edu.nurseflow.reportes.dto.ReportePeriodoRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reportes")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name="Reportes", description="Indicadores del personal y exportacion PDF")
public class ReporteController {
    private final ReporteService service;
    private final AuthService auth;
    public ReporteController(ReporteService service, AuthService auth) { this.service=service;this.auth=auth; }

    @GetMapping("/actividades")
    @Operation(summary="Indicadores agrupados por profesional en un periodo de fecha limite")
    public ReportePeriodoRespuesta consultar(@RequestParam Instant desde,@RequestParam Instant hasta,
            @RequestParam(required=false) Long profesionalId) {
        return service.consultar(desde,hasta,profesionalId);
    }

    @GetMapping(value="/actividades.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary="Descargar PDF generado en el backend y auditar la exportacion")
    public ResponseEntity<byte[]> pdf(@RequestParam Instant desde,@RequestParam Instant hasta,
            @RequestParam(required=false) Long profesionalId,@AuthenticationPrincipal Jwt jwt) {
        byte[] bytes=service.exportarPdf(desde,hasta,profesionalId,auth.actual(jwt));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=nurseflow-reporte.pdf")
            .header(HttpHeaders.CACHE_CONTROL,"no-store")
            .body(bytes);
    }
}
