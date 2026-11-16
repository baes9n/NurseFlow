import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../core/api.service';
import { ToastService } from '../core/toast.service';
import { apiMessage } from '../core/errors';
import { Profesional } from '../core/models';
import { IconComponent } from '../shared/icon';
import { PagerComponent } from '../shared/ui';
import { SortHeaderComponent } from '../shared/sort-header';
interface Form {id:number|null;correo:string;contrasena:string;nombres:string;apellidos:string;numeroLicencia:string;}
const empty=():Form=>({id:null,correo:'',contrasena:'',nombres:'',apellidos:'',numeroLicencia:''});
@Component({selector:'nf-personal',standalone:true,imports:[FormsModule,IconComponent,PagerComponent,SortHeaderComponent],template:`
<div class="page-head"><div><div class="eyebrow">ADMINISTRACIÓN</div><h1>Personal de enfermería</h1><p>Administra profesionales, credenciales y disponibilidad operativa.</p></div><button class="btn btn-primary" (click)="open()"><nf-icon name="plus"/> Nuevo profesional</button></div>
<div class="surface"><div class="filter-bar"><div class="search-control"><nf-icon name="search"/><input placeholder="Buscar profesional por nombre o correo" aria-label="Buscar profesional" [(ngModel)]="buscar" (keyup.enter)="load(0)" (ngModelChange)="scheduleSearch()"/></div><select [(ngModel)]="activo" (change)="load(0)" aria-label="Estado"><option value="">Todos los estados</option><option value="true">Activos</option><option value="false">Inactivos</option></select><button class="btn btn-outline" (click)="load(0)"><nf-icon name="filter"/> Filtrar</button><button type="button" class="btn btn-clear" (click)="clearFilters()">Limpiar</button><button type="button" class="btn btn-clear" (click)="recentFirst()" title="Mostrar últimos registros">Más recientes ↓</button></div>
@if(error()){<div class="alert error"><nf-icon name="alert"/>{{error()}}</div>}
@if(loading()){<div class="loading-block"><div class="spinner"></div>Cargando profesionales…</div>}
@else if(rows().length===0){<div class="empty-state"><span class="empty-symbol"><nf-icon name="users"/></span><h3>Sin profesionales</h3><p>No se encontraron registros con los filtros actuales.</p></div>}
@else{<div class="table-scroll"><table class="data-table"><thead><tr><th [attr.aria-sort]="sortField==='apellidos' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="PROFESIONAL" field="apellidos" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th [attr.aria-sort]="sortField==='usuario.correo' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="CORREO" field="usuario.correo" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th [attr.aria-sort]="sortField==='numeroLicencia' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="LICENCIA" field="numeroLicencia" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th [attr.aria-sort]="sortField==='activo' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="ESTADO" field="activo" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th class="right">ACCIONES</th></tr></thead><tbody>@for(p of rows();track p.id){<tr><td><div class="entity-cell"><span class="entity-avatar teal">{{initials(p)}}</span><div><strong>{{p.nombres}} {{p.apellidos}}</strong><small>Profesional #{{p.id}}</small></div></div></td><td>{{p.correo}}</td><td>{{p.numeroLicencia||'—'}}</td><td><span class="badge" [class]="p.activo?'badge teal':'badge muted'">{{p.activo?'Activo':'Inactivo'}}</span></td><td class="right"><div class="table-actions"><button class="icon-btn" (click)="open(p)" title="Editar" aria-label="Editar"><nf-icon name="edit"/></button><button class="btn btn-small" [class]="p.activo?'btn-outline danger-text':'btn-outline'" (click)="toggle(p)">{{p.activo?'Desactivar':'Activar'}}</button></div></td></tr>}</tbody></table></div>}
<nf-pager [page]="page()" [pages]="pages()" [total]="total()" [pageSize]="pageSize" [configurable]="true" (change)="load($event)" (sizeChange)="changePageSize($event)"/></div>
@if(modal()){<div class="modal-backdrop" (click)="modal.set(false)"><section class="modal" role="dialog" aria-modal="true" aria-label="Formulario profesional" (click)="$event.stopPropagation()"><header class="modal-head"><div><div class="eyebrow">PERSONAL</div><h2>{{form.id?'Editar profesional':'Nuevo profesional'}}</h2></div><button class="icon-btn" (click)="modal.set(false)" aria-label="Cerrar"><nf-icon name="close"/></button></header><form (ngSubmit)="save()" #f="ngForm"><div class="modal-body"><div class="form-grid"><label class="field"><span>Nombres *</span><input name="nombres" [(ngModel)]="form.nombres" required maxlength="120"/></label><label class="field"><span>Apellidos *</span><input name="apellidos" [(ngModel)]="form.apellidos" required maxlength="120"/></label></div>
<label class="field"><span>Número de licencia</span><input name="licencia" [(ngModel)]="form.numeroLicencia" maxlength="40" placeholder="Opcional"/></label>
@if(!form.id){<label class="field"><span>Correo electrónico *</span><input type="email" email name="correo" [(ngModel)]="form.correo" required maxlength="160"/></label><label class="field"><span>Contraseña inicial *</span><input type="password" name="contrasena" [(ngModel)]="form.contrasena" required minlength="12" maxlength="100" autocomplete="new-password"/><small>Mínimo 12 caracteres.</small></label>}
@if(modalError()){<div class="alert error"><nf-icon name="alert"/>{{modalError()}}</div>}
</div><footer class="modal-foot"><button class="btn btn-outline" type="button" (click)="modal.set(false)">Cancelar</button><button class="btn btn-primary" [disabled]="f.invalid||saving()" type="submit">{{saving()?'Guardando…':'Guardar'}}</button></footer></form></section></div>}
`})
export class PersonalComponent implements OnInit {
 private api=inject(ApiService);private toast=inject(ToastService);
 rows=signal<Profesional[]>([]);page=signal(0);pages=signal(0);total=signal(0);pageSize=10;loading=signal(true);error=signal('');modal=signal(false);saving=signal(false);modalError=signal('');buscar='';activo='';form:Form=empty();
 sortField='id';sortDirection:'asc'|'desc'='desc';
 sort(field:string){if(this.sortField===field){this.sortDirection=this.sortDirection==='asc'?'desc':'asc';}else{this.sortField=field;this.sortDirection=field==='id'||field==='creadoEn'||field==='creadaEn'?'desc':'asc';}void this.load(0);}
 private searchTimer:ReturnType<typeof setTimeout>|undefined;
 scheduleSearch(){clearTimeout(this.searchTimer);this.searchTimer=setTimeout(()=>void this.load(0),350);}
 recentFirst(){this.sortField='id';this.sortDirection='desc';void this.load(0);}
 clearFilters(){this.buscar='';this.activo='';void this.load(0);}
 changePageSize(n:number){this.pageSize=n;void this.load(0);}
 ngOnInit(){void this.load();} initials(p:Profesional){return (p.nombres[0]||'N')+(p.apellidos[0]||'');}
 private listRequestSeq=0;
 async load(page=0){const seq=++this.listRequestSeq;this.loading.set(true);this.error.set('');try{const r=await firstValueFrom(this.api.page<Profesional>('profesionales',{buscar:this.buscar,activo:this.activo,pagina:page,tamano:this.pageSize,ordenarPor:this.sortField,direccion:this.sortDirection}));if(seq!==this.listRequestSeq)return;this.rows.set(r.contenido);this.page.set(r.pagina);this.pages.set(r.totalPaginas);this.total.set(r.totalElementos);}catch(e){if(seq===this.listRequestSeq)this.error.set(apiMessage(e));}finally{if(seq===this.listRequestSeq)this.loading.set(false);}}
 open(p?:Profesional){this.form=p?{id:p.id,correo:p.correo,contrasena:'',nombres:p.nombres,apellidos:p.apellidos,numeroLicencia:p.numeroLicencia||''}:empty();this.modalError.set('');this.modal.set(true);}
 async save(){if(this.saving())return;this.saving.set(true);this.modalError.set('');try{
 const body={nombres:this.form.nombres.trim(),apellidos:this.form.apellidos.trim(),numeroLicencia:this.form.numeroLicencia||null};
 if(this.form.id) await firstValueFrom(this.api.put('profesionales/'+this.form.id,body));
 else await firstValueFrom(this.api.post('profesionales',{...body,correo:this.form.correo,contrasena:this.form.contrasena}));
 this.toast.show(this.form.id?'Profesional actualizado':'Profesional creado con cuenta de acceso');this.modal.set(false);this.form=empty();await this.load(this.page());
 }catch(e){this.modalError.set(apiMessage(e));}finally{this.saving.set(false);}}
 async toggle(p:Profesional){if(!confirm(`¿${p.activo?'Desactivar':'Activar'} al profesional ${p.nombres} ${p.apellidos}?`))return;
 try{await firstValueFrom(this.api.patch('profesionales/'+p.id+'/estado',{activo:!p.activo}));this.toast.show('Estado actualizado');await this.load(this.page());}catch(e){this.toast.show(apiMessage(e),'error');}}
}
