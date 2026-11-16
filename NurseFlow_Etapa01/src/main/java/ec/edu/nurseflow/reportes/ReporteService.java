package ec.edu.nurseflow.reportes;

import ec.edu.nurseflow.auditoria.AuditoriaService;
import ec.edu.nurseflow.reportes.dto.*;
import ec.edu.nurseflow.usuarios.Usuario;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {
    private final NamedParameterJdbcTemplate db;
    private final AuditoriaService auditoria;
    private final PdfReporteService pdf;
    private final Clock reloj;
    @Autowired
    public ReporteService(NamedParameterJdbcTemplate db, AuditoriaService auditoria, PdfReporteService pdf) {
        this(db,auditoria,pdf,Clock.systemUTC());
    }
    ReporteService(NamedParameterJdbcTemplate db, AuditoriaService auditoria, PdfReporteService pdf, Clock reloj) {
        this.db=db;this.auditoria=auditoria;this.pdf=pdf;this.reloj=reloj;
    }

    static void validar(Instant desde, Instant hasta) {
        if(desde==null || hasta==null || !desde.isBefore(hasta))
            throw new IllegalArgumentException("El periodo debe tener inicio anterior al fin");
        if(Duration.between(desde,hasta).compareTo(Duration.ofDays(366))>0)
            throw new IllegalArgumentException("El periodo maximo permitido es de 366 dias");
    }

    @Transactional(readOnly=true)
    public ReportePeriodoRespuesta consultar(Instant desde,Instant hasta,Long profesionalId) {
        validar(desde,hasta);
        if(profesionalId!=null && profesionalId<1)
            throw new IllegalArgumentException("El ID profesional debe ser positivo");
        Instant ahora=reloj.instant();
        MapSqlParameterSource args=new MapSqlParameterSource()
            .addValue("desde",Timestamp.from(desde)).addValue("hasta",Timestamp.from(hasta))
            .addValue("ahora",Timestamp.from(ahora));
        String filtro="";
        if(profesionalId!=null){filtro=" and p.id = :profesionalId ";args.addValue("profesionalId",profesionalId);}
        // Estadistica por fecha LIMITE de la actividad; el fin del periodo es exclusivo.
        String consulta="select p.id as profesional_id, concat(p.nombres, ' ', p.apellidos) as profesional, " +
            "count(*) as total, " +
            "count(*) filter (where a.estado='PENDIENTE') as pendientes, " +
            "count(*) filter (where a.estado='EN_PROGRESO') as en_progreso, " +
            "count(*) filter (where a.estado='COMPLETADA') as completadas, " +
            "count(*) filter (where a.estado='CANCELADA') as canceladas, " +
            "count(*) filter (where a.estado in ('PENDIENTE','EN_PROGRESO') and a.fecha_limite < :ahora) as vencidas " +
            "from actividades a join asignaciones_turno ast on ast.id=a.asignacion_id " +
            "join profesionales p on p.id=ast.profesional_id " +
            "where a.fecha_limite >= :desde and a.fecha_limite < :hasta " + filtro +
            "group by p.id,p.nombres,p.apellidos order by p.apellidos,p.nombres,p.id";
        List<ResumenProfesional> filas=db.query(consulta,args,(rs,n)->new ResumenProfesional(
            rs.getLong("profesional_id"),rs.getString("profesional"),
            rs.getLong("total"),rs.getLong("pendientes"),rs.getLong("en_progreso"),
            rs.getLong("completadas"),rs.getLong("canceladas"),rs.getLong("vencidas")));
        return new ReportePeriodoRespuesta(desde,hasta,ahora,profesionalId,
            filas.stream().mapToLong(ResumenProfesional::total).sum(),
            filas.stream().mapToLong(ResumenProfesional::pendientes).sum(),
            filas.stream().mapToLong(ResumenProfesional::enProgreso).sum(),
            filas.stream().mapToLong(ResumenProfesional::completadas).sum(),
            filas.stream().mapToLong(ResumenProfesional::canceladas).sum(),
            filas.stream().mapToLong(ResumenProfesional::vencidas).sum(),filas);
    }

    @Transactional
    public byte[] exportarPdf(Instant desde, Instant hasta, Long profesionalId, Usuario actor) {
        ReportePeriodoRespuesta datos=consultar(desde,hasta,profesionalId);
        byte[] archivo=pdf.generar(datos);
        auditoria.registrar(actor,"EXPORTAR_REPORTE_PDF","Reporte",actor.getId(),
            "Periodo "+desde+" / "+hasta+"; profesional="+profesionalId);
        return archivo;
    }
}
