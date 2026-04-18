#!/usr/bin/env bash
set -euo pipefail

BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
ML_URL="${ML_URL:-http://localhost:8000}"

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

EMAIL="user.$(date +%s)@example.com"
PASSWORD="password123"
IMG_PATH="$TMP_DIR/sample.png"

python - <<'PY' "$IMG_PATH"
from PIL import Image
import sys
Image.new("RGB", (32, 32), color=(120, 120, 120)).save(sys.argv[1], format="PNG")
PY

echo "[1/6] ML health"
curl -fsS "$ML_URL/health" > "$TMP_DIR/ml_health.json"

echo "[2/6] Backend health"
curl -fsS "$BACKEND_URL/actuator/health" > "$TMP_DIR/backend_health.json"

echo "[3/6] Register"
curl -fsS -X POST "$BACKEND_URL/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" > "$TMP_DIR/register.json"

echo "[4/6] Login"
curl -fsS -X POST "$BACKEND_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" > "$TMP_DIR/login.json"

TOKEN="$(python - <<'PY' "$TMP_DIR/login.json"
import json,sys
with open(sys.argv[1]) as f:
    print(json.load(f)["token"])
PY
)"

echo "[5/6] Upload"
curl -fsS -X POST "$BACKEND_URL/api/analysis/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@$IMG_PATH;type=image/png" > "$TMP_DIR/upload.json"

echo "[6/6] History"
curl -fsS "$BACKEND_URL/api/analysis/history" \
  -H "Authorization: Bearer $TOKEN" > "$TMP_DIR/history.json"

python - <<'PY' "$TMP_DIR/upload.json" "$TMP_DIR/history.json"
import json,sys
with open(sys.argv[1]) as f:
    upload = json.load(f)
with open(sys.argv[2]) as f:
    history = json.load(f)
assert upload["predictionLabel"] in {"real", "fake"}
assert 0.0 <= float(upload["confidence"]) <= 1.0
assert len(upload["imageSha256"]) == 64
assert len(upload["resultSha256"]) == 64
assert isinstance(history, list) and len(history) >= 1
print("E2E smoke test passed")
PY

echo "Done. Artifacts in $TMP_DIR (removed on exit)."
