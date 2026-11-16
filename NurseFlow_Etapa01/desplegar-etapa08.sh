#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if ! command -v docker >/dev/null 2>&1; then echo 'ERROR: Docker no esta instalado'; exit 1; fi
if [[ ! -f .env ]]; then echo 'ERROR: No existe .env (conserva el que ya generaste en Etapa 2)'; exit 1; fi
# Compose carga .env automaticamente; nunca lo imprimimos.
docker compose -f compose.yaml -f compose.deploy.yaml config --quiet
printf '\nSe reutilizara el proyecto Docker "nurseflow" y su volumen de PostgreSQL.\n'
printf 'No ejecutaremos docker compose down -v.\n\n'
docker compose -f compose.yaml -f compose.deploy.yaml up -d --build
printf '\nContenedores:\n'
docker compose -f compose.yaml -f compose.deploy.yaml ps
WEB_PORT="$(sed -n 's/^WEB_PORT=//p' .env | tail -1 | tr -d '\r')"
printf '\nAbre http://localhost:%s/\n' "${WEB_PORT:-8088}"
