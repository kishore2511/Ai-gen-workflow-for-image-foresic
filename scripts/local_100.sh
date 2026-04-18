#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
ML_URL="${ML_URL:-http://localhost:8000}"
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-180}"

cleanup() {
  if [[ "${KEEP_STACK_UP:-0}" != "1" ]]; then
    docker compose -f "$ROOT_DIR/docker-compose.yml" down >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

wait_for_url() {
  local name="$1"
  local url="$2"
  local deadline=$((SECONDS + TIMEOUT_SECONDS))

  echo "Waiting for $name at $url"
  until curl -fsS "$url" >/dev/null 2>&1; do
    if (( SECONDS >= deadline )); then
      echo "Timed out waiting for $name ($url) after ${TIMEOUT_SECONDS}s" >&2
      return 1
    fi
    sleep 2
  done
}

echo "[1/4] Starting local stack (no AWS required)"
docker compose -f "$ROOT_DIR/docker-compose.yml" up --build -d

echo "[2/4] Waiting for services"
wait_for_url "ML health" "$ML_URL/health"
wait_for_url "Backend health" "$BACKEND_URL/actuator/health"

echo "[3/4] Running local E2E smoke test"
"$ROOT_DIR/scripts/local_e2e.sh"

echo "[4/4] Local MVP verification complete"
echo "Local pipeline is working end-to-end without AWS."
