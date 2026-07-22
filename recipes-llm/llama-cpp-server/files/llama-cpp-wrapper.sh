#!/bin/bash
# Ollama-compatible server wrapper for llama.cpp

set -e

# Configuration
MODELS_DIR="${OLLAMA_MODELS:-/var/lib/ollama/models}"
HOST="${OLLAMA_HOST:-0.0.0.0:11434}"
CONFIG_FILE="${OLLAMA_CONFIG:-/etc/llama-cpp/config.json}"

# Extract host and port
IFS=':' read -r BIND_IP BIND_PORT <<< "$HOST"

# Ensure models directory exists
mkdir -p "$MODELS_DIR"

# Log startup
echo "Starting Ollama-compatible server using llama.cpp"
echo "Listening on: $HOST"
echo "Models directory: $MODELS_DIR"

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
    --ctx-size 2048 \
    --n-gpu-layers 0 \
    --threads 4 \
    --verbose
