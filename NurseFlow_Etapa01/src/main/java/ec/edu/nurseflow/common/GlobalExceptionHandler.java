package ec.edu.nurseflow.common;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> campos.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "Datos invalidos", req, campos);
    }
    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> badRequest(Exception ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST,
            ex instanceof IllegalArgumentException ? ex.getMessage() : "Cuerpo JSON invalido", req, Map.of());
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Parametro invalido: " + ex.getName(), req, Map.of());
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> noPermitido(AccessDeniedException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta operacion", req, Map.of());
    }
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ApiError> conflict(ConflictoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), req, Map.of());
    }
    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<ApiError> notFound(NoEncontradoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), req, Map.of());
    }
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiError> unauthenticated(CredencialesInvalidasException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.UNAUTHORIZED, ex.getMessage(), req, Map.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, "El registro entra en conflicto con datos existentes", req, Map.of());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado en {}", req.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
            "No se pudo procesar la solicitud", req, Map.of());
    }
    private ResponseEntity<ApiError> respuesta(HttpStatus status, String mensaje,
            HttpServletRequest req, Map<String, String> campos) {
        return ResponseEntity.status(status).body(new ApiError(
            Instant.now(), status.value(), status.getReasonPhrase(),
            mensaje, req.getRequestURI(), campos));
    }
}
