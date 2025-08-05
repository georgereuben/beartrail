#!/bin/bash

# Startup script for Java Spring Boot project database and Kafka setup
# Ensures PostgreSQL is running, user and database exist, and password is set
# Ensures Kafka is running and required topic exists

DB_USER="admin"
DB_PASS="password"
DB_NAME="market_data"
KAFKA_BROKER="localhost:9092"
KAFKA_TOPIC="market-data-updates"

# Check if PostgreSQL is running
pg_isready > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "PostgreSQL is not running. Please start PostgreSQL and rerun this script."
  exit 1
fi

echo "PostgreSQL is running."

# Create user if not exists (using postgres superuser)
USER_EXISTS=$(psql -U postgres -d postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname='$DB_USER'" 2>/dev/null)
if [ "$USER_EXISTS" != "1" ]; then
  echo "Creating user $DB_USER..."
  psql -U postgres -d postgres -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASS' CREATEDB;"
else
  echo "User $DB_USER already exists."
  # Grant CREATEDB privilege if it doesn't have it
  echo "Ensuring $DB_USER has CREATEDB privilege..."
  psql -U postgres -d postgres -c "ALTER USER $DB_USER CREATEDB;"
fi

# Set password for user (always, for safety)
echo "Setting password for $DB_USER..."
psql -U postgres -d postgres -c "ALTER USER $DB_USER WITH PASSWORD '$DB_PASS';"

# Create database if not exists
DB_EXISTS=$(psql -U postgres -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" 2>/dev/null)
if [ "$DB_EXISTS" != "1" ]; then
  echo "Creating database $DB_NAME..."
  psql -U postgres -d postgres -c "CREATE DATABASE $DB_NAME OWNER $DB_USER;"
else
  echo "Database $DB_NAME already exists."
fi

echo "Database setup complete."

# Check if Kafka is running (in container)
docker exec beartrail-kafka kafka-topics --bootstrap-server "$KAFKA_BROKER" --list > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "Kafka is not running in container 'beartrail-kafka' or kafka-topics CLI is not available. Please start Kafka and ensure the container is running."
  exit 1
fi

echo "Kafka is running in container 'beartrail-kafka'."

# Create topic if not exists
TOPIC_EXISTS=$(docker exec beartrail-kafka kafka-topics --bootstrap-server "$KAFKA_BROKER" --list | grep "^$KAFKA_TOPIC$")
if [ -z "$TOPIC_EXISTS" ]; then
  echo "Creating Kafka topic $KAFKA_TOPIC in container..."
  docker exec beartrail-kafka kafka-topics --bootstrap-server "$KAFKA_BROKER" --create --topic "$KAFKA_TOPIC" --partitions 1 --replication-factor 1
else
  echo "Kafka topic $KAFKA_TOPIC already exists in container."
fi

echo "Kafka setup complete."
