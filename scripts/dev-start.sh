#!/usr/bin/env bash
#
# Starts the full Greyhaven development stack: Compose PostgreSQL, the Spring Boot
# backend and the Vite dev server, with the frontend proxy pinned to the backend port
# this script actually started.
#
# Each run restarts backend and frontend: anything already listening on the configured
# ports is stopped first so a re-run always picks up the latest compiled code. Ctrl+C
# stops backend and frontend; PostgreSQL is left running so the database survives
# between sessions.
#
# Usage:
#   scripts/dev-start.sh
#   BACKEND_PORT=8081 FRONTEND_PORT=5174 scripts/dev-start.sh
#
set -euo pipefail

BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"
READY_TIMEOUT_SECONDS="${READY_TIMEOUT_SECONDS:-180}"
PORT_STOP_TIMEOUT_SECONDS="${PORT_STOP_TIMEOUT_SECONDS:-30}"

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
log_dir="$repo_root/logs"
backend_job=""
frontend_job=""

log() { printf '[dev-start] %s\n' "$*"; }
fail() { printf '[dev-start] ERROR: %s\n' "$*" >&2; exit 1; }

# Windows PID of whatever listens on a TCP port, empty when the port is free.
listening_pid() {
	netstat -ano 2>/dev/null \
		| awk -v port="$1" '/LISTENING/ && $2 ~ (":" port "$") { print $NF; exit }'
}

kill_tree() {
	local pid="${1:-}"
	[[ -n "$pid" ]] || return 0
	if command -v taskkill >/dev/null 2>&1; then
		# Double slashes keep Git Bash from rewriting the flags into paths.
		taskkill //F //T //PID "$pid" >/dev/null 2>&1 || true
	else
		kill "$pid" 2>/dev/null || true
	fi
}

# Stop whatever currently owns a port (previous stack, leftover java/node, etc.).
stop_listening_on() {
	local port="$1" label="$2"
	local pid
	pid="$(listening_pid "$port")"
	[[ -n "$pid" ]] || return 0
	log "Stopping existing $label on port $port (PID $pid)."
	kill_tree "$pid"
}

wait_until_port_free() {
	local port="$1" label="$2"
	local deadline=$((SECONDS + PORT_STOP_TIMEOUT_SECONDS))
	local pid
	while ((SECONDS < deadline)); do
		pid="$(listening_pid "$port")"
		[[ -z "$pid" ]] && return 0
		kill_tree "$pid"
		sleep 1
	done
	fail "$label port $port is still in use by PID $(listening_pid "$port") after stop attempt."
}

restart_app_ports() {
	log "Restarting app processes on ports $BACKEND_PORT and $FRONTEND_PORT."
	stop_listening_on "$BACKEND_PORT" "backend"
	stop_listening_on "$FRONTEND_PORT" "frontend"
	wait_until_port_free "$BACKEND_PORT" "Backend"
	wait_until_port_free "$FRONTEND_PORT" "Frontend"
}

wait_until_ready() {
	local label="$1" probe="$2" diagnostics="$3"
	local deadline=$((SECONDS + READY_TIMEOUT_SECONDS))
	while ((SECONDS < deadline)); do
		if "$probe"; then
			log "$label is ready."
			return 0
		fi
		sleep 2
	done
	fail "$label did not become ready within ${READY_TIMEOUT_SECONDS}s. Check: $diagnostics"
}

postgres_healthy() {
	[[ "$(docker inspect -f '{{.State.Health.Status}}' greyhaven-postgres 2>/dev/null)" == "healthy" ]]
}

backend_healthy() {
	curl -fsS "http://localhost:$BACKEND_PORT/actuator/health" 2>/dev/null | grep -q '"status":"UP"'
}

frontend_healthy() {
	curl -fsS -o /dev/null "http://127.0.0.1:$FRONTEND_PORT/" 2>/dev/null
}

# Maven Enforcer rejects anything but JDK 25, so fail here with a clearer message. The
# JDK is resolved through JAVA_HOME exactly like mvnw does it, rather than through PATH,
# because Git Bash ignores Windows-style (C:/...) PATH entries.
require_java_25() {
	local java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}java"
	local output first_line
	output="$("$java_bin" -version 2>&1 || true)"
	first_line="${output%%$'\n'*}"
	[[ "$first_line" == *version* ]] || fail "Could not run '$java_bin'. Point JAVA_HOME at a JDK 25 installation. Got: $first_line"
	[[ "$first_line" == *'"25'* ]] || fail "Greyhaven requires JDK 25, found: $first_line"
}

cleanup() {
	trap - EXIT INT TERM
	log "Stopping frontend and backend (PostgreSQL keeps running)."
	kill_tree "$(listening_pid "$FRONTEND_PORT")"
	kill_tree "$(listening_pid "$BACKEND_PORT")"
	[[ -z "$frontend_job" ]] || kill "$frontend_job" 2>/dev/null || true
	[[ -z "$backend_job" ]] || kill "$backend_job" 2>/dev/null || true
}

require_java_25
command -v docker >/dev/null 2>&1 || fail "docker not found. Start Docker Desktop first."
restart_app_ports
mkdir -p "$log_dir"
trap cleanup EXIT INT TERM

log "Starting PostgreSQL via Compose."
docker compose -f "$repo_root/docker-compose.yml" up -d
wait_until_ready "PostgreSQL" postgres_healthy "docker compose logs postgres"

# Truncate before launch so logs/backend.log only reflects this run.
: >"$log_dir/backend.log"
: >"$log_dir/frontend.log"

log "Starting backend on port $BACKEND_PORT (log: logs/backend.log)."
(
	cd "$repo_root/backend"
	./mvnw spring-boot:run \
		-Dspring-boot.run.profiles=local \
		-Dspring-boot.run.arguments="--server.port=$BACKEND_PORT"
) >"$log_dir/backend.log" 2>&1 &
backend_job=$!
wait_until_ready "Backend" backend_healthy "logs/backend.log"

if [[ ! -d "$repo_root/frontend/node_modules" ]]; then
	log "Installing frontend dependencies."
	(cd "$repo_root/frontend" && npm install) >"$log_dir/npm-install.log" 2>&1 \
		|| fail "npm install failed. See logs/npm-install.log."
fi

log "Starting frontend on port $FRONTEND_PORT (log: logs/frontend.log)."
# Bound to 127.0.0.1 rather than Vite's default: on Node 17+ "localhost" resolves to ::1
# first and would leave http://127.0.0.1:5173 refusing connections.
(
	cd "$repo_root/frontend"
	GREYHAVEN_API_PROXY="http://localhost:$BACKEND_PORT" \
		npm run dev -- --host 127.0.0.1 --port "$FRONTEND_PORT" --strictPort
) >"$log_dir/frontend.log" 2>&1 &
frontend_job=$!
wait_until_ready "Frontend" frontend_healthy "logs/frontend.log"

log "Greyhaven is up:"
log "  App:       http://localhost:$FRONTEND_PORT"
log "  API:       http://localhost:$BACKEND_PORT/api/v1/bootstrap"
log "  Health:    http://localhost:$BACKEND_PORT/actuator/health"
log "  Database:  localhost:5434/greyhaven"
log "Press Ctrl+C to stop."
wait
