import { HttpErrorResponse } from '@angular/common/http';
export function apiMessage(err:unknown):string {
 if (err instanceof HttpErrorResponse) {
   if (err.status===0) return 'No se pudo conectar con la API. Verifica que Spring Boot esté ejecutándose.';
   const payload=err.error;
   if(typeof payload==='string' && payload && payload.length<200) return payload;
   const message=payload?.mensaje || payload?.message || payload?.detalle;
   if(typeof message==='string' && message.trim()) return message;
   const campos=payload?.campos || payload?.detalles;
   if(campos && typeof campos==='object') return Object.values(campos).join(' · ');
   if(err.status===401) return 'La sesión expiró. Vuelve a iniciar sesión.';
   if(err.status===403) return 'No tienes permiso para ejecutar esta acción.';
   if(err.status===409) return 'La operación entra en conflicto con los datos existentes.';
   return `Error del servidor (HTTP ${err.status}).`;
 }
 return err instanceof Error ? err.message : 'Ocurrió un problema inesperado.';
}
export function formatDate(v:string|null|undefined, time=true):string {
 if(!v)return '—';const d=new Date(v);if(Number.isNaN(d.getTime()))return '—';
 return d.toLocaleString('es-EC',{day:'2-digit',month:'short',year:'numeric',...(time?{hour:'2-digit',minute:'2-digit',hour12:false}:{})});
}
export function dateInput(v:string|null|undefined):string {
 if(!v)return '';const d=new Date(v);if(Number.isNaN(d.getTime()))return '';
 const z=(n:number)=>String(n).padStart(2,'0');return `${d.getFullYear()}-${z(d.getMonth()+1)}-${z(d.getDate())}T${z(d.getHours())}:${z(d.getMinutes())}`;
}
export function isoFromInput(value:string):string {return new Date(value).toISOString();}
export function displayStatus(v:string):string {
 return ({PROGRAMADO:'Programado',EN_CURSO:'En curso',CERRADO:'Cerrado',CANCELADO:'Cancelado',CANCELADA:'Cancelada',PENDIENTE:'Pendiente',EN_PROGRESO:'En progreso',COMPLETADA:'Completada',ACTIVA:'Activa',GENERAL:'General',INCIDENTE:'Incidente',CAMBIO_TURNO:'Cambio de turno'} as Record<string,string>)[v] || v;
}
