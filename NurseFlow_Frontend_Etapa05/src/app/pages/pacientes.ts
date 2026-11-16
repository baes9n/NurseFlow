import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { ToastService } from '../core/toast.service';
import { apiMessage } from '../core/errors';
import { Paciente } from '../core/models';
import { IconComponent } from '../shared/icon';
import { PagerComponent } from '../shared/ui';
import { SortHeaderComponent } from '../shared/sort-header';
interface PacienteForm {id:number|null;codigo:string;nombres:string;apellidos:string;fechaNacimiento:string;}
const empty=():PacienteForm=>({id:null,codigo:'',nombres:'',apellidos:'',fechaNacimiento:''});
@Component({selector:'nf-pacientes',standalone:true,imports:[FormsModule,IconComponent,PagerComponent,SortHeaderComponent],template:`
<div class="page-head"><div><div class="eyebrow">REGISTROS</div><h1>Pacientes</h1><p>Registro y consulta de pacientes con información básica y datos de prueba.</p></div>@if(auth.isAdmin()){<button class="btn btn-primary" (click)="open()"><nf-icon name="plus"/> Nuevo paciente</button>}</div>
<div class="surface"><div class="filter-bar"><div class="search-control"><nf-icon name="search"/><input aria-label="Buscar pacientes" placeholder="Buscar por nombre, apellido o código" [(ngModel)]="buscar" (keyup.enter)="load(0)" (ngModelChange)="scheduleSearch()" /></div><select aria-label="Filtrar estado" [(ngModel)]="activo" (change)="load(0)"><option value="">Todos los estados</option><option value="true">Activos</option><option value="false">Inactivos</option></select><button class="btn btn-outline" (click)="load(0)"><nf-icon name="filter"/> Filtrar</button><button type="button" class="btn btn-clear" (click)="clearFilters()">Limpiar</button><button type="button" class="btn btn-clear" (click)="recentFirst()" title="Mostrar últimos registros">Más recientes ↓</button></div>
@if(error()){<div class="alert error"><nf-icon name="alert"/>{{error()}}</div>}
@if(loading()){<div class="loading-block"><div class="spinner"></div> Cargando pacientes…</div>}
@else if(rows().length===0){<div class="empty-state"><span class="empty-symbol"><nf-icon name="patient"/></span><h3>No se encontraron pacientes</h3><p>Intenta cambiar los filtros{{auth.isAdmin()?' o registra tu primer paciente':''}}.</p></div>}
@else{<div class="table-scroll"><table class="data-table"><thead><tr><th [attr.aria-sort]="sortField==='nombres' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="PACIENTE" field="nombres" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th [attr.aria-sort]="sortField==='codigo' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="CÓDIGO" field="codigo" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th [attr.aria-sort]="sortField==='fechaNacimiento' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="FECHA DE NACIMIENTO" field="fechaNacimiento" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th [attr.aria-sort]="sortField==='activo' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="ESTADO" field="activo" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th>@if(auth.isAdmin()){<th class="right">ACCIONES</th>}</tr></thead><tbody>@for(p of rows();track p.id){<tr><td><div class="entity-cell"><span class="entity-avatar blue">{{initials(p)}}</span><div><strong>{{p.nombres}} {{p.apellidos}}</strong><small>ID #{{p.id}}</small></div></div></td><td><span class="mono">{{p.codigo}}</span></td><td>{{p.fechaNacimiento||'—'}}</td><td><span class="badge" [class]="p.activo?'badge teal':'badge muted'">{{p.activo?'Activo':'Inactivo'}}</span></td>@if(auth.isAdmin()){<td class="right"><div class="table-actions"><button class="icon-btn" (click)="open(p)" title="Editar" aria-label="Editar paciente"><nf-icon name="edit"/></button>@if(p.activo){<button class="icon-btn danger" (click)="disable(p)" title="Desactivar" aria-label="Desactivar paciente"><nf-icon name="trash"/></button>}</div></td>}</tr>}</tbody></table></div>}
<nf-pager [page]="page()" [pages]="pages()" [total]="total()" [pageSize]="pageSize" [configurable]="true" (change)="load($event)" (sizeChange)="changePageSize($event)"/>
</div>
@if(modal()){<div class="modal-backdrop" (click)="modal.set(false)"><section class="modal" role="dialog" aria-modal="true" aria-label="Formulario de paciente" (click)="$event.stopPropagation()"><header class="modal-head"><div><div class="eyebrow">REGISTROS</div><h2>{{form.id?'Editar paciente':'Nuevo paciente'}}</h2></div><button class="icon-btn" aria-label="Cerrar" (click)="modal.set(false)"><nf-icon name="close"/></button></header><form (ngSubmit)="save()" #f="ngForm"><div class="modal-body">
<label class="field"><span>Código de paciente *</span><input [(ngModel)]="form.codigo" name="codigo" required pattern="[A-Za-z0-9-]{3,40}" maxlength="40" placeholder="PAC-0001"/></label>
<div class="form-grid"><label class="field"><span>Nombres *</span><input name="nombres" [(ngModel)]="form.nombres" required maxlength="120"/></label><label class="field"><span>Apellidos *</span><input name="apellidos" [(ngModel)]="form.apellidos" required maxlength="120"/></label></div>
<label class="field"><span>Fecha de nacimiento</span><input name="fechaNacimiento" type="date" [max]="today" [(ngModel)]="form.fechaNacimiento"/></label><div class="form-note">Utiliza únicamente pacientes ficticios para el entorno académico.</div>
@if(modalError()){<div class="alert error"><nf-icon name="alert"/>{{modalError()}}</div>}
</div><footer class="modal-foot"><button type="button" class="btn btn-outline" (click)="modal.set(false)">Cancelar</button><button type="submit" class="btn btn-primary" [disabled]="f.invalid||saving()">{{saving()?'Guardando…':'Guardar paciente'}}</button></footer></form></section></div>}
`})
export class PacientesComponent implements OnInit {
 private api=inject(ApiService);auth=inject(AuthService);private toast=inject(ToastService);
 rows=signal<Paciente[]>([]);page=signal(0);pages=signal(0);total=signal(0);pageSize=10;loading=signal(true);error=signal('');modal=signal(false);saving=signal(false);modalError=signal('');
 buscar='';activo='';form:PacienteForm=empty();today=new Date().toISOString().slice(0,10);
 sortField='id';sortDirection:'asc'|'desc'='desc';
 sort(field:string){if(this.sortField===field){this.sortDirection=this.sortDirection==='asc'?'desc':'asc';}else{this.sortField=field;this.sortDirection=field==='id'||field==='creadoEn'||field==='creadaEn'?'desc':'asc';}void this.load(0);}
 private searchTimer:ReturnType<typeof setTimeout>|undefined;
 scheduleSearch(){clearTimeout(this.searchTimer);this.searchTimer=setTimeout(()=>void this.load(0),350);}
 recentFirst(){this.sortField='id';this.sortDirection='desc';void this.load(0);}
 clearFilters(){this.buscar='';this.activo='';void this.load(0);}
 changePageSize(n:number){this.pageSize=n;void this.load(0);}
 ngOnInit(){void this.load();} initials(p:Paciente){return (p.nombres[0]||'P')+(p.apellidos[0]||'');}
 private listRequestSeq=0;
 async load(page=0){const seq=++this.listRequestSeq;this.loading.set(true);this.error.set('');try{const r=await firstValueFrom(this.api.page<Paciente>('pacientes',{buscar:this.buscar,activo:this.activo,pagina:page,tamano:this.pageSize,ordenarPor:this.sortField,direccion:this.sortDirection}));if(seq!==this.listRequestSeq)return;this.rows.set(r.contenido);this.page.set(r.pagina);this.pages.set(r.totalPaginas);this.total.set(r.totalElementos);}catch(e){if(seq===this.listRequestSeq)this.error.set(apiMessage(e));}finally{if(seq===this.listRequestSeq)this.loading.set(false);}}
 open(p?:Paciente){this.form=p?{id:p.id,codigo:p.codigo,nombres:p.nombres,apellidos:p.apellidos,fechaNacimiento:p.fechaNacimiento||''}:empty();this.modalError.set('');this.modal.set(true);}
 async save(){if(!this.form.codigo.trim()||!this.form.nombres.trim()||!this.form.apellidos.trim()||this.saving())return;this.saving.set(true);this.modalError.set('');try{
 const body={codigo:this.form.codigo.trim(),nombres:this.form.nombres.trim(),apellidos:this.form.apellidos.trim(),fechaNacimiento:this.form.fechaNacimiento||null};
 if(this.form.id) await firstValueFrom(this.api.put('pacientes/'+this.form.id,body));else await firstValueFrom(this.api.post('pacientes',body));
 this.toast.show(this.form.id?'Paciente actualizado':'Paciente registrado');this.modal.set(false);await this.load(this.page());
 }catch(e){this.modalError.set(apiMessage(e));}finally{this.saving.set(false);}}
 async disable(p:Paciente){if(!confirm(`¿Desactivar a ${p.nombres} ${p.apellidos}? El historial se conservará.`))return;try{await firstValueFrom(this.api.delete('pacientes/'+p.id));this.toast.show('Paciente desactivado');await this.load(this.page());}catch(e){this.toast.show(apiMessage(e),'error');}}
}
