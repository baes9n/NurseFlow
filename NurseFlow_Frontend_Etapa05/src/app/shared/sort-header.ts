import { Component, input, output } from '@angular/core';
/** Encabezado de tabla reutilizable. El evento recarga la pagina desde la API. */
@Component({selector:'nf-sort-header',standalone:true,template:`
<button type="button" class="sort-header" (click)="change.emit(field())"
 [class.active]="field()===active()" [attr.aria-label]="'Ordenar por '+label()+', '+(field()===active()?(direction()==='asc'?'ascendente':'descendente'):'más recientes primero')">
 <span>{{label()}}</span><span class="sort-arrow" aria-hidden="true">{{field()===active()?(direction()==='asc'?'↑':'↓'):'↕'}}</span>
</button>`})
export class SortHeaderComponent {
 label=input.required<string>(); field=input.required<string>(); active=input.required<string>(); direction=input<'asc'|'desc'>('desc'); change=output<string>();
}
