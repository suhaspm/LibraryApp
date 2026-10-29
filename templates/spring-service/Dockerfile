# syntax=docker/dockerfile:1.7
# Shared by all Spring Boot services: CI builds app.jar with Gradle first, then runs
#   docker build -f services/<svc>/Dockerfile services/<svc>
# Building the jar outside Docker keeps the Gradle cache warm and the image free of build tools.

# Runs on the CI machine's native platform even when building arm64: the jar is platform-neutral,
# so there is no reason to run this step under slow QEMU emulation.
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jre-noble AS extract
WORKDIR /work
COPY build/libs/app.jar app.jar
# Split the jar into layers (dependencies rarely change, application code often) so a
# code-only change pushes a few hundred KB instead of the whole 40 MB.
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-noble
# Fixed non-root UID so Kubernetes can enforce runAsNonRoot without guessing.
RUN groupadd --system --gid 10001 app && useradd --system --uid 10001 --gid app --no-create-home app
WORKDIR /app
COPY --from=extract /work/extracted/dependencies/ ./
COPY --from=extract /work/extracted/spring-boot-loader/ ./
COPY --from=extract /work/extracted/snapshot-dependencies/ ./
COPY --from=extract /work/extracted/application/ ./
USER 10001:10001
EXPOSE 8080 8081
# Size the heap from the container memory limit, not the host's RAM.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
