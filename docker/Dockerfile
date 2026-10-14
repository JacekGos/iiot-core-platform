# ---- Build stage ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon :pipeline-engine:bootJar \
 && find pipeline-engine/build/libs -name '*.jar' ! -name '*-plain.jar' -exec cp {} app.jar \;

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S -g 1000 app && adduser -S -u 1000 -G app app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/app.jar app.jar
USER 1000:1000
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]