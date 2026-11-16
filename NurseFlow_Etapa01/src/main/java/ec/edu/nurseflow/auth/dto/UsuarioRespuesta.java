package ec.edu.nurseflow.auth.dto;
import ec.edu.nurseflow.usuarios.RolUsuario;
import ec.edu.nurseflow.usuarios.Usuario;
public record UsuarioRespuesta(Long id, String correo, RolUsuario rol, boolean activo) {
    public static UsuarioRespuesta de(Usuario user) {
        return new UsuarioRespuesta(user.getId(), user.getCorreo(), user.getRol(), user.getActivo());
    }
}
