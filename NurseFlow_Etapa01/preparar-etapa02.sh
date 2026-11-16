#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [[ ! -f .env ]]; then
  cp .env.example .env
  echo "Se ha creado .env desde .env.example"
fi
command -v openssl >/dev/null || { echo 'Instala openssl'; exit 1; }
# Si ya hay un secreto configurado, no volver a generarlo.
if ! grep -Eq '^JWT_SECRET_B64=[A-Za-z0-9+/=]{43,}$' .env; then
  secret="$(openssl rand -base64 48 | tr -d '\n')"
  if grep -q '^JWT_SECRET_B64=' .env; then
     sed -i "s|^JWT_SECRET_B64=.*|JWT_SECRET_B64=${secret}|" .env
  else
     printf '\nJWT_SECRET_B64=%s\n' "$secret" >> .env
  fi
  echo 'Clave JWT aleatoria generada en .env (no mostrar ni subir a Git).'
fi
if ! grep -Eq '^BOOTSTRAP_ADMIN_EMAIL=[^[:space:]]+@[^[:space:]]+$' .env; then
  if grep -q '^BOOTSTRAP_ADMIN_EMAIL=' .env; then
    sed -i 's|^BOOTSTRAP_ADMIN_EMAIL=.*|BOOTSTRAP_ADMIN_EMAIL=admin@nurseflow.local|' .env
  else printf '\nBOOTSTRAP_ADMIN_EMAIL=admin@nurseflow.local\n' >> .env; fi
fi
if ! grep -Eq '^BOOTSTRAP_ADMIN_PASSWORD=.{12,}$' .env; then
  pass="$(openssl rand -hex 16)"
  if grep -q '^BOOTSTRAP_ADMIN_PASSWORD=' .env; then
    sed -i "s|^BOOTSTRAP_ADMIN_PASSWORD=.*|BOOTSTRAP_ADMIN_PASSWORD=${pass}|" .env
  else printf '\nBOOTSTRAP_ADMIN_PASSWORD=%s\n' "$pass" >> .env; fi
  echo "Clave inicial generada para admin@nurseflow.local: ${pass}"
  echo "Copiala ahora y cambiala tras ingresar. No la compartas en capturas."
fi
chmod 600 .env
printf '\n.env preparado. Conserva tu base de datos existente.\n'
printf 'Recuerda ejecutar: set -a; source .env; set +a; mvn clean spring-boot:run\n'
