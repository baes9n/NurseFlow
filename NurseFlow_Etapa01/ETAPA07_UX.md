# NurseFlow — Etapa 7 | Usabilidad

## Cambios en el Backend
- Nuevo filtro `buscar` en `GET /api/v1/asignaciones` para localizar por nombres, apellidos, área o ID, con paginación real.
- `AsignacionRespuesta` incluye nombre del profesional, área e inicio del turno para resultados legibles.
- `ActividadRespuesta` incluye el nombre del profesional para evitar catálogos truncados en las tablas.
- Ordenamiento adicional permitido para fechas, estados, correos y responsables. Los campos siguen limitados a una lista segura.
- Auditoría admite `ordenarPor` y `direccion` y continúa protegida por ADMIN.
- No se requieren migraciones nuevas. No cambió la estructura de tablas, las reglas clínicas ni las credenciales.

## Verificación
Con el backend y PostgreSQL iniciados:

```bash
cd /data/Projects/NurseFlow_Etapa01
./verificar-etapa07-ux.sh
```

Ejecuta además las pruebas existentes de las etapas 2, 3, 4 y 6; se mantienen los endpoints y permisos previos.
