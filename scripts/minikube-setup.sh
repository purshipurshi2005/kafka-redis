#!/bin/bash

# Exit on error
set -e

# Check if minikube is running
if ! minikube status | grep -q "Running"; then
    echo "Starting Minikube..."
    minikube start --driver=docker --cpus=4 --memory=8192 --addons=ingress,dashboard,metrics-server
fi

# Set docker env to use minikube's docker daemon
eval $(minikube docker-env)

# Build the application image
echo "Building application image..."
./gradlew bootBuildImage --imageName=kafka-redis-status-service:0.0.1-SNAPSHOT

# Create namespace if it doesn't exist
kubectl create namespace kafka-redis-status --dry-run=client -o yaml | kubectl apply -f -

# Deploy Kafka and Redis using Helm
echo "Deploying Kafka and Redis..."
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Install Kafka
helm upgrade --install kafka bitnami/kafka \
    --namespace kafka-redis-status \
    --set replicaCount=1 \
    --set zookeeper.replicaCount=1 \
    --set persistence.enabled=false \
    --set deleteTopicEnable=true

# Install Redis
helm upgrade --install redis bitnami/redis \
    --namespace kafka-redis-status \
    --set architecture=standalone \
    --set auth.enabled=false \
    --set master.persistence.enabled=false

# Deploy OpenTelemetry Collector
echo "Deploying OpenTelemetry Collector..."
kubectl apply -f kubernetes/otel-collector.yaml -n kafka-redis-status

# Wait for dependencies to be ready
echo "Waiting for dependencies to be ready..."
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=kafka -n kafka-redis-status --timeout=300s
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=redis -n kafka-redis-status --timeout=300s
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=otel-collector -n kafka-redis-status --timeout=300s

# Deploy the application
echo "Deploying application..."
kubectl apply -k kubernetes/base

# Wait for the application to be ready
echo "Waiting for application to be ready..."
kubectl wait --for=condition=available deployment/kafka-redis-status-service -n kafka-redis-status --timeout=300s

# Get the application URL
echo "Application is ready!"
echo "You can access the application at:"
minikube service kafka-redis-status-service -n kafka-redis-status --url

# Port forward the management port for easy access to actuator endpoints
echo "Port forwarding management port (8081)..."
kubectl port-forward svc/kafka-redis-status-service 8081:8081 -n kafka-redis-status &
echo "Management endpoints available at: http://localhost:8081/actuator" 