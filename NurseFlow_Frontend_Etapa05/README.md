# NurseFlow — Etapa 05 (Angular)

Frontend SPA en español conectado a la API REST Spring Boot de la **Etapa 04**. No incluye datos inventados ni servicios mock. Se incluyen vistas de login, dashboard, pacientes, profesionales, turnos/asignaciones, actividades/procedimientos, novedades, auditoría y cambio de contraseña.

## Requisitos

- Backend de la Etapa 04 ejecutándose en `http://localhost:8080` con PostgreSQL activo.
- Node.js **24.15 o superior**, npm y conexión a Internet para instalar dependencias. Angular 22 requiere Node.js >=24.15 (o versiones compatibles de Node.js 22.22.3+ / 26+).
- Angular CLI local incluida en `devDependencies`; **no es necesario instalar `ng` globalmente**.

## Inicio

```bash
cd /data/Projects
unzip -o ~/Downloads/NurseFlow_Frontend_Etapa05.zip -d /data/Projects
cd NurseFlow_Frontend_Etapa05
node -v
npm install
npm start
```

Abrir **http://localhost:4200**. Usar las credenciales del administrador que ya tienes en la Etapa 02. Para probar el rol ENFERMERIA, crear un profesional desde el frontend, cerrar sesión e iniciar con ese correo y contraseña.

### No cambiar el Backend

La configuración `proxy.conf.json` reenvía `/api` a `http://127.0.0.1:8080`, por lo que Angular y la API tienen el mismo origen a ojos del navegador. No hay que abrir el puerto de PostgreSQL ni configurar CORS para desarrollo por proxy.

Para arrancar el Backend (en **otra terminal**):

```bash
cd /data/Projects/NurseFlow_Etapa01
set -a; source .env; set +a
mvn spring-boot:run
```

Para probar el frontend:

```bash
npm run build
./verificar-etapa05.sh
```

## Arquitectura frontend

- `core/`: modelos TypeScript acordes a las respuestas Java, API HTTP, autenticación JWT, interceptor, guards, mensajes y notificaciones.
- `layout/`: menú por roles, cabecera, notificaciones, navegación SPA.
- `pages/`: componentes standalone (uno por área de negocio), formularios y consultas reales.
- `shared/`: componentes reutilizables de iconos, badges y paginación.

### Permisos y seguridad

- El frontend oculta operaciones administrativas a enfermería, **pero solo el Backend decide los permisos**. No sustituir `@PreAuthorize` en Java.
- El JWT se conserva en `sessionStorage` durante la sesión de pestaña y se envía en Authorization para las peticiones. El servidor revoca el token en logout. Las respuestas `401` eliminan los datos de sesión.
- No se guardan contraseñas ni JWT en repositorios ni en archivos.
- Usar **solo pacientes ficticios** para la presentación.

## Alcance y próximas mejoras

El Dashboard de esta etapa obtiene métricas mediante los conteos reales de las consultas paginadas del Backend, no mediante cálculos falsos. Los módulos muestran búsquedas, paginación, carga, errores, confirmaciones y estados vacíos.

Los selectores de referencia cargan **hasta 100 registros** para asignaciones, turnos, pacientes y profesionales en esta etapa. Si se supera esa cantidad, conviene implementar una búsqueda remota para los selectores. La paginación de las tablas sí se realiza en el servidor.

Próxima etapa: indicadores/reportes por profesional y período desde el Backend, PDF descargable, panel específico de tareas vencidas con alertas automáticas y calendario visual. Estas innovaciones no se presentan como terminadas todavía.

**Verificación:** generado contra los DTOs/controladores reales de la Etapa 04. La compilación Angular completa requiere `npm install` en un entorno con conexión al registro npm.
