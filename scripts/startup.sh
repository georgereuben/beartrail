#!/bin/bash

# BearTrail Platform Startup Script
# Orchestrates all microservices and their dependencies
# Ensures proper initialization order and health checks

set -e

# Configuration
COMPOSE_FILE="../docker-compose.yml"
DB_USER="beartrail"
DB_PASS="beartrail123"
DB_CONTAINER="beartrail-postgres"
KAFKA_CONTAINER="beartrail-kafka"
REDIS_CONTAINER="beartrail-redis"
KAFKA_BROKER="localhost:9092"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if a container is running
is_container_running() {
    docker ps --format "table {{.Names}}" | grep -q "^$1$"
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local check_command=$2
    local timeout=${3:-60}
    local interval=${4:-2}

    log_info "Waiting for $service_name to be ready..."

    while [ $timeout -gt 0 ]; do
        if eval "$check_command" > /dev/null 2>&1; then
            log_success "$service_name is ready."
            return 0
        fi
        echo "Waiting for $service_name... ($timeout seconds remaining)"
        sleep $interval
        timeout=$((timeout - interval))
    done

    log_error "Timeout waiting for $service_name to be ready."
    return 1
}

# Function to verify database setup
verify_database() {
    local db_name=$1
    log_info "Verifying database '$db_name'..."

    local db_exists=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$db_name" -tAc "SELECT 1" 2>/dev/null)
    if [ "$db_exists" != "1" ]; then
        log_error "Database '$db_name' not accessible."
        return 1
    fi

    log_success "Database '$db_name' is accessible."
    return 0
}

# Function to verify TimescaleDB extension
verify_timescaledb() {
    local db_name=$1
    log_info "Verifying TimescaleDB extension in '$db_name'..."

    local timescaledb_exists=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$db_name" -tAc "SELECT 1 FROM pg_extension WHERE extname = 'timescaledb'" 2>/dev/null)
    if [ "$timescaledb_exists" != "1" ]; then
        log_warning "TimescaleDB extension not found in '$db_name'."
        return 1
    fi

    log_success "TimescaleDB extension is available in '$db_name'."
    return 0
}

# Function to create Kafka topic if not exists
create_kafka_topic() {
    local topic_name=$1
    local partitions=${2:-3}
    local replication_factor=${3:-1}

    log_info "Checking Kafka topic '$topic_name'..."

    local topic_exists=$(docker exec "$KAFKA_CONTAINER" kafka-topics --bootstrap-server "$KAFKA_BROKER" --list 2>/dev/null | grep "^$topic_name$")
    if [ -z "$topic_exists" ]; then
        log_info "Creating Kafka topic '$topic_name'..."
        if docker exec "$KAFKA_CONTAINER" kafka-topics --bootstrap-server "$KAFKA_BROKER" --create --topic "$topic_name" --partitions "$partitions" --replication-factor "$replication_factor" > /dev/null 2>&1; then
            log_success "Kafka topic '$topic_name' created successfully."
        else
            log_error "Failed to create Kafka topic '$topic_name'."
            return 1
        fi
    else
        log_success "Kafka topic '$topic_name' already exists."
    fi
    return 0
}

echo "🚀 Starting BearTrail Platform..."
echo "========================================"

# Start infrastructure services
log_info "Starting infrastructure services..."

# Start PostgreSQL with TimescaleDB
if ! is_container_running "$DB_CONTAINER"; then
    log_info "Starting PostgreSQL with TimescaleDB..."
    docker-compose -f "$COMPOSE_FILE" up -d postgresql

    wait_for_service "PostgreSQL" "docker exec $DB_CONTAINER pg_isready -U $DB_USER" 60 2
    if [ $? -ne 0 ]; then
        log_error "Failed to start PostgreSQL."
        exit 1
    fi
else
    log_success "PostgreSQL container is already running."
fi

# Verify all databases
databases=("beartrail_user_management" "beartrail_market_data" "beartrail_order_simulation" "beartrail_portfolio" "beartrail_leaderboard" "beartrail_notification")
for db in "${databases[@]}"; do
    verify_database "$db"
    if [ $? -ne 0 ]; then
        log_error "Database verification failed for $db."
        exit 1
    fi
done

# Verify TimescaleDB for market data service
verify_timescaledb "beartrail_market_data"

# Start Redis
if ! is_container_running "$REDIS_CONTAINER"; then
    log_info "Starting Redis..."
    docker-compose -f "$COMPOSE_FILE" up -d redis

    wait_for_service "Redis" "docker exec $REDIS_CONTAINER redis-cli ping" 30 1
    if [ $? -ne 0 ]; then
        log_error "Failed to start Redis."
        exit 1
    fi
else
    log_success "Redis container is already running."
fi

# Start Kafka ecosystem
if ! is_container_running "beartrail-zookeeper"; then
    log_info "Starting Zookeeper..."
    docker-compose -f "$COMPOSE_FILE" up -d zookeeper

    wait_for_service "Zookeeper" "docker exec beartrail-zookeeper zkCli.sh ls /" 30 2
fi

if ! is_container_running "$KAFKA_CONTAINER"; then
    log_info "Starting Kafka..."
    docker-compose -f "$COMPOSE_FILE" up -d kafka

    wait_for_service "Kafka" "docker exec $KAFKA_CONTAINER kafka-topics --bootstrap-server $KAFKA_BROKER --list" 60 2
    if [ $? -ne 0 ]; then
        log_error "Failed to start Kafka."
        exit 1
    fi
else
    log_success "Kafka container is already running."
fi

# Create required Kafka topics
kafka_topics=("market-data-updates" "user-events" "order-events" "portfolio-updates" "leaderboard-updates" "notifications")
for topic in "${kafka_topics[@]}"; do
    create_kafka_topic "$topic"
done

log_success "Infrastructure services are ready!"
echo ""

# Build all services
log_info "Building all microservices..."
if [ -f "../gradlew" ]; then
    cd .. && ./gradlew build -x test && cd scripts
    if [ $? -eq 0 ]; then
        log_success "All microservices built successfully."
    else
        log_error "Failed to build microservices."
        exit 1
    fi
else
    log_warning "Gradle wrapper not found. Skipping build step."
fi

echo ""
echo "✅ BearTrail Platform is ready!"
echo "========================================"
echo "Infrastructure Status:"
echo "  📊 PostgreSQL with TimescaleDB: Running ($DB_CONTAINER)"
echo "  🔄 Redis Cache: Running ($REDIS_CONTAINER)"
echo "  📨 Kafka Message Broker: Running ($KAFKA_CONTAINER)"
echo ""
echo "Available Databases:"
for db in "${databases[@]}"; do
    echo "  📂 $db"
done
echo ""
echo "Available Kafka Topics:"
for topic in "${kafka_topics[@]}"; do
    echo "  📤 $topic"
done
echo ""
echo "🎯 Available Services:"
echo "  • User Management Service"
echo "  • Market Data Service (with TimescaleDB)"
echo "  • Order Simulation Service"
echo "  • Portfolio Service"
echo "  • Leaderboard Service"
echo "  • Notification Service"
echo ""
echo "🚀 Start individual services with:"
echo "  cd ../services/[service-name] && ./gradlew bootRun"
echo ""
echo "🔧 Or use service-specific startup scripts:"
echo "  ./startup-market-data.sh"
echo ""
echo "📊 Monitor services:"
echo "  • PostgreSQL: localhost:5432"
echo "  • Redis: localhost:6379"
echo "  • Kafka: localhost:9092"
echo ""
echo "Happy Trading! 📈"
