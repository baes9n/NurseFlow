package ec.edu.nurseflow.actividades;

import ec.edu.nurseflow.asignaciones.AsignacionTurno;
import ec.edu.nurseflow.pacientes.Paciente;
import ec.edu.nurseflow.usuarios.Usuario;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "actividades")
public class Actividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_id", nullable = false)
    private AsignacionTurno asignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = true)
    private Paciente paciente;

    @Column(name = "titulo", nullable = false, length = 160)
    private String titulo;

    @Column(name = "descripcion", nullable = true, length = 3000)
    private String descripcion;

    @Column(name = "fecha_limite", nullable = false)
    private Instant fechaLimite;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoActividad estado;

    @Column(name = "completado_en", nullable = true)
    private Instant completadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completado_por_id", nullable = true)
    private Usuario completadoPor;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public Actividad() {
    }

    public Long getId() { return id; }

    public AsignacionTurno getAsignacion() { return asignacion; }
    public void setAsignacion(AsignacionTurno asignacion) { this.asignacion = asignacion; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Instant getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(Instant fechaLimite) { this.fechaLimite = fechaLimite; }

    public EstadoActividad getEstado() { return estado; }
    public void setEstado(EstadoActividad estado) { this.estado = estado; }

    public Instant getCompletadoEn() { return completadoEn; }
    public void setCompletadoEn(Instant completadoEn) { this.completadoEn = completadoEn; }

    public Usuario getCompletadoPor() { return completadoPor; }
    public void setCompletadoPor(Usuario completadoPor) { this.completadoPor = completadoPor; }

    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }

}
