# Estágio 1: Build (Gradle + JDK 21)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle settings files, and dependencies
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Downloads the dependencies (Gradle cache dependencies)
RUN ./gradlew dependencies --no-daemon

# Copies the source code and generates the executable (.jar)
COPY src src
RUN ./gradlew bootJar -DskipTests --no-daemon

# Stage 2: Execution (JRE 21 only, optimized and lightweight image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the .jar generated in the previous stage from the build/libs folder.
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]