FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY persona/pom.xml persona/pom.xml
COPY mx-region/pom.xml mx-region/pom.xml
COPY chl-region/pom.xml chl-region/pom.xml
COPY canada-region/pom.xml canada-region/pom.xml
COPY us-region/pom.xml us-region/pom.xml
COPY functional-tests/pom.xml functional-tests/pom.xml
COPY commonlib-local /tmp/commonlib-local
RUN mvn install:install-file \
    -Dfile=/tmp/commonlib-local/mp-cmd-flashpicks-common-lib-0.0.1-SNAPSHOT.jar \
    -DpomFile=/tmp/commonlib-local/mp-cmd-flashpicks-common-lib-0.0.1-SNAPSHOT.pom \
    -DgroupId=com.example -DartifactId=mp-cmd-flashpicks-common-lib \
    -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar && \
    ls -la /root/.m2/repository/com/example/mp-cmd-flashpicks-common-lib/0.0.1-SNAPSHOT/
RUN mvn dependency:go-offline || true
COPY common common
COPY persona persona
COPY mx-region mx-region
COPY chl-region chl-region
COPY canada-region canada-region
COPY us-region us-region
COPY functional-tests functional-tests
RUN mvn package -DskipTests -q -pl !functional-tests

FROM eclipse-temurin:21-jre
WORKDIR /app
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.1.0/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
COPY --from=build /app/persona/target/persona-*.jar app.jar
EXPOSE 8080
ENV OTEL_JAVAAGENT_ENABLED=false
ENTRYPOINT ["java", \
  "-javaagent:/app/opentelemetry-javaagent.jar", \
  "-Dotel.service.name=hello-springboot", \
  "-Dotel.traces.exporter=otlp", \
  "-Dotel.metrics.exporter=none", \
  "-Dotel.logs.exporter=none", \
  "-jar", "app.jar"]
