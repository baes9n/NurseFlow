#!/usr/bin/env python3
"""Genera una copia SIN secretos, lista para crear un repositorio Git unico.
No inicializa Git ni inventa un historial de commits.
"""
from pathlib import Path
import shutil,sys
root=Path(__file__).resolve().parent
frontend=root.parent/'NurseFlow_Frontend_Etapa05'
target=root.parent/'NurseFlow_Repositorio'
if not frontend.is_dir(): sys.exit('FALLO: carpeta del frontend no encontrada')
if target.exists(): sys.exit(f'FALLO: {target} ya existe; no la sobrescribire')
exclude={'.env','.git','target','node_modules','dist','.angular','backups','evidence','screenshots','__pycache__','.idea','.vscode'}

def ignore(directory,names):
    return {n for n in names if n in exclude or (n.startswith('.env.') and n != '.env.example') or n.endswith('.log') or n.endswith('.pyc') or n.endswith('.sql.gz')}
try:
    shutil.copytree(root,target/'NurseFlow_Etapa01',ignore=ignore)
    shutil.copytree(frontend,target/'NurseFlow_Frontend_Etapa05',ignore=ignore)
    ci=target/'.github'/'workflows'/'ci.yml'
    ci.parent.mkdir(parents=True,exist_ok=True)
    ci.write_text("""name: NurseFlow CI
on: [push, pull_request]
jobs:
  backend:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: NurseFlow_Etapa01
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
          cache-dependency-path: NurseFlow_Etapa01/pom.xml
      - run: mvn -B -ntp clean test
  frontend:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: NurseFlow_Frontend_Etapa05
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '24'
      - run: npm install --no-audit --no-fund
      - run: npm run build
""",encoding='utf-8')
    (target/'.gitignore').write_text('.env\n**/.env\n**/node_modules/\n**/target/\n**/dist/\n**/.angular/\n**/backups/\n**/evidence/\n**/screenshots/\n*.sql.gz\n',encoding='utf-8')
    (target/'README.md').write_text("""# NurseFlow — Examen final Programación Web

Caso 11 — Gestión de actividades de enfermería.

- `NurseFlow_Etapa01/`: API Spring Boot 3.5 / Java 21 / PostgreSQL.
- `NurseFlow_Frontend_Etapa05/`: Angular 22.
- `NurseFlow_Etapa01/ETAPA08_DESPLIEGUE.md`: ejecución local mediante Docker Compose.

Se exporta el código sin `.env`, credenciales, backups ni bases de datos.
Crea un historial Git real de aquí en adelante; no atribuyas a este repositorio
commits anteriores que no existieron.
""",encoding='utf-8')
except Exception:
    shutil.rmtree(target,ignore_errors=True)
    raise
print('Repositorio listo para versionar:',target)
print('No se han copiado .env, backups, node_modules ni datos de PostgreSQL')
print('Siguiente paso: cd',target,'&& git init && git add . && git commit -m "Base funcional NurseFlow con despliegue local"')
