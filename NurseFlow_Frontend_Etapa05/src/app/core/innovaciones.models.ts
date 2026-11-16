export interface AlertaOperacion {actividadId:number;titulo:string;estado:string;fechaLimite:string;turnoId:number;profesionalId:number;profesional:string;prioridad:'VENCIDA'|'PROXIMA';}
export interface TableroAlertas {generadoEn:string;umbralProximas:string;vencidas:number;proximas:number;alertas:AlertaOperacion[];hayMas:boolean;}
export interface ResumenProfesional {profesionalId:number;profesional:string;total:number;pendientes:number;enProgreso:number;completadas:number;canceladas:number;vencidas:number;}
export interface ReportePeriodo {desde:string;hasta:string;generadoEn:string;profesionalId:number|null;total:number;pendientes:number;enProgreso:number;completadas:number;canceladas:number;vencidas:number;profesionales:ResumenProfesional[];}
