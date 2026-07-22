SUMMARY = "AI-enabled playground image with Ollama LLM support"
DESCRIPTION = "Custom Yocto image for embedded AI experimentation with llama.cpp and Ollama-compatible server"

LICENSE = "MIT"

inherit core-image

# Base image features
IMAGE_FEATURES += "ssh-server-dropbear \
                   package-management \
                   debug-tweaks \
                   tools-debug \
                   tools-profile \
                  "

# Core packages
IMAGE_INSTALL:append = " \
    packagegroup-core-boot \
    packagegroup-core-full-cmdline \
    kernel-modules \
    "

# Development tools
IMAGE_INSTALL:append = " \
    git \
    curl \
    wget \
    vim \
    htop \
    screen \
    python3 \
    python3-pip \
    "

# LLM/AI packages
IMAGE_INSTALL:append = " \
    llama-cpp \
    llama-cpp-server \
    "

# Networking
IMAGE_INSTALL:append = " \
    iptables \
    nfs-utils \
    openssh-sftp-server \
    "

# Extra filesystem space for models (10GB)
IMAGE_ROOTFS_EXTRA_SPACE = "10485760"

# Enable systemd as init manager
DISTRO_FEATURES:append = " systemd"
VIRTUAL-RUNTIME_init_manager = "systemd"
VIRTUAL-RUNTIME_initscripts = "systemd-compat-units"

# Set root password to 'root' for development
EXTRA_IMAGE_FEATURES += "empty-root-password"

# Create some helpful README in the image
create_ollama_readme() {
    cat > ${IMAGE_ROOTFS}/home/root/OLLAMA_README.txt << 'EOF'
=============================================================================
Ollama-compatible LLM Server on Yocto
=============================================================================

This image includes llama.cpp with an Ollama-compatible API server.

QUICK START:
-----------

1. Place your GGUF model files in: /var/lib/ollama/models/
   Example: /var/lib/ollama/models/llama-2-7b-chat.Q4_K_M.gguf

2. Start the server:
   systemctl start llama-cpp-server
   
3. Check status:
   systemctl status llama-cpp-server
   
4. Test the API (llama-server does not implement Ollama's native /api/*):
   curl http://localhost:11434/v1/models

5. Generate text:
   curl -X POST http://localhost:11434/v1/completions -d '{
     "prompt": "Why is the sky blue?"
   }'

COMMANDS:
--------

- llama-cli       : Command-line interface for llama.cpp
- llama-server    : Native llama.cpp server
- llama-quantize  : Quantize models to smaller sizes
- llama-perplexity: Calculate model perplexity

CONFIGURATION:
-------------

Service config: /etc/llama-cpp/config.json
Service unit:   /etc/systemd/system/llama-cpp-server.service

Environment variables:
- OLLAMA_HOST: Server bind address (default: 0.0.0.0:11434)
- OLLAMA_MODELS: Models directory (default: /var/lib/ollama/models)

DOWNLOADING MODELS:
------------------

Models can be downloaded from:
- https://huggingface.co/models?library=gguf
- https://huggingface.co/TheBloke

Example:
  wget https://huggingface.co/TheBloke/Llama-2-7B-Chat-GGUF/resolve/main/llama-2-7b-chat.Q4_K_M.gguf
  mv llama-2-7b-chat.Q4_K_M.gguf /var/lib/ollama/models/

LOGS:
----

View server logs:
  journalctl -u llama-cpp-server -f

=============================================================================
EOF
    chown root:root ${IMAGE_ROOTFS}/home/root/OLLAMA_README.txt
}

ROOTFS_POSTPROCESS_COMMAND += "create_ollama_readme; "
