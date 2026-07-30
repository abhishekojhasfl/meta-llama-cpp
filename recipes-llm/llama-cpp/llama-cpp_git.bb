SUMMARY = "llama.cpp"
DESCRIPTION = "llama.cpp is a plain C/C++ implementation of Meta's LLaMA model with minimal dependencies for efficient local LLM inference"
HOMEPAGE = "https://github.com/ggml-org/llama.cpp"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=223b26b3c1143120c87e2b13111d3e99"

SRC_URI = "git://github.com/ggml-org/llama.cpp.git;protocol=https;branch=master \
    file://0001-fix-impl-lib-versioning.patch \
    file://llama-cpp-server.service \
"
SRCREV = "0324696b8e5fe340dc94b64714e4c9aab03084a2"

S = "${WORKDIR}/git"

DEPENDS = "curl"

inherit cmake pkgconfig systemd

# llama-cpp-server: systemd service that runs llama-server as an API daemon,
# split out of ${PN} into its own package.
PACKAGES =+ "${PN}-server"

SYSTEMD_PACKAGES = "${PN}-server"
SYSTEMD_SERVICE:${PN}-server = "llama-cpp-server.service"
SYSTEMD_AUTO_ENABLE:${PN}-server = "enable"

EXTRA_OECMAKE = "\
    -DGGML_NATIVE=OFF \
    -DCMAKE_BUILD_TYPE=Release \
    -DLLAMA_BUILD_APP=ON \
    -DLLAMA_BUILD_SERVER=ON \
    -DLLAMA_BUILD_TOOLS=ON \
    -DLLAMA_BUILD_TESTS=OFF \
    -DLLAMA_BUILD_EXAMPLES=ON \
    -DLLAMA_BUILD_UI=OFF \
    -DFETCHCONTENT_FULLY_DISCONNECTED=OFF \
"

do_install:append() {
    # Create a shared directory to hold models on the target
    install -d ${D}${datadir}/edgeai/models

    # Install the llama-cpp-server systemd service
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/llama-cpp-server.service ${D}${systemd_unitdir}/system/
}

FILES:${PN} += " \
    ${bindir}/* \
    ${datadir}/edgeai/models \
"

FILES:${PN}-dev += " \
    ${includedir} \
    ${libdir}/pkgconfig \
    ${libdir}/cmake \
"

FILES:${PN}-server = " \
    ${systemd_unitdir}/system/llama-cpp-server.service \
"

RDEPENDS:${PN}-server += "${PN}"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
