package ec.edu.nurseflow.sistema;

import ec.edu.nurseflow.sistema.dto.EstadoSistemaResponse;
import java.time.Instant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class EstadoSistemaService {
    private final JdbcTemplate jdbcTemplate;

    public EstadoSistemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public EstadoSistemaResponse comprobar() {
        Integer resultado = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        if (!Integer.valueOf(1).equals(resultado)) {
            throw new IllegalStateException("La comprobacion de base de datos no fue exitosa");
        }
        return new EstadoSistemaResponse("NurseFlow", "OPERATIVO", "CONECTADA", Instant.now());
    }
}
