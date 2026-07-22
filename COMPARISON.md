# meta-ollama vs meta-llama-cpp: Detailed Comparison

## Overview

This document provides a comprehensive comparison between the hypothetical `meta-ollama` layer (based on the official Ollama Go binary) and the `meta-llama-cpp` layer (based on llama.cpp).

## Architecture Comparison

### meta-ollama (Go-based)

```
┌─────────────────────────────────────┐
│     Ollama Application (Go)         │
│  ┌──────────────────────────────┐   │
│  │   HTTP API Server (Go)       │   │
│  │   - REST endpoints           │   │
│  │   - Model management         │   │
│  │   - Request handling         │   │
│  └──────────────────────────────┘   │
│  ┌──────────────────────────────┐   │
│  │   LLM Backend                │   │
│  │   - llama.cpp (embedded)     │   │
│  │   - GPU support (CUDA/ROCm)  │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
         │
         ▼
   Go Runtime (~20-50MB)
```

**Key Points:**
- Full Ollama application packaged as binary
- Go runtime required
- Includes all Ollama features (model pull, push, management)
- Larger footprint but more features
- GPU acceleration available

### meta-llama-cpp (C++-based)

```
┌─────────────────────────────────────┐
│   Ollama-Compatible Wrapper (Bash)  │
│  ┌──────────────────────────────┐   │
│  │   llama-server (C++)         │   │
│  │   - HTTP API Server          │   │
│  │   - Compatible endpoints     │   │
│  │   - Direct inference         │   │
│  └──────────────────────────────┘   │
│  ┌──────────────────────────────┐   │
│  │   llama.cpp Core             │   │
│  │   - CPU optimized            │   │
│  │   - Minimal dependencies     │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
         │
         ▼
   libc + libstdc++ (minimal)
```

**Key Points:**
- Direct llama.cpp with API wrapper
- No Go runtime needed
- Smaller footprint, embedded-optimized
- CPU-focused (better for embedded)
- Ollama API compatibility layer

## Feature Comparison

| Feature | meta-ollama | meta-llama-cpp |
|---------|-------------|-----------------|
| **Binary Size** | ~100-150MB | ~5-15MB |
| **Runtime Deps** | Go runtime, glibc | glibc, libstdc++ |
| **Memory Usage** | Higher (Go GC) | Lower (C++ manual) |
| **CPU Performance** | Good | Excellent |
| **GPU Support** | Full (CUDA, ROCm) | Limited/None |
| **Model Management** | Built-in (pull/push) | Manual |
| **API Compatibility** | 100% Ollama | ~80% Ollama |
| **Startup Time** | Slower (Go init) | Faster |
| **Build Time** | Faster (pre-built) | Slower (compilation) |

## Use Case Comparison

### When to Use meta-ollama

✅ **Best For:**
- Desktop/server environments
- Systems with >8GB RAM
- Need GPU acceleration (CUDA/ROCm)
- Want full Ollama feature set
- Need model registry integration
- Team collaboration features

❌ **Not Ideal For:**
- Embedded systems <2GB RAM
- Size-constrained environments
- Pure CPU inference
- Long-term battery devices

### When to Use meta-llama-cpp

✅ **Best For:**
- Embedded Linux devices
- ARM-based systems (Raspberry Pi, etc.)
- Memory-constrained environments (<4GB)
- CPU-only inference
- Long-running edge devices
- Size-critical deployments
- IoT applications

❌ **Not Ideal For:**
- Need GPU acceleration
- Want automatic model management
- Require full Ollama ecosystem
- Need multi-user collaboration

## Technical Deep Dive

### Dependency Tree

**meta-ollama:**
```
ollama
├── go (runtime)
├── glibc
├── libstdc++
├── Optional: CUDA libraries
├── Optional: ROCm libraries
└── systemd (for service)
```

**meta-llama-cpp:**
```
llama-cpp
├── glibc
├── libstdc++
├── libcurl (for server)
└── systemd (for service)

llama-cpp-server
├── llama-cpp
├── bash
└── systemd
```

### Yocto Recipe Complexity

**meta-ollama Recipe Approach:**
```bitbake
# Simpler - binary package
inherit bin_package systemd

SRC_URI = "https://ollama.com/download/ollama-linux-${ARCH}.tgz"

do_install() {
    # Just copy binary
    install -m 0755 ollama ${D}${bindir}/
}
```

**meta-llama-cpp Recipe Approach:**
```bitbake
# More complex - full build
inherit cmake systemd

SRC_URI = "git://github.com/ggerganov/llama.cpp.git"

do_compile() {
    # Full CMake build with optimizations
    cmake --build ${B}
}

do_install() {
    # Install binaries, libs, headers
    # Custom server wrapper
}
```

### Performance Metrics (Estimated)

**Inference Speed (tokens/sec on CPU):**
- meta-ollama: 10-15 tokens/sec (7B model on 4-core ARM)
- meta-llama-cpp: 15-25 tokens/sec (same hardware)

**Memory Footprint (7B Q4 model):**
- meta-ollama: ~4.5GB (model + runtime)
- meta-llama-cpp: ~4.0GB (model + minimal runtime)

**Storage Requirements:**
- meta-ollama: ~150MB (binary + deps)
- meta-llama-cpp: ~15MB (binary + deps)

## API Compatibility

`llama-server` does **not** implement Ollama's native `/api/*` REST API. It exposes an
OpenAI-compatible API instead. Clients written against Ollama's native API must be
switched to the OpenAI-style endpoints below (see `llama-server-api-guide.md` for
request/response examples).

### Supported Endpoints

| Ollama endpoint | meta-ollama | meta-llama-cpp equivalent |
|-----------------|-------------|----------------------------|
| `/api/generate` | ✅ Full | ❌ Not implemented — use `/v1/completions` |
| `/api/chat` | ✅ Full | ❌ Not implemented — use `/v1/chat/completions` |
| `/api/pull` | ✅ Full | ❌ Not implemented — manual GGUF download |
| `/api/push` | ✅ Full | ❌ N/A |
| `/api/create` | ✅ Full | ❌ Not implemented — manual GGUF placement |
| `/api/tags` | ✅ Full | ❌ Not implemented — use `/v1/models` |
| `/api/show` | ✅ Full | ❌ Not implemented |
| `/api/copy` | ✅ Full | ❌ N/A |
| `/api/delete` | ✅ Full | ⚠️  File delete |
| `/api/embeddings` | ✅ Full | ✅ Available via `/v1/embeddings` (OpenAI format) |

### API Limitations in meta-llama-cpp

1. **API Shape:**
   - No native Ollama `/api/*` endpoints at all — only the OpenAI-compatible
     `/v1/completions`, `/v1/chat/completions`, `/v1/models`, `/v1/embeddings` endpoints
   - Existing Ollama clients need code changes, not just a different host/port

2. **Model Management:**
   - No automatic model pulling from registry
   - Manual GGUF file placement required
   - No model versioning

3. **Advanced Features:**
   - No model creation from Modelfile
   - No model push to registry
   - Limited metadata support

4. **Workarounds:**
   - Use `wget`/`curl` for model downloads
   - Script-based model management
   - File-based model organization

## Resource Requirements

### Minimum System Requirements

**meta-ollama:**
```
CPU:  2+ cores
RAM:  4GB minimum, 8GB recommended
Storage: 20GB (5GB app + 15GB models)
Network: Required for model pulling
```

**meta-llama-cpp:**
```
CPU:  1+ cores
RAM:  2GB minimum, 4GB recommended
Storage: 10GB (500MB app + 9.5GB models)
Network: Optional (manual downloads)
```

### Recommended Hardware

**For meta-ollama:**
- Desktop/Server: x86_64 with GPU
- RAM: 16GB+
- GPU: NVIDIA (CUDA) or AMD (ROCm)

**For meta-llama-cpp:**
- Embedded: ARM64 (Raspberry Pi 4+)
- RAM: 4-8GB
- CPU: Multi-core ARM Cortex-A72+

## Build and Deployment

### Build Time Comparison

**meta-ollama:**
```bash
bitbake ollama-bin
# ~5-10 minutes (mostly download time)
# Primarily network-bound
```

**meta-llama-cpp:**
```bash
bitbake llama-cpp
# ~20-45 minutes (compilation)
# CPU-bound, depends on host
```

### Image Size Impact

**Adding meta-ollama to image:**
```
Base image:     500MB
+ meta-ollama:  650MB (+150MB)
+ Models:       4GB per 7B model
Total:          ~4.7GB minimum
```

**Adding meta-llama-cpp to image:**
```
Base image:        500MB
+ meta-llama-cpp: 515MB (+15MB)
+ Models:          4GB per 7B model
Total:             ~4.5GB minimum
```

## Integration Example

### For yocto-playground

**Using meta-ollama:**
```conf
# layers.conf
sources/meta-ollama

# build.conf or local.conf
MACHINE ?= "genericx86-64"
IMAGE_INSTALL:append = " ollama-bin"
IMAGE_ROOTFS_EXTRA_SPACE = "15728640"  # 15GB
```

**Using meta-llama-cpp:**
```conf
# layers.conf
sources/meta-llama-cpp

# build.conf or local.conf
MACHINE ?= "qemuarm64"
IMAGE_INSTALL:append = " llama-cpp llama-cpp-server"
IMAGE_ROOTFS_EXTRA_SPACE = "10485760"  # 10GB
```

## Migration Path

### From meta-ollama to meta-llama-cpp

1. **Prepare Models:**
   ```bash
   # Convert if needed (not usually required for GGUF)
   # Models are compatible
   ```

2. **Update Configuration:**
   ```bash
   # Change layer in bblayers.conf
   # Update IMAGE_INSTALL
   ```

3. **Rebuild:**
   ```bash
   bitbake -c cleansstate ollama-bin
   bitbake llama-cpp llama-cpp-server
   ```

4. **Deploy:**
   ```bash
   # Copy models to /var/lib/ollama/models
   systemctl start llama-cpp-server
   ```

## Conclusion

### Choose **meta-ollama** if you need:
- Full Ollama ecosystem
- GPU acceleration
- Automatic model management
- Team collaboration features
- Desktop/server deployment

### Choose **meta-llama-cpp** if you need:
- Embedded/IoT deployment
- Minimal resource footprint
- CPU-optimized inference
- Long-term edge operation
- Maximum efficiency

Both layers can coexist in a Yocto build system, allowing you to choose the right tool for each specific use case or device type.
