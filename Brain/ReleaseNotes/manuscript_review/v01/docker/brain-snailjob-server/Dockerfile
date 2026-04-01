FROM bellsoft/liberica-openjdk-rocky:17.0.16-cds

LABEL maintainer="Codex"

RUN mkdir -p /brain/snailjob/logs

WORKDIR /brain/snailjob

ENV LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""

COPY artifacts/backend/brain-snailjob-server.jar ./app.jar

ENTRYPOINT ["sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -XX:+HeapDumpOnOutOfMemoryError -XX:+UseZGC ${JAVA_OPTS} -jar app.jar"]
