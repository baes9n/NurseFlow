package ec.edu.nurseflow.alertas;

import ec.edu.nurseflow.alertas.dto.*;
import ec.edu.nurseflow.usuarios.*;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertaService {
    private final NamedParameterJdbcTemplate db;
    private final Clock reloj;
    @Autowired
    public AlertaService(NamedParameterJdbcTemplate db) { this(db, Clock.systemUTC()); }
    // Constructor util para pruebas con reloj reproducible.
    AlertaService(NamedParameterJdbcTemplate db, Clock reloj) { this.db=db; this.reloj=reloj; }

    @Transactional(readOnly=true)
    public TableroAlertasRespuesta consultar(Usuario actor) {
        Instant ahora=reloj.instant();
        Instant umbral=ahora.plus(24, ChronoUnit.HOURS);
        boolean admin=actor.getRol()==RolUsuario.ADMIN;
        String filtro=admin ? "" : " and p.usuario_id = :usuarioId ";
        MapSqlParameterSource args=new MapSqlParameterSource()
            .addValue("ahora", Timestamp.from(ahora))
            .addValue("umbral", Timestamp.from(umbral))
            .addValue("usuarioId", actor.getId());
        String from=" from actividades a join asignaciones_turno ast on ast.id=a.asignacion_id " +
            " join profesionales p on p.id=ast.profesional_id " +
            " where a.estado in ('PENDIENTE','EN_PROGRESO') " + filtro;
        String resumen="select count(*) filter (where a.fecha_limite < :ahora) as vencidas, " +
            "count(*) filter (where a.fecha_limite >= :ahora and a.fecha_limite <= :umbral) as proximas " + from;
        long[] totales=db.queryForObject(resumen, args, (rs,n)->new long[]{rs.getLong("vencidas"),rs.getLong("proximas")});
        String consulta="select a.id, a.titulo, a.estado, a.fecha_limite, ast.turno_id, " +
            "p.id as profesional_id, concat(p.nombres, ' ', p.apellidos) as profesional " + from +
            " and a.fecha_limite <= :umbral order by a.fecha_limite asc, a.id asc limit 31";
        List<AlertaRespuesta> items=db.query(consulta,args,(rs,n)->new AlertaRespuesta(
            rs.getLong("id"),rs.getString("titulo"),rs.getString("estado"),
            rs.getTimestamp("fecha_limite").toInstant(),rs.getLong("turno_id"),
            rs.getLong("profesional_id"),rs.getString("profesional"),
            rs.getTimestamp("fecha_limite").toInstant().isBefore(ahora)?"VENCIDA":"PROXIMA"));
        boolean hayMas=items.size()>30;
        return new TableroAlertasRespuesta(ahora,umbral,totales[0],totales[1],
            hayMas ? List.copyOf(items.subList(0,30)) : items,hayMas);
    }
}
