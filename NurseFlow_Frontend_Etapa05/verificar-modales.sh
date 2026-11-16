#!/usr/bin/env bash
set -euo pipefail
BASE="$(cd "$(dirname "$0")" && pwd)"
T="$BASE/src/app/pages/turnos.ts"
A="$BASE/src/app/pages/actividades.ts"
C="$BASE/src/styles.css"
for f in "$T" "$A" "$C"; do
  if [[ ! -f "$f" ]]; then echo "FALLO: falta $f"; exit 1; fi
done
if grep -q 'class="detail-overlay"' "$T" && grep -q 'class="detail-overlay"' "$A" && grep -q '.detail-overlay{position:fixed;' "$C"; then
  echo "OK: Asignaciones usa una ventana modal."
  echo "OK: Procedimientos usa una ventana modal."
  echo "OK: Estilos de ventana centrada y fija instalados."
  echo "Directorio comprobado: $BASE"
else
  echo 'FALLO: el Frontend activo todavía contiene la interfaz anterior.'
  exit 1
fi
