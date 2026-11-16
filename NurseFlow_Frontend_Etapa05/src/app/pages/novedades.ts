import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { ToastService } from '../core/toast.service';
import { apiMessage, formatDate } from '../core/errors';
import { Novedad, TipoNovedad } from '../core/models';
import { IconComponent } from '../shared/icon';
import { BadgeComponent, PagerComponent } from '../shared/ui';
import { LookupComponent } from '../shared/lookup';
@Component({selector:'nf-novedades',standalone:true,imports:[FormsModule,IconComponent,BadgeComponent,PagerComponent,LookupComponent],template:`
<div class="page-head"><div><div class="eyebrow">SEGUIMIENTO</div><h1>Novedades de turno</h1><p>Observaciones e incidentes registrados por el personal durante sus turnos.</p></div><button class="btn btn-primary" (click)="open()"><nf-icon name="plus"/> Nueva novedad</button></div>
<div class="surface"><div class="filter-bar"><select aria-label="Filtrar por tipo" [(ngModel)]="tipo" (change)="load(0)"><option value="">Todos los tipos</option><option value="GENERAL">General</option><option value="INCIDENTE">Incidente</option><option value="CAMBIO_TURNO">Cambio de turno</option></select><label class="filter-inline"><span>Turno #</span><input type="number" min="1" [(ngModel)]="turnoFiltro" placeholder="Todos"/></label><button class="btn btn-outline" (click)="load(0)"><nf-icon name="filter"/> Filtrar</button><button type="button" class="btn btn-clear" (click)="clearFilters()">Limpiar</button></div><div class="list-sortbar"><span>Ordenar novedades</span><button class="btn btn-small btn-outline" (click)="sortNovedades('registradaEn')">Fecha {{sortField==='registradaEn'?(sortDirection==='asc'?'↑':'↓'):'↕'}}</button><button class="btn btn-small btn-outline" (click)="sortNovedades('tipo')">Tipo {{sortField==='tipo'?(sortDirection==='asc'?'↑':'↓'):'↕'}}</button></div>
@if(error()){<div class="alert error"><nf-icon name="alert"/>{{error()}}</div>}
@if(loading()){<div class="loading-block"><div class="spinner"></div>Consultando novedades…</div>}
@else if(rows().length===0){<div class="empty-state"><span class="empty-symbol"><nf-icon name="bell"/></span><h3>No hay novedades</h3><p>Los reportes del personal aparecerán aquí.</p></div>}
@else{<div class="notice-list">@for(n of rows();track n.id){<article class="notice-item"><span class="notice-icon" [class.incident]="n.tipo==='INCIDENTE'"><nf-icon [name]="n.tipo==='INCIDENTE'?'alert':'bell'"/></span><div class="notice-copy"><div class="notice-header"><strong>Turno #{{n.turnoId}}</strong><nf-badge [value]="n.tipo"/></div><p>{{n.descripcion}}</p><small>{{when(n.registradaEn)}} · Usuario #{{n.registradoPorId}}</small></div></article>}</div>}
<nf-pager [page]="page()" [pages]="pages()" [total]="total()" [pageSize]="pageSize" [configurable]="true" (change)="load($event)" (sizeChange)="changePageSize($event)"/></div>
@if(modal()){<div class="modal-backdrop" (click)="modal.set(false)"><section class="modal" role="dialog" aria-modal="true" aria-label="Registrar novedad" (click)="$event.stopPropagation()"><header class="modal-head"><div><div class="eyebrow">NOVEDADES</div><h2>Registrar novedad</h2></div><button class="icon-btn" (click)="modal.set(false)" aria-label="Cerrar"><nf-icon name="close"/></button></header><form (ngSubmit)="save()" #f="ngForm"><div class="modal-body"><label class="field"><span>Turno *</span><nf-lookup tipo="turno" label="Seleccionar turno" placeholder="Buscar turno por área o #ID…" [value]="form.turnoId||null" (change)="form.turnoId=$event||0"/></label><label class="field"><span>Tipo *</span><select name="tipo" [(ngModel)]="form.tipo" required><option value="GENERAL">Observación general</option><option value="INCIDENTE">Incidente</option><option value="CAMBIO_TURNO">Cambio de turno</option></select></label><label class="field"><span>Descripción *</span><textarea rows="5" maxlength="3000" name="descripcion" [(ngModel)]="form.descripcion" required placeholder="Describe la novedad del turno…"></textarea></label>@if(modalError()){<div class="alert error"><nf-icon name="alert"/>{{modalError()}}</div>}</div><footer class="modal-foot"><button class="btn btn-outline" type="button" (click)="modal.set(false)">Cancelar</button><button class="btn btn-primary" type="submit" [disabled]="f.invalid||!form.turnoId||saving()">{{saving()?'Registrando…':'Registrar novedad'}}</button></footer></form></section></div>}
`})
export class NovedadesComponent implements OnInit {
 private api=inject(ApiService);private toast=inject(ToastService);
 rows=signal<Novedad[]>([]);loading=signal(true);error=signal('');page=signal(0);pages=signal(0);total=signal(0);pageSize=10;modal=signal(false);modalError=signal('');saving=signal(false);tipo='';turnoFiltro:number|null=null;form:{turnoId:number;tipo:TipoNovedad;descripcion:string}={turnoId:0,tipo:'GENERAL',descripcion:''};when=formatDate;
 sortField='registradaEn';sortDirection:'asc'|'desc'='desc';sortNovedades(f:string){this.sortDirection=this.sortField===f&&this.sortDirection==='asc'?'desc':'asc';this.sortField=f;void this.load(0);}
 clearFilters(){this.tipo='';this.turnoFiltro=null;void this.load(0);}
 changePageSize(n:number){this.pageSize=n;void this.load(0);}
 ngOnInit(){void this.load();}
 private listRequestSeq=0;
 async load(page=0){const seq=++this.listRequestSeq;this.loading.set(true);this.error.set('');try{const r=await firstValueFrom(this.api.page<Novedad>('novedades',{pagina:page,tamano:this.pageSize,tipo:this.tipo,turnoId:this.turnoFiltro,ordenarPor:this.sortField,direccion:this.sortDirection}));if(seq!==this.listRequestSeq)return;this.rows.set(r.contenido);this.page.set(r.pagina);this.pages.set(r.totalPaginas);this.total.set(r.totalElementos);}catch(e){if(seq===this.listRequestSeq)this.error.set(apiMessage(e));}finally{if(seq===this.listRequestSeq)this.loading.set(false);}}
 open(){this.form={turnoId:0,tipo:'GENERAL',descripcion:''};this.modalError.set('');this.modal.set(true);}
 async save(){if(!this.form.turnoId||!this.form.descripcion.trim()||this.saving())return;this.saving.set(true);this.modalError.set('');try{await firstValueFrom(this.api.post('novedades',{turnoId:this.form.turnoId,tipo:this.form.tipo,descripcion:this.form.descripcion.trim()}));this.toast.show('Novedad registrada');this.modal.set(false);await this.load(0);}catch(e){this.modalError.set(apiMessage(e));}finally{this.saving.set(false);}}
}
