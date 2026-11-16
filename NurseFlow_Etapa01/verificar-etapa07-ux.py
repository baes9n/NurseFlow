#!/usr/bin/env python3
"""Comprobaciones de integracion de consultas UX sin modificar ningun registro."""
import os, json, sys
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode

BASE=f"http://127.0.0.1:{os.getenv('SERVER_PORT','8080')}/api/v1/"
def get(path,token=None,params=None):
    url=BASE+path+('?' + urlencode(params) if params else '')
    headers={'Accept':'application/json'}
    if token:headers['Authorization']='Bearer '+token
    try:
        with urlopen(Request(url,headers=headers),timeout=10) as r:
            body=r.read();return r.status,json.loads(body.decode()) if body else None
    except HTTPError as e:return e.code,None

def check(condition,message):
    if not condition:print('FALLO: '+message);sys.exit(1)
    print('OK: '+message)
try:
    status,health=get('sistema/estado');check(status==200,'API disponible')
    status,_=get('pacientes');check(status==401,'API protegida sin JWT')
    body=json.dumps({'correo':os.getenv('BOOTSTRAP_ADMIN_EMAIL',''),'contrasena':os.getenv('BOOTSTRAP_ADMIN_PASSWORD','')}).encode()
    req=Request(BASE+'auth/login',data=body,headers={'Content-Type':'application/json'},method='POST')
    with urlopen(req,timeout=10) as r:token=json.load(r)['accessToken']
    check(bool(token),'Login administrativo')
    # Cada ordenamiento se aplica en el SQL del backend, no se reordena una pagina aislada.
    checks=[
       ('pacientes','fechaNacimiento','asc'),('pacientes','activo','desc'),
       ('profesionales','apellidos','asc'),('profesionales','usuario.correo','asc'),
       ('turnos','id','desc'),('turnos','area','asc'),
       ('actividades','creadoEn','desc'),('actividades','asignacion.profesional.apellidos','asc'),
       ('asignaciones','asignadoEn','desc'),('novedades','registradaEn','desc'),
       ('auditorias','creadaEn','desc'),('auditorias','usuario.correo','asc')
    ]
    for route,field,direction in checks:
        status,record=get(route,token,{'pagina':0,'tamano':5,'ordenarPor':field,'direccion':direction})
        check(status==200 and isinstance(record,dict) and isinstance(record.get('contenido'),list),f'{route} ordenado por {field} ({direction})')
    status,r=get('asignaciones',token,{'buscar':'a','pagina':0,'tamano':5})
    check(status==200 and isinstance(r.get('contenido'),list),'Autocompletado remoto de asignaciones')
    print('ETAPA 07: pruebas de consultas y seguridad superadas. No se escribieron datos.')
except (URLError,KeyError,ValueError) as e:
    print('FALLO:',str(e));sys.exit(1)
