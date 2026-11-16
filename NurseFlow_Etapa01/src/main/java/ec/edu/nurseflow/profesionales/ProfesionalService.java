package ec.edu.nurseflow.profesionales;

import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.asignaciones.*;
import ec.edu.nurseflow.turnos.EstadoTurno;
import ec.edu.nurseflow.common.*;
import ec.edu.nurseflow.profesionales.dto.*;
import ec.edu.nurseflow.usuarios.*;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfesionalService {
    private final ProfesionalRepository profesionales;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AsignacionTurnoRepository asignaciones;
    private final AuditoriaService auditoria;
    public ProfesionalService(ProfesionalRepository profesionales, UsuarioRepository usuarios,
            PasswordEncoder encoder, AuditoriaService auditoria, AsignacionTurnoRepository asignaciones) {
        this.profesionales=profesionales; this.usuarios=usuarios;
        this.encoder=encoder; this.auditoria=auditoria; this.asignaciones=asignaciones;
    }

    @Transactional
    public ProfesionalRespuesta crear(CrearProfesionalRequest req, Usuario actor) {
        String correo = req.correo().strip().toLowerCase(Locale.ROOT);
        String licencia = limpiarLicencia(req.numeroLicencia());
        if (usuarios.existsByCorreo(correo)) throw new ConflictoException("El correo ya esta registrado");
        if (licencia != null && profesionales.existsByNumeroLicencia(licencia))
            throw new ConflictoException("El numero de licencia ya esta registrado");
        Usuario user = new Usuario(); user.setCorreo(correo);
        user.setPasswordHash(encoder.encode(req.contrasena()));
        user.setRol(RolUsuario.ENFERMERIA); user.setActivo(true);
        user.setCreadoEn(Instant.now()); user.setTokenVersion(0);
        usuarios.save(user);
        Profesional p = new Profesional(); p.setUsuario(user);
        p.setNombres(req.nombres().strip()); p.setApellidos(req.apellidos().strip());
        p.setNumeroLicencia(licencia); p.setActivo(true);
        profesionales.saveAndFlush(p);
        auditoria.registrar(actor, "CREAR_PROFESIONAL", "Profesional", p.getId(), "Alta de personal");
        return ProfesionalRespuesta.de(p);
    }

    @Transactional(readOnly=true)
    public ProfesionalRespuesta obtener(Long id) { return ProfesionalRespuesta.de(cargar(id)); }

    @Transactional(readOnly=true)
    public PaginaRespuesta<ProfesionalRespuesta> listar(String buscar, Boolean activo, int pagina, int tamano,
            String ordenarPor, String direccion) {
        Pageable pageable = Paginacion.crear(pagina, tamano, ordenarPor, direccion,
            Set.of("id", "nombres", "apellidos", "numeroLicencia", "activo", "usuario.correo"));
        Specification<Profesional> spec = (root, query, cb) -> cb.conjunction();
        if (activo != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("activo"), activo));
        if (buscar != null && !buscar.isBlank()) {
            String term = "%" + buscar.strip().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombres")), term),
                cb.like(cb.lower(root.get("apellidos")), term),
                cb.like(cb.lower(root.join("usuario").get("correo")), term),
                cb.like(cb.lower(root.get("numeroLicencia")), term)));
        }
        return PaginaRespuesta.desde(profesionales.findAll(spec, pageable).map(ProfesionalRespuesta::de));
    }

    @Transactional
    public ProfesionalRespuesta editar(Long id, EditarProfesionalRequest req, Usuario actor) {
        Profesional p = cargar(id);
        String licencia = limpiarLicencia(req.numeroLicencia());
        if (licencia != null && profesionales.existsByNumeroLicenciaAndIdNot(licencia, id))
            throw new ConflictoException("El numero de licencia ya esta registrado");
        p.setNombres(req.nombres().strip()); p.setApellidos(req.apellidos().strip());
        p.setNumeroLicencia(licencia);
        profesionales.save(p);
        auditoria.registrar(actor, "EDITAR_PROFESIONAL", "Profesional", id, "Datos de personal actualizados");
        return ProfesionalRespuesta.de(p);
    }

    @Transactional
    public ProfesionalRespuesta cambiarEstado(Long id, boolean activo, Usuario actor) {
        Profesional p = profesionales.bloquear(id)
            .orElseThrow(() -> new NoEncontradoException("Profesional no encontrado"));
        if (!activo && asignaciones.tieneAsignacionActivaProfesional(id,
                EstadoAsignacion.ACTIVA, EstadoTurno.CANCELADO))
            throw new ConflictoException("Retira o cancela los turnos activos antes de desactivar al profesional");
        if (p.getActivo().equals(activo)) throw new ConflictoException("El profesional ya tiene ese estado");
        p.setActivo(activo);
        p.getUsuario().setActivo(activo);
        profesionales.save(p); usuarios.save(p.getUsuario());
        auditoria.registrar(actor, activo ? "ACTIVAR_PROFESIONAL" : "DESACTIVAR_PROFESIONAL",
            "Profesional", id, "Cambio de estado");
        return ProfesionalRespuesta.de(p);
    }

    private Profesional cargar(Long id) {
        return profesionales.findById(id).orElseThrow(() -> new NoEncontradoException("Profesional no encontrado"));
    }
    private String limpiarLicencia(String licencia) {
        if (licencia == null || licencia.isBlank()) return null;
        return licencia.strip().toUpperCase(Locale.ROOT);
    }
}
