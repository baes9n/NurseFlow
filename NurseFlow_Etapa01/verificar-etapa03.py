#!/usr/bin/env python3
"""Pruebas E2E contra API local: generan solo registros ficticios."""
import os, json, urllib.request, urllib.error, uuid
from datetime import datetime, timezone, timedelta
from concurrent.futures import ThreadPoolExecutor
from threading import Barrier

BASE = 'http://localhost:' + os.getenv('SERVER_PORT','8080') + '/api/v1'
admin_email = os.getenv('BOOTSTRAP_ADMIN_EMAIL','admin@nurseflow.local')
admin_pwd = os.getenv('ADMIN_TEST_PASSWORD') or os.getenv('BOOTSTRAP_ADMIN_PASSWORD')
if not admin_pwd:
    raise SystemExit('Falta BOOTSTRAP_ADMIN_PASSWORD o ADMIN_TEST_PASSWORD en el entorno.')

def request(method, path, payload=None, token=None):
    headers={'Content-Type':'application/json'}
    if token: headers['Authorization']='Bearer '+token
    data=json.dumps(payload).encode('utf-8') if payload is not None else None
    req=urllib.request.Request(BASE+path, headers=headers, data=data, method=method)
    try:
        with urllib.request.urlopen(req, timeout=20) as r:
            data=r.read()
            return r.status, json.loads(data) if data else {}
    except urllib.error.HTTPError as e:
        data=e.read()
        try: obj=json.loads(data) if data else {}
        except ValueError: obj={'mensaje':'Respuesta no JSON'}
        return e.code,obj

def expect(actual, expected, label):
    if actual[0]!=expected:
        raise AssertionError(f'{label}: esperado HTTP {expected}, recibido {actual[0]} - {actual[1]}')
    print(f'OK: {label} (HTTP {expected})')
    return actual[1]

admin_login=expect(request('POST','/auth/login',{'correo':admin_email,'contrasena':admin_pwd}),200,'login admin')
ADMIN=admin_login['accessToken']
expect(request('GET','/turnos'),401,'turnos protegidos sin JWT')
unique=uuid.uuid4().hex[:12]
prof_pwd=uuid.uuid4().hex+'Xy!'
correo=f'enfermeria.turnos.{unique}@nurseflow.local'
prof=expect(request('POST','/profesionales',{
    'correo':correo,'contrasena':prof_pwd,'nombres':'Prueba','apellidos':'Turnos',
    'numeroLicencia':'TEST-'+unique.upper()},ADMIN),201,'crear profesional ficticio')
P=prof['id']
nurse=expect(request('POST','/auth/login',{'correo':correo,'contrasena':prof_pwd}),200,'login enfermeria')['accessToken']
# Todas las horas en UTC, y fechas futuras variables para poder repetir el script.
day=(datetime.now(timezone.utc)+timedelta(days=10)).replace(hour=0,minute=0,second=0,microsecond=0)
def t(h): return (day+timedelta(hours=h)).isoformat().replace('+00:00','Z')
def turno(start, end, area):
    return expect(request('POST','/turnos',{'area':area,'inicio':t(start),'fin':t(end)},ADMIN),201,
        f'crear turno {start:02d}-{end:02d}')['id']
def asig(turno_id, pro_id=P, token=ADMIN):
    return request('POST','/asignaciones',{'turnoId':turno_id,'profesionalId':pro_id}, token)

t1=turno(7,13,'Laboratorio E2E A')
t2=turno(11,17,'Laboratorio E2E B')
t3=turno(13,19,'Laboratorio E2E C')
a1=expect(asig(t1),201,'primera asignacion permitida')['id']
expect(asig(t2),409,'rechazar turnos superpuestos 07-13 y 11-17')
expect(asig(t1),409,'rechazar asignacion duplicada')
a3=expect(asig(t3),201,'permitir turnos consecutivos 07-13 y 13-19')['id']
expect(request('POST','/asignaciones',{'turnoId':t2,'profesionalId':P}, nurse),403,
    'enfermeria no puede crear asignaciones')
expect(request('GET',f'/turnos/{t1}',token=nurse),200,'enfermeria consulta turno propio')
expect(request('GET',f'/turnos/{t2}',token=nurse),403,'enfermeria no consulta turno ajeno')
expect(request('PUT',f'/turnos/{t3}',{'area':'Prueba','inicio':t(12),'fin':t(20)},ADMIN),409,
    'edicion de turno no puede crear superposicion')
expect(request('POST','/turnos',{'area':'Prueba','inicio':t(20),'fin':t(18)},ADMIN),400,
    'validacion de horario invertido')
# Otra enfermera, dos peticiones que compiten por la misma fila bajo PESSIMISTIC_WRITE.
other=uuid.uuid4().hex[:12]
pro2=expect(request('POST','/profesionales',{
    'correo':f'enfermeria.race.{other}@nurseflow.local', 'contrasena':uuid.uuid4().hex+'A!',
    'nombres':'Prueba','apellidos':'Concurrencia','numeroLicencia':'RACE-'+other.upper()},ADMIN),201,
    'crear segundo profesional de prueba')['id']
t4=turno(8,12,'Laboratorio carrera D')
t5=turno(9,14,'Laboratorio carrera E')
barrera = Barrier(2)
def competencia(turno_id):
    barrera.wait(timeout=10)
    return asig(turno_id, pro2)
with ThreadPoolExecutor(max_workers=2) as pool:
    f1=pool.submit(competencia,t4)
    f2=pool.submit(competencia,t5)
    r1,r2=f1.result(),f2.result()
if sorted([r1[0],r2[0]]) != [201,409]:
    raise AssertionError(f'concurrencia esperaba un 201 y un 409, recibio {r1[0]}, {r2[0]}')
print('OK: dos asignaciones simultaneas incompatibles: una confirmada y otra rechazada')
expect(request('GET','/asignaciones?pagina=0&tamano=10',token=ADMIN),200,
    'consulta paginada de asignaciones')
expect(request('PATCH',f'/turnos/{t1}/cancelar',token=ADMIN),200,'cancelar turno y sus asignaciones')
a=expect(request('GET',f'/asignaciones/{a1}',token=ADMIN),200,'consultar asignacion cancelada')
if a['estado'] != 'CANCELADA': raise AssertionError('La asignacion no fue cancelada')
print('OK: cancelacion en cascada logica, estado=CANCELADA')
expect(request('GET','/auditorias?pagina=0&tamano=10',token=ADMIN),200,'auditoria de operaciones')
print('ETAPA 03: PRUEBAS DE HUMO SUPERADAS. Los registros generados son ficticios.')
