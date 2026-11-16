package ec.edu.nurseflow.notificaciones;

import ec.edu.nurseflow.usuarios.Usuario;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notificaciones")
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoNotificacion tipo;

    @Column(name = "mensaje", nullable = false, length = 500)
    private String mensaje;

    @Column(name = "entidad_destino", nullable = true, length = 70)
    private String entidadDestino;

    @Column(name = "registro_destino_id", nullable = true)
    private Long registroDestinoId;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    @Column(name = "leida_en", nullable = true)
    private Instant leidaEn;

    protected Notificacion() {
    }

    public Long getId() { return id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getEntidadDestino() { return entidadDestino; }
    public void setEntidadDestino(String entidadDestino) { this.entidadDestino = entidadDestino; }

    public Long getRegistroDestinoId() { return registroDestinoId; }
    public void setRegistroDestinoId(Long registroDestinoId) { this.registroDestinoId = registroDestinoId; }

    public Instant getCreadaEn() { return creadaEn; }
    public void setCreadaEn(Instant creadaEn) { this.creadaEn = creadaEn; }

    public Instant getLeidaEn() { return leidaEn; }
    public void setLeidaEn(Instant leidaEn) { this.leidaEn = leidaEn; }

}
