# NurseFlow — Etapa 7 | Refinamiento de usabilidad

## Funcionalidades nuevas
1. Encabezados ordenables en pacientes, personal, turnos, actividades, auditoría, alertas, calendario, reportes y seguimiento del dashboard. El Backend ordena las consultas paginadas: pulsar un encabezado no reordena solamente los registros visibles.
2. Predeterminados: pacientes/personal/turnos por ID descendente (orden de alta); actividades por creación descendente; auditoría por fecha descendente; novedades y procedimientos por fecha descendente. Las alertas conservan la prioridad de vencimiento por seguridad operativa.
3. Autocompletado remoto en asignaciones (profesional), actividades (asignación y paciente), novedades (turno) y reportes (profesional). No limita la selección a los primeros 100 registros; consulta hasta 12 sugerencias por vez y permite refinar por texto.
4. Navegación con flechas, Enter y Escape en los selectores, botón de limpiar y mensajes de resultado.
5. Filtros con búsqueda diferida, Limpiar, Más recientes y paginación configurable de 10, 25 o 50 filas.
6. Conserva las ventanas centradas de Asignaciones y Procedimientos.

## Instalación
Este ZIP usa las carpetas originales (`NurseFlow_Etapa01` y `NurseFlow_Frontend_Etapa05`). Detén Angular/Spring Boot, descomprime sobre `/data/Projects` y vuelve a iniciar primero Spring Boot y después Angular. No sobreescribas `.env`.

```bash
cd /data/Projects/NurseFlow_Frontend_Etapa05
npm install
npm run build
npm start
```

Verifica en el navegador que se puede ordenar por encabezado y que las búsquedas encuentran registros que antes no aparecían al superar 100 resultados.
