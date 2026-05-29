# syntax=docker/dockerfile:1.6
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY identity-service/pom.xml identity-service/
COPY satellite-service/pom.xml satellite-service/
COPY gateway/pom.xml gateway/
COPY soap-service/pom.xml soap-service/

RUN mvn -B -pl soap-service -am -DskipTests dependency:go-offline || true

COPY soap-service/src soap-service/src
RUN mvn -B -pl soap-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/soap-service/target/soap-service-*.jar /app/soap-service.jar
EXPOSE 8083
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
  CMD wget -qO- http://localhost:8083/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java","-jar","/app/soap-service.jar"]
