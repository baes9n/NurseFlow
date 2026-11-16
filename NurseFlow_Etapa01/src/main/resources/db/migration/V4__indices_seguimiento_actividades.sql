-- Indices para consultas combinadas de actividades y seguimiento.
-- No borra ni cambia los registros existentes.
CREATE INDEX IF NOT EXISTS idx_actividades_paciente_estado_limite
    ON actividades(paciente_id, estado, fecha_limite);
CREATE INDEX IF NOT EXISTS idx_actividades_asignacion_estado
    ON actividades(asignacion_id, estado);
CREATE INDEX IF NOT EXISTS idx_procedimientos_profesional_fecha
    ON procedimientos(profesional_id, realizado_en DESC);
CREATE INDEX IF NOT EXISTS idx_novedades_turno_tipo_fecha
    ON novedades(turno_id, tipo, registrada_en DESC);
