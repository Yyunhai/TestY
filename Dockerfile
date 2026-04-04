FROM node:18-alpine AS frontend-builder

WORKDIR /workspace

COPY frontend/web/package*.json ./frontend/web/
COPY frontend/web/babel.config.js ./frontend/web/
COPY frontend/web/jsconfig.json ./frontend/web/
COPY frontend/web/vue.config.js ./frontend/web/
COPY frontend/web/public ./frontend/web/public
COPY frontend/web/src ./frontend/web/src

RUN mkdir -p /workspace/backend/app/src/main/resources
RUN cd frontend/web && npm ci && npm run build

FROM maven:3.9.9-eclipse-temurin-8 AS backend-builder

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY backend/modules/logging/pom.xml ./backend/modules/logging/pom.xml
COPY backend/modules/repository/pom.xml ./backend/modules/repository/pom.xml
COPY backend/modules/service/pom.xml ./backend/modules/service/pom.xml
COPY backend/modules/document/pom.xml ./backend/modules/document/pom.xml
COPY backend/app/pom.xml ./backend/app/pom.xml

COPY backend/modules/logging/src ./backend/modules/logging/src
COPY backend/modules/repository/src ./backend/modules/repository/src
COPY backend/modules/service/src ./backend/modules/service/src
COPY backend/modules/document/src ./backend/modules/document/src
COPY backend/app/src ./backend/app/src

COPY --from=frontend-builder /workspace/frontend/web/dist ./backend/app/src/main/resources/static

RUN mvn -pl backend/app -am package -DskipTests

FROM eclipse-temurin:8-jre-alpine

WORKDIR /app

COPY --from=backend-builder /workspace/backend/app/target/testy-app-1.0-SNAPSHOT.jar /app/app.jar

RUN mkdir -p /app/logs

VOLUME ["/app/logs"]

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
