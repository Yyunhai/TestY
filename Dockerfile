FROM node:18-alpine AS frontend-builder

WORKDIR /workspace

COPY web/package*.json ./web/
COPY web/babel.config.js ./web/
COPY web/jsconfig.json ./web/
COPY web/vue.config.js ./web/
COPY web/public ./web/public
COPY web/src ./web/src

RUN mkdir -p /workspace/Spring-boot-test/src/main/resources
RUN cd web && npm ci && npm run build

FROM maven:3.9.9-eclipse-temurin-8 AS backend-builder

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY testy-repository/pom.xml ./testy-repository/pom.xml
COPY testy-service/pom.xml ./testy-service/pom.xml
COPY Spring-boot-test/pom.xml ./Spring-boot-test/pom.xml

COPY testy-repository/src ./testy-repository/src
COPY testy-service/src ./testy-service/src
COPY Spring-boot-test/src ./Spring-boot-test/src

COPY --from=frontend-builder /workspace/Spring-boot-test/src/main/resources/static ./Spring-boot-test/src/main/resources/static

RUN mvn -pl Spring-boot-test -am package -DskipTests

FROM eclipse-temurin:8-jre-alpine

WORKDIR /app

COPY --from=backend-builder /workspace/Spring-boot-test/target/spring-boot-test-1.0-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
