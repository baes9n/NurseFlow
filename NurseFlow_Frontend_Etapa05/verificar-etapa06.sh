#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
BACK="http://127.0.0.1:${SERVER_PORT:-8080}"
FRONT="http://127.0.0.1:4200"
check(){ local url="$1" expected="$2" label="$3" got
  got="$(curl -sS --max-time 10 -o /dev/null -w '%{http_code}' "$url")" || { echo "ERROR: $label (sin conexión)";exit 1; }
  [[ "$got" == "$expected" ]] || { echo "ERROR: $label (HTTP $got, esperado $expected)";exit 1; }
  echo "OK: $label (HTTP $got)"; }
check "$BACK/api/v1/sistema/estado" 200 'API'
check "$FRONT" 200 'Frontend'
check "$FRONT/api/v1/alertas" 401 'Alertas protegidas por JWT'
check "$FRONT/api/v1/reportes/actividades" 401 'Reportes protegidos por JWT'
echo 'ETAPA 06: Conectividad correcta. Completa la comprobación funcional en el navegador.'
