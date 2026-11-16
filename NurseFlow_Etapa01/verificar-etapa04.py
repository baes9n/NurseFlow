#!/usr/bin/env python3
"""Pruebas E2E de actividades, procedimientos, novedades y cierre de turno.
Solo genera datos ficticios. Requiere el Backend, PostgreSQL y credenciales de prueba.
"""
import os, json, uuid, urllib.request, urllib.error
from datetime import datetime, timedelta, timezone
from concurrent.futures import ThreadPoolExecutor
from threading import Barrier

BASE = 'http://localhost:' + os.getenv('SERVER_PORT','8080') + '/api/v1'
EMAIL = os.getenv('BOOTSTRAP_ADMIN_EMAIL', 'admin@nurseflow.local')
PWD = os.getenv('ADMIN_TEST_PASSWORD') or os.getenv('BOOTSTRAP_ADMIN_PASSWORD')
if not PWD:
    raise SystemExit('Falta BOOTSTRAP_ADMIN_PASSWORD o ADMIN_TEST_PASSWORD en .env')

def request(method, path, payload=None, token=None):
    headers={'Content-Type':'application/json'}
    if token: headers['Authorization']='Bearer '+token
    data=json.dumps(payload).encode() if payload is not None else None
    req=urllib.request.Request(BASE+path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req,timeout=25) as response:
            raw=response.read()
            return response.status,json.loads(raw) if raw else {}
    except urllib.error.HTTPError as e:
        raw=e.read()
        try: obj=json.loads(raw) if raw else {}
        except ValueError: obj={'respuesta':'No JSON'}
        return e.code,obj

def expect(result, code, description):
    if result[0]!=code:
        raise AssertionError(f'{description}: esperado {code}; recibido {result[0]}: {result[1]}')
    print(f'OK: {description} (HTTP {code})')
    return result[1]

admin=expect(request('POST','/auth/login',{'correo':EMAIL,'contrasena':PWD}),200,'login admin')['accessToken']
expect(request('GET','/actividades'),401,'actividades privadas sin token')
unique=uuid.uuid4().hex[:10]

def nuevo_prof(suffix):
    correo=f'enfermeria.etapa04.{suffix}.{unique}@nurseflow.local'
    clave=uuid.uuid4().hex+'A!'
    pro=expect(request('POST','/profesionales',{
        'correo':correo,'contrasena':clave,'nombres':'Prueba','apellidos':f'Etapa04-{suffix}',
        'numeroLicencia':f'ET4-{suffix.upper()}-{unique.upper()}'}, admin),201,'crear profesional '+suffix)
    jwt=expect(request('POST','/auth/login',{'correo':correo,'contrasena':clave}),200,
        'login enfermeria '+suffix)['accessToken']
    return pro['id'],jwt

P,NURSE=nuevo_prof('a')
Q,OTHER=nuevo_prof('b')
paciente=expect(request('POST','/pacientes',{'codigo':'E4-'+unique,'nombres':'Paciente',
    'apellidos':'Ficticio','fechaNacimiento':'2000-01-01'},admin),201,'crear paciente ficticio')['id']

# Turno futuro para poder repetir pruebas sin depender del reloj del sistema.
day=(datetime.now(timezone.utc)+timedelta(days=20)).replace(hour=0,minute=0,second=0,microsecond=0)
def t(h,base=day): return (base+timedelta(hours=h)).isoformat().replace('+00:00','Z')
turno=expect(request('POST','/turnos',{'area':'Enfermeria prueba E4','inicio':t(7),'fin':t(13)},admin),
    201,'crear turno ficticio')['id']
a=expect(request('POST','/asignaciones',{'turnoId':turno,'profesionalId':P},admin),201,
    'asignar profesional')['id']
expect(request('POST','/actividades',{'asignacionId':a,'pacienteId':paciente,
    'titulo':'Actividad ajena','fechaLimite':t(10)},OTHER),403,'enfermeria no crea actividades')
expect(request('POST','/actividades',{'asignacionId':a,'pacienteId':paciente,
    'titulo':'Fecha fuera de turno','fechaLimite':t(16)},admin),400,'limite fuera del turno')

act=expect(request('POST','/actividades',{'asignacionId':a,'pacienteId':paciente,
    'titulo':'Control de signos vitales','descripcion':'Datos de demostracion','fechaLimite':t(9)},admin),201,
    'crear actividad pendiente')['id']
pending=expect(request('POST','/actividades',{'asignacionId':a,
    'titulo':'Revisar inventario','fechaLimite':t(12)},admin),201,'crear segunda actividad')['id']
expect(request('GET',f'/actividades/{act}',token=NURSE),200,'profesional consulta actividad propia')
expect(request('GET',f'/actividades/{act}',token=OTHER),403,'profesional no consulta actividad ajena')
expect(request('GET',f'/actividades?turnoId={turno}&estado=PENDIENTE&texto=Control&pagina=0&tamano=5',token=admin),200,
    'filtros combinados y paginacion SQL')
expect(request('DELETE',f'/asignaciones/{a}',token=admin),409,'no retirar asignacion con actividades')
expect(request('PUT',f'/turnos/{turno}',{'area':'Turno','inicio':t(6),'fin':t(14)},admin),409,
    'no editar turno que tiene actividades')
expect(request('PATCH',f'/turnos/{turno}/iniciar',token=admin),200,'iniciar turno')
expect(request('PATCH',f'/turnos/{turno}/cerrar',token=admin),409,
    'bloquear cierre con actividades pendientes')
expect(request('PATCH',f'/actividades/{act}/iniciar',token=OTHER),403,
    'enfermeria no inicia tarea ajena')
expect(request('PATCH',f'/actividades/{act}/iniciar',token=NURSE),200,'iniciar actividad propia')
proc=expect(request('POST','/procedimientos',{'actividadId':act,'detalle':'Procedimiento ficticio',
    'observaciones':'Registro de demostracion'},NURSE),201,'registrar procedimiento')['id']
expect(request('GET',f'/procedimientos/{proc}',token=OTHER),403,'procedimiento ajeno prohibido')
expect(request('GET',f'/procedimientos?turnoId={turno}&pagina=0&tamano=5',token=NURSE),200,
    'consultar procedimientos propios')
novedad=expect(request('POST','/novedades',{'turnoId':turno,'tipo':'GENERAL',
    'descripcion':'Observacion ficticia para pruebas'},NURSE),201,'registrar novedad')['id']
expect(request('GET',f'/novedades/{novedad}',token=NURSE),200,'leer novedad de turno propio')
expect(request('GET',f'/novedades/{novedad}',token=OTHER),403,'bloquear novedad de turno ajeno')

# Dos finalizaciones simultaneas de una misma actividad: solo una puede confirmar.
barrier=Barrier(2)
def terminar():
    barrier.wait(timeout=10)
    return request('PATCH',f'/actividades/{act}/completar',token=NURSE)
with ThreadPoolExecutor(max_workers=2) as pool:
    f1=pool.submit(terminar)
    f2=pool.submit(terminar)
    r1,r2=f1.result(),f2.result()
if sorted((r1[0],r2[0])) != [200,409]:
    raise AssertionError(f'Finalizacion concurrente: esperados 200 y 409, obtenidos {r1[0]} y {r2[0]}: {r1[1]} {r2[1]}')
completed=r1[1] if r1[0]==200 else r2[1]
if completed['estado']!='COMPLETADA' or not completed.get('completadoEn') or not completed.get('completadoPorId'):
    raise AssertionError('Falta fecha y usuario responsable de la actividad completada')
print('OK: finalizacion concurrente unica, fecha y usuario registrados')
expect(request('POST','/procedimientos',{'actividadId':act,'detalle':'Despues de completar'},NURSE),409,
    'rechazar procedimientos de actividad finalizada')
expect(request('PATCH',f'/turnos/{turno}/cerrar',token=admin),409,
    'cierre aun bloqueado por segunda actividad')
expect(request('PATCH',f'/actividades/{pending}/cancelar',token=admin),200,
    'cancelar segunda actividad')
expect(request('PATCH',f'/turnos/{turno}/cerrar',token=admin),200,
    'cerrar turno sin actividades abiertas')
expect(request('POST','/novedades',{'turnoId':turno,'tipo':'GENERAL',
    'descripcion':'Intento tardio'},NURSE),409,'no registrar novedad en turno cerrado')
expect(request('PUT',f'/actividades/{act}',{'titulo':'Edicion prohibida',
    'fechaLimite':t(10)},admin),409,'no editar registro completado')
expect(request('PATCH',f'/actividades/{act}/corregir',{'titulo':'Control de signos vitales (aclarado)',
    'descripcion':'Correccion justificada de demostracion','motivo':'Aclarar el nombre de la actividad registrada'},NURSE),403,
    'enfermeria no puede corregir actividad cerrada')
expect(request('PATCH',f'/actividades/{act}/corregir',{'titulo':'Control de signos vitales (aclarado)',
    'descripcion':'Correccion justificada de demostracion','motivo':'Aclarar el nombre de la actividad registrada'},admin),200,
    'correccion autorizada y auditada de registro cerrado')
expect(request('GET','/auditorias?pagina=0&tamano=10',token=admin),200,'consultar auditoria')

# Prueba del tablero de tareas vencidas (fecha pasada, sin depender de datos previos).
last_day=(datetime.now(timezone.utc)-timedelta(days=3)).replace(hour=0,minute=0,second=0,microsecond=0)
turno_old=expect(request('POST','/turnos',{'area':'Tareas vencidas E4',
    'inicio':t(7,last_day),'fin':t(13,last_day)},admin),201,'crear turno historico ficticio')['id']
asig_old=expect(request('POST','/asignaciones',{'turnoId':turno_old,'profesionalId':Q},admin),201,
    'asignar a segundo profesional')['id']
old=expect(request('POST','/actividades',{'asignacionId':asig_old,
    'titulo':'Tarea vencida E4','fechaLimite':t(10,last_day)},admin),201,'crear tarea vencida')['id']
vencidas=expect(request('GET',f'/actividades?turnoId={turno_old}&vencidas=true',token=admin),200,
    'consultar tareas vencidas desde PostgreSQL')
if not any(v['id']==old and v['vencida'] for v in vencidas['contenido']):
    raise AssertionError('La tarea vencida no fue encontrada en el listado')
print('ETAPA 04: PRUEBAS DE HUMO SUPERADAS. Los registros son ficticios.')
