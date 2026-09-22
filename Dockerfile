FROM docker.io/eclipse-temurin:17-jre-focal

RUN useradd -ms /bin/bash webgoat
RUN chgrp -R 0 /home/webgoat
RUN chmod -R g=u /home/webgoat

USER webgoat

COPY --chown=webgoat target/webgoat-*.jar /home/webgoat/webgoat.jar

EXPOSE 8080
EXPOSE 9090

WORKDIR /home/webgoat
ENTRYPOINT [ "java", \
   "-Duser.home=/home/webgoat", \
   "-Dfile.encoding=UTF-8", \
   "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
   "--add-opens", "java.base/java.util=ALL-UNNAMED", \
   "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED", \
   "--add-opens", "java.base/java.text=ALL-UNNAMED", \
   "--add-opens", "java.desktop/java.beans=ALL-UNNAMED", \
   "--add-opens", "java.desktop/java.awt.font=ALL-UNNAMED", \
   "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", \
   "--add-opens", "java.base/java.io=ALL-UNNAMED", \
   "--add-opens", "java.base/java.util=ALL-UNNAMED", \
   "-Drunning.in.docker=true", \
   "-Dwebgoat.host=0.0.0.0", \
   "-Dwebwolf.host=0.0.0.0", \
   "-Dwebgoat.port=8080", \
   "-Dwebwolf.port=9090", \
   "-jar", "webgoat.jar" ]

# ================ ARNICA SECURITY ANNOTATION BLOCK START ================
LABEL org.opencontainers.image.source="https://github.com/nir-gitgoat-dev/WebGoat"
LABEL org.opencontainers.image.path="Dockerfile"
# These automated labels, added by the security team, enhance traceability and security.
# For more details, see: https://docs.arnica.io/arnica-documentation/developers/adding-oci-tags-to-docker-images.
# To exclude this file, please replace this change with: #test-mock-unreal-token-\d+ followed by the dismissal reason.
# ================ ARNICA SECURITY ANNOTATION BLOCK END ================
