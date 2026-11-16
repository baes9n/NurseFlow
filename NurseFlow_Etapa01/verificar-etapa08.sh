#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
# Solo lee WEB_PORT/WEB_BIND desde .env; no expone contrasenas.
WEB_PORT="${WEB_PORT:-8088}"
if [[ -f .env ]]; then
  cfg_port="$(sed -n 's/^WEB_PORT=//p' .env | tail -1 | tr -d '\r')"
  if [[ -n "$cfg_port" ]]; then WEB_PORT="$cfg_port"; fi
fi
BASE="http://127.0.0.1:${WEB_PORT}"
# `up -d` puede devolver el control antes de que Spring Boot termine de iniciar.
ready=0
for attempt in {1..30}; do
  status="$(curl -s -o /dev/null -w '%{http_code}' --max-time 3 "$BASE/api/v1/sistema/estado" || true)"
  if [[ "$status" == 200 ]]; then ready=1; break; fi
  sleep 2
done
if [[ "$ready" != 1 ]]; then
  echo 'FALLO: API sin respuesta por Nginx. Consulta docker compose -f compose.yaml -f compose.deploy.yaml logs backend web'
  exit 1
fi
check() {
  local label="$1" expect="$2" path="$3" status
  status="$(curl -sS -o /dev/null -w '%{http_code}' --max-time 10 "$BASE$path")"
  if [[ "$status" != "$expect" ]]; then
    printf 'FALLO: %s (esperado %s, recibido %s)\n' "$label" "$expect" "$status"; exit 1
  fi
  printf 'OK: %s (HTTP %s)\n' "$label" "$status"
}
check 'Angular en Nginx' 200 '/'
check 'Ruta Angular directa con fallback SPA' 200 '/app/dashboard'
check 'API real por proxy' 200 '/api/v1/sistema/estado'
check 'Endpoint privado rechaza anonimos' 401 '/api/v1/pacientes'
check 'Documentacion OpenAPI' 200 '/v3/api-docs'
python3 - "$BASE" <<'PYCODE'
import json,sys,urllib.request
url=sys.argv[1]+'/api/v1/sistema/estado'
data=json.load(urllib.request.urlopen(url,timeout=10))
assert data.get('aplicacion')=='NurseFlow',data
assert data.get('baseDatos')=='CONECTADA',data
print('OK: API conectada a PostgreSQL')
PYCODE
printf '\nETAPA 08: Despliegue local comprobado.\n'
