#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
[[ -f .env ]] || { echo "Falta .env; conservar el de la Etapa 02"; exit 1; }
set -a
source .env
set +a
python3 verificar-etapa03.py
