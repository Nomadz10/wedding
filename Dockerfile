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
# Ubuntu 22.04 (jammy) base on purpose: its libheif 1.12 + libde265 decode iPhone
# HEIC reliably. Ubuntu 24.04's split-plugin libheif throws
# "Decoder plugin generated an error: Unspecified" on the same files.
FROM eclipse-temurin:21-jre-jammy
# imagemagick = general transcoding/resize; libheif-examples = `heif-convert`;
# libde265 = the HEVC decoder HEIC needs.
RUN apt-get update \
    && apt-get install -y --no-install-recommends imagemagick libheif1 libde265-0 libheif-examples \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/wedding-1.0.0.jar app.jar
# Railway/most hosts inject PORT; Spring reads it via server.port=${PORT:8080}
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
