package ec.edu.nurseflow.reportes.dto;
public record ResumenProfesional(Long profesionalId, String profesional,
    long total, long pendientes, long enProgreso, long completadas, long canceladas, long vencidas) {}
