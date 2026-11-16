#!/usr/bin/env bash
set -euo pipefail
echo '--- API y PostgreSQL ---'
curl --fail-with-body -sS http://localhost:8080/api/v1/sistema/estado
echo
echo '--- Actuator ---'
curl --fail-with-body -sS http://localhost:8080/actuator/health
echo
echo '--- Flyway ---'
docker compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT version,description,success FROM flyway_schema_history ORDER BY installed_rank;"'
echo '--- Tablas ---'
docker compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "\dt"'
echo '--- Documentacion Swagger ---'
echo 'http://localhost:8080/swagger-ui.html'
