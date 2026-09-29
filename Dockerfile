# ============================================================
# Dockerfile - used ONLY for hosting the app online (e.g. Render)
# Stage 1: build the JAR using Maven + JDK 17
# Stage 2: run the JAR on a small Java 17 runtime image
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Limit memory so it fits in free hosting plans (512 MB)
ENV JAVA_OPTS="-Xmx350m -Xss512k"
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
