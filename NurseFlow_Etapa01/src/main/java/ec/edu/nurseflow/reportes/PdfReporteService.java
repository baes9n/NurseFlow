package ec.edu.nurseflow.reportes;

import ec.edu.nurseflow.reportes.dto.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.stereotype.Service;

@Service
public class PdfReporteService {
    private static final PDFont REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont NEGRITA = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Guayaquil"));
    private static final float MARGEN = 45;
    private static final float ANCHO = PDRectangle.A4.getWidth();
    private static final float ALTO = PDRectangle.A4.getHeight();
    private static final Color AZUL = new Color(24,48,78);
    private static final Color GRIS = new Color(74,90,109);

    public byte[] generar(ReportePeriodoRespuesta reporte) {
        try(PDDocument documento=new PDDocument();ByteArrayOutputStream buffer=new ByteArrayOutputStream()) {
            List<ResumenProfesional> filas=reporte.profesionales();
            int indice=0;int pagina=1;
            do {
                PDPage hoja=new PDPage(PDRectangle.A4);documento.addPage(hoja);
                try(PDPageContentStream c=new PDPageContentStream(documento,hoja)) {
                    encabezado(c,reporte,pagina);
                    float y=ALTO-204;
                    if(pagina==1){
                        escribir(c, MARGEN,y,10,true,AZUL,
                            "Total actividades: "+reporte.total()+"    Pendientes: "+reporte.pendientes()+
                            "    En progreso: "+reporte.enProgreso());
                        y-=19;
                        escribir(c, MARGEN,y,10,true,AZUL,
                            "Completadas: "+reporte.completadas()+"    Canceladas: "+reporte.canceladas()+
                            "    Vencidas: "+reporte.vencidas());
                        y-=35;
                    }
                    c.setNonStrokingColor(AZUL);
                    c.addRect(MARGEN,y-11,ANCHO-2*MARGEN,23);c.fill();
                    escribir(c,MARGEN+8,y-4,8,true,Color.WHITE,"PROFESIONAL");
                    String[] headers={"TOTAL","PEND.","CURSO","COMP.","CANC.","VENC."};
                    float[] x={290,342,393,443,489,539};
                    for(int h=0;h<headers.length;h++) escribir(c,x[h],y-4,7,true,Color.WHITE,headers[h]);
                    y-=33;
                    if(filas.isEmpty()){
                        escribir(c,MARGEN+4,y,10,false,GRIS,"No hay actividades en el periodo seleccionado.");
                    }
                    while(indice<filas.size() && y>85) {
                        ResumenProfesional r=filas.get(indice++);
                        if(indice%2==0){c.setNonStrokingColor(new Color(242,246,251));c.addRect(MARGEN,y-9,ANCHO-2*MARGEN,23);c.fill();}
                        escribir(c,MARGEN+8,y,8,false,AZUL,recortar(r.profesional(),38));
                        long[] vals={r.total(),r.pendientes(),r.enProgreso(),r.completadas(),r.canceladas(),r.vencidas()};
                        for(int j=0;j<vals.length;j++) escribir(c,x[j],y,8,false,GRIS,String.valueOf(vals[j]));
                        y-=24;
                    }
                    linea(c,MARGEN,65,ANCHO-MARGEN,65,new Color(202,212,222));
                    escribir(c,MARGEN,51,8,false,GRIS,"NurseFlow  |  Datos de prueba  |  Periodo por fecha limite de actividad");
                    escribir(c,ANCHO-92,51,8,false,GRIS,"Pagina "+pagina);
                }
                pagina++;
            } while(indice<filas.size());
            documento.save(buffer);
            return buffer.toByteArray();
        }catch(IOException ex){throw new IllegalStateException("No se pudo crear el PDF de actividades",ex);}
    }

    private void encabezado(PDPageContentStream c,ReportePeriodoRespuesta r,int pagina) throws IOException {
        c.setNonStrokingColor(AZUL);c.addRect(0,ALTO-94,ANCHO,94);c.fill();
        escribir(c,MARGEN,ALTO-46,22,true,Color.WHITE,"NURSEFLOW");
        escribir(c,MARGEN,ALTO-68,11,false,Color.WHITE,"Reporte de actividades de enfermeria");
        escribir(c,MARGEN,ALTO-121,10,true,AZUL,"Periodo analizado");
        escribir(c,MARGEN,ALTO-140,10,false,GRIS,FECHA.format(r.desde())+"  -  "+FECHA.format(r.hasta())+" (fin exclusivo)");
        escribir(c,MARGEN,ALTO-159,9,false,GRIS,"Generado el "+FECHA.format(r.generadoEn())+"   |   Zona America/Guayaquil");
        if(r.profesionalId()!=null) escribir(c,MARGEN,ALTO-177,9,false,GRIS,"Filtro de profesional: #"+r.profesionalId());
    }
    private void linea(PDPageContentStream c,float x1,float y1,float x2,float y2,Color color) throws IOException {
        c.setStrokingColor(color);c.moveTo(x1,y1);c.lineTo(x2,y2);c.stroke();
    }
    private String recortar(String s,int max){return s==null?"":s.length()>max?s.substring(0,max-1)+".":s;}
    private String compatible(String value,PDFont font) {
        if(value==null)return "";
        StringBuilder result=new StringBuilder();
        for(int offset=0;offset<value.length();) {
            int cp=value.codePointAt(offset);offset+=Character.charCount(cp);
            if(Character.isISOControl(cp)){result.append(' ');continue;}
            String ch=new String(Character.toChars(cp));
            try{font.encode(ch);result.append(ch);}catch(IOException | IllegalArgumentException ex){result.append('?');}
        }
        return result.toString();
    }
    private void escribir(PDPageContentStream c,float x,float y,int size,boolean bold,Color color,String str) throws IOException {
        PDFont font=bold?NEGRITA:REGULAR;
        c.beginText();c.setNonStrokingColor(color);c.setFont(font,size);
        c.newLineAtOffset(x,y);c.showText(compatible(str,font));c.endText();
    }
}
