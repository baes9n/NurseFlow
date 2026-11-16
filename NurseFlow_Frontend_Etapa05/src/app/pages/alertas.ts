import { Component, OnDestroy, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { interval, firstValueFrom, Subscription } from 'rxjs';
import { ApiService } from '../core/api.service';
import { apiMessage, formatDate } from '../core/errors';
import { TableroAlertas } from '../core/innovaciones.models';
import { IconComponent } from '../shared/icon';
import { SortHeaderComponent } from '../shared/sort-header';

@Component({selector:'nf-alertas',standalone:true,imports:[RouterLink,IconComponent,SortHeaderComponent],template:`
<section class="page-head"><div><div class="eyebrow">SEGUIMIENTO AUTOMÁTICO</div><h1>Alertas operativas</h1><p>Actividades abiertas vencidas o cuyo plazo terminará en las próximas 24 horas.</p></div><button type="button" class="btn btn-outline" (click)="load()" [disabled]="loading()"><nf-icon name="refresh"/> Actualizar</button></section>
<div class="stats-grid alert-stats">
 <div class="stat-card"><div class="stat-top"><span>Vencidas</span><span class="stat-icon rose"><nf-icon name="alert"/></span></div><strong>{{loading()&&!data()?'—':data()?.vencidas ?? '—'}}</strong><div class="stat-hint">Plazo ya cumplido sin finalizar</div></div>
 <div class="stat-card"><div class="stat-top"><span>Próximas a vencer</span><span class="stat-icon sky"><nf-icon name="clock"/></span></div><strong>{{loading()&&!data()?'—':data()?.proximas ?? '—'}}</strong><div class="stat-hint">Dentro de las próximas 24 horas</div></div>
 <div class="stat-card"><div class="stat-top"><span>Última actualización</span><span class="stat-icon mint"><nf-icon name="refresh"/></span></div><strong class="stat-time">{{data()?hora(data()!.generadoEn):'—'}}</strong><div class="stat-hint">Actualización automática cada 60 segundos</div></div>
</div>
@if(error()){<div class="alert error"><nf-icon name="alert"/> <span>{{error()}}</span><button type="button" class="btn btn-small" (click)="load()">Reintentar</button></div>}
<section class="surface"><div class="panel-heading"><div><h2>Actividades que requieren atención</h2><p>Las 30 alertas prioritarias provienen de PostgreSQL; los encabezados ordenan las visibles.</p></div><a routerLink="/app/actividades" class="simple-link">Consultar todas <nf-icon name="arrow"/></a></div>
@if(loading()&&!data()){<div class="loading-block"><div class="spinner"></div>Consultando alertas…</div>}
@else if(data()&&data()!.alertas.length===0){<div class="empty-state"><span class="empty-symbol"><nf-icon name="check"/></span><h3>Sin alertas pendientes</h3><p>No hay actividades abiertas vencidas o próximas a vencer.</p></div>}
@else if(data()){<div class="table-scroll"><table class="data-table"><thead><tr><th [attr.aria-sort]="sortField==='prioridad'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="PRIORIDAD" field="prioridad" [active]="sortField" [direction]="sortDirection" (change)="sortAlerts($event)"/></th><th [attr.aria-sort]="sortField==='titulo'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="ACTIVIDAD" field="titulo" [active]="sortField" [direction]="sortDirection" (change)="sortAlerts($event)"/></th><th [attr.aria-sort]="sortField==='profesional'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="PROFESIONAL" field="profesional" [active]="sortField" [direction]="sortDirection" (change)="sortAlerts($event)"/></th><th [attr.aria-sort]="sortField==='fechaLimite'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="PLAZO" field="fechaLimite" [active]="sortField" [direction]="sortDirection" (change)="sortAlerts($event)"/></th><th class="right">SEGUIMIENTO</th></tr></thead><tbody>
@for(a of sortedAlerts();track a.actividadId){<tr><td><span class="alert-priority" [class.late]="a.prioridad==='VENCIDA'">{{a.prioridad==='VENCIDA'?'Vencida':'Próxima'}}</span></td><td><strong>{{a.titulo}}</strong><div class="cell-sub">Actividad #{{a.actividadId}} · Turno #{{a.turnoId}}</div></td><td>{{a.profesional}}</td><td [class.text-danger]="a.prioridad==='VENCIDA'">{{when(a.fechaLimite)}}</td><td class="right"><a class="btn btn-small btn-outline" routerLink="/app/actividades" [queryParams]="{turnoId:a.turnoId}">Ver turno</a></td></tr>}
</tbody></table></div>@if(data()!.hayMas){<p class="alerts-more">Se muestran las primeras 30 alertas. Consulta el listado completo de actividades para ver las demás.</p>}}
</section>
`})
export class AlertasComponent implements OnInit,OnDestroy {
 private api=inject(ApiService);private timer?:Subscription;
 data=signal<TableroAlertas|null>(null);loading=signal(false);error=signal('');when=formatDate;
 sortField='fechaLimite';sortDirection:'asc'|'desc'='asc';
 sortedAlerts=computed(()=>{const values=[...(this.data()?.alertas||[])];const key=this.sortField;const dir=this.sortDirection==='asc'?1:-1;
  return values.sort((a,b)=>{const x=String(a[key as keyof typeof a]),y=String(b[key as keyof typeof b]);return x.localeCompare(y,'es')*dir;});});
 sortAlerts(field:string){if(field===this.sortField)this.sortDirection=this.sortDirection==='asc'?'desc':'asc';else{this.sortField=field;this.sortDirection='asc';}
  this.data.update(v=>v?{...v,alertas:[...v.alertas]}:v);}
 ngOnInit(){void this.load();this.timer=interval(60000).subscribe(()=>void this.load());}
 ngOnDestroy(){this.timer?.unsubscribe();}
 hora(v:string){const d=new Date(v);return Number.isNaN(d.getTime())?'—':d.toLocaleTimeString('es-EC',{hour:'2-digit',minute:'2-digit',hour12:false});}
 async load(){if(this.loading())return;this.loading.set(true);this.error.set('');try{this.data.set(await firstValueFrom(this.api.get<TableroAlertas>('alertas')));}catch(e){this.error.set(apiMessage(e));}finally{this.loading.set(false);}}
}
