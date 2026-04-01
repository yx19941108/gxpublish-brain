FROM bellsoft/liberica-openjdk-rocky:17.0.16-cds

LABEL maintainer="Codex"

RUN mkdir -p /brain/server/logs /brain/server/temp

WORKDIR /brain/server

ENV LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""

COPY artifacts/backend/brain-admin.jar ./app.jar

ENTRYPOINT ["sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${SERVER_PORT:-9888} -XX:+HeapDumpOnOutOfMemoryError -XX:+UseZGC ${JAVA_OPTS} -jar app.jar"]
