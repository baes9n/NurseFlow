import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../core/api.service';
import { apiMessage } from '../core/errors';
import { ReportePeriodo } from '../core/innovaciones.models';
import { IconComponent } from '../shared/icon';
import { LookupComponent } from '../shared/lookup';
import { SortHeaderComponent } from '../shared/sort-header';
import { computed } from '@angular/core';

@Component({selector:'nf-reportes',standalone:true,imports:[FormsModule,IconComponent,LookupComponent,SortHeaderComponent],template:`
<section class="page-head"><div><div class="eyebrow">ANÁLISIS Y DOCUMENTACIÓN</div><h1>Reportes de enfermería</h1><p>Resumen estadístico por profesional y período, obtenido directamente de PostgreSQL.</p></div><button class="btn btn-primary" type="button" (click)="descargar()" [disabled]="!reporte()||descargando()"><nf-icon name="report"/> {{descargando()?'Generando PDF…':'Descargar PDF'}}</button></section>
<section class="surface report-filter"><form (ngSubmit)="consultar()" #f="ngForm" class="report-filter-form"><label class="field"><span>Desde *</span><input type="date" name="desde" [(ngModel)]="desde" required/></label><label class="field"><span>Hasta (incluido) *</span><input type="date" name="hasta" [(ngModel)]="hasta" required/></label><label class="field"><span>Profesional</span><nf-lookup tipo="profesional" label="Profesional del reporte" placeholder="Buscar profesional por nombre…" [allowNone]="true" [value]="profesionalId" (change)="profesionalId=$event"/></label><button class="btn btn-primary" type="submit" [disabled]="f.invalid||loading()"><nf-icon name="filter"/> {{loading()?'Consultando…':'Generar reporte'}}</button></form><p class="form-note">Se incluyen actividades cuya <strong>fecha límite</strong> está en el período. El último día se incluye completo. Máximo 366 días.</p></section>
@if(error()){<div class="alert error"><nf-icon name="alert"/>{{error()}}</div>}
@if(reporte()){
<div class="stats-grid report-stats"><div class="stat-card"><div class="stat-top"><span>Total de actividades</span><span class="stat-icon sky"><nf-icon name="tasks"/></span></div><strong>{{reporte()!.total}}</strong></div><div class="stat-card"><div class="stat-top"><span>Completadas</span><span class="stat-icon mint"><nf-icon name="check"/></span></div><strong>{{reporte()!.completadas}}</strong></div><div class="stat-card"><div class="stat-top"><span>Pendientes y en progreso</span><span class="stat-icon violet"><nf-icon name="clock"/></span></div><strong>{{reporte()!.pendientes+reporte()!.enProgreso}}</strong></div><div class="stat-card"><div class="stat-top"><span>Vencidas sin resolver</span><span class="stat-icon rose"><nf-icon name="alert"/></span></div><strong>{{reporte()!.vencidas}}</strong></div></div>
<section class="surface"><div class="panel-heading"><div><h2>Resultados por profesional</h2><p>Pulsa los encabezados para ordenar los resultados; el PDF conserva los filtros y registra la exportación en auditoría.</p></div><button type="button" class="btn btn-outline" (click)="descargar()" [disabled]="descargando()"><nf-icon name="report"/> Exportar PDF</button></div>
@if(reporte()!.profesionales.length===0){<div class="empty-state"><span class="empty-symbol"><nf-icon name="report"/></span><h3>Sin resultados para este período</h3><p>Prueba seleccionando otras fechas o un profesional distinto.</p></div>}
@else{<div class="table-scroll"><table class="data-table"><thead><tr><th [attr.aria-sort]="sortField==='profesional'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="PROFESIONAL" field="profesional" [active]="sortField" [direction]="sortDirection" (change)="sortReport($event)"/></th><th [attr.aria-sort]="sortField==='total'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="TOTAL" field="total" [active]="sortField" [direction]="sortDirection" (change)="sortReport($event)"/></th><th [attr.aria-sort]="sortField==='pendientes'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="PENDIENTES" field="pendientes" [active]="sortField" [direction]="sortDirection" (change)="sortReport($event)"/></th><th [attr.aria-sort]="sortField==='enProgreso'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="EN PROGRESO" field="enProgreso" [active]="sortField" [direction]="sortDirection" (change)="sortReport($event)"/></th><th [attr.aria-sort]="sortField==='completadas'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="COMPLETADAS" field="completadas" [active]="sortField" [direction]="sortDirection" (change)="sortReport($event)"/></th><th [attr.aria-sort]="sortField==='canceladas'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="CANCELADAS" field="canceladas" [active]="sortField" [direction]="sortDirection" (change)="sortReport($event)"/></th><th [attr.aria-sort]="sortField==='vencidas'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="VENCIDAS" field="vencidas" [active]="sortField" [direction]="sortDirection" (change)="sortReport($event)"/></th></tr></thead><tbody>@for(p of sortedReport();track p.profesionalId){<tr><td><strong>{{p.profesional}}</strong><div class="cell-sub">#{{p.profesionalId}}</div></td><td>{{p.total}}</td><td>{{p.pendientes}}</td><td>{{p.enProgreso}}</td><td>{{p.completadas}}</td><td>{{p.canceladas}}</td><td [class.text-danger]="p.vencidas>0">{{p.vencidas}}</td></tr>}</tbody></table></div>}
</section>}
`})
export class ReportesComponent implements OnInit {
 private api=inject(ApiService);private hoy=new Date();
 desde=this.fechaLocal(new Date(this.hoy.getFullYear(),this.hoy.getMonth(),1));
 hasta=this.fechaLocal(new Date(this.hoy.getFullYear(),this.hoy.getMonth()+1,0));
 profesionalId:number|null=null;sortField='profesional';sortDirection:'asc'|'desc'='asc';
 reporte=signal<ReportePeriodo|null>(null);loading=signal(false);descargando=signal(false);error=signal('');
 private filtros:{desde:string;hasta:string;profesionalId:number|null}|null=null;
 sortedReport=computed(()=>{
  const registros=[...(this.reporte()?.profesionales||[])];const field=this.sortField;const dir=this.sortDirection==='asc'?1:-1;
  return registros.sort((a,b)=>{const av=a[field as keyof typeof a],bv=b[field as keyof typeof b];return (typeof av==='string'?String(av).localeCompare(String(bv),'es'):Number(av)-Number(bv))*dir;});
 });
 sortReport(field:string){if(this.sortField===field)this.sortDirection=this.sortDirection==='asc'?'desc':'asc';else{this.sortField=field;this.sortDirection=field==='profesional'?'asc':'desc';}
  this.reporte.update(r=>r?{...r,profesionales:[...r.profesionales]}:r);}
 ngOnInit(){void this.consultar();}
 private fechaLocal(d:Date){const z=(n:number)=>String(n).padStart(2,'0');return `${d.getFullYear()}-${z(d.getMonth()+1)}-${z(d.getDate())}`;}
 private rango(){const inicio=new Date(this.desde+'T00:00:00'),termino=new Date(this.hasta+'T00:00:00');termino.setDate(termino.getDate()+1);
  if(!this.desde||!this.hasta||Number.isNaN(+inicio)||Number.isNaN(+termino)||inicio>=termino)throw new Error('El período seleccionado es inválido.');
  if(termino.getTime()-inicio.getTime()>366*24*60*60*1000)throw new Error('El periodo máximo es de 366 días.');
  return {desde:inicio.toISOString(),hasta:termino.toISOString(),profesionalId:this.profesionalId};}
 async consultar(){this.error.set('');this.loading.set(true);this.reporte.set(null);this.filtros=null;
  try{const filtro=this.rango();const r=await firstValueFrom(this.api.get<ReportePeriodo>('reportes/actividades',filtro));this.reporte.set(r);this.filtros=filtro;}
  catch(e){this.error.set(apiMessage(e));}finally{this.loading.set(false);}}
 async descargar(){if(!this.filtros||this.descargando())return;this.descargando.set(true);this.error.set('');
  try{const blob=await firstValueFrom(this.api.download('reportes/actividades.pdf',this.filtros));
    if(blob.type!=='application/pdf' && blob.size<50)throw new Error('La API no devolvió un PDF válido.');
    const url=URL.createObjectURL(blob),link=document.createElement('a');link.href=url;link.download='NurseFlow_reporte_actividades.pdf';
    document.body.appendChild(link);link.click();link.remove();setTimeout(()=>URL.revokeObjectURL(url),1000);
  }catch(e){this.error.set(apiMessage(e));}finally{this.descargando.set(false);}}
}
