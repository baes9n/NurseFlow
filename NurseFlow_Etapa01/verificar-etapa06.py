#!/usr/bin/env python3
"""Pruebas de humo Etapa 06. Crea datos exclusivamente ficticios y no borra registros."""
import os, uuid, json, urllib.request, urllib.error, urllib.parse
from datetime import datetime, timedelta, timezone

BASE = 'http://127.0.0.1:' + os.getenv('SERVER_PORT','8080') + '/api/v1'
EMAIL = os.getenv('BOOTSTRAP_ADMIN_EMAIL','admin@nurseflow.local')
PWD = os.getenv('ADMIN_TEST_PASSWORD') or os.getenv('BOOTSTRAP_ADMIN_PASSWORD')
if not PWD: raise SystemExit('Falta ADMIN_TEST_PASSWORD o BOOTSTRAP_ADMIN_PASSWORD en .env')

def req(method, path, body=None, token=None, binary=False):
    headers = {'Content-Type':'application/json'}
    if token: headers['Authorization']='Bearer '+token
    data = json.dumps(body).encode() if body is not None else None
    r = urllib.request.Request(BASE+path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(r, timeout=30) as response:
            raw = response.read()
            return response.status, (raw if binary else json.loads(raw) if raw else {}),response.headers
    except urllib.error.HTTPError as e:
        raw = e.read()
        try: obj=json.loads(raw)
        except ValueError: obj={'mensaje':str(raw[:200])}
        return e.code,obj,e.headers

def expect(response, code, label):
    if response[0]!=code: raise AssertionError(f'{label}: esperado {code}, obtenido {response[0]} - {response[1]}')
    print(f'OK: {label} (HTTP {code})')
    return response[1]

admin=expect(req('POST','/auth/login',{'correo':EMAIL,'contrasena':PWD}),200,'login admin')['accessToken']
expect(req('GET','/alertas'),401,'alertas protegidas sin JWT')
expect(req('GET','/reportes/actividades'),401,'reportes protegidos sin JWT')
unique=uuid.uuid4().hex[:10]
correo=f'nurseflow.etapa06.{unique}@nurseflow.local'
clave=uuid.uuid4().hex+'A!'
p=expect(req('POST','/profesionales',{'correo':correo,'contrasena':clave,
  'nombres':'Profesional','apellidos':'Ficticio'+unique,'numeroLicencia':'E6-'+unique},admin),201,'crear profesional ficticio')['id']
nurse=expect(req('POST','/auth/login',{'correo':correo,'contrasena':clave}),200,'login enfermeria')['accessToken']
expect(req('GET','/reportes/actividades?desde=2026-11-01T00%3A00%3A00Z&hasta=2026-12-01T00%3A00%3A00Z',token=nurse),403,'enfermeria no accede a reportes de equipo')

def iso(v):return v.replace(microsecond=0).isoformat().replace('+00:00','Z')
now=datetime.now(timezone.utc).replace(microsecond=0)

def actividad(start,end,limite,titulo):
    turno=expect(req('POST','/turnos',{'area':'Prueba alertas Etapa 06','inicio':iso(start),'fin':iso(end)},admin),201,'crear turno ficticio')['id']
    asignacion=expect(req('POST','/asignaciones',{'turnoId':turno,'profesionalId':p},admin),201,'asignar profesional de prueba')['id']
    return expect(req('POST','/actividades',{'asignacionId':asignacion,'titulo':titulo,'fechaLimite':iso(limite)},admin),201,'crear actividad ficticia')['id']

v=actividad(now-timedelta(days=4),now-timedelta(days=3),now-timedelta(days=3,hours=4),'Actividad vencida E6')
f=actividad(now+timedelta(hours=1),now+timedelta(hours=6),now+timedelta(hours=3),'Actividad por vencer E6')
res=expect(req('GET','/alertas',token=nurse),200,'alertas filtradas por rol de enfermeria')
ids={a['actividadId'] for a in res['alertas']}
if not {v,f}.issubset(ids) or res['vencidas']<1 or res['proximas']<1:
    raise AssertionError('Faltan las alertas vencida y proxima de la enfermera de prueba: '+str(res))
print('OK: alertas personales vencidas y proximas con datos reales')
res_admin=expect(req('GET','/alertas',token=admin),200,'tablero del administrador')
if res_admin['vencidas']<1 or res_admin['proximas']<1: raise AssertionError('Alertas administrativas incoherentes')

qs=urllib.parse.urlencode({'desde':iso(now-timedelta(days=7)),'hasta':iso(now+timedelta(days=7)),'profesionalId':p})
rep=expect(req('GET','/reportes/actividades?'+qs,token=admin),200,'reporte por profesional y periodo')
if rep['total']!=2 or len(rep['profesionales'])!=1 or rep['profesionales'][0]['profesionalId']!=p:
    raise AssertionError('El reporte no agrupo las dos actividades del profesional de prueba: '+str(rep))
if rep['vencidas']<1 or rep['pendientes']!=2: raise AssertionError('Conteo de estados en reporte incorrecto')
print('OK: indicadores agrupados y comprobados por profesional')
res=expect(req('GET','/reportes/actividades.pdf?'+qs,token=admin,binary=True),200,'descarga PDF autenticada')
if not res.startswith(b'%PDF-'): raise AssertionError('La respuesta no empieza con firma PDF')
print('OK: PDF real generado por el backend, firma %PDF-')
invalid=urllib.parse.urlencode({'desde':iso(now+timedelta(days=2)),'hasta':iso(now)})
expect(req('GET','/reportes/actividades?'+invalid,token=admin),400,'rechazo de fechas invertidas')
expect(req('GET','/reportes/actividades.pdf?'+qs,token=nurse,binary=True),403,'PDF prohibido a enfermeria')
expect(req('GET','/auditorias?pagina=0&tamano=10',token=admin),200,'auditoria de exportacion')
print('ETAPA 06: PRUEBAS DE HUMO SUPERADAS. Datos de prueba ficticios.')
