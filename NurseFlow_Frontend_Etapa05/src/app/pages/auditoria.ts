import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../core/api.service';
import { apiMessage, formatDate } from '../core/errors';
import { Auditoria } from '../core/models';
import { IconComponent } from '../shared/icon';
import { PagerComponent } from '../shared/ui';
import { SortHeaderComponent } from '../shared/sort-header';
@Component({selector:'nf-auditoria',standalone:true,imports:[FormsModule,IconComponent,PagerComponent,SortHeaderComponent],template:`
<div class="page-head"><div><div class="eyebrow">TRAZABILIDAD</div><h1>Registro de auditoría</h1><p>Historial de acciones críticas realizadas por los usuarios de la plataforma.</p></div><button class="btn btn-outline" (click)="load(0)"><nf-icon name="refresh"/> Actualizar</button></div>
<div class="surface"><div class="filter-bar"><div class="search-control"><nf-icon name="search"/><input placeholder="Acción, ej. CREAR_ACTIVIDAD" aria-label="Filtrar por acción" [(ngModel)]="accion" (keyup.enter)="load(0)" (ngModelChange)="scheduleSearch()"/></div><input placeholder="Entidad, ej. Turno" aria-label="Filtrar entidad" [(ngModel)]="entidad" (keyup.enter)="load(0)" (ngModelChange)="scheduleSearch()"/><button class="btn btn-outline" (click)="load(0)"><nf-icon name="filter"/> Filtrar</button><button type="button" class="btn btn-clear" (click)="clearFilters()">Limpiar</button></div>
@if(error()){<div class="alert error"><nf-icon name="alert"/>{{error()}}</div>}
@if(loading()){<div class="loading-block"><div class="spinner"></div>Consultando auditoría…</div>}
@else if(rows().length===0){<div class="empty-state"><span class="empty-symbol"><nf-icon name="shield"/></span><h3>Sin registros</h3><p>No se encontraron operaciones con estos filtros.</p></div>}
@else{<div class="table-scroll"><table class="data-table"><thead><tr><th [attr.aria-sort]="sortField==='creadaEn' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="FECHA Y HORA" field="creadaEn" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th [attr.aria-sort]="sortField==='accion' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="ACCIÓN" field="accion" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th [attr.aria-sort]="sortField==='usuario.correo' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="USUARIO" field="usuario.correo" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th [attr.aria-sort]="sortField==='registroId' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="REGISTRO" field="registroId" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th>DETALLE</th></tr></thead><tbody>@for(a of rows();track a.id){<tr><td class="nowrap">{{when(a.fechaHora)}}</td><td><span class="badge blue">{{a.accion}}</span></td><td>{{a.usuario}}</td><td>{{a.entidad}} #{{a.registroId}}</td><td class="muted-cell">{{a.detalle}}</td></tr>}</tbody></table></div>}
<nf-pager [page]="page()" [pages]="pages()" [total]="total()" [pageSize]="pageSize" [configurable]="true" (change)="load($event)" (sizeChange)="changePageSize($event)"/></div>
`})
export class AuditoriaComponent implements OnInit {
 private api=inject(ApiService);
 rows=signal<Auditoria[]>([]);loading=signal(true);error=signal('');page=signal(0);pages=signal(0);total=signal(0);pageSize=10;accion='';entidad='';when=formatDate;
 sortField='creadaEn';sortDirection:'asc'|'desc'='desc';
 sort(field:string){if(this.sortField===field){this.sortDirection=this.sortDirection==='asc'?'desc':'asc';}else{this.sortField=field;this.sortDirection=field==='id'||field==='creadoEn'||field==='creadaEn'?'desc':'asc';}void this.load(0);}
 private searchTimer:ReturnType<typeof setTimeout>|undefined;
 scheduleSearch(){clearTimeout(this.searchTimer);this.searchTimer=setTimeout(()=>void this.load(0),350);}
 clearFilters(){this.accion='';this.entidad='';void this.load(0);}
 changePageSize(n:number){this.pageSize=n;void this.load(0);}
 ngOnInit(){void this.load();}
 private listRequestSeq=0;
 async load(page=0){const seq=++this.listRequestSeq;this.loading.set(true);this.error.set('');try{const r=await firstValueFrom(this.api.page<Auditoria>('auditorias',{pagina:page,tamano:this.pageSize,accion:this.accion,entidad:this.entidad,ordenarPor:this.sortField,direccion:this.sortDirection}));if(seq!==this.listRequestSeq)return;this.rows.set(r.contenido);this.page.set(r.pagina);this.pages.set(r.totalPaginas);this.total.set(r.totalElementos);}catch(e){if(seq===this.listRequestSeq)this.error.set(apiMessage(e));}finally{if(seq===this.listRequestSeq)this.loading.set(false);}}
}
