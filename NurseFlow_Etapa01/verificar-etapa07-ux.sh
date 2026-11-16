#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [[ ! -f .env ]]; then echo "Falta .env (conserva el de las etapas previas)";exit 1; fi
set -a
source .env
set +a
python3 verificar-etapa07-ux.py
