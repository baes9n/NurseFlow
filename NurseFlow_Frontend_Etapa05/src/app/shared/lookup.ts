import { Component, ElementRef, OnChanges, OnDestroy, SimpleChanges, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApiService, Query } from '../core/api.service';
import { formatDate, apiMessage } from '../core/errors';

type Tipo = 'profesional' | 'paciente' | 'turno' | 'asignacion';
type Row = Record<string, unknown> & {id:number};
/** Selector remoto: nunca descarga el catalogo completo, siempre busca por servidor. */
@Component({selector:'nf-lookup',standalone:true,imports:[FormsModule],host:{'(document:click)':'outside($event)'},template:`
<div class="lookup" [class.lookup-disabled]="disabled()">
  <div class="lookup-input-wrap">
   <input type="text" autocomplete="off" role="combobox" [attr.aria-label]="label()"
    aria-autocomplete="list" [attr.aria-expanded]="opened()" [disabled]="disabled()"
    [placeholder]="placeholder()" [ngModel]="text()" [ngModelOptions]="{standalone:true}" (ngModelChange)="search($event)"
    (focus)="focus()" (keydown)="onKey($event)"/>
   @if(loading()){<span class="lookup-loading" aria-label="Buscando">◌</span>}
   @else if(value()!=null){<button type="button" class="lookup-clear" (click)="clear()" [disabled]="disabled()" aria-label="Quitar selección">×</button>}
   @else{<span class="lookup-search" aria-hidden="true">⌕</span>}
  </div>
  @if(opened()){
   <div class="lookup-menu" role="listbox" [attr.aria-label]="label()">
    @if(allowNone()) {<button type="button" role="option" class="lookup-option" (mousedown)="$event.preventDefault()" (click)="choose(null)">Sin selección</button>}
    @for(item of options();track item.id;let i=$index){
     <button type="button" role="option" class="lookup-option" [class.is-active]="i===activeIndex()"
      [attr.aria-selected]="item.id===value()" [disabled]="notAllowed(item)"
      (mousedown)="$event.preventDefault()" (click)="choose(item)">{{format(item)}}
      @if(notAllowed(item)){<small>No disponible</small>}
     </button>
    }
    @if(loading()){<div class="lookup-hint">Buscando sugerencias…</div>}
    @else if(options().length===0){<div class="lookup-hint">No hay coincidencias. Prueba otro nombre, área o número.</div>}
    @else{<div class="lookup-hint">{{total()}} coincidencias · escribe para precisar la búsqueda</div>}
    @if(error()){<div class="lookup-hint lookup-error">{{error()}}</div>}
   </div>
  }
</div>`})
export class LookupComponent implements OnChanges, OnDestroy {
 private api=inject(ApiService);private el=inject(ElementRef<HTMLElement>);
 tipo=input.required<Tipo>();label=input('Seleccionar registro');placeholder=input('Buscar por nombre o código…');
 value=input<number|null>(null);change=output<number|null>();
 filters=input<Query>({});allowNone=input(false);disabled=input(false);
 text=signal('');opened=signal(false);loading=signal(false);options=signal<Row[]>([]);total=signal(0);error=signal('');activeIndex=signal(-1);
 private timer:ReturnType<typeof setTimeout>|undefined;private seq=0;
 ngOnChanges(changes:SimpleChanges){
  if(changes['value']){
   const id=this.value();if(id==null){if(!this.opened())this.text.set('');}
   else {const item=this.options().find(o=>o.id===id);if(item)this.text.set(this.format(item));else void this.resolve(id);}
  }
  if(changes['filters']&&!changes['filters'].firstChange&&this.opened())this.fetch(this.text());
 }
 ngOnDestroy(){clearTimeout(this.timer);this.seq++;}
 outside(event:MouseEvent){if(!this.el.nativeElement.contains(event.target as Node))this.opened.set(false);}
 focus(){if(this.disabled())return;this.opened.set(true);this.fetch(this.value()!=null?'':this.text());}
 search(s:string){this.text.set(s);this.opened.set(true);this.activeIndex.set(-1);
   if(this.value()!=null)this.change.emit(null);
   clearTimeout(this.timer);this.timer=setTimeout(()=>this.fetch(s),250);
 }
 onKey(e:KeyboardEvent){
  if(e.key==='Escape'){this.opened.set(false);return;}
  if(e.key==='ArrowDown'||e.key==='ArrowUp'){e.preventDefault();if(!this.opened()){this.focus();return;}
    const count=this.options().length;if(count)this.activeIndex.set((this.activeIndex()+(e.key==='ArrowDown'?1:count-1))%count);return;}
  if(e.key==='Enter'&&this.opened()){e.preventDefault();const item=this.options()[this.activeIndex()]??this.options()[0];if(item&&!this.notAllowed(item))this.choose(item);}
 }
 async resolve(id:number){const token=++this.seq;
  try{const r=await firstValueFrom(this.api.get<Row>(`${this.endpoint()}/${id}`));if(token===this.seq&&this.value()===id)this.text.set(this.format(r));}
  catch{if(token===this.seq&&this.value()===id)this.text.set(`#${id}`);}
 }
 private endpoint(){return ({profesional:'profesionales',paciente:'pacientes',turno:'turnos',asignacion:'asignaciones'} as const)[this.tipo()];}
 private params(typed:string):Query {const query:Query={pagina:0,tamano:12,ordenarPor:this.tipo()==='turno'?'inicio':this.tipo()==='asignacion'?'asignadoEn':'id',direccion:'desc',...this.filters()};
   if(typed.trim())query[this.tipo()==='turno'?'area':'buscar']=typed.trim().replace(/^#/, '');
   return query;
 }
 async fetch(s:string){const token=++this.seq;this.loading.set(true);this.error.set('');
  try{
    // Para turnos, buscar por ID exacto tambien, sin obligar a desplazarse entre paginas.
    const [page,exact]=await Promise.all([
      firstValueFrom(this.api.page<Row>(this.endpoint(),this.params(s))),
      this.tipo()==='turno'&&/^#?\d+$/.test(s.trim()) ? firstValueFrom(this.api.get<Row>(`${this.endpoint()}/${s.trim().replace('#','')}`)).catch(()=>null) : Promise.resolve(null)
    ]);
    if(token!==this.seq)return;
    const list=page.contenido.filter(r=>!this.notAllowed(r));
    if(exact&&!this.notAllowed(exact)&&!list.some(x=>x.id===exact.id))list.unshift(exact);
    this.options.set(list);this.total.set(page.totalElementos);
  }catch(e){if(token===this.seq){this.options.set([]);this.error.set(apiMessage(e));}}
  finally{if(token===this.seq)this.loading.set(false);}
 }
 notAllowed(r:Row){if(this.tipo()==='turno')return r['estado']==='CERRADO'||r['estado']==='CANCELADO';return false;}
 format(r:Row){const num=String(r.id);
  switch(this.tipo()){
   case 'profesional':return `${r['nombres']??''} ${r['apellidos']??''} · #${num}`.trim();
   case 'paciente':return `${r['codigo']??''} · ${r['nombres']??''} ${r['apellidos']??''}`.trim();
   case 'turno':return `#${num} · ${r['area']??''} · ${r['inicio']?formatDate(String(r['inicio'])):''}`;
   case 'asignacion':return `#${num} · ${r['profesionalNombre']??('Profesional #'+String(r['profesionalId']))} · ${r['turnoArea']??('Turno #'+String(r['turnoId']))} · ${r['turnoInicio']?formatDate(String(r['turnoInicio'])):''}`;
  }
 }
 choose(item:Row|null){this.seq++;clearTimeout(this.timer);this.opened.set(false);this.activeIndex.set(-1);
  this.text.set(item?this.format(item):'');this.change.emit(item?.id??null);
 }
 clear(){this.choose(null);this.focus();}
}
