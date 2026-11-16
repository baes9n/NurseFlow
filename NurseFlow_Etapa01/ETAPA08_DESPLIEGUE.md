# NurseFlow — Etapa 8 | Despliegue local reproducible y repositorio Git

## Objetivo
Esta etapa NO modifica entidades, tablas, usuarios ni reglas de negocio. Prepara la misma
aplicación de la Etapa 7 para ejecutarse con Docker Compose (PostgreSQL + Java + Nginx/Angular),
conservar el volumen de la base y recopilar evidencias para el examen.

## Instalación
Detén las terminales de Spring Boot y `npm start` (no hace falta detener PostgreSQL).
Con el ZIP en Descargas:

```bash
cd /data/Projects
unzip -o ~/Downloads/NurseFlow_Etapa08_Despliegue_Completa.zip -d /data/Projects
cd /data/Projects/NurseFlow_Etapa01
bash respaldar-base-etapa08.sh
bash desplegar-etapa08.sh
bash verificar-etapa08.sh
```

Si `mvn package` o `npm run build` fallan durante `docker compose up`, muestra los logs para diagnosticar.
El primer build descarga Maven, npm y las imagenes Docker, por lo que necesita Internet.

La aplicacion queda por defecto en **http://localhost:8088/** y Swagger
**http://localhost:8088/swagger-ui.html**.

### Por qué no se pierden los datos
Usamos `docker compose -f compose.yaml -f compose.deploy.yaml ...`. El primer archivo
sigue declarando el proyecto `name: nurseflow` y el volumen `nurseflow_postgres_data`.
El segundo archivo solo incorpora las aplicaciones; no recrea ni borra la base de datos.
**Nunca ejecutar `docker compose down -v`** si deseas conservar los datos.

### Comandos de mantenimiento
```bash
cd /data/Projects/NurseFlow_Etapa01
docker compose -f compose.yaml -f compose.deploy.yaml ps
docker compose -f compose.yaml -f compose.deploy.yaml logs -f backend web
docker compose -f compose.yaml -f compose.deploy.yaml up -d --build
# Para detener aplicaciones y DB sin borrar datos:
docker compose -f compose.yaml -f compose.deploy.yaml stop
```

### Consideraciones de seguridad
- Escucha en `127.0.0.1:8088` por defecto: **no está publicado en Internet**.
- Las contraseñas reales y el secreto JWT siguen en `NurseFlow_Etapa01/.env` y se excluyen del repositorio.
- La muestra `.env.example` contiene credenciales solo locales: cambia las contraseñas y el secreto antes de exponer el sistema.
- Solo datos ficticios para el examen; no cargues información clínica real.
- Swagger se enruta a través de Nginx **solo para demostración local**; restringir antes de publicar.
- Antes de hosting público: HTTPS, dominio, política de retención y protección de información sensible.
- El frontend todavía conserva JWT en `sessionStorage`, aceptable para la práctica local, pero requiere revisión de protección frente a XSS antes de un uso clínico real.

## Git y control de versiones
```bash
cd /data/Projects/NurseFlow_Etapa01
python3 exportar-repositorio-etapa08.py
cd /data/Projects/NurseFlow_Repositorio
git init
git add .
git status --short  # REVISA que no haya secretos o datos personales
# Crea un commit con la fecha real; no se puede recrear el historial de etapas anteriores.
git commit -m "Base funcional NurseFlow: Backend, Angular y despliegue local"
```

Se generará un repositorio único con ambos proyectos y CI para pruebas Java y build Angular.
Publicación en GitHub, despliegue HTTPS público e informe Word se prepararán después de verificar
esta instancia local.
