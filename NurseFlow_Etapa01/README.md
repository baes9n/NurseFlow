# NurseFlow — Etapa 04: actividades, procedimientos y novedades

**Examen práctico de Programación Web — Caso 11, Gestión de actividades de enfermería.**

Esta entrega contiene el proyecto completo actualizado desde las etapas 1–3 y mantiene la misma carpeta `NurseFlow_Etapa01`, el volumen de PostgreSQL, el usuario administrador y el archivo `.env` local. **No ejecutar `docker compose down -v` ni eliminar el volumen.** Todos los datos utilizados en los scripts son ficticios.

## Requisitos

- JDK 21 (el proyecto compila con `--release 21`), Maven y Docker/Compose.
- PostgreSQL 16 iniciado desde `docker compose up -d` (localhost:5441).
- `.env` configurado en etapas anteriores con `JWT_SECRET_B64`, credenciales y administrador.

## Instalación de la actualización

Detener el Backend anterior con `Ctrl+C`, pero mantener PostgreSQL iniciado.

```bash
cd /data/Projects
unzip -o ~/Downloads/NurseFlow_Etapa04.zip -d /data/Projects
cd /data/Projects/NurseFlow_Etapa01
chmod +x verificar-etapa0*.sh
set -a; source .env; set +a
mvn clean test && mvn spring-boot:run
```

Flyway ejecutará únicamente la migración nueva `V4__indices_seguimiento_actividades.sql`. No se alteran las tablas ni sus registros existentes.

Verificar, desde otra terminal:

```bash
cd /data/Projects/NurseFlow_Etapa01
chmod +x verificar-etapa0*.sh
./verificar-etapa02.sh
./verificar-etapa03.sh
./verificar-etapa04.sh
```

Los scripts crean profesionales/pacientes/turnos/actividades ficticios con identificadores aleatorios para evitar duplicados. Algunos registros quedan deliberadamente como tareas vencidas para futuras demostraciones. Para consultar la documentación: http://localhost:8080/swagger-ui.html.

## Endpoints nuevos

Todos los endpoints requieren JWT. Los datos de profesionales y pacientes y los permisos de etapas previas permanecen activos.

| Método y ruta | Rol | Descripción |
|---|---|---|
| `POST /api/v1/actividades` | ADMIN | Crea una actividad pendiente dentro de un turno/asignación activa |
| `GET /api/v1/actividades` | ADMIN/ENFERMERIA | Lista por turno, profesional, paciente, estado, texto, fecha límite, vencimiento; orden y paginación SQL |
| `GET /api/v1/actividades/{id}` | ADMIN/ENFERMERIA | Enfermería solo ve sus actividades |
| `PUT /api/v1/actividades/{id}` | ADMIN | Modifica tareas todavía pendientes |
| `PATCH /api/v1/actividades/{id}/iniciar` | ADMIN/ENFERMERIA | Marca EN_PROGRESO |
| `PATCH /api/v1/actividades/{id}/completar` | ADMIN/ENFERMERIA | Fecha y usuario tomados del servidor/JWT |
| `PATCH /api/v1/actividades/{id}/cancelar` | ADMIN | Cancelación lógica de tarea abierta |
| `PATCH /api/v1/actividades/{id}/corregir` | ADMIN | Corrige texto con motivo documentado, incluso tras cierre |
| `POST /api/v1/procedimientos` | ENFERMERIA | Registra un procedimiento en tarea propia EN_PROGRESO |
| `GET /api/v1/procedimientos` | ADMIN/ENFERMERIA | Lista filtrable, paginada y protegida |
| `GET /api/v1/procedimientos/{id}` | ADMIN/ENFERMERIA | Consulta por ID con permisos |
| `POST /api/v1/novedades` | ADMIN/ENFERMERIA | Registra novedad en turno no cerrado, solo propio para enfermería |
| `GET /api/v1/novedades` | ADMIN/ENFERMERIA | Lista filtrable, paginada y protegida |
| `GET /api/v1/novedades/{id}` | ADMIN/ENFERMERIA | Consulta protegida |
| `PATCH /api/v1/turnos/{id}/iniciar` | ADMIN | PROGRAMADO → EN_CURSO |
| `PATCH /api/v1/turnos/{id}/cerrar` | ADMIN | EN_CURSO → CERRADO, si todas las tareas están finalizadas |

### Ejemplos JSON

Crear actividad (`POST /api/v1/actividades`, Bearer admin):

```json
{
  "asignacionId": 1,
  "pacienteId": 1,
  "titulo": "Control de signos vitales",
  "descripcion": "Prueba con paciente ficticio",
  "fechaLimite": "2026-12-15T10:00:00Z"
}
```

La fecha límite debe estar entre el inicio y el fin de su turno. Se debe usar una asignación activa. Los IDs y la fecha deben adaptarse a registros existentes.

Registrar procedimiento (`POST /api/v1/procedimientos`, Bearer enfermería):

```json
{
  "actividadId": 1,
  "detalle": "Procedimiento ficticio para examen",
  "observaciones": "Solo demostración"
}
```

Solo el profesional responsable podrá registrar procedimientos cuando la actividad esté `EN_PROGRESO`.

Registrar novedad (`POST /api/v1/novedades`):

```json
{
  "turnoId": 1,
  "tipo": "GENERAL",
  "descripcion": "Novedad ficticia durante el turno"
}
```

Corrección administrativa (`PATCH /api/v1/actividades/1/corregir`):

```json
{
  "titulo": "Control de signos vitales (aclaración)",
  "descripcion": "Corrección justificada",
  "motivo": "Se corrigió la descripción por una errata detectada en la revisión"
}
```

## Reglas de negocio aplicadas

1. Una actividad pertenece a una asignación y por ella a un turno y responsable.
2. El vencimiento se calcula en el Backend según fecha límite y estado; `GET /actividades?vencidas=true` se filtra desde PostgreSQL.
3. Una actividad completada almacena fecha/hora y usuario responsable. Su transición se permite una sola vez, incluso ante solicitudes concurrentes.
4. Una tarea finalizada no puede cambiar de estado. La corrección excepcional de título/descripción requiere rol ADMIN y motivo con auditoría de antes/después, incluso cuando el turno esté cerrado.
5. El profesional solo puede consultar y modificar sus actividades; el registro de procedimientos también exige ser responsable de la actividad.
6. No se cierra un turno con actividades abiertas; un turno cerrado no admite nuevas tareas ni novedades.
7. No se cancela un turno ni se retira una asignación que ya tenga actividades. No se permite cambiar el horario de un turno con actividades asociadas.
8. El Backend valida los parámetros, usa `409` para conflictos, `403` para permisos y `400` para datos inválidos.
9. Los procedimientos y novedades son registros históricos, sin endpoint de modificación destructiva.

### Integridad y concurrencia

Las operaciones críticas se ejecutan con `@Transactional` y un orden de bloqueo coherente: `turnos` y luego `actividades`, usando `PESSIMISTIC_WRITE`. La finalización actualiza estado, fecha y usuario en la misma transacción. La tabla `actividades` ya incluye una restricción SQL de coherencia para las actividades completadas. La corrección administrativa genera auditoría en la misma transacción que la actualización.

## Pruebas incluidas

- Tests unitarios de reglas de estado, límite temporal y vencimiento.
- `verificar-etapa04.py` para flujos completos: permisos, consultas avanzadas, procedimiento, novedad, cierre de turno, corrección auditada y dos finalizaciones simultáneas.
- Se mantienen los scripts de las etapas anteriores.

**Estado de validación de esta entrega**: sintaxis Java revisada con el compilador JDK 21, reglas puras compiladas, Python y Bash comprobados. Maven completo y la integración con PostgreSQL deben ejecutarse y confirmarse en el equipo del estudiante.

## Próximo trabajo

Etapa 5: interfaz Angular responsive con panel de administrador/enfermería. Posteriormente: tableros de tareas vencidas y estadísticas, calendario visual y generación de reportes por profesional y período.
