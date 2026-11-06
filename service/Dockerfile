From eclipse-temurin:21-jre

ARG ARTIFACT_FILE
COPY ./target/${ARTIFACT_FILE} /app/service.jar

RUN groupadd --system --gid 1001 faaast \
    && useradd --system --uid 1001 --gid 1001 --no-create-home faaast \
    # restrict permissions on working directory /app
    && chgrp -R 0 /app \
    && chmod -R g=u /app \
    # Create directories to which FA³ST needs permissions
    && mkdir /app/resources /app/logs /app/PKI /app/USERS_PKI \
    # Grant read and write permissions on created directories
    && chmod -R ugo+rw /app/resources /app/PKI /app/USERS_PKI /app/logs

USER faaast
WORKDIR /app
ENTRYPOINT ["java", "-jar", "service.jar"]