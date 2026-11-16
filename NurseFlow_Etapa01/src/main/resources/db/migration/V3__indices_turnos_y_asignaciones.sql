-- Etapa 03: indices para listados y chequeos de turnos.
-- V1 y V2 NO se modifican. Todas las filas existentes permanecen intactas.
CREATE INDEX IF NOT EXISTS idx_turnos_fin ON turnos(fin);
CREATE INDEX IF NOT EXISTS idx_asignaciones_turno_estado ON asignaciones_turno(turno_id, estado);
