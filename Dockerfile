# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build

# Install Maven
ARG MAVEN_VERSION=3.9.6
ARG USER_HOME_DIR="/root"
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN apt-get update && \
    apt-get install -y curl && \
    mkdir -p /usr/share/maven /usr/share/maven/ref && \
    curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 && \
    rm -f /tmp/apache-maven.tar.gz && \
    ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

# Set the working directory
WORKDIR /app

# Copy the project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# List the contents of the target directory
RUN ls -la target

# Find the JAR file
RUN find /app -name "*.jar"

# Run stage
FROM eclipse-temurin:21-jre-alpine

# Add a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set the working directory in the container
WORKDIR /app

# Copy the compiled jar file from the build stage
COPY --from=build /app/target/*.jar /app/K8sMutate.jar

# Make sure the jar is owned by the non-root user
RUN chown -R appuser:appgroup /app

# Use the non-root user to run the application
USER appuser

# Expose the port the application runs on
EXPOSE 8080

# Set Java options for better container support
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar K8sMutate.jar"]