package ec.edu.nurseflow.sistema;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class EstadoSistemaServiceTest {
    @Test
    void informaConexionCuandoLaConsultaRespondeUno() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        var respuesta = new EstadoSistemaService(jdbc).comprobar();
        assertEquals("OPERATIVO", respuesta.estado());
        assertEquals("CONECTADA", respuesta.baseDatos());
        verify(jdbc).queryForObject("SELECT 1", Integer.class);
    }
}
