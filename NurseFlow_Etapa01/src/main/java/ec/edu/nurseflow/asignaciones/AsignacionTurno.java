package ec.edu.nurseflow.asignaciones;

import ec.edu.nurseflow.profesionales.Profesional;
import ec.edu.nurseflow.turnos.Turno;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "asignaciones_turno")
public class AsignacionTurno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoAsignacion estado;

    @Column(name = "asignado_en", nullable = false)
    private Instant asignadoEn;

    public AsignacionTurno() {
    }

    public Long getId() { return id; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public Profesional getProfesional() { return profesional; }
    public void setProfesional(Profesional profesional) { this.profesional = profesional; }

    public EstadoAsignacion getEstado() { return estado; }
    public void setEstado(EstadoAsignacion estado) { this.estado = estado; }

    public Instant getAsignadoEn() { return asignadoEn; }
    public void setAsignadoEn(Instant asignadoEn) { this.asignadoEn = asignadoEn; }

}
