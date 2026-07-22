SUMMARY = "Ollama-compatible API server using llama.cpp"
DESCRIPTION = "Provides an Ollama-compatible REST API server built on top of llama.cpp for running LLMs on embedded devices"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "llama-cpp"
RDEPENDS:${PN} = "llama-cpp bash ca-certificates curl"

inherit systemd useradd

SRC_URI = "file://llama-cpp-server.service \
           file://llama-cpp-wrapper.sh \
           file://config.json \
          "

SYSTEMD_SERVICE:${PN} = "llama-cpp-server.service"
SYSTEMD_AUTO_ENABLE = "enable"

# Create ollama user and group
USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r ollama"
USERADD_PARAM:${PN} = "-r -g ollama -d /var/lib/ollama -s /bin/bash -c 'Ollama Service User' ollama"

do_install() {
    # Install systemd service
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/llama-cpp-server.service ${D}${systemd_unitdir}/system/
    
    # Install wrapper script
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/llama-cpp-wrapper.sh ${D}${bindir}/llama-cpp-server
    
    # Install configuration
    install -d ${D}${sysconfdir}/llama-cpp
    install -m 0644 ${UNPACKDIR}/config.json ${D}${sysconfdir}/llama-cpp/
    
    # Create model storage directory
    install -d ${D}${localstatedir}/lib/ollama
    install -d ${D}${localstatedir}/lib/ollama/models
}

FILES:${PN} = "${bindir}/* \
               ${systemd_unitdir}/system/* \
               ${sysconfdir}/llama-cpp/* \
               ${localstatedir}/lib/ollama \
              "

pkg_postinst:${PN}() {
    if [ -z "$D" ]; then
        chown -R ollama:ollama ${localstatedir}/lib/ollama
    fi
}
