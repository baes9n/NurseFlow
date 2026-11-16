# NurseFlow — Etapa 06 | Innovaciones finales

## Entrega

Este ZIP contiene **ambos proyectos completos** en las rutas existentes:

- `NurseFlow_Etapa01/` = Backend Spring Boot (ahora v0.6), PostgreSQL y Flyway.
- `NurseFlow_Frontend_Etapa05/` = Frontend Angular (ahora v0.6).

Se conservan los nombres de las carpetas originales para poder actualizar con `unzip -o` sin crear instalaciones paralelas.

### Funcionalidades incorporadas

1. `GET /api/v1/alertas` — tablero de tareas pendientes vencidas y que vencen en las próximas 24 horas, consulta SQL real, permiso por usuario y autoactualización visual cada 60 segundos.
2. `Calendario` — vista mensual de turnos (incluidos los que cruzan medianoche) con navegación por meses y consultas filtradas y paginadas desde Spring Boot.
3. `GET /api/v1/reportes/actividades` y `GET /api/v1/reportes/actividades.pdf` — indicadores por profesional y fecha límite con PDF real producido en el Backend mediante Apache PDFBox, auditoría de descarga y acceso solo para administrador.

**Interpretación de fechas de los reportes:** se agrupan actividades por `fecha_limite` (no por fecha de creación ni realización) y el fin del período es exclusivo. En Angular se seleccionan días completos en zona local. La zona de presentación del PDF es `America/Guayaquil`.

**Privacidad:** el PDF contiene datos agregados del personal, no datos personales de pacientes. Durante las pruebas se utilizan cuentas ficticias. No despliegues datos reales sin revisar permisos, tratamiento de datos y política de seguridad.

### 1. Actualizar SIN perder PostgreSQL ni `.env`

En tu computadora:

```bash
# Detener Spring Boot y Angular con Ctrl+C, mantener Docker PostgreSQL activo
cd /data/Projects
unzip -o ~/Downloads/NurseFlow_Etapa06_Completa.zip -d /data/Projects
```

Los archivos `.env` **no se incluyen** en la entrega y no deben reemplazarse. Las migraciones V1 a V4 no cambian y no hace falta una V5 para estos reportes de solo lectura. **No uses `docker compose down -v`.**

### 2. Backend

```bash
cd /data/Projects/NurseFlow_Etapa01
set -a; source .env; set +a
mvn clean test && mvn spring-boot:run
```

Descargará Apache PDFBox y sus dependencias Maven en el primer arranque, si todavía no están en caché.

### 3. Frontend (otra terminal)

```bash
cd /data/Projects/NurseFlow_Frontend_Etapa05
npm install
npm start
```

Visitar `http://localhost:4200`. Entrar con el administrador y comprobar "Alertas operativas", "Calendario" y "Reportes PDF". Como personal de enfermería se verán únicamente alertas y turnos propios y no el módulo de reportes.

### 4. Pruebas de integración

En una tercera terminal:

```bash
cd /data/Projects/NurseFlow_Etapa01
./verificar-etapa02.sh
./verificar-etapa03.sh
./verificar-etapa04.sh
./verificar-etapa06.sh

cd /data/Projects/NurseFlow_Frontend_Etapa05
./verificar-etapa05.sh
./verificar-etapa06.sh
```

Las pruebas `02-04` generan nuevos datos ficticios. Las pruebas `06` generan turnos y tareas de ejemplo, descargan un PDF en memoria y verifican permisos y estadísticas. Las pruebas no borran registros.

### 5. Demostración recomendada en el examen

- Inicia sesión como administrador y abre las alertas: muestra una tarea vencida, otra próxima y el detalle del turno.
- Abre el calendario: cambia de mes y selecciona un día para visualizar turnos reales.
- Abre reportes: elige un intervalo y profesional, presenta las estadísticas, exporta PDF y comprueba que aparece la operación en la auditoría.
- Inicia sesión como enfermería: comprueba que únicamente aparecen alertas y turnos propios; la ruta de reportes requiere rol ADMIN en el Backend.

### Estado de verificación

Se revisaron los archivos fuente y la sintaxis Java/TypeScript, y se prepararon pruebas de humo. **La compilación Maven y Angular y las comprobaciones contra tu PostgreSQL deben realizarse en tu laptop**, porque el entorno de empaquetado no tiene acceso a los repositorios de dependencias.
