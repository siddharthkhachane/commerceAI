#!/usr/bin/env bash
# CommerceAI local dev startup (macOS / Linux)
# Usage: ./scripts/start.sh   OR   npm start

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

step() { echo ""; echo "==> $1"; }

echo "CommerceAI — starting local dev environment"
echo "Project root: $ROOT"

step "Checking prerequisites"
for cmd in node npm docker; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Error: $cmd is not installed or not on PATH." >&2
    exit 1
  fi
done

if command -v java >/dev/null 2>&1; then
  java -version
else
  echo "Warning: Java not found. Backend needs Java 21+." >&2
fi

step "Ensuring environment files"
[[ -f .env ]] || cp .env.example .env
[[ -f frontend/.env.local ]] || cp frontend/.env.example frontend/.env.local

step "Installing dependencies (if needed)"
[[ -d node_modules ]] || npm install
[[ -d frontend/node_modules ]] || npm install --prefix frontend

step "Starting PostgreSQL (Docker Compose)"
docker compose up -d

echo "Waiting for PostgreSQL..."
for i in $(seq 1 30); do
  health="$(docker inspect --format '{{.State.Health.Status}}' commerceai-postgres 2>/dev/null || true)"
  if [[ "$health" == "healthy" ]]; then
    echo "PostgreSQL is ready."
    break
  fi
  if docker ps --filter "name=commerceai-postgres" --filter "status=running" -q | grep -q .; then
    if [[ -z "$health" ]]; then
      sleep 2
      echo "PostgreSQL is running."
      break
    fi
  fi
  sleep 1
done

step "Starting frontend + backend"
cat <<'EOF'

  Frontend:  http://localhost:3000
  Backend:   http://localhost:8080
  Health:    http://localhost:8080/api/health

  Admin:     admin@commerceai.com / admin12345
  Stop:      Ctrl+C (then run 'npm run docker:down' to stop Postgres)

EOF

if [[ -f backend/gradlew ]]; then
  npx concurrently -n frontend,backend -c blue,green \
    "npm run dev --prefix frontend" \
    "cd backend && ./gradlew bootRun --args='--spring.profiles.active=local'"
else
  npm run dev
fi
