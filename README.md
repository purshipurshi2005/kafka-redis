# Kafka to Redis Status Service

A resilient Spring Boot microservice that consumes Protobuf messages from Kafka, persists them with status in Redis (with TTL), and exposes a REST API for status retrieval.

## Features

- Consumes Protobuf messages from Kafka topics
- Persists message status in Redis with configurable TTL
- Exposes REST API for status retrieval
- Implements resilience patterns (Circuit Breaker, Bulkhead, Rate Limiter)
- Supports distributed tracing with OpenTelemetry
- Secure communication with mTLS and OAuth2
- Comprehensive monitoring and health checks

## Prerequisites

- Java 17 or later
- Gradle 8.6 or later
- Docker and Docker Compose (for local development)
- Kafka cluster (for production)
- Redis cluster (for production)

## Local Development Setup

### Option 1: Docker Compose (Standalone)

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/kafka-redis-status-service.git
   cd kafka-redis-status-service
   ```

2. Start local dependencies using Docker Compose:
   ```bash
   docker-compose up -d
   ```
   This will start:
   - Kafka (localhost:9092)
   - Redis (localhost:6379)
   - OpenTelemetry Collector (localhost:4317)

3. Build the project:
   ```bash
   ./gradlew build
   ```

4. Run the application:
   ```bash
   ./gradlew bootRun
   ```

The service will start on port 8080 with the following endpoints:
- REST API: http://localhost:8080/api/v1
- Actuator: http://localhost:8080/api/v1/actuator
- Health Check: http://localhost:8080/api/v1/actuator/health

### Option 2: Minikube (Kubernetes)

Prerequisites:
- Minikube installed
- kubectl installed
- Helm installed

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/kafka-redis-status-service.git
   cd kafka-redis-status-service
   ```

2. Run the setup script:
   ```bash
   ./scripts/minikube-setup.sh
   ```

   This script will:
   - Start Minikube if not running
   - Build the application Docker image
   - Create a namespace
   - Deploy Kafka and Redis using Helm
   - Deploy the OpenTelemetry Collector
   - Deploy the application
   - Set up port forwarding for management endpoints

3. Access the application:
   - The script will output the application URL
   - Management endpoints are available at http://localhost:8081/actuator
   - Kafka is available at kafka:9092 within the cluster
   - Redis is available at redis:6379 within the cluster

4. View logs:
   ```bash
   kubectl logs -f deployment/kafka-redis-status-service -n kafka-redis-status
   ```

5. Access the Kubernetes dashboard:
   ```bash
   minikube dashboard
   ```

6. Clean up:
   ```bash
   # Delete the namespace and all resources
   kubectl delete namespace kafka-redis-status
   
   # Stop Minikube
   minikube stop
   ```

## Configuration

The application uses Spring profiles for different environments. Copy `src/main/resources/application-example.yml` to create your environment-specific configuration:

```bash
cp src/main/resources/application-example.yml src/main/resources/application-{env}.yml
```

Key configuration areas:
- Kafka connection and consumer settings
- Redis connection and TTL settings
- Security (OAuth2, mTLS)
- Resilience4j circuit breakers and rate limiters
- OpenTelemetry tracing

## Environment Variables

The following environment variables can be used to configure the application:

| Variable | Description | Default |
|----------|-------------|---------|
| SPRING_PROFILES_ACTIVE | Active Spring profile | dev |
| KAFKA_BOOTSTRAP_SERVERS | Kafka bootstrap servers | localhost:9092 |
| REDIS_HOST | Redis host | localhost |
| REDIS_PORT | Redis port | 6379 |
| REDIS_PASSWORD | Redis password | |
| REDIS_SSL | Enable Redis SSL | false |
| REDIS_TTL_SECONDS | Redis key TTL in seconds | 86400 |
| SERVER_PORT | Application port | 8080 |
| OAUTH2_ISSUER_URI | OAuth2 issuer URI | |
| OAUTH2_JWK_SET_URI | OAuth2 JWK set URI | |
| TRACING_SERVICE_NAME | OpenTelemetry service name | kafka-redis-status-service |
| TRACING_ENDPOINT | OpenTelemetry collector endpoint | http://localhost:4317 |

## API Documentation

### Status Endpoint

```
GET /api/v1/status/{requestId}
```

Response:
```json
{
  "requestId": "string",
  "status": "RECEIVED|PROCESSING|PROCESSED|FAILED|EXPIRED",
  "updatedAt": "timestamp",
  "errorMessage": "string",
  "metadata": {
    "key": "value"
  }
}
```

### Health Endpoint

```
GET /api/v1/actuator/health
```

## Development

### Building

```bash
./gradlew build
```

### Testing

```bash
./gradlew test
```

### Running Tests with Testcontainers

The project uses Testcontainers for integration tests. Make sure Docker is running before executing tests:

```bash
./gradlew test
```

## Deployment

### Docker

Build the Docker image:

```bash
./gradlew bootBuildImage
```

Run the container:

```bash
docker run -p 8080:8080 kafka-redis-status-service:0.0.1-SNAPSHOT
```

### Kubernetes

The project includes a basic Helm chart for Kubernetes deployment. See the `kubernetes` directory for details.

## Monitoring

The service exposes metrics via Spring Boot Actuator and Prometheus:

- JVM metrics
- Kafka consumer metrics
- Redis metrics
- Resilience4j circuit breaker metrics
- Custom business metrics

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 