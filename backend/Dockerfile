FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/despensa-natural-del-caribe-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
