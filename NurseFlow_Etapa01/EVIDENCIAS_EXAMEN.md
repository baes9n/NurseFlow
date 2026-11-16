# NurseFlow — Matriz de evidencias del examen

Usar exclusivamente datos de prueba ficticios. Las siguientes son **pruebas propuestas**;
las evidencias se deben capturar y documentar una vez ejecutadas, no se deben inventar.

| ID | Módulo | Escenario | Resultado esperado | Evidencia |
|---|---|---|---|---|
| P01 | Auth | Login ADMIN válido | 200, ruta protegida habilitada | Captura pendiente |
| P02 | Pacientes | Alta de paciente | 201, aparece en tabla | Captura pendiente |
| P03 | Asignaciones | Enfermería en turno libre | 201 | Captura pendiente |
| E01 | Turnos | Profesional asignado en dos intervalos superpuestos | 409 | Captura pendiente |
| E02 | Actividades | Completar actividad ya finalizada | 409 | Captura pendiente |
| E03 | Validación | Turno con fin antes del inicio | 400 | Captura pendiente |
| S01 | Seguridad | Solicitud privada sin JWT | 401 | Captura pendiente |
| S02 | Seguridad | Enfermería exporta reporte administrativo | 403 | Captura pendiente |
| C01 | Concurrencia | Asignaciones simultáneas incompatibles | Una 201, otra 409 | Captura pendiente |
| I01 | Innovación | Alertas con tareas reales vencidas / próximas | Actualización y filtros por rol | Captura pendiente |
| I02 | Innovación | Calendario con turnos y cambios de mes | Fechas reales en calendario | Captura pendiente |
| I03 | Innovación | PDF por profesional/periodo | Archivo PDF descargado | Captura pendiente |
| UX01 | Usabilidad | Clic en encabezado de tabla | Orden en backend y página actualizada | Captura pendiente |
| UX02 | Usabilidad | Profesional por autocompletado | Sugerencias paginadas, selección válida | Captura pendiente |
| D01 | Despliegue | Docker Compose y URL local | 3 servicios, HTTP 200 | Captura pendiente |

Antes de la defensa documentar: solicitud, respuesta, condiciones iniciales, dato ficticio,
resultado observado y captura/pantalla. En la presentación demostrar el caso de turnos
superpuestos y la concurrencia. El examen solicita al menos 3 casos correctos, 3 erróneos,
uno de acceso no autorizado, uno de regla de negocio, uno de duplicidad/concurrencia y 3 innovaciones.
