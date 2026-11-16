package ec.edu.nurseflow.profesionales.dto;

import ec.edu.nurseflow.profesionales.Profesional;
public record ProfesionalRespuesta(Long id, Long usuarioId, String correo, String nombres,
    String apellidos, String numeroLicencia, boolean activo) {
    public static ProfesionalRespuesta de(Profesional p) {
        return new ProfesionalRespuesta(p.getId(), p.getUsuario().getId(),
            p.getUsuario().getCorreo(), p.getNombres(), p.getApellidos(),
            p.getNumeroLicencia(), p.getActivo());
    }
}
