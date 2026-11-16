import { Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { displayStatus } from '../core/errors';
@Component({selector:'nf-badge',standalone:true,template:`<span [class]="'badge '+css()">{{ label() }}</span>`})
export class BadgeComponent {
 value=input.required<string>(); label=()=>displayStatus(this.value());
 css=()=>({PROGRAMADO:'blue',EN_CURSO:'teal',CERRADO:'muted',CANCELADO:'red',CANCELADA:'red',PENDIENTE:'amber',EN_PROGRESO:'blue',COMPLETADA:'teal',ACTIVA:'teal',INCIDENTE:'red',GENERAL:'blue',CAMBIO_TURNO:'amber'} as Record<string,string>)[this.value()]||'muted';
}
@Component({selector:'nf-pager',standalone:true,imports:[FormsModule],template:`
@if(pages()>0){<div class="pager"><span>Página {{page()+1}} de {{pages()}} · {{total()}} registros</span><div class="pager-actions">@if(configurable()){<label class="pager-size">Filas <select [ngModel]="pageSize()" (ngModelChange)="sizeChange.emit($event)" aria-label="Filas por página"><option [ngValue]="10">10</option><option [ngValue]="25">25</option><option [ngValue]="50">50</option></select></label>}<button class="icon-btn" type="button" aria-label="Anterior" [disabled]="page()===0" (click)="change.emit(page()-1)"><span aria-hidden="true">‹</span></button><button class="icon-btn" type="button" aria-label="Siguiente" [disabled]="page()+1>=pages()" (click)="change.emit(page()+1)"><span aria-hidden="true">›</span></button></div></div>}`})
export class PagerComponent {page=input(0);pages=input(0);total=input(0);pageSize=input(10);configurable=input(false);change=output<number>();sizeChange=output<number>();}
