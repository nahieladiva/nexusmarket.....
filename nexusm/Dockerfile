# ---------- Stage 1: build with Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------- Stage 2: small image with the JRE only
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV TZ=America/Bogota
COPY --from=build /build/target/nexusmarket-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
