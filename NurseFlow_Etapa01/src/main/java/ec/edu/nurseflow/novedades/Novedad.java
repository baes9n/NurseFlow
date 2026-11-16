package ec.edu.nurseflow.novedades;

import ec.edu.nurseflow.turnos.Turno;
import ec.edu.nurseflow.usuarios.Usuario;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "novedades")
public class Novedad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Usuario registradoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoNovedad tipo;

    @Column(name = "descripcion", nullable = false, length = 3000)
    private String descripcion;

    @Column(name = "registrada_en", nullable = false)
    private Instant registradaEn;

    public Novedad() {
    }

    public Long getId() { return id; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public Usuario getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(Usuario registradoPor) { this.registradoPor = registradoPor; }

    public TipoNovedad getTipo() { return tipo; }
    public void setTipo(TipoNovedad tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Instant getRegistradaEn() { return registradaEn; }
    public void setRegistradaEn(Instant registradaEn) { this.registradaEn = registradaEn; }

}
