package ec.edu.nurseflow.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.auth.dto.LoginRequest;
import ec.edu.nurseflow.common.CredencialesInvalidasException;
import ec.edu.nurseflow.usuarios.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

class AuthServiceTest {
    @Test void credencialesDesconocidasNoGeneranToken() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        when(repo.findByCorreo("ninguno@test.local")).thenReturn(Optional.empty());
        AuthService service = new AuthService(repo, mock(PasswordEncoder.class),
            mock(JwtEncoder.class), mock(TokenRevocadoRepository.class),
            mock(AuditoriaService.class), "nurseflow-local", 1800);
        assertThrows(CredencialesInvalidasException.class,
            () -> service.login(new LoginRequest("NINGUNO@test.local","equivocada")));
    }
    @Test void tokenDemasiadoLargoNoSePermite() {
        assertThrows(IllegalArgumentException.class, () ->
            new AuthService(mock(UsuarioRepository.class),mock(PasswordEncoder.class),
                mock(JwtEncoder.class),mock(TokenRevocadoRepository.class),
                mock(AuditoriaService.class),"nurseflow-local",100000));
    }
}
