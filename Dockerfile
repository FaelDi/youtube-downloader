# Stage 1: Builder
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy dependency manifests first for layer caching
COPY pom.xml .
COPY mvnw .
COPY .mvn/ .mvn/

# Download dependencies offline
RUN ./mvnw dependency:go-offline -q

# Copy source and build
COPY src/ src/

RUN ./mvnw clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
