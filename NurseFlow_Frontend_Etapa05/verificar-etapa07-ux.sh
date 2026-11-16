#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
for file in src/app/shared/lookup.ts src/app/shared/sort-header.ts src/app/pages/actividades.ts src/app/pages/turnos.ts; do
 [[ -f "$file" ]] || { echo "FALLO: falta $file"; exit 1; }
done
for x in 'nf-lookup' 'nf-sort-header';do grep -R -l "$x" src/app/pages >/dev/null || { echo "FALLO: falta $x";exit 1;};done
if grep -q 'detail-overlay' src/app/pages/turnos.ts && grep -q 'detail-overlay' src/app/pages/actividades.ts;then echo 'OK: Modales de asignaciones y procedimientos conservados';else echo 'FALLO: No hay modales';exit 1;fi
if grep -q 'LookupComponent' src/app/pages/actividades.ts && grep -q 'LookupComponent' src/app/pages/turnos.ts;then echo 'OK: Autocompletado integrado';fi
if grep -q 'SortHeaderComponent' src/app/pages/actividades.ts && grep -q 'SortHeaderComponent' src/app/pages/turnos.ts;then echo 'OK: Ordenamiento por columnas integrado';fi
if command -v curl >/dev/null;then
 code=$(curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:4200/ || true)
 [[ "$code" == 200 ]] && echo 'OK: Frontend HTTP 200' || echo 'AVISO: Angular no esta iniciado en localhost:4200'
fi
printf 'ETAPA 07: verificaciones estructurales completadas. Ejecuta npm run build para validar Angular.\n'
