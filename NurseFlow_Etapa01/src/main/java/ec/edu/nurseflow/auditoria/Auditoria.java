package ec.edu.nurseflow.auditoria;

import ec.edu.nurseflow.usuarios.Usuario;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auditorias")
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "accion", nullable = false, length = 70)
    private String accion;

    @Column(name = "entidad", nullable = false, length = 70)
    private String entidad;

    @Column(name = "registro_id", nullable = false)
    private Long registroId;

    @Column(name = "detalle", nullable = true, length = 3000)
    private String detalle;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    protected Auditoria() {
    }

    public Long getId() { return id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }

    public Long getRegistroId() { return registroId; }
    public void setRegistroId(Long registroId) { this.registroId = registroId; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public Instant getCreadaEn() { return creadaEn; }
    public void setCreadaEn(Instant creadaEn) { this.creadaEn = creadaEn; }

}
