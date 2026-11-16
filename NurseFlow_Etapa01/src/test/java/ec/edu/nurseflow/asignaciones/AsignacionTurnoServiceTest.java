package ec.edu.nurseflow.asignaciones;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import ec.edu.nurseflow.asignaciones.dto.CrearAsignacionRequest;
import ec.edu.nurseflow.actividades.ActividadRepository;
import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.common.ConflictoException;
import ec.edu.nurseflow.profesionales.*;
import ec.edu.nurseflow.turnos.*;
import ec.edu.nurseflow.usuarios.*;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AsignacionTurnoServiceTest {
    private final AsignacionTurnoRepository asignaciones=mock(AsignacionTurnoRepository.class);
    private final TurnoRepository turnos=mock(TurnoRepository.class);
    private final ProfesionalRepository profesionales=mock(ProfesionalRepository.class);
    private final AuditoriaService auditoria=mock(AuditoriaService.class);
    private final AsignacionTurnoService service=new AsignacionTurnoService(asignaciones,turnos,
        profesionales,auditoria,mock(EntityManager.class),mock(ActividadRepository.class));

    @Test void rechazaProfesionalInactivoAntesDeConsultarConflictos() {
        Turno t=new Turno(); t.setEstado(EstadoTurno.PROGRAMADO);
        when(turnos.bloquear(1L)).thenReturn(Optional.of(t));
        Profesional p=mock(Profesional.class); when(p.getActivo()).thenReturn(false);
        when(profesionales.bloquear(2L)).thenReturn(Optional.of(p));
        assertThrows(ConflictoException.class,
            () -> service.crear(new CrearAsignacionRequest(1L,2L), new Usuario()));
        verify(asignaciones, never()).saveAndFlush(any());
    }

    @Test void rechazaAsignacionesFueraDeTurnosProgramados() {
        Turno t=new Turno(); t.setEstado(EstadoTurno.CANCELADO);
        when(turnos.bloquear(1L)).thenReturn(Optional.of(t));
        assertThrows(ConflictoException.class,
            () -> service.crear(new CrearAsignacionRequest(1L,2L), new Usuario()));
        verifyNoInteractions(profesionales);
    }
}
