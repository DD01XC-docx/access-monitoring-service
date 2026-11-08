# 1. frontend
FROM node:22-slim AS web
WORKDIR /web
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# 2. backend
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
# cached layer
RUN ./mvnw -q -B dependency:go-offline -Dskip.installnodenpm -Dskip.npm
COPY src src
COPY --from=web /web/dist frontend/dist
RUN ./mvnw -q -B package -DskipTests -Dskip.installnodenpm -Dskip.npm

# 3. runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home amc
COPY --from=build /app/target/*.jar app.jar
USER amc
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
