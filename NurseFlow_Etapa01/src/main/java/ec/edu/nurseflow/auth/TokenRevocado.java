package ec.edu.nurseflow.auth;

import ec.edu.nurseflow.usuarios.Usuario;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tokens_revocados")
public class TokenRevocado {
    @Id @Column(name = "jti", length = 64)
    private String jti;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;
    @Column(name = "revocado_en", nullable = false)
    private Instant revocadoEn;

    protected TokenRevocado() {}
    public TokenRevocado(String jti, Usuario usuario, Instant expiraEn) {
        this.jti = jti;
        this.usuario = usuario;
        this.expiraEn = expiraEn;
        this.revocadoEn = Instant.now();
    }
    public String getJti() { return jti; }
}
