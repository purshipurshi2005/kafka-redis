FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app
COPY . .
RUN ./gradlew clean bootJar

FROM eclipse-temurin:17-jre

WORKDIR /app

# Create a non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/*-boot.jar app.jar

# Change ownership to the non-root user
RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"] 