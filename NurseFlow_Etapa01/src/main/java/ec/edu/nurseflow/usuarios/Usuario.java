package ec.edu.nurseflow.usuarios;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "correo", nullable = false, length = 160)
    private String correo;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 30)
    private RolUsuario rol;
    @Column(name = "activo", nullable = false)
    private Boolean activo;
    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;
    @Column(name = "token_version", nullable = false)
    private Integer tokenVersion;

    public Usuario() {}
    public Long getId() { return id; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public Integer getTokenVersion() { return tokenVersion; }
    public void setTokenVersion(Integer tokenVersion) { this.tokenVersion = tokenVersion; }
}
