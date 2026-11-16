package ec.edu.nurseflow.procedimientos;

import ec.edu.nurseflow.actividades.Actividad;
import ec.edu.nurseflow.profesionales.Profesional;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "procedimientos")
public class Procedimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @Column(name = "detalle", nullable = false, length = 3000)
    private String detalle;

    @Column(name = "observaciones", nullable = true, length = 3000)
    private String observaciones;

    @Column(name = "realizado_en", nullable = false)
    private Instant realizadoEn;

    public Procedimiento() {
    }

    public Long getId() { return id; }

    public Actividad getActividad() { return actividad; }
    public void setActividad(Actividad actividad) { this.actividad = actividad; }

    public Profesional getProfesional() { return profesional; }
    public void setProfesional(Profesional profesional) { this.profesional = profesional; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Instant getRealizadoEn() { return realizadoEn; }
    public void setRealizadoEn(Instant realizadoEn) { this.realizadoEn = realizadoEn; }

}
