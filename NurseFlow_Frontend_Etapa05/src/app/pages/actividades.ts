import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { ToastService } from '../core/toast.service';
import { apiMessage, dateInput, isoFromInput, formatDate } from '../core/errors';
import { Actividad, Asignacion, Procedimiento, Turno } from '../core/models';
import { IconComponent } from '../shared/icon';
import { BadgeComponent, PagerComponent } from '../shared/ui';
import { SortHeaderComponent } from '../shared/sort-header';
import { LookupComponent } from '../shared/lookup';
type Mode='create'|'edit'|'correct';
interface ActivityForm {id:number|null;asignacionId:number;pacienteId:number;titulo:string;descripcion:string;fechaLimite:string;motivo:string;}
const empty=():ActivityForm=>({id:null,asignacionId:0,pacienteId:0,titulo:'',descripcion:'',fechaLimite:'',motivo:''});
@Component({selector:'nf-actividades',standalone:true,imports:[FormsModule,IconComponent,BadgeComponent,PagerComponent,SortHeaderComponent,LookupComponent],host:{'(document:keydown.escape)':'closeProcedures()'},template:`
<div class="page-head"><div><div class="eyebrow">PLAN DE CUIDADOS</div><h1>Actividades</h1><p>Supervisa pendientes, registra ejecuciones y conserva la trazabilidad de cada actividad.</p></div>@if(auth.isAdmin()){<button class="btn btn-primary" (click)="open()"><nf-icon name="plus"/> Nueva actividad</button>}</div>
<div class="surface"><div class="filter-bar filters-wide"><div class="search-control"><nf-icon name="search"/><input placeholder="Buscar por título" aria-label="Buscar actividades" [(ngModel)]="texto" (keyup.enter)="load(0)" (ngModelChange)="scheduleSearch()"/></div>
<select [(ngModel)]="estado" (change)="load(0)" aria-label="Estado de actividad"><option value="">Todos los estados</option><option value="PENDIENTE">Pendiente</option><option value="EN_PROGRESO">En progreso</option><option value="COMPLETADA">Completada</option><option value="CANCELADA">Cancelada</option></select>
<select [(ngModel)]="vencidas" (change)="load(0)" aria-label="Actividades vencidas"><option value="">Todas las fechas</option><option value="true">Solo vencidas</option><option value="false">No vencidas</option></select><label class="filter-inline"><span>Turno #</span><input type="number" min="1" [(ngModel)]="turnoFiltro" placeholder="Todos"/></label><button class="btn btn-outline" (click)="load(0)"><nf-icon name="filter"/> Filtrar</button><button type="button" class="btn btn-clear" (click)="clearFilters()">Limpiar</button><button type="button" class="btn btn-clear" (click)="recentFirst()" title="Mostrar últimos registros">Más recientes ↓</button></div>
@if(error()){<div class="alert error"><nf-icon name="alert"/>{{error()}}</div>}
@if(loading()){<div class="loading-block"><div class="spinner"></div>Cargando actividades…</div>}
@else if(rows().length===0){<div class="empty-state"><span class="empty-symbol"><nf-icon name="tasks"/></span><h3>Sin actividades para mostrar</h3><p>Prueba otros filtros{{auth.isAdmin()?' o crea una actividad vinculada a una asignación':''}}.</p></div>}
@else{<div class="table-scroll"><table class="data-table"><thead><tr><th [attr.aria-sort]="sortField==='titulo' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="ACTIVIDAD" field="titulo" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th [attr.aria-sort]="sortField==='asignacion.profesional.apellidos' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="RESPONSABLE" field="asignacion.profesional.apellidos" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th [attr.aria-sort]="sortField==='fechaLimite' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="FECHA LÍMITE" field="fechaLimite" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th [attr.aria-sort]="sortField==='estado' ? (sortDirection==='asc'?'ascending':'descending') : null"><nf-sort-header label="ESTADO" field="estado" [active]="sortField" [direction]="sortDirection" (change)="sort($event)"/></th><th class="right">ACCIONES</th></tr></thead><tbody>@for(a of rows();track a.id){<tr><td><strong class="cell-title">{{a.titulo}}</strong><div class="cell-sub">#{{a.id}} · Turno #{{a.turnoId}} · Asignación #{{a.asignacionId}}</div>@if(a.descripcion){<div class="cell-description">{{a.descripcion}}</div>}</td><td>{{a.profesionalNombre||profName(a.profesionalId)}}</td><td><span [class.text-danger]="a.vencida">{{when(a.fechaLimite)}}</span>@if(a.vencida){<div class="overdue-label">¡Vencida!</div>}</td><td><nf-badge [value]="a.estado"/></td><td class="right"><div class="table-actions wrap-actions"><button type="button" class="btn btn-small btn-outline" (click)="select(a)" [attr.aria-expanded]="chosen()?.id===a.id" aria-controls="nf-procedimientos-panel" title="Ver procedimientos de esta actividad">Procedimientos</button>
@if(a.estado==='PENDIENTE'){
 @if(!auth.isAdmin()){<button class="btn btn-small btn-accent" (click)="transition(a,'iniciar')">Iniciar</button>}
 @if(auth.isAdmin()){<button class="icon-btn" title="Editar" aria-label="Editar actividad" (click)="open(a,'edit')"><nf-icon name="edit"/></button><button class="icon-btn danger" title="Cancelar" aria-label="Cancelar actividad" (click)="transition(a,'cancelar')"><nf-icon name="trash"/></button>}
}
@if(a.estado==='EN_PROGRESO'){
 @if(!auth.isAdmin()){<button class="btn btn-small btn-primary" (click)="transition(a,'completar')">Completar</button>}
 @if(auth.isAdmin()){<button class="btn btn-small btn-outline danger-text" (click)="transition(a,'cancelar')">Cancelar</button>}
}
@if(auth.isAdmin()&&(a.estado==='COMPLETADA'||a.estado==='CANCELADA')){<button class="icon-btn" title="Corrección auditada" aria-label="Corregir actividad" (click)="open(a,'correct')"><nf-icon name="edit"/></button>}
</div></td></tr>}</tbody></table></div>}
<nf-pager [page]="page()" [pages]="pages()" [total]="total()" [pageSize]="pageSize" [configurable]="true" (change)="load($event)" (sizeChange)="changePageSize($event)"/></div>
@if(chosen()){<div class="detail-overlay" (click)="closeProcedures()"><section id="nf-procedimientos-panel" class="surface assign-panel detail-dialog" role="dialog" aria-modal="true" aria-label="Procedimientos de la actividad" (click)="$event.stopPropagation()"><div class="panel-heading"><div><h2>Procedimientos · {{chosen()?.titulo}}</h2><p>Actividad #{{chosen()?.id}} · Registro histórico de procedimientos</p></div><button class="icon-btn" (click)="closeProcedures()" aria-label="Cerrar detalles"><nf-icon name="close"/></button></div>
@if(!auth.isAdmin()&&chosen()?.estado==='EN_PROGRESO'){<form (ngSubmit)="registerProcedure()" class="procedure-form" #pf="ngForm"><div class="form-grid"><label class="field"><span>Procedimiento realizado *</span><textarea [(ngModel)]="procedureForm.detalle" name="detalle" required maxlength="3000" rows="2" placeholder="Describe el procedimiento…"></textarea></label><label class="field"><span>Observaciones</span><textarea [(ngModel)]="procedureForm.observaciones" name="observaciones" maxlength="3000" rows="2" placeholder="Opcional"></textarea></label></div><button type="submit" class="btn btn-primary" [disabled]="pf.invalid||procedureSaving()">{{procedureSaving()?'Guardando…':'Registrar procedimiento'}}</button></form>}
@if(auth.isAdmin()){<div class="form-note detail-guidance">Como administrador puedes consultar los procedimientos. Para registrar uno, inicia sesión como el profesional responsable y pon la actividad en progreso.</div>}
@else if(chosen()?.estado!=='EN_PROGRESO'){<div class="form-note detail-guidance">Esta actividad está en estado {{chosen()?.estado}}. Solo puedes registrar procedimientos mientras esté en progreso.</div>}
@if(procedureLoading()){<div class="loading-block"><div class="spinner"></div>Cargando procedimientos…</div>}
@else if(procedures().length===0){<div class="empty-mini"><nf-icon name="activity"/><span>Aún no hay procedimientos para esta actividad.</span></div>}
@else{<div class="procedure-list">@for(p of procedures();track p.id){<article class="procedure-item"><span class="procedure-dot"><nf-icon name="check"/></span><div><strong>{{p.detalle}}</strong>@if(p.observaciones){<p>{{p.observaciones}}</p>}<small>{{when(p.realizadoEn)}} · Profesional #{{p.profesionalId}}</small></div></article>}</div>}
<div class="list-sortbar"><span>Procedimientos</span><button type="button" class="btn btn-small btn-outline" (click)="sortProcedures()">{{procSortDirection==='desc'?'Más recientes ↓':'Más antiguos ↑'}}</button></div><nf-pager [page]="procPage()" [pages]="procPages()" [total]="procTotal()" (change)="loadProcedures($event)"/></section></div>}
@if(modal()){<div class="modal-backdrop" (click)="modal.set(false)"><section class="modal" role="dialog" aria-modal="true" aria-label="Formulario de actividad" (click)="$event.stopPropagation()"><header class="modal-head"><div><div class="eyebrow">ACTIVIDADES DE ENFERMERÍA</div><h2>{{mode()==='create'?'Nueva actividad':mode()==='edit'?'Editar actividad':'Corrección administrativa'}}</h2></div><button class="icon-btn" aria-label="Cerrar" (click)="modal.set(false)"><nf-icon name="close"/></button></header><form (ngSubmit)="save()" #f="ngForm"><div class="modal-body">
@if(mode()==='create'){<label class="field"><span>Asignación responsable *</span><nf-lookup tipo="asignacion" label="Asignación responsable" placeholder="Buscar enfermero, área o número de asignación…" [filters]="assignLookupFilter" [value]="form.asignacionId||null" (change)="onAssignment($event)"/></label>@if(selectedAssignment()){<div class="form-note">Turno: {{selectedAssignment()?.area}} · {{when(selectedAssignment()?.inicio)}} a {{when(selectedAssignment()?.fin)}}. El plazo debe estar dentro del turno.</div>}}
@if(mode()!=='correct'){<label class="field"><span>Paciente vinculado</span><nf-lookup tipo="paciente" label="Paciente vinculado" placeholder="Buscar paciente por nombre o código…" [filters]="patientLookupFilter" [allowNone]="true" [value]="form.pacienteId||null" (change)="form.pacienteId=$event||0"/></label>}
<label class="field"><span>Título *</span><input name="titulo" [(ngModel)]="form.titulo" required maxlength="160" placeholder="Ej. Control de signos vitales"/></label>
<label class="field"><span>Descripción</span><textarea name="descripcion" [(ngModel)]="form.descripcion" rows="3" maxlength="3000" placeholder="Indicaciones o detalles operativos"></textarea></label>
@if(mode()!=='correct'){<label class="field"><span>Fecha límite *</span><input type="datetime-local" name="fechaLimite" [(ngModel)]="form.fechaLimite" required/></label>}
@if(mode()==='correct'){<label class="field"><span>Motivo de la corrección *</span><textarea name="motivo" [(ngModel)]="form.motivo" required minlength="10" maxlength="500" rows="3" placeholder="Explica por qué se modifica este registro cerrado"></textarea></label><div class="form-note">La corrección preservará los datos de finalización y quedará registrada en auditoría.</div>}
@if(modalError()){<div class="alert error"><nf-icon name="alert"/>{{modalError()}}</div>}
</div><footer class="modal-foot"><button type="button" class="btn btn-outline" (click)="modal.set(false)">Cancelar</button><button type="submit" class="btn btn-primary" [disabled]="f.invalid||saving()||mode()==='create'&&!form.asignacionId">{{saving()?'Guardando…':mode()==='correct'?'Guardar corrección':'Guardar actividad'}}</button></footer></form></section></div>}
`})
export class ActividadesComponent implements OnInit {
 private route=inject(ActivatedRoute);private api=inject(ApiService);auth=inject(AuthService);private toast=inject(ToastService);
 rows=signal<Actividad[]>([]);loading=signal(true);error=signal('');page=signal(0);pages=signal(0);total=signal(0);pageSize=10;texto='';estado='';vencidas='';turnoFiltro:number|null=null;
 modal=signal(false);mode=signal<Mode>('create');form:ActivityForm=empty();saving=signal(false);modalError=signal('');assignLookupFilter={estado:'ACTIVA'};patientLookupFilter={activo:true};
 turnoElegido=signal<Turno|null>(null);
 chosen=signal<Actividad|null>(null);procedures=signal<Procedimiento[]>([]);procedureLoading=signal(false);procedureSaving=signal(false);procedureForm={detalle:'',observaciones:''};procSortDirection:'asc'|'desc'='desc';procPage=signal(0);procPages=signal(0);procTotal=signal(0);when=formatDate;
 sortField='creadoEn';sortDirection:'asc'|'desc'='desc';
 sort(field:string){if(this.sortField===field){this.sortDirection=this.sortDirection==='asc'?'desc':'asc';}else{this.sortField=field;this.sortDirection=field==='id'||field==='creadoEn'||field==='creadaEn'?'desc':'asc';}void this.load(0);}
 private searchTimer:ReturnType<typeof setTimeout>|undefined;
 scheduleSearch(){clearTimeout(this.searchTimer);this.searchTimer=setTimeout(()=>void this.load(0),350);}
 recentFirst(){this.sortField='creadoEn';this.sortDirection='desc';void this.load(0);}
 clearFilters(){this.texto='';this.estado='';this.vencidas='';this.turnoFiltro=null;void this.load(0);}
 changePageSize(n:number){this.pageSize=n;void this.load(0);}
 ngOnInit(){const q=this.route.snapshot.queryParamMap;
  const turno=Number(q.get('turnoId'));if(Number.isInteger(turno)&&turno>0)this.turnoFiltro=turno;
  if(q.get('vencidas')==='true')this.vencidas='true';
  void this.load();}
 private listRequestSeq=0;
 async load(page=0){const seq=++this.listRequestSeq;this.loading.set(true);this.error.set('');try{const r=await firstValueFrom(this.api.page<Actividad>('actividades',{pagina:page,tamano:this.pageSize,estado:this.estado,vencidas:this.vencidas,turnoId:this.turnoFiltro,texto:this.texto,ordenarPor:this.sortField,direccion:this.sortDirection}));if(seq!==this.listRequestSeq)return;this.rows.set(r.contenido);this.page.set(r.pagina);this.pages.set(r.totalPaginas);this.total.set(r.totalElementos);}catch(e){if(seq===this.listRequestSeq)this.error.set(apiMessage(e));}finally{if(seq===this.listRequestSeq)this.loading.set(false);}}
 profName(id:number){return `Profesional #${id}`;}
 selectedAssignment(){return this.turnoElegido();}
 async onAssignment(id:number|null){this.form.asignacionId=id||0;this.turnoElegido.set(null);
  if(!id)return;
  try{const a=await firstValueFrom(this.api.get<Asignacion>('asignaciones/'+id));if(this.form.asignacionId!==id)return;
   const t=await firstValueFrom(this.api.get<Turno>('turnos/'+a.turnoId));if(this.form.asignacionId!==id)return;
   this.turnoElegido.set(t);this.form.fechaLimite=dateInput(t.fin);
  }catch(e){this.modalError.set('No se pudo consultar el turno elegido: '+apiMessage(e));}}
 open(a?:Actividad,mode:Mode='create'){this.mode.set(mode);this.form=a?{id:a.id,asignacionId:a.asignacionId,pacienteId:a.pacienteId||0,titulo:a.titulo,descripcion:a.descripcion||'',fechaLimite:dateInput(a.fechaLimite),motivo:''}:empty();this.modalError.set('');this.modal.set(true);this.turnoElegido.set(null);}
 async save(){this.modalError.set('');if(!this.form.titulo.trim()||this.saving())return;
 if(this.mode()!=='correct') {const d=new Date(this.form.fechaLimite);if(Number.isNaN(+d)){this.modalError.set('Debes indicar una fecha límite válida.');return;}}
 this.saving.set(true);try{let result:Actividad;
 if(this.mode()==='correct'){
   if(this.form.motivo.trim().length<10){this.modalError.set('El motivo debe tener al menos 10 caracteres.');return;}
   result=await firstValueFrom(this.api.patch<Actividad>('actividades/'+this.form.id+'/corregir',{titulo:this.form.titulo.trim(),descripcion:this.form.descripcion,motivo:this.form.motivo.trim()}));
 } else if(this.mode()==='edit'){
   result=await firstValueFrom(this.api.put<Actividad>('actividades/'+this.form.id,{pacienteId:this.form.pacienteId||null,titulo:this.form.titulo.trim(),descripcion:this.form.descripcion,fechaLimite:isoFromInput(this.form.fechaLimite)}));
 } else {
   result=await firstValueFrom(this.api.post<Actividad>('actividades',{asignacionId:this.form.asignacionId,pacienteId:this.form.pacienteId||null,titulo:this.form.titulo.trim(),descripcion:this.form.descripcion,fechaLimite:isoFromInput(this.form.fechaLimite)}));
 }
 this.toast.show('Actividad guardada correctamente');this.modal.set(false);await this.load(this.page());
 }catch(e){this.modalError.set(apiMessage(e));}finally{this.saving.set(false);}}
 async transition(a:Actividad,action:'iniciar'|'completar'|'cancelar'){
 const text={iniciar:'iniciar',completar:'completar',cancelar:'cancelar'}[action];if(!confirm(`¿Quieres ${text} la actividad «${a.titulo}»?`))return;
 try{const updated=await firstValueFrom(this.api.patch<Actividad>('actividades/'+a.id+'/'+action));this.toast.show('Actividad actualizada');if(this.chosen()?.id===a.id)this.chosen.set(updated);await this.load(this.page());}catch(e){this.toast.show(apiMessage(e),'error');}}
 select(a:Actividad){this.chosen.set(a);this.procedures.set([]);this.procedureForm={detalle:'',observaciones:''};void this.loadProcedures(0);}
 closeProcedures(){this.chosen.set(null);this.procedures.set([]);this.procedureForm={detalle:'',observaciones:''};}
 sortProcedures(){this.procSortDirection=this.procSortDirection==='desc'?'asc':'desc';void this.loadProcedures(0);}
 async loadProcedures(page=0){if(!this.chosen())return;this.procedureLoading.set(true);try{const r=await firstValueFrom(this.api.page<Procedimiento>('procedimientos',{actividadId:this.chosen()!.id,pagina:page,tamano:10,ordenarPor:'realizadoEn',direccion:this.procSortDirection}));this.procedures.set(r.contenido);this.procPage.set(r.pagina);this.procPages.set(r.totalPaginas);this.procTotal.set(r.totalElementos);}catch(e){this.toast.show(apiMessage(e),'error');}finally{this.procedureLoading.set(false);}}
 async registerProcedure(){if(!this.chosen()||!this.procedureForm.detalle.trim()||this.procedureSaving())return;this.procedureSaving.set(true);
 try{await firstValueFrom(this.api.post('procedimientos',{actividadId:this.chosen()!.id,detalle:this.procedureForm.detalle.trim(),observaciones:this.procedureForm.observaciones.trim()}));this.toast.show('Procedimiento registrado');this.procedureForm={detalle:'',observaciones:''};await this.loadProcedures(0);}catch(e){this.toast.show(apiMessage(e),'error');}finally{this.procedureSaving.set(false);}}
}
