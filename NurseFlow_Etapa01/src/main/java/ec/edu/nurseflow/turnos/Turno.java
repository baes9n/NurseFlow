package ec.edu.nurseflow.turnos;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "turnos")
public class Turno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area", nullable = false, length = 80)
    private String area;

    @Column(name = "inicio", nullable = false)
    private Instant inicio;

    @Column(name = "fin", nullable = false)
    private Instant fin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoTurno estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public Turno() {
    }

    public Long getId() { return id; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Instant getInicio() { return inicio; }
    public void setInicio(Instant inicio) { this.inicio = inicio; }

    public Instant getFin() { return fin; }
    public void setFin(Instant fin) { this.fin = fin; }

    public EstadoTurno getEstado() { return estado; }
    public void setEstado(EstadoTurno estado) { this.estado = estado; }

    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }

}
