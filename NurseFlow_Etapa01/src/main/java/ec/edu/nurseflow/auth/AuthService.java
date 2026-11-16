package ec.edu.nurseflow.auth;

import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.auth.dto.*;
import ec.edu.nurseflow.common.CredencialesInvalidasException;
import ec.edu.nurseflow.usuarios.Usuario;
import ec.edu.nurseflow.usuarios.UsuarioRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwords;
    private final JwtEncoder jwtEncoder;
    private final TokenRevocadoRepository revocados;
    private final AuditoriaService auditoria;
    private final String issuer;
    private final long ttlSeconds;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwords,
            JwtEncoder jwtEncoder, TokenRevocadoRepository revocados, AuditoriaService auditoria,
            @Value("${nurseflow.auth.issuer}") String issuer,
            @Value("${nurseflow.auth.access-token-seconds}") long ttlSeconds) {
        if (ttlSeconds < 300 || ttlSeconds > 7200)
            throw new IllegalArgumentException("JWT_TTL_SECONDS debe estar entre 300 y 7200");
        this.usuarios = usuarios; this.passwords = passwords; this.jwtEncoder = jwtEncoder;
        this.revocados = revocados; this.auditoria = auditoria; this.issuer = issuer;
        this.ttlSeconds = ttlSeconds;
    }

    @Transactional
    public TokenRespuesta login(LoginRequest req) {
        String email = req.correo().trim().toLowerCase(java.util.Locale.ROOT);
        Usuario u = usuarios.findByCorreo(email).orElseThrow(CredencialesInvalidasException::new);
        if (!Boolean.TRUE.equals(u.getActivo()) || !passwords.matches(req.contrasena(), u.getPasswordHash()))
            throw new CredencialesInvalidasException();
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer).subject(u.getId().toString())
            .issuedAt(now).expiresAt(now.plusSeconds(ttlSeconds))
            .id(UUID.randomUUID().toString())
            .claim("version", u.getTokenVersion())
            .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        auditoria.registrar(u, "INICIO_SESION", "Usuario", u.getId(), "Inicio de sesion correcto");
        return new TokenRespuesta(token, "Bearer", ttlSeconds, UsuarioRespuesta.de(u));
    }

    @Transactional(readOnly = true)
    public Usuario actual(Jwt jwt) {
        return usuarios.findById(Long.valueOf(jwt.getSubject()))
            .filter(u -> Boolean.TRUE.equals(u.getActivo()))
            .orElseThrow(CredencialesInvalidasException::new);
    }

    @Transactional
    public void logout(Jwt jwt) {
        Usuario u = actual(jwt);
        if (!revocados.existsById(jwt.getId())) {
            revocados.save(new TokenRevocado(jwt.getId(), u, jwt.getExpiresAt()));
            auditoria.registrar(u, "CIERRE_SESION", "Usuario", u.getId(), "Token revocado");
        }
    }

    @Transactional
    public void cambiarContrasena(Jwt jwt, CambiarContrasenaRequest req) {
        Usuario u = actual(jwt);
        if (!passwords.matches(req.actual(), u.getPasswordHash())) throw new CredencialesInvalidasException();
        if (passwords.matches(req.nueva(), u.getPasswordHash()))
            throw new IllegalArgumentException("La nueva contrasena debe ser diferente a la anterior");
        u.setPasswordHash(passwords.encode(req.nueva()));
        u.setTokenVersion(u.getTokenVersion() + 1); // invalida TODOS los JWT anteriores
        usuarios.save(u);
        auditoria.registrar(u, "CAMBIO_CONTRASENA", "Usuario", u.getId(), "Cambio de contrasena");
    }
}
