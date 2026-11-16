import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../core/api.service';
import { apiMessage, formatDate } from '../core/errors';
import { Turno } from '../core/models';
import { IconComponent } from '../shared/icon';
import { BadgeComponent } from '../shared/ui';
import { SortHeaderComponent } from '../shared/sort-header';

interface DiaCalendario {clave:string;numero:number;mesActual:boolean;hoy:boolean;turnos:Turno[];}
@Component({selector:'nf-calendario',standalone:true,imports:[RouterLink,IconComponent,BadgeComponent,SortHeaderComponent],template:`
<section class="page-head"><div><div class="eyebrow">PROGRAMACIÓN VISUAL</div><h1>Calendario de turnos</h1><p>Consulta la programación real, con filtros por mes y acceso a la gestión de turnos.</p></div><a class="btn btn-outline" routerLink="/app/turnos"><nf-icon name="calendar"/> Gestionar turnos</a></section>
<div class="surface calendar-panel"><div class="calendar-top"><div><div class="eyebrow">VISTA MENSUAL</div><h2>{{titulo()}}</h2></div><div class="calendar-nav"><button type="button" class="btn btn-outline" (click)="cambiar(-1)" aria-label="Mes anterior"><nf-icon name="chevron"/></button><button type="button" class="btn btn-outline" (click)="hoy()">Hoy</button><button type="button" class="btn btn-outline" (click)="cambiar(1)" aria-label="Mes siguiente"><nf-icon name="arrow"/></button></div></div>
@if(error()){<div class="alert error"><nf-icon name="alert"/>{{error()}} <button class="btn btn-small" (click)="load()">Reintentar</button></div>}
@if(loading()){<div class="loading-block"><div class="spinner"></div>Consultando turnos del mes…</div>}
<div class="calendar-grid" role="group" aria-label="Calendario mensual de turnos">
 @for(n of semanas;track n){<div class="calendar-week-label">{{n}}</div>}
 @for(d of dias();track d.clave){<button type="button" class="calendar-day" [class.faded]="!d.mesActual" [class.today]="d.hoy" [class.selected]="seleccion()===d.clave" (click)="seleccion.set(d.clave)" [attr.aria-label]="d.clave+', '+d.turnos.length+' turnos'" [attr.aria-pressed]="seleccion()===d.clave"><span class="calendar-number">{{d.numero}}</span><span class="calendar-day-events">@for(t of d.turnos.slice(0,2);track t.id){<span class="calendar-event" [class.closed]="t.estado==='CERRADO'" [class.cancelled]="t.estado==='CANCELADO'" [title]="t.area">{{t.area}}</span>}@if(d.turnos.length>2){<small>+{{d.turnos.length-2}} más</small>}</span></button>}
</div>
</div>
<section class="surface calendar-details"><div class="panel-heading"><div><h2>Turnos del {{fechaSeleccionada()}}</h2><p>Los turnos que cruzan medianoche aparecen en los días correspondientes.</p></div><a class="simple-link" routerLink="/app/turnos">Ver lista <nf-icon name="arrow"/></a></div>
@if(turnosDia().length===0){<div class="empty-mini"><nf-icon name="calendar"/><span>No hay turnos programados para esta fecha.</span></div>}
@else{<div class="table-scroll"><table class="data-table"><thead><tr><th [attr.aria-sort]="sortField==='area'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="ÁREA" field="area" [active]="sortField" [direction]="sortDirection" (change)="sortDay($event)"/></th><th [attr.aria-sort]="sortField==='inicio'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="INICIO" field="inicio" [active]="sortField" [direction]="sortDirection" (change)="sortDay($event)"/></th><th [attr.aria-sort]="sortField==='fin'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="FIN" field="fin" [active]="sortField" [direction]="sortDirection" (change)="sortDay($event)"/></th><th [attr.aria-sort]="sortField==='estado'?(sortDirection==='asc'?'ascending':'descending'):null"><nf-sort-header label="ESTADO" field="estado" [active]="sortField" [direction]="sortDirection" (change)="sortDay($event)"/></th></tr></thead><tbody>@for(t of sortedDay();track t.id){<tr><td><strong>{{t.area}}</strong><div class="cell-sub">Turno #{{t.id}}</div></td><td>{{when(t.inicio)}}</td><td>{{when(t.fin)}}</td><td><nf-badge [value]="t.estado"/></td></tr>}</tbody></table></div>}
</section>
`})
export class CalendarioComponent implements OnInit {
 private api=inject(ApiService);
 mes=signal(new Date(new Date().getFullYear(),new Date().getMonth(),1));
 seleccion=signal(this.clave(new Date()));
 turnos=signal<Turno[]>([]);loading=signal(false);error=signal('');when=formatDate;
 semanas=['LUN','MAR','MIÉ','JUE','VIE','SÁB','DOM'];
 titulo=computed(()=>this.mes().toLocaleDateString('es-EC',{month:'long',year:'numeric'}));
 dias=computed<DiaCalendario[]>(()=>{
   const mes=this.mes(),turnos=this.turnos(),hoy=this.clave(new Date());
   const start=new Date(mes.getFullYear(),mes.getMonth(),1);
   start.setDate(start.getDate()-((start.getDay()+6)%7));
   return Array.from({length:42},(_,i)=>{
     const d=new Date(start.getFullYear(),start.getMonth(),start.getDate()+i),sig=new Date(d.getFullYear(),d.getMonth(),d.getDate()+1);
     const eventos=turnos.filter(t=>new Date(t.inicio)<sig&&new Date(t.fin)>d);
     return {clave:this.clave(d),numero:d.getDate(),mesActual:d.getMonth()===mes.getMonth(),hoy:this.clave(d)===hoy,turnos:eventos};
   });
 });
 turnosDia=computed(()=>this.dias().find(d=>d.clave===this.seleccion())?.turnos??[]);
 fechaSeleccionada=computed(()=>{const [y,m,d]=this.seleccion().split('-').map(Number);return new Date(y,m-1,d).toLocaleDateString('es-EC',{day:'numeric',month:'long',year:'numeric'});});
sortField='inicio';sortDirection:'asc'|'desc'='desc';sortRefresh=signal(0);
 sortedDay=computed(()=>{this.sortRefresh();const rows=[...this.turnosDia()];const key=this.sortField;const direction=this.sortDirection==='asc'?1:-1;
  return rows.sort((a,b)=>String(a[key as keyof Turno]).localeCompare(String(b[key as keyof Turno]),'es')*direction);});
 sortDay(field:string){this.sortDirection=this.sortField===field&&this.sortDirection==='asc'?'desc':'asc';this.sortField=field;this.sortRefresh.update(n=>n+1);}
 ngOnInit(){void this.load();}
 clave(d:Date){const z=(n:number)=>String(n).padStart(2,'0');return `${d.getFullYear()}-${z(d.getMonth()+1)}-${z(d.getDate())}`;}
 hoy(){const now=new Date();this.mes.set(new Date(now.getFullYear(),now.getMonth(),1));this.seleccion.set(this.clave(now));void this.load();}
 cambiar(offset:number){const m=this.mes(),next=new Date(m.getFullYear(),m.getMonth()+offset,1);this.mes.set(next);this.seleccion.set(this.clave(next));void this.load();}
 async load(){this.error.set('');this.loading.set(true);this.turnos.set([]);
 try{const m=this.mes(),desde=new Date(m.getFullYear(),m.getMonth(),1);
  desde.setDate(desde.getDate()-((desde.getDay()+6)%7));
  const hasta=new Date(desde.getFullYear(),desde.getMonth(),desde.getDate()+42);
  // El backend pagina realmente: recopilamos todas las paginas de este mes (maximo 2000 turnos).
  const acumulados:Turno[]=[];
  let pagina=0;let total=1;
  do{const r=await firstValueFrom(this.api.page<Turno>('turnos',{desde:desde.toISOString(),hasta:hasta.toISOString(),pagina,tamano:100,ordenarPor:'inicio',direccion:'asc'}));acumulados.push(...r.contenido);total=r.totalPaginas;pagina++;}while(pagina<total&&pagina<20);
  this.turnos.set(acumulados);
  if(pagina<total)this.error.set('Hay demasiados turnos en este mes. Se muestran los primeros 2000; utiliza la lista de turnos para el resto.');
 }catch(e){this.error.set(apiMessage(e));}finally{this.loading.set(false);}}
}
