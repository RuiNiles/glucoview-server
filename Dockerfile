# Stage 1: Cache Gradle dependencies
FROM gradle:9.1.0-jdk21 AS cache
RUN mkdir -p /home/gradle/app
WORKDIR /home/gradle/app
COPY build.gradle.* gradle.properties ./
COPY gradle ./gradle
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle clean build -i --stacktrace || true

# Stage 2: Build Application
FROM gradle:9.1.0-jdk21 AS build
WORKDIR /home/gradle/src
COPY --from=cache /home/gradle/.gradle /home/gradle/.gradle
COPY --chown=gradle:gradle . .
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle buildFatJar --no-daemon

# Stage 3: Runtime image (JRE is enough to run the jar)
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/*.jar /app/glucoviewserver.jar
RUN useradd -r -s /sbin/nologin appuser && chown appuser:appuser /app/glucoviewserver.jar
USER appuser

ENTRYPOINT ["java","-jar","/app/glucoviewserver.jar"]
