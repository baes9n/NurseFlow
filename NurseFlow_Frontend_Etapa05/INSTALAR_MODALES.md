# NurseFlow — Actualización correcta de ventanas modales

El ZIP anterior creó una carpeta NUEVA `NurseFlow_Frontend_Etapa06_UI_Corregida`, pero Angular se ejecutaba en `NurseFlow_Frontend_Etapa05`. Esta versión usa la ruta correcta dentro del ZIP.

## Instalar (detener npm start primero)

```bash
cd /data/Projects
unzip -o ~/Downloads/NurseFlow_Frontend_Etapa06_MODALES_Instalacion_Corregida.zip -d /data/Projects
cd /data/Projects/NurseFlow_Frontend_Etapa05
bash verificar-modales.sh
npm start
```

Abre http://localhost:4200, recarga con Ctrl+Shift+R y pulsa `Asignaciones` en Turnos o `Procedimientos` en Actividades.

No se altera PostgreSQL ni el Backend.
