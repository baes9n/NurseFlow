import { Injectable, signal } from '@angular/core';
export interface Toast {id:number;message:string;type:'success'|'error'|'info';}
@Injectable({providedIn:'root'})
export class ToastService {
 items=signal<Toast[]>([]); private count=0;
 show(message:string,type:Toast['type']='success') {
   const id=++this.count;this.items.update(v=>[...v,{id,message,type}]);setTimeout(()=>this.dismiss(id),5500);
 }
 dismiss(id:number){this.items.update(v=>v.filter(x=>x.id!==id));}
}
