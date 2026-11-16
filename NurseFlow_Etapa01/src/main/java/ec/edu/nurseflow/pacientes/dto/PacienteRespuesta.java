package ec.edu.nurseflow.pacientes.dto;

import ec.edu.nurseflow.pacientes.Paciente;
import java.time.LocalDate;
public record PacienteRespuesta(Long id, String codigo, String nombres, String apellidos,
        LocalDate fechaNacimiento, boolean activo) {
    public static PacienteRespuesta de(Paciente p) {
        return new PacienteRespuesta(p.getId(),p.getCodigo(),p.getNombres(),
            p.getApellidos(),p.getFechaNacimiento(),p.getActivo());
    }
}
