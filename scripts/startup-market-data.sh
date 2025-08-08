#!/bin/bash

# Startup script for Market Data Service
# Ensures PostgreSQL with TimescaleDB is running, user and database exist
# Ensures Kafka is running and required topic exists

DB_USER="beartrail"
DB_PASS="beartrail123"
DB_NAME="beartrail_market_data"
DB_CONTAINER="beartrail-postgres"
KAFKA_BROKER="localhost:9092"
KAFKA_TOPIC="market-data-updates"
KAFKA_CONTAINER="beartrail-kafka"

# Use correct relative path for docker-compose.yml from scripts folder
COMPOSE_PATH="../docker-compose.yml"

echo "Starting Market Data Service setup..."

# Check if PostgreSQL container is running
if ! docker ps | grep -q "$DB_CONTAINER"; then
  echo "PostgreSQL container '$DB_CONTAINER' is not running. Starting Docker Compose services..."
  docker-compose -f "$COMPOSE_PATH" up -d postgresql

  # Wait for PostgreSQL to be ready
  echo "Waiting for PostgreSQL to be ready..."
  timeout=60
  while [ $timeout -gt 0 ]; do
    if docker exec "$DB_CONTAINER" pg_isready -U "$DB_USER" -d "$DB_NAME" > /dev/null 2>&1; then
      echo "PostgreSQL is ready."
      break
    fi
    echo "Waiting for PostgreSQL... ($timeout seconds remaining)"
    sleep 2
    timeout=$((timeout - 2))
  done

  if [ $timeout -le 0 ]; then
    echo "Timeout waiting for PostgreSQL to be ready."
    exit 1
  fi
else
  echo "PostgreSQL container '$DB_CONTAINER' is running."
fi

# Verify database and user exist
echo "Verifying database setup..."
DB_EXISTS=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -tAc "SELECT 1" 2>/dev/null)
if [ "$DB_EXISTS" != "1" ]; then
  echo "Error: Database '$DB_NAME' or user '$DB_USER' not accessible."
  echo "Please ensure Docker Compose has initialized the database properly."
  exit 1
fi

# Verify TimescaleDB extension is available
echo "Verifying TimescaleDB extension..."
TIMESCALEDB_EXISTS=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -tAc "SELECT 1 FROM pg_extension WHERE extname = 'timescaledb'" 2>/dev/null)
if [ "$TIMESCALEDB_EXISTS" != "1" ]; then
  echo "Warning: TimescaleDB extension not found in database '$DB_NAME'."
  echo "This may cause migration failures. Please check database initialization."
else
  echo "TimescaleDB extension is available."
fi

echo "Database setup verified."

# Check if Kafka container is running
if ! docker ps | grep -q "$KAFKA_CONTAINER"; then
  echo "Kafka container '$KAFKA_CONTAINER' is not running. Starting Docker Compose services..."
  docker-compose -f "$COMPOSE_PATH" up -d kafka

  # Wait for Kafka to be ready
  echo "Waiting for Kafka to be ready..."
  timeout=60
  while [ $timeout -gt 0 ]; do
    if docker exec "$KAFKA_CONTAINER" kafka-topics --bootstrap-server "$KAFKA_BROKER" --list > /dev/null 2>&1; then
      echo "Kafka is ready."
      break
    fi
    echo "Waiting for Kafka... ($timeout seconds remaining)"
    sleep 2
    timeout=$((timeout - 2))
  done

  if [ $timeout -le 0 ]; then
    echo "Timeout waiting for Kafka to be ready."
    exit 1
  fi
else
  echo "Kafka container '$KAFKA_CONTAINER' is running."
fi

# Create topic if not exists
echo "Checking Kafka topic '$KAFKA_TOPIC'..."
TOPIC_EXISTS=$(docker exec "$KAFKA_CONTAINER" kafka-topics --bootstrap-server "$KAFKA_BROKER" --list 2>/dev/null | grep "^$KAFKA_TOPIC$")
if [ -z "$TOPIC_EXISTS" ]; then
  echo "Creating Kafka topic '$KAFKA_TOPIC'..."
  docker exec "$KAFKA_CONTAINER" kafka-topics --bootstrap-server "$KAFKA_BROKER" --create --topic "$KAFKA_TOPIC" --partitions 3 --replication-factor 1
  if [ $? -eq 0 ]; then
    echo "Kafka topic '$KAFKA_TOPIC' created successfully."
  else
    echo "Failed to create Kafka topic '$KAFKA_TOPIC'."
    exit 1
  fi
else
  echo "Kafka topic '$KAFKA_TOPIC' already exists."
fi

echo "Kafka setup complete."

# Verify Redis is available (used by the market data service)
REDIS_CONTAINER="beartrail-redis"
if ! docker ps | grep -q "$REDIS_CONTAINER"; then
  echo "Redis container '$REDIS_CONTAINER' is not running. Starting Redis..."
  docker-compose -f "$COMPOSE_PATH" up -d redis

  # Wait for Redis to be ready
  echo "Waiting for Redis to be ready..."
  timeout=30
  while [ $timeout -gt 0 ]; do
    if docker exec "$REDIS_CONTAINER" redis-cli ping > /dev/null 2>&1; then
      echo "Redis is ready."
      break
    fi
    echo "Waiting for Redis... ($timeout seconds remaining)"
    sleep 1
    timeout=$((timeout - 1))
  done

  if [ $timeout -le 0 ]; then
    echo "Warning: Timeout waiting for Redis to be ready."
  fi
else
  echo "Redis container '$REDIS_CONTAINER' is running."
fi

echo ""
echo "✅ Market Data Service dependencies are ready!"
echo "   - PostgreSQL with TimescaleDB: $DB_CONTAINER"
echo "   - Database: $DB_NAME"
echo "   - User: $DB_USER"
echo "   - Kafka: $KAFKA_CONTAINER"
echo "   - Topic: $KAFKA_TOPIC"
echo "   - Redis: $REDIS_CONTAINER"
echo ""
echo "You can now start the Market Data Service with:"
echo "   cd ../services/market-data && ./gradlew bootRun"
echo ""
