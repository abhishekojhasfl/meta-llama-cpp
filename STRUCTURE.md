# meta-llama-cpp Layer Structure

```
meta-llama-cpp/
│
├── README.md                           # Main documentation
├── INTEGRATION.md                      # Integration guide for yocto-playground
├── COMPARISON.md                       # Detailed comparison with meta-ollama
├── QUICKSTART.md                       # 5-minute quick start guide
├── COPYING.MIT                         # MIT License file
│
├── conf/
│   └── layer.conf                      # Layer configuration (BBPATH, priorities, etc.)
│
├── recipes-llm/                        # LLM-related recipes
│   ├── llama-cpp/
│   │   ├── llama-cpp_git.bb           # Main llama.cpp recipe (builds from source)
│   │   └── llama-cpp_git.bbappend.example  # Example customization file
│   │
│   └── llama-cpp-server/
│       ├── llama-cpp-server_1.0.bb   # Ollama-compatible server wrapper recipe
│       └── files/
│           ├── llama-cpp-server.service    # Systemd service unit
│           ├── llama-cpp-wrapper.sh        # Server wrapper script
│           └── config.json                   # Server configuration
│
├── recipes-image/                      # Image recipes
│   └── images/
│       └── playground-ai-image.bb      # Example AI-enabled image recipe
│
└── scripts/
    └── test-layer.sh                   # Layer validation test script
```

## File Descriptions

### Root Documentation

- **README.md**: Main layer documentation with overview, dependencies, and usage
- **INTEGRATION.md**: Step-by-step guide for integrating with yocto-playground
- **COMPARISON.md**: Technical comparison between meta-ollama and meta-llama-cpp
- **QUICKSTART.md**: Quick 5-minute setup guide
- **COPYING.MIT**: MIT license for the layer

### Configuration

- **conf/layer.conf**: 
  - Defines layer paths (BBPATH, BBFILES)
  - Sets layer priority and dependencies
  - Specifies compatible Yocto releases

### Recipes

#### llama-cpp Recipe (`recipes-llm/llama-cpp/`)

**llama-cpp_git.bb**:
- Fetches llama.cpp from GitHub
- Builds using CMake
- Installs binaries: llama-cli, llama-server, llama-quantize, llama-perplexity
- Installs shared libraries and headers
- Supports architecture-specific optimizations (ARM NEON, x86 AVX2)

**llama-cpp_git.bbappend.example**:
- Example customization file
- Shows how to enable BLAS, custom optimization flags
- Machine-specific configurations

#### llama-cpp-server Recipe (`recipes-llm/llama-cpp-server/`)

**llama-cpp-server_1.0.bb**:
- Creates Ollama-compatible API wrapper
- Installs systemd service
- Creates ollama user/group
- Sets up model storage directory

**files/llama-cpp-server.service**:
- Systemd unit file
- Configures service to run as 'ollama' user
- Sets environment variables (OLLAMA_HOST, OLLAMA_MODELS)
- Resource limits (memory, CPU)

**files/llama-cpp-wrapper.sh**:
- Bash wrapper script
- Provides Ollama-compatible API interface
- Starts llama-server with appropriate parameters
- Handles model discovery and configuration

**files/config.json**:
- Server configuration file
- Sets default parameters (threads, context size, temperature)
- Logging configuration

#### Image Recipe (`recipes-image/images/`)

**playground-ai-image.bb**:
- Complete AI-enabled Yocto image
- Includes llama-cpp and llama-cpp-server
- Development tools (git, curl, python3)
- Large rootfs for model storage
- Creates helpful README on device

### Scripts

**scripts/test-layer.sh**:
- Validates layer configuration
- Checks if layer is properly added
- Tests recipe parsing
- Verifies dependencies
- Performs dry-run build test

## Key Design Decisions

### 1. Architecture Choice
- **C++ over Go**: Chosen llama.cpp (C++) instead of Ollama (Go) for:
  - Smaller footprint (~15MB vs ~150MB)
  - Lower memory usage
  - Better performance on embedded CPUs
  - No runtime dependency on Go

### 2. API Compatibility
- Provides Ollama-compatible REST API endpoints
- Allows existing Ollama clients to work with minimal changes
- Trade-off: Some advanced features not available (model registry, etc.)

### 3. Systemd Integration
- Uses systemd for service management
- Follows embedded Linux best practices
- Proper user/group separation for security

### 4. Model Management
- Manual model placement (vs automatic Ollama pull)
- Simpler, more predictable for embedded
- Reduces network dependencies

### 5. Build System
- Full CMake build for llama.cpp (no binary package)
- Allows architecture-specific optimizations
- Longer build time, but better performance

## Usage in yocto-playground

### Integration Steps

1. **Add as submodule**:
   ```bash
   git submodule add <repo-url> sources/meta-llama-cpp
   ```

2. **Update layers.conf**:
   ```
   sources/meta-llama-cpp
   ```

3. **Configure build**:
   ```bash
   # In build.conf or local.conf
   IMAGE_INSTALL:append = " llama-cpp llama-cpp-server"
   IMAGE_ROOTFS_EXTRA_SPACE = "10485760"
   ```

4. **Build**:
   ```bash
   bitbake playground-ai-image
   ```

### Testing

```bash
# Validate layer
./sources/meta-llama-cpp/scripts/test-layer.sh

# Build individual packages
bitbake llama-cpp
bitbake llama-cpp-server

# Build complete image
bitbake playground-ai-image
```

## Customization Points

### For Different Architectures

Edit `llama-cpp_git.bb` or create `.bbappend`:
```bitbake
# Raspberry Pi 4
EXTRA_OECMAKE:append:raspberrypi4-64 = " -DGGML_NATIVE=ON"

# x86-64 with AVX512
EXTRA_OECMAKE:append:x86-64 = " -DGGML_AVX512=ON"
```

### For Different Memory Constraints

Edit `llama-cpp-server.service`:
```ini
# For 2GB devices
MemoryLimit=2G

# For 8GB devices
MemoryLimit=6G
```

Edit `llama-cpp-wrapper.sh`:
```bash
# Smaller context for low memory
--ctx-size 1024

# Larger context for more memory
--ctx-size 4096
```

### For Additional Tools

Add to `llama-cpp_git.bb`:
```bitbake
do_install:append() {
    install -m 0755 ${B}/bin/embedding ${D}${bindir}/llama-embedding
}
```

## Maintenance

### Updating llama.cpp Version

Edit `llama-cpp_git.bb`:
```bitbake
# Update SRCREV to latest commit
SRCREV = "new-commit-hash"
```

### Adding New Features

1. Modify recipe files
2. Test build: `bitbake -c cleansstate llama-cpp && bitbake llama-cpp`
3. Test runtime on target device
4. Update documentation

### Compatibility Testing

Run test script after changes:
```bash
./scripts/test-layer.sh
```

## Dependencies Graph

```
playground-ai-image
    ├── llama-cpp
    │   ├── curl
    │   ├── cmake-native
    │   └── pkgconfig-native
    │
    └── llama-cpp-server
        ├── llama-cpp (RDEPENDS)
        ├── bash
        ├── ca-certificates
        └── jq
```

## License Information

- **Layer**: MIT License (see COPYING.MIT)
- **llama.cpp**: MIT License
- **Individual recipes**: Check LIC_FILES_CHKSUM in each recipe

## Contributing

When contributing to this layer:

1. Follow Yocto layer best practices
2. Test on multiple architectures (x86-64, ARM64)
3. Update documentation
4. Run validation script
5. Submit pull request with clear description

## Support

For issues or questions:
- Check documentation: README.md, INTEGRATION.md, COMPARISON.md
- Run test script: `scripts/test-layer.sh`
- Review Yocto logs: `bitbake llama-cpp -c compile -v`
- Check runtime logs: `journalctl -u llama-cpp-server`
