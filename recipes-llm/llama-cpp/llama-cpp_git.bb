SUMMARY = "Port of Facebook's LLaMA model in C/C++"
DESCRIPTION = "llama.cpp is a plain C/C++ implementation of Meta's LLaMA model with minimal dependencies for efficient local LLM inference"
HOMEPAGE = "https://github.com/ggerganov/llama.cpp"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=223b26b3c1143120c87e2b13111d3e99"

SRCREV = "4310aa4f871c104698f6a6614a362bdec87c247a"
SRC_URI = "git://github.com/ggml-org/llama.cpp.git;protocol=https;branch=master"

DEPENDS = "curl"

inherit cmake pkgconfig

S = "${UNPACKDIR}/git"

# Architecture-specific optimizations
EXTRA_OECMAKE = ""

# For ARM64 with NEON support
EXTRA_OECMAKE:append:aarch64 = " -DGGML_NATIVE=OFF -DGGML_BLAS=OFF"

# For x86_64 with AVX2 support
EXTRA_OECMAKE:append:x86-64 = " -DGGML_AVX2=ON -DGGML_FMA=ON"

# Disable CUDA/ROCm for embedded systems
EXTRA_OECMAKE += " -DGGML_CUDA=OFF -DGGML_HIP=OFF"

# Enable server build
EXTRA_OECMAKE += " -DBUILD_SHARED_LIBS=ON"

# Disable some optional features to reduce dependencies
EXTRA_OECMAKE += " -DGGML_OPENCL=OFF"

do_install:append() {
    # Install binaries
    install -d ${D}${bindir}
    install -m 0755 ${B}/bin/llama-cli ${D}${bindir}/llama-cli
    install -m 0755 ${B}/bin/llama-server ${D}${bindir}/llama-server
    install -m 0755 ${B}/bin/llama-quantize ${D}${bindir}/llama-quantize
    install -m 0755 ${B}/bin/llama-perplexity ${D}${bindir}/llama-perplexity

    # Install shared libraries
    install -d ${D}${libdir}
    install -m 0755 ${B}/bin/libllama.so* ${D}${libdir}/
    install -m 0755 ${B}/bin/libggml-cpu.so* ${D}${libdir}/
    install -m 0755 ${B}/bin/libggml.so* ${D}${libdir}/
    install -m 0755 ${B}/bin/libggml-base.so* ${D}${libdir}/
    install -m 0755 ${B}/bin/libmtmd.so* ${D}${libdir}/

    # Install headers for development
    install -d ${D}${includedir}/llama
    install -m 0644 ${S}/include/llama.h ${D}${includedir}/llama/
    install -m 0644 ${S}/ggml/include/ggml.h ${D}${includedir}/llama/
}

PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

FILES:${PN} = "${bindir}/* ${libdir}/*.so*"
FILES:${PN}-dev = "${includedir}/*"
FILES:${PN}-dbg = "${bindir}/.debug ${libdir}/.debug"

# Allow already-stripped binaries (llama.cpp pre-strips some)
INSANE_SKIP:${PN} += "already-stripped ldflags installed-vs-shipped"

RDEPENDS:${PN} = "libcurl"
