# ── Stage 1: Build ──────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy the parent POM first — needed to resolve shared dependency versions
COPY pom.xml .

# Copy only THIS service's pom and source — not the other 5 modules
COPY income-service/pom.xml income-service/pom.xml
COPY income-service/src income-service/src

# Build just this module (parent resolved via relativePath, modules list ignored)
RUN mvn -f income-service/pom.xml clean package -DskipTests

# ── Stage 2: Run ────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/income-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]