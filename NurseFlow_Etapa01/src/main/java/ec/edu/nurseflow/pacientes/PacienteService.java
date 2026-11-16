package ec.edu.nurseflow.pacientes;

import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.common.*;
import ec.edu.nurseflow.pacientes.dto.*;
import ec.edu.nurseflow.usuarios.Usuario;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {
    private final PacienteRepository pacientes;
    private final AuditoriaService auditoria;
    public PacienteService(PacienteRepository pacientes, AuditoriaService auditoria) {
        this.pacientes=pacientes; this.auditoria=auditoria;
    }

    @Transactional
    public PacienteRespuesta crear(PacienteRequest req, Usuario actor) {
        String codigo = normalizarCodigo(req.codigo());
        if (pacientes.existsByCodigo(codigo)) throw new ConflictoException("Codigo de paciente duplicado");
        Paciente p = new Paciente(); p.setCodigo(codigo);
        p.setNombres(req.nombres().strip()); p.setApellidos(req.apellidos().strip());
        p.setFechaNacimiento(req.fechaNacimiento()); p.setActivo(true); p.setCreadoEn(Instant.now());
        pacientes.saveAndFlush(p);
        auditoria.registrar(actor, "CREAR_PACIENTE", "Paciente", p.getId(), "Alta de paciente de prueba");
        return PacienteRespuesta.de(p);
    }

    @Transactional(readOnly=true)
    public PacienteRespuesta obtener(Long id) { return PacienteRespuesta.de(cargar(id)); }

    @Transactional(readOnly=true)
    public PaginaRespuesta<PacienteRespuesta> listar(String buscar, Boolean activo,
            int pagina, int tamano, String ordenarPor, String direccion) {
        Pageable pageable = Paginacion.crear(pagina, tamano, ordenarPor, direccion,
            Set.of("id", "codigo", "nombres", "apellidos", "fechaNacimiento", "activo", "creadoEn"));
        Specification<Paciente> spec = (root, query, cb) -> cb.conjunction();
        if (activo != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("activo"), activo));
        if (buscar != null && !buscar.isBlank()) {
            String term = "%" + buscar.strip().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("codigo")), term),
                cb.like(cb.lower(root.get("nombres")), term),
                cb.like(cb.lower(root.get("apellidos")), term)));
        }
        return PaginaRespuesta.desde(pacientes.findAll(spec, pageable).map(PacienteRespuesta::de));
    }

    @Transactional
    public PacienteRespuesta editar(Long id, PacienteRequest req, Usuario actor) {
        Paciente p=cargar(id);
        if (!p.getActivo()) throw new ConflictoException("Paciente inactivo: no puede editarse");
        String codigo=normalizarCodigo(req.codigo());
        if (pacientes.existsByCodigoAndIdNot(codigo,id)) throw new ConflictoException("Codigo de paciente duplicado");
        p.setCodigo(codigo); p.setNombres(req.nombres().strip());
        p.setApellidos(req.apellidos().strip()); p.setFechaNacimiento(req.fechaNacimiento());
        pacientes.save(p);
        auditoria.registrar(actor, "EDITAR_PACIENTE", "Paciente", id, "Registro actualizado");
        return PacienteRespuesta.de(p);
    }

    @Transactional
    public void desactivar(Long id, Usuario actor) {
        Paciente p=cargar(id);
        if (!p.getActivo()) throw new ConflictoException("Paciente ya esta inactivo");
        p.setActivo(false); pacientes.save(p);
        auditoria.registrar(actor, "DESACTIVAR_PACIENTE", "Paciente", id, "Baja logica (sin borrar historial)");
    }

    private Paciente cargar(Long id) {
        return pacientes.findById(id).orElseThrow(() -> new NoEncontradoException("Paciente no encontrado"));
    }
    private String normalizarCodigo(String codigo) { return codigo.strip().toUpperCase(Locale.ROOT); }
}
