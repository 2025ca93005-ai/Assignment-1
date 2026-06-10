#!/usr/bin/env bash
# ============================================================
# start.sh — Single-script runner for AI Text Summarizer
#
# Usage:
#   ./start.sh                          # Build + run locally (Java)
#   ./start.sh --token hf_xxx           # Build + run locally with token
#   ./start.sh docker                   # Build Docker image + run container
#   ./start.sh docker --token hf_xxx    # Docker run with token
#   ./start.sh docker-build --token hf_xxx  # Force rebuild + run
# ============================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/summarizer-backend"
MODE="${1:-local}"
TOKEN="${HUGGINGFACE_TOKEN:-}"

# Parse --token argument
for arg in "$@"; do
  if [[ "$arg" == --token=* ]]; then
    TOKEN="${arg#--token=}"
  elif [[ "$prev" == "--token" ]]; then
    TOKEN="$arg"
  fi
  prev="$arg"
done

echo ""
echo "========================================"
echo "   AI Text Summarizer — Start Script    "
echo "========================================"
echo ""

# ── Docker mode ────────────────────────────────────────────────────────────
if [[ "$MODE" == "docker" || "$MODE" == "docker-build" ]]; then
    command -v docker >/dev/null 2>&1 || { echo "ERROR: Docker not found."; exit 1; }

    cd "$BACKEND_DIR"

    if [[ "$MODE" == "docker-build" ]] || ! docker images -q summarizer-backend | grep -q .; then
        echo "[1/2] Building Docker image 'summarizer-backend'..."
        docker build -t summarizer-backend .
    fi

    echo "[2/2] Starting container on http://localhost:8080 ..."
    echo "      Press Ctrl+C to stop."
    echo ""

    if [[ -n "$TOKEN" ]]; then
        docker run --rm -p 8080:8080 -e HUGGINGFACE_TOKEN="$TOKEN" summarizer-backend
    else
        echo "WARNING: No HUGGINGFACE_TOKEN set. Export it first: export HUGGINGFACE_TOKEN=hf_xxx"
        docker run --rm -p 8080:8080 summarizer-backend
    fi
    exit 0
fi

# ── Local Java mode ─────────────────────────────────────────────────────────
command -v java >/dev/null 2>&1 || { echo "ERROR: Java not found. Install JDK 25+ or use './start.sh docker'"; exit 1; }

cd "$BACKEND_DIR"

SECRETS_FILE="src/main/resources/secrets.properties"
if [[ -n "$TOKEN" && ! -f "$SECRETS_FILE" ]]; then
    echo "huggingface.token=$TOKEN" > "$SECRETS_FILE"
    echo "Created $SECRETS_FILE with provided token."
elif [[ ! -f "$SECRETS_FILE" && -z "$TOKEN" ]]; then
    echo "WARNING: $SECRETS_FILE not found and no --token provided."
    echo "         Create it: echo 'huggingface.token=hf_xxx' > $SECRETS_FILE"
    echo ""
fi

chmod +x mvnw
echo "[1/2] Building application..."
./mvnw clean package -DskipTests -q

echo "[2/2] Starting application on http://localhost:8080 ..."
echo "      Press Ctrl+C to stop."
echo ""
java -jar target/summarizer-backend-0.0.1-SNAPSHOT.jar
