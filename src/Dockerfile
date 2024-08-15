# Use OpenJDK 21 on Alpine as it's more lightweight
FROM eclipse-temurin:21-jre-alpine

# Add a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set the working directory in the container
WORKDIR /app

# Copy the compiled jar file to the container
COPY target/k8smutate-1.0-SNAPSHOT.jar /app/K8sMutate.jar

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