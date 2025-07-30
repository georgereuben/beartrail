#!/bin/bash

# BearTrail Development Environment Setup Script

echo "🚀 Starting BearTrail development environment..."

# Build all services
echo "📦 Building all services..."
./gradlew clean build -x test

# Start infrastructure services
echo "🔧 Starting infrastructure services..."
docker-compose up -d postgresql redis kafka zookeeper

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Initialize databases
echo "🗄️ Initializing databases..."
docker-compose exec postgresql psql -U beartrail -d beartrail -f /docker-entrypoint-initdb.d/init-databases.sql

echo "✅ Development environment is ready!"
echo "📊 Grafana: http://localhost:3000 (admin/admin123)"
echo "📈 Prometheus: http://localhost:9090"
echo "🗄️ PostgreSQL: localhost:5432 (beartrail/beartrail123)"
echo "🔴 Redis: localhost:6379"
echo "📨 Kafka: localhost:9092"
