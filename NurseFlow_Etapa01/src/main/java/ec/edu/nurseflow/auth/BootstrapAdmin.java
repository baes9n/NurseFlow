package ec.edu.nurseflow.auth;

import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.usuarios.*;
import java.time.Instant;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapAdmin implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BootstrapAdmin.class);
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoria;
    private final String email;
    private final String password;

    public BootstrapAdmin(UsuarioRepository usuarios, PasswordEncoder encoder, AuditoriaService auditoria,
            @Value("${nurseflow.bootstrap.admin-email}") String email,
            @Value("${nurseflow.bootstrap.admin-password}") String password) {
        this.usuarios=usuarios; this.encoder=encoder; this.auditoria=auditoria;
        this.email=email; this.password=password;
    }

    @Override @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.existsByRol(RolUsuario.ADMIN)) return;
        if (email == null || email.isBlank() || password == null || password.length() < 12)
            throw new IllegalStateException("Falta administrador inicial. Ejecuta ./preparar-etapa02.sh.");
        String normalized = email.strip().toLowerCase(Locale.ROOT);
        if (!normalized.contains("@") || usuarios.existsByCorreo(normalized))
            throw new IllegalStateException("BOOTSTRAP_ADMIN_EMAIL invalido o ya utilizado");
        Usuario u = new Usuario();
        u.setCorreo(normalized); u.setPasswordHash(encoder.encode(password));
        u.setRol(RolUsuario.ADMIN); u.setActivo(true);
        u.setCreadoEn(Instant.now()); u.setTokenVersion(0);
        usuarios.saveAndFlush(u);
        auditoria.registrar(u, "CREACION_ADMIN_INICIAL", "Usuario", u.getId(), "Bootstrap inicial");
        log.info("Administrador inicial creado: {} (contraseña no registrada en logs)", normalized);
    }
}
