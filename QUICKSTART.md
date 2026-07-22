# Quick Start Guide: meta-llama-cpp

## 5-Minute Setup

### Prerequisites

```bash
# Ensure you have Yocto environment
cd yocto-playground
source sources/poky/oe-init-build-env
```

### 1. Add the Layer

```bash
# Add meta-llama-cpp as a submodule (or clone it)
cd /path/to/yocto-playground
git submodule add https://github.com/Abhishekojha38/meta-llama-cpp sources/meta-llama-cpp

# Add to layers.conf
echo "sources/meta-llama-cpp" >> layers.conf
```

### 2. Configure Build

Edit `build.conf` or `conf/local.conf`:

```bash
# Add packages to image
IMAGE_INSTALL:append = " llama-cpp llama-cpp-server"

# Allocate space for models (10GB)
IMAGE_ROOTFS_EXTRA_SPACE = "10485760"
```

### 3. Build

```bash
# Using CQFD (from yocto-playground)
cqfd init
cqfd run
```

### 4. Deploy & Test

Refer to [llama-server-api-guide.md](llama-server-api-guide.md) for detailed instructions on how to deploy and test the server.

## Common Commands

```bash
# Check build dependencies
bitbake-layers show-recipes llama-cpp

# Clean rebuild
bitbake -c cleansstate llama-cpp
bitbake llama-cpp

# View logs during runtime
journalctl -u llama-cpp-server -f
```

## Troubleshooting

**Build fails with CMake errors:**
```bash
# Check dependencies
bitbake -c cleanall llama-cpp
bitbake llama-cpp -c fetch -c unpack -c configure
```

**Server won't start:**
```bash
# Check service status
systemctl status llama-cpp-server

# Check model permissions
ls -la /var/lib/ollama/models
chown -R ollama:ollama /var/lib/ollama
```

**Out of memory:**
```bash
# Reduce context size in wrapper
--ctx-size 1024  # instead of 2048

# Limit systemd service memory
# Edit llama-cpp-server.service
MemoryLimit=2G
```

## Next Steps

- Read [INTEGRATION.md](INTEGRATION.md) for detailed integration
- See [COMPARISON.md](COMPARISON.md) for meta-ollama vs meta-llama-cpp
- Check [README.md](README.md) for full documentation
- Browse recipes in `recipes-llm/` for customization

## Quick Reference

| Component | Location | Purpose |
|-----------|----------|---------|
| llama.cpp binary | `/usr/bin/llama-cli` | CLI inference |
| Server binary | `/usr/bin/llama-server` | HTTP server |
| Wrapper script | `/usr/bin/llama-cpp-server` | Ollama-compatible |
| Models dir | `/var/lib/ollama/models/` | Model storage |
| Config | `/etc/llama-cpp/config.json` | Server config |
| Service | `llama-cpp-server.service` | Systemd unit |

Find models at: https://huggingface.co/models?library=gguf
