#!/bin/bash
# Ollama-compatible server wrapper for llama.cpp

set -e

CONFIG_FILE="${OLLAMA_CONFIG:-/etc/llama-cpp/config.json}"

# Read a value out of config.json, if the file exists and jq is available.
# Falls back to an empty string so callers can apply their own default.
config_get() {
    local jq_filter="$1"
    if [ -f "$CONFIG_FILE" ] && command -v jq >/dev/null 2>&1; then
        jq -r "${jq_filter} // empty" "$CONFIG_FILE" 2>/dev/null
    fi
}

# Configuration (env vars take precedence over config.json, which takes
# precedence over the hardcoded defaults)
CONFIG_HOST="$(config_get '.server.host')"
CONFIG_PORT="$(config_get '.server.port')"
if [ -n "$CONFIG_HOST" ] || [ -n "$CONFIG_PORT" ]; then
    CONFIG_HOST_PORT="${CONFIG_HOST:-0.0.0.0}:${CONFIG_PORT:-11434}"
else
    CONFIG_HOST_PORT=""
fi
HOST="${OLLAMA_HOST:-${CONFIG_HOST_PORT:-0.0.0.0:11434}}"

MODELS_DIR="${OLLAMA_MODELS:-$(config_get '.models.path')}"
MODELS_DIR="${MODELS_DIR:-/var/lib/ollama/models}"

CTX_SIZE="$(config_get '.models.default_context_size')"
CTX_SIZE="${CTX_SIZE:-2048}"

THREADS="$(config_get '.models.default_threads')"
THREADS="${THREADS:-4}"

GPU_LAYERS="$(config_get '.inference.gpu_layers')"
GPU_LAYERS="${GPU_LAYERS:-0}"

# Extract host and port
IFS=':' read -r BIND_IP BIND_PORT <<< "$HOST"

# Ensure models directory exists
mkdir -p "$MODELS_DIR"

# Log startup
echo "Starting Ollama-compatible server using llama.cpp"
echo "Listening on: $HOST"
echo "Models directory: $MODELS_DIR"
echo "Context size: $CTX_SIZE, threads: $THREADS, gpu layers: $GPU_LAYERS"

# Find available models
DEFAULT_MODEL=""
if [ -d "$MODELS_DIR" ]; then
    for model in "$MODELS_DIR"/*.gguf; do
        if [ -f "$model" ]; then
            DEFAULT_MODEL="$model"
            echo "Found model: $(basename $model)"
            break
        fi
    done
fi

# Start llama-server with Ollama-compatible settings
exec /usr/bin/llama-server \
    --host "$BIND_IP" \
    --port "$BIND_PORT" \
    ${DEFAULT_MODEL:+--model "$DEFAULT_MODEL"} \
    --ctx-size "$CTX_SIZE" \
    --n-gpu-layers "$GPU_LAYERS" \
    --threads "$THREADS" \
    --verbose
