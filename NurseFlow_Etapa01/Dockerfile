# NurseFlow Backend - Java 21 / Spring Boot
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -B -ntp clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd -r nurseflow && useradd -r -g nurseflow -d /app nurseflow
COPY --from=build --chown=nurseflow:nurseflow /build/target/nurseflow-backend-*.jar /app/nurseflow.jar
USER nurseflow
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"
ENTRYPOINT ["java", "-jar", "/app/nurseflow.jar"]
