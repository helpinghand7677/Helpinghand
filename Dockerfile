# ============================================================
# Stage 1: Build the WAR file using Maven
# ============================================================
FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /app
COPY pom.xml .
# Download dependencies first so Docker can cache this layer
RUN mvn dependency:go-offline -B

COPY ..
RUN mvn clean package -DskipTests

# ============================================================
# Stage 2: Run the WAR on Tomcat
# ============================================================
FROM tomcat:9.0-jdk11-temurin

# Remove Tomcat's default sample apps to keep the image small/clean
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy our WAR as ROOT so the app is served at http://localhost:8080/
COPY --from=build /app/target/helping-hand-backend.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
