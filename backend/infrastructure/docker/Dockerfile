FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy pre-built JAR
ARG SERVICE_NAME
COPY services/${SERVICE_NAME}/build/libs/*.jar app.jar

RUN mkdir -p /app/config && chown -R appuser:appgroup /app

USER appuser

# App port + remote debug port
EXPOSE 8080 5005

ENTRYPOINT ["sh", "-c", "exec java \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=${DEBUG_SUSPEND:-n},address=*:5005 \
  ${JAVA_OPTS:-} \
  -jar app.jar \
  --spring.config.additional-location=optional:file:/app/config/"]
