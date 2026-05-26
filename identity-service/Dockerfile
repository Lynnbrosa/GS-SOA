# syntax=docker/dockerfile:1.6
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY identity-service/pom.xml identity-service/
COPY satellite-service/pom.xml satellite-service/
COPY gateway/pom.xml gateway/

RUN mvn -B -pl identity-service -am -DskipTests dependency:go-offline || true

COPY identity-service/src identity-service/src
RUN mvn -B -pl identity-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/identity-service/target/identity-service-*.jar /app/identity-service.jar
EXPOSE 8081
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
  CMD wget -qO- http://localhost:8081/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java","-jar","/app/identity-service.jar"]
