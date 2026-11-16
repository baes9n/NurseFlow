#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [[ ! -f .env ]]; then echo 'ERROR: .env no existe'; exit 1; fi
# Variables solo locales, nunca se muestran en la salida.
set -a
# shellcheck disable=SC1091
source .env
set +a
: "${POSTGRES_DB:=nurseflow}"
: "${DB_USERNAME:=nurseflow}"
umask 077
mkdir -p backups
outfile="backups/nurseflow_$(date +%Y%m%d_%H%M%S).sql.gz"
# pg_dump dentro del contenedor de PostgreSQL existente, sin borrar ni alterar datos.
if ! docker compose exec -T postgres pg_dump -U "$DB_USERNAME" -d "$POSTGRES_DB" --no-owner --no-privileges | gzip > "$outfile"; then
  rm -f "$outfile"; echo 'ERROR: fallo la copia de seguridad'; exit 1
fi
if [[ ! -s "$outfile" ]]; then rm -f "$outfile"; echo 'ERROR: copia vacia'; exit 1; fi
printf 'Copia de seguridad: %s\n' "$outfile"
printf 'No subir esta copia a GitHub: puede contener datos personales.\n'
