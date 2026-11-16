#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
[[ -f .env ]] || { echo 'Falta .env'; exit 1; }
set -a; source .env; set +a
: "${BOOTSTRAP_ADMIN_EMAIL:?Falta BOOTSTRAP_ADMIN_EMAIL}"
# En caso de cambio de clave, ADMIN_TEST_PASSWORD permite verificar la cuenta actual.
admin_pwd="${ADMIN_TEST_PASSWORD:-${BOOTSTRAP_ADMIN_PASSWORD:-}}"
[[ -n "$admin_pwd" ]] || { echo 'Configura ADMIN_TEST_PASSWORD'; exit 1; }
base="http://localhost:${SERVER_PORT:-8080}"
command -v python3 >/dev/null
code=$(curl -sS -o /dev/null -w '%{http_code}' "$base/api/v1/pacientes")
[[ "$code" == '401' ]] || { echo "FALLO: esperado 401 sin token, obtuve $code"; exit 1; }
echo 'OK: acceso anonimo a pacientes rechazado (401)'
# Credenciales en stdin del proceso curl; no imprimir secreto ni token en terminal.
login=$(PASSWORD_PARA_TEST="$admin_pwd" python3 -c 'import json,os; print(json.dumps({"correo":os.environ["BOOTSTRAP_ADMIN_EMAIL"],"contrasena":os.environ["PASSWORD_PARA_TEST"]}))')
response=$(printf '%s' "$login" | curl -sS -X POST "$base/api/v1/auth/login" -H 'Content-Type: application/json' --data-binary @-)
token=$(printf '%s' "$response" | python3 -c 'import sys,json;print(json.load(sys.stdin).get("accessToken", ""))')
[[ -n "$token" ]] || { echo 'FALLO: no pude iniciar sesion como admin'; exit 1; }
echo 'OK: login JWT de administrador'
code=$(curl -sS -o /dev/null -w '%{http_code}' "$base/api/v1/auth/yo" -H "Authorization: Bearer $token")
[[ "$code" == '200' ]] || { echo "FALLO: /yo: $code"; exit 1; }
echo 'OK: usuario autenticado (200)'
code=$(curl -sS -o /dev/null -w '%{http_code}' "$base/api/v1/auditorias" -H "Authorization: Bearer $token")
[[ "$code" == '200' ]] || { echo "FALLO: auditorias: $code"; exit 1; }
echo 'OK: auditoria accesible al administrador'
code=$(curl -sS -o /dev/null -w '%{http_code}' "$base/api/v1/auth/logout" -X POST -H "Authorization: Bearer $token")
[[ "$code" == '204' ]] || { echo "FALLO: logout: $code"; exit 1; }
code=$(curl -sS -o /dev/null -w '%{http_code}' "$base/api/v1/auth/yo" -H "Authorization: Bearer $token")
[[ "$code" == '401' ]] || { echo "FALLO: token revocado debe fallar con 401, fue $code"; exit 1; }
echo 'OK: logout revoca token en el servidor (401 al reutilizar)'
echo 'Etapa 02: pruebas de humo superadas.'
