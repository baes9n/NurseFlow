#!/usr/bin/env bash
set -euo pipefail
API="http://127.0.0.1:8080"
FRONT="http://127.0.0.1:4200"
check() {
  local url="$1" expected="$2" label="$3" got
  got="$(curl --silent --output /dev/null --write-out '%{http_code}' --max-time 10 "$url")" || { echo "FALLO: $label (sin conexión)"; exit 1; }
  if [[ "$got" != "$expected" ]]; then echo "FALLO: $label (HTTP $got, esperado $expected)"; exit 1; fi
  echo "OK: $label (HTTP $got)"
}
check "$API/api/v1/sistema/estado" 200 "Spring Boot activo"
check "$FRONT/" 200 "Angular sirve el frontend"
check "$FRONT/api/v1/sistema/estado" 200 "Proxy Angular -> Spring Boot"
check "$FRONT/api/v1/pacientes" 401 "JWT obligatorio por el proxy"
echo "ETAPA 05: pruebas de conectividad superadas. Comprueba además login y las vistas en el navegador."
