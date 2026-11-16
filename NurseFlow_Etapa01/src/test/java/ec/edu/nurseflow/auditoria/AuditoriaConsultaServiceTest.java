package ec.edu.nurseflow.auditoria;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import ec.edu.nurseflow.common.PaginaRespuesta;
import ec.edu.nurseflow.usuarios.Usuario;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;

class AuditoriaConsultaServiceTest {
    @Test
    void listaAuditoriaConCorreoDelUsuario() {
        AuditoriaRepository repo = mock(AuditoriaRepository.class);
        Usuario usuario = new Usuario();
        usuario.setCorreo("demo@nurseflow.local");
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setAccion("INICIO_SESION");
        auditoria.setEntidad("Usuario");
        auditoria.setRegistroId(1L);
        auditoria.setCreadaEn(Instant.parse("2026-11-16T12:00:00Z"));
        when(repo.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(auditoria)));

        PaginaRespuesta<?> respuesta = new AuditoriaConsultaService(repo)
            .listar(null, null, null, 0, 20);
        assertEquals(1, respuesta.totalElementos());
        assertEquals(1, respuesta.contenido().size());
        assertEquals("demo@nurseflow.local",
            ((ec.edu.nurseflow.auditoria.dto.AuditoriaRespuesta) respuesta.contenido().get(0)).usuario());
    }

    @Test
    void consultaMantieneTransaccionDeLecturaYFetchDeUsuario() throws Exception {
        Method consulta = AuditoriaConsultaService.class.getMethod("listar",
            Long.class, String.class, String.class, int.class, int.class);
        Transactional tx = consulta.getAnnotation(Transactional.class);
        assertNotNull(tx);
        assertTrue(tx.readOnly());
        Method metodoRepo = AuditoriaRepository.class.getMethod("findAll",
            Specification.class, Pageable.class);
        EntityGraph graph = metodoRepo.getAnnotation(EntityGraph.class);
        assertNotNull(graph);
        assertArrayEquals(new String[]{"usuario"}, graph.attributePaths());
    }
}
