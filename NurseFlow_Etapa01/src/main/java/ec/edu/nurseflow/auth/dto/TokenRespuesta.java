package ec.edu.nurseflow.auth.dto;
public record TokenRespuesta(String accessToken, String tokenType,
    long expiresIn, UsuarioRespuesta usuario) {}
