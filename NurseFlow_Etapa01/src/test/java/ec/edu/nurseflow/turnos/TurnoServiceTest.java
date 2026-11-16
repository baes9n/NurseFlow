package ec.edu.nurseflow.turnos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import ec.edu.nurseflow.asignaciones.*;
import ec.edu.nurseflow.actividades.ActividadRepository;
import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.common.ConflictoException;
import ec.edu.nurseflow.profesionales.*;
import ec.edu.nurseflow.turnos.dto.TurnoRequest;
import ec.edu.nurseflow.usuarios.Usuario;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;

class TurnoServiceTest {
    private final TurnoRepository turnos=mock(TurnoRepository.class);
    private final AsignacionTurnoRepository asignaciones=mock(AsignacionTurnoRepository.class);
    private final ProfesionalRepository profesionales=mock(ProfesionalRepository.class);
    private final AuditoriaService auditoria=mock(AuditoriaService.class);
    private final TurnoService service=new TurnoService(turnos, asignaciones, profesionales, auditoria, mock(ActividadRepository.class));

    @Test void impideCreacionConHorarioInvertido() {
        TurnoRequest req = new TurnoRequest("Urgencias",
            Instant.parse("2026-12-01T17:00:00Z"), Instant.parse("2026-12-01T07:00:00Z"));
        assertThrows(IllegalArgumentException.class, () -> service.crear(req, new Usuario()));
        verifyNoInteractions(turnos, asignaciones);
    }

    @Test void noPermiteEditarTurnoCuandoNuevoHorarioSeSuperpone() {
        Turno turno=new Turno(); turno.setEstado(EstadoTurno.PROGRAMADO);
        turno.setInicio(Instant.parse("2026-12-01T07:00:00Z"));
        turno.setFin(Instant.parse("2026-12-01T13:00:00Z"));
        when(turnos.bloquear(10L)).thenReturn(Optional.of(turno));
        when(asignaciones.idsProfesionalesActivosPorTurno(10L)).thenReturn(List.of(3L));
        when(profesionales.bloquear(3L)).thenReturn(Optional.of(mock(Profesional.class)));
        when(asignaciones.contarSolapamientos(eq(3L), any(), any(), eq(10L),
            eq(EstadoAsignacion.ACTIVA), eq(EstadoTurno.CANCELADO))).thenReturn(1L);
        TurnoRequest req=new TurnoRequest("Hospitalizacion",
            Instant.parse("2026-12-01T10:00:00Z"), Instant.parse("2026-12-01T16:00:00Z"));
        assertThrows(ConflictoException.class, () -> service.editar(10L, req, new Usuario()));
        verify(turnos, never()).saveAndFlush(any());
    }
}
