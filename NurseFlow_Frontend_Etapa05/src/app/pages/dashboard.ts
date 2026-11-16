import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, firstValueFrom } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { apiMessage, formatDate } from '../core/errors';
import { Actividad, Turno, Pagina } from '../core/models';
import { IconComponent } from '../shared/icon';
import { BadgeComponent } from '../shared/ui';
import { SortHeaderComponent } from '../shared/sort-header';
@Component({selector:'nf-dashboard',standalone:true,imports:[RouterLink,IconComponent,BadgeComponent,SortHeaderComponent],template:`
<section class="page-head"><div><div class="eyebrow">CENTRO DE OPERACIONES</div><h1>{{saludo()}}, <span class="heading-accent">bienvenido.</span></h1><p>Estas son las actividades y turnos registrados en tu espacio de trabajo.</p></div><div class="head-actions"><button class="btn btn-outline" type="button" (click)="load()" [disabled]="loading()"><nf-icon name="refresh"/> Actualizar</button>@if(auth.isAdmin()){<a class="btn btn-primary" routerLink="/app/turnos"><nf-icon name="plus"/> Gestionar turnos</a>}</div></section>
@if(error()){<div class="alert error"><nf-icon name="alert"/><span>{{error()}}</span><button class="btn btn-small" (click)="load()">Reintentar</button></div>}
<div class="stats-grid">
 <div class="stat-card"><div class="stat-top"><span>Actividades pendientes</span><span class="stat-icon sky"><nf-icon name="tasks"/></span></div><strong>{{loading()?'—':pending()}}</strong><div class="stat-hint"><span class="dot blue"></span> Por ejecutar</div></div>
 <div class="stat-card"><div class="stat-top"><span>Actividades vencidas</span><span class="stat-icon rose"><nf-icon name="alert"/></span></div><strong>{{loading()?'—':overdue()}}</strong><div class="stat-hint">Requieren seguimiento</div></div>
 <div class="stat-card"><div class="stat-top"><span>Turnos en curso</span><span class="stat-icon mint"><nf-icon name="calendar"/></span></div><strong>{{loading()?'—':active()}}</strong><div class="stat-hint"><span class="dot teal"></span> En ejecución</div></div>
 <div class="stat-card"><div class="stat-top"><span>Actividades completadas</span><span class="stat-icon violet"><nf-icon name="check"/></span></div><strong>{{loading()?'—':completed()}}</strong><div class="stat-hint">Historial registrado</div></div>
</div>
<div class="innovation-shortcuts"><a class="shortcut" routerLink="/app/alertas"><nf-icon name="alert"/><span><strong>Alertas de tareas</strong><small>Vencidas y próximas</small></span><nf-icon name="arrow"/></a><a class="shortcut" routerLink="/app/calendario"><nf-icon name="calendar"/><span><strong>Calendario mensual</strong><small>Turnos por día</small></span><nf-icon name="arrow"/></a>@if(auth.isAdmin()){<a class="shortcut" routerLink="/app/reportes"><nf-icon name="report"/><span><strong>Reportes PDF</strong><small>Indicadores por profesional</small></span><nf-icon name="arrow"/></a>}</div>
<div class="dashboard-grid">
 <section class="surface panel-large"><div class="panel-heading"><div><h2>Seguimiento de actividades</h2><p>Seis actividades · ordenamiento desde el servidor</p></div><a class="simple-link" routerLink="/app/actividades">Ver todas <nf-icon name="arrow"/></a></div>
  @if(loading()){<div class="loading-block"><div class="spinner"></div> Consultando actividades…</div>}
  @else if(recent().length===0){<div class="empty-mini"><nf-icon name="tasks"/><strong>Sin actividades</strong><span>Cuando se creen actividades aparecerán aquí.</span></div>}
  @else{<div class="table-scroll"><table class="data-table compact-table"><thead><tr><th [attr.aria-sort]="activitySort==='titulo'?(activityDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="ACTIVIDAD" field="titulo" [active]="activitySort" [direction]="activityDirection" (change)="sortActivities($event)"/></th><th [attr.aria-sort]="activitySort==='fechaLimite'?(activityDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="PLAZO" field="fechaLimite" [active]="activitySort" [direction]="activityDirection" (change)="sortActivities($event)"/></th><th [attr.aria-sort]="activitySort==='estado'?(activityDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="ESTADO" field="estado" [active]="activitySort" [direction]="activityDirection" (change)="sortActivities($event)"/></th></tr></thead><tbody>@for(a of recent();track a.id){<tr><td><div class="cell-title">{{a.titulo}}</div><div class="cell-sub">Actividad #{{a.id}} · Turno #{{a.turnoId}}</div></td><td><span [class.text-danger]="a.vencida">{{when(a.fechaLimite)}}</span></td><td><nf-badge [value]="a.estado"/>@if(a.vencida){<span class="overdue-label">Vencida</span>}</td></tr>}</tbody></table></div>}
 </section>
 <div class="dashboard-side">
  <section class="surface"><div class="panel-heading"><div><h2>Turnos</h2><p>Programación actual</p></div><a class="simple-link" routerLink="/app/turnos">Ver todos</a></div>
   <div class="list-sortbar"><span>Ordenar turnos</span><button type="button" class="btn btn-small btn-outline" (click)="sortShifts('inicio')">Fecha {{shiftField==='inicio'?(shiftDir==='asc'?'↑':'↓'):'↕'}}</button><button type="button" class="btn btn-small btn-outline" (click)="sortShifts('area')">Área {{shiftField==='area'?(shiftDir==='asc'?'↑':'↓'):'↕'}}</button></div>
   @if(loading()){<div class="loading-block"><div class="spinner"></div></div>}
   @else if(turnos().length===0){<div class="empty-mini small"><nf-icon name="calendar"/><span>No hay turnos disponibles.</span></div>}
   @else{<div class="shift-list">@for(t of turnos();track t.id){<div class="shift-item"><span class="shift-symbol"><nf-icon name="calendar"/></span><div class="shift-main"><strong>{{t.area}}</strong><small>{{when(t.inicio)}}</small></div><nf-badge [value]="t.estado"/></div>}</div>}
  </section>
  <section class="hint-card"><div class="hint-symbol"><nf-icon name="shield"/></div><h3>Operaciones seguras</h3><p>Los cambios de turnos y actividades son validados por el servidor y registrados para su trazabilidad.</p></section>
 </div>
</div>
`})
export class DashboardComponent implements OnInit {
 private api=inject(ApiService);auth=inject(AuthService);
 loading=signal(true);error=signal('');pending=signal(0);overdue=signal(0);active=signal(0);completed=signal(0);recent=signal<Actividad[]>([]);turnos=signal<Turno[]>([]);
 activitySort='creadoEn';activityDirection:'asc'|'desc'='desc';shiftField='inicio';shiftDir:'asc'|'desc'='desc';
 sortShifts(field:string){this.shiftDir=this.shiftField===field&&this.shiftDir==='asc'?'desc':'asc';this.shiftField=field;void this.load();}
 sortActivities(field:string){this.activityDirection=this.activitySort===field&&this.activityDirection==='asc'?'desc':'asc';this.activitySort=field;void this.load();}
 ngOnInit(){void this.load();} when=formatDate;
 saludo(){const hr=new Date().getHours();return hr<12?'Buenos días':hr<19?'Buenas tardes':'Buenas noches';}
 async load(){this.loading.set(true);this.error.set('');
 try {
 const r=await firstValueFrom(forkJoin({
 pending:this.api.page<Actividad>('actividades',{estado:'PENDIENTE',tamano:1}),
 overdue:this.api.page<Actividad>('actividades',{vencidas:true,tamano:1}),
 active:this.api.page<Turno>('turnos',{estado:'EN_CURSO',tamano:1}),
 completed:this.api.page<Actividad>('actividades',{estado:'COMPLETADA',tamano:1}),
 recent:this.api.page<Actividad>('actividades',{tamano:6,ordenarPor:this.activitySort,direccion:this.activityDirection}),
 shifts:this.api.page<Turno>('turnos',{tamano:5,ordenarPor:this.shiftField,direccion:this.shiftDir})
 }));
 this.pending.set(r.pending.totalElementos);this.overdue.set(r.overdue.totalElementos);this.active.set(r.active.totalElementos);this.completed.set(r.completed.totalElementos);this.recent.set(r.recent.contenido);this.turnos.set(r.shifts.contenido);
 }catch(e){this.error.set(apiMessage(e));}finally{this.loading.set(false);}}
}
