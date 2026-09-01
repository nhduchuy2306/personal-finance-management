FROM eclipse-temurin:21-jre-alpine
ENV SPRING_PROFILES_ACTIVE=stag
COPY build/libs/*.jar app.jar
EXPOSE 8080 5005
ENTRYPOINT ["sh", "-c", "exec java \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=${DEBUG_SUSPEND:-n},address=*:5005 \
  ${JAVA_OPTS:-} \
  -jar app.jar \
  --spring.config.additional-location=optional:file:/app/config/"]
