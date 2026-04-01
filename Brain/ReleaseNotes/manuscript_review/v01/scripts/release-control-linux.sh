#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.yml"
ENV_FILE="${ROOT_DIR}/.env"

infra_services=(mysql redis minio)
core_services=(brain-server nginx-web)
extended_services=(brain-monitor-admin brain-snailjob-server)

require_env() {
  if [[ ! -f "${ENV_FILE}" ]]; then
    echo ".env not found. Copy .env.example to .env and fill real values first."
    exit 1
  fi
}

compose() {
  docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" "$@"
}

resolve_group() {
  case "${1:-minimal}" in
    infra) printf '%s\n' "${infra_services[@]}" ;;
    core|minimal) printf '%s\n' "${infra_services[@]}" "${core_services[@]}" ;;
    extended|all|full) printf '%s\n' "${infra_services[@]}" "${core_services[@]}" "${extended_services[@]}" ;;
    *)
      echo "Unknown group: $1"
      exit 1
      ;;
  esac
}

require_env

ACTION="${1:-status}"
TARGET="${2:-minimal}"

mapfile -t SERVICES < <(resolve_group "${TARGET}")

case "${ACTION}" in
  start)
    compose up -d "${SERVICES[@]}"
    ;;
  stop)
    compose stop "${SERVICES[@]}"
    ;;
  restart)
    compose restart "${SERVICES[@]}"
    ;;
  status|ps)
    compose ps
    ;;
  logs)
    SERVICE="${3:-brain-server}"
    compose logs -f --tail=200 "${SERVICE}"
    ;;
  build)
    compose build "${SERVICES[@]}"
    ;;
  down)
    compose down
    ;;
  *)
    cat <<'EOF'
Usage:
  ./scripts/release-control-linux.sh start [infra|minimal|extended]
  ./scripts/release-control-linux.sh stop [infra|minimal|extended]
  ./scripts/release-control-linux.sh restart [infra|minimal|extended]
  ./scripts/release-control-linux.sh build [infra|minimal|extended]
  ./scripts/release-control-linux.sh status
  ./scripts/release-control-linux.sh logs minimal <service-name>
  ./scripts/release-control-linux.sh down
EOF
    exit 1
    ;;
esac
