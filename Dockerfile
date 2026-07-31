# ---- Stage 1: build the jar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Cache dependencies first (faster rebuilds)
COPY pom.xml .
RUN mvn -q -e dependency:go-offline
# Then build
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Stage 2: slim runtime image ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/wedding-1.0.0.jar app.jar
# Railway/most hosts inject PORT; Spring reads it via server.port=${PORT:8080}
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
