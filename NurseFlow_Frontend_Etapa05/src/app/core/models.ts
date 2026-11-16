export type Rol = 'ADMIN' | 'ENFERMERIA';
export type EstadoTurno = 'PROGRAMADO' | 'EN_CURSO' | 'CERRADO' | 'CANCELADO';
export type EstadoActividad = 'PENDIENTE' | 'EN_PROGRESO' | 'COMPLETADA' | 'CANCELADA';
export type EstadoAsignacion = 'ACTIVA' | 'CANCELADA';
export type TipoNovedad = 'GENERAL' | 'INCIDENTE' | 'CAMBIO_TURNO';
export interface Pagina<T> {contenido:T[]; totalElementos:number; totalPaginas:number; pagina:number; tamano:number;}
export interface Usuario {id:number; correo:string; rol:Rol; activo:boolean;}
export interface TokenRespuesta {accessToken:string; tokenType:string; expiresIn:number; usuario:Usuario;}
export interface Profesional {id:number; usuarioId:number; correo:string; nombres:string; apellidos:string; numeroLicencia:string|null; activo:boolean;}
export interface Paciente {id:number; codigo:string; nombres:string; apellidos:string; fechaNacimiento:string|null; activo:boolean;}
export interface Turno {id:number; area:string; inicio:string; fin:string; estado:EstadoTurno; creadoEn:string;}
export interface Asignacion {id:number; turnoId:number; profesionalId:number; estado:EstadoAsignacion; asignadoEn:string; profesionalNombre?:string; turnoArea?:string; turnoInicio?:string;}
export interface Actividad {id:number; asignacionId:number; turnoId:number; profesionalId:number; pacienteId:number|null; titulo:string; descripcion:string|null; fechaLimite:string; estado:EstadoActividad; vencida:boolean; completadoEn:string|null; completadoPorId:number|null; creadoEn:string; profesionalNombre?:string;}
export interface Procedimiento {id:number; actividadId:number; profesionalId:number; detalle:string; observaciones:string|null; realizadoEn:string;}
export interface Novedad {id:number; turnoId:number; registradoPorId:number; tipo:TipoNovedad; descripcion:string; registradaEn:string;}
export interface Auditoria {id:number; usuario:string; accion:string; entidad:string; registroId:number; detalle:string; fechaHora:string;}
export interface ApiError {timestamp?:string; status?:number; error?:string; mensaje?:string; message?:string; detalles?:Record<string,string>; campos?:Record<string,string>;}
