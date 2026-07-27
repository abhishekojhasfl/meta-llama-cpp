SUMMARY = "Port of Facebook's LLaMA model in C/C++"
DESCRIPTION = "llama.cpp is a plain C/C++ implementation of Meta's LLaMA model with minimal dependencies for efficient local LLM inference"
HOMEPAGE = "https://github.com/ggml-org/llama.cpp"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=223b26b3c1143120c87e2b13111d3e99"

SRC_URI = "git://github.com/ggml-org/llama.cpp.git;protocol=https;branch=master \
    file://0001-fix-impl-lib-versioning.patch \
"
SRCREV = "0324696b8e5fe340dc94b64714e4c9aab03084a2"

S = "${WORKDIR}/git"

DEPENDS = "curl"

inherit cmake pkgconfig

# CMake options below are defined upstream in llama.cpp (see /home/aojha/Practice/llama.cpp):
#   - GGML_* options: ggml/CMakeLists.txt
#   - LLAMA_* options: CMakeLists.txt
#
#   BUILD_SHARED_LIBS     - "standard CMake option; build shared libs (.so)"
#   GGML_NATIVE           - "ggml: optimize the build for the current system" (-march=native).
#                           Disabled for cross-compilation, since the build host CPU may differ
#                           from the target device's CPU.
#   GGML_BLAS             - "ggml: use BLAS". Disabled to avoid an extra BLAS dependency/backend.
#   GGML_CUDA             - "ggml: use CUDA". Disabled: no NVIDIA CUDA toolchain on target.
#   GGML_HIP              - "ggml: use HIP". Disabled: no AMD ROCm/HIP toolchain on target.
#   GGML_VULKAN           - "ggml: use Vulkan". Disabled: no Vulkan backend on target.
#   GGML_OPENCL           - "ggml: use OpenCL". Disabled: no OpenCL backend on target.
#   GGML_METAL            - "ggml: use Metal". Disabled: Metal is Apple-only, irrelevant on Linux/Yocto.
#   GGML_BACKEND_DL       - "ggml: build backends as dynamic libraries (requires BUILD_SHARED_LIBS)".
#                           Disabled: backends are linked in directly rather than loaded at runtime.
#   CMAKE_BUILD_TYPE      - standard CMake option; Release enables optimizations for production builds.
#   LLAMA_BUILD_COMMON    - "llama: build common utils library", required by tools/examples/server below.
#   LLAMA_BUILD_APP       - "llama: build the unified binary".
#   LLAMA_BUILD_SERVER    - "llama: build server example" (the llama.cpp HTTP server binary).
#   LLAMA_BUILD_TOOLS     - "llama: build tools" (e.g. quantize, gguf utilities).
#   LLAMA_BUILD_TESTS     - "llama: build tests".
#   LLAMA_BUILD_EXAMPLES  - "llama: build examples".
#   LLAMA_BUILD_UI        - "llama: build the embedded Web UI for server". Disabled to avoid
#                           bundling/prebuilt-UI download dependencies in the recipe build.
EXTRA_OECMAKE = "\
    -DBUILD_SHARED_LIBS=ON \
    \
    -DGGML_NATIVE=OFF \
    -DGGML_BLAS=OFF \
    -DGGML_CUDA=OFF \
    -DGGML_HIP=OFF \
    -DGGML_VULKAN=OFF \
    -DGGML_OPENCL=OFF \
    -DGGML_METAL=OFF \
    -DGGML_BACKEND_DL=OFF \
    -DCMAKE_BUILD_TYPE=Release \
    -DLLAMA_BUILD_COMMON=ON \
    -DLLAMA_BUILD_APP=ON \
    -DLLAMA_BUILD_SERVER=ON \
    -DLLAMA_BUILD_TOOLS=ON \
    -DLLAMA_BUILD_TESTS=OFF \
    -DLLAMA_BUILD_EXAMPLES=ON \
    -DLLAMA_BUILD_UI=OFF \
"

do_install() {
    DESTDIR=${D} cmake --install ${B}
}

FILES:${PN} += " \
    ${bindir}/* \
    ${libdir}/*.so.* \
"

FILES:${PN}-dev += " \
    ${includedir} \
    ${libdir}/*.so \
    ${libdir}/pkgconfig \
    ${libdir}/cmake \
"

RDEPENDS:${PN} += "libcurl"