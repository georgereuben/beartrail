#!/bin/bash

# Startup script for Java Spring Boot project database setup
# Ensures PostgreSQL is running, user and database exist, and password is set

DB_USER="postgres"
DB_PASS="password"
DB_NAME="market_data"

# Check if PostgreSQL is running
pg_isready > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "PostgreSQL is not running. Please start PostgreSQL and rerun this script."
  exit 1
fi

echo "PostgreSQL is running."

# Create user if not exists
USER_EXISTS=$(psql -U "$DB_USER" -d postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname='$DB_USER'")
if [ "$USER_EXISTS" != "1" ]; then
  echo "Creating user $DB_USER..."
  psql -d postgres -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASS' SUPERUSER;"
else
  echo "User $DB_USER already exists."
fi

# Set password for user (always, for safety)
echo "Setting password for $DB_USER..."
psql -d postgres -c "ALTER USER $DB_USER WITH PASSWORD '$DB_PASS';"

# Create database if not exists
DB_EXISTS=$(psql -U "$DB_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'")
if [ "$DB_EXISTS" != "1" ]; then
  echo "Creating database $DB_NAME..."
  psql -U "$DB_USER" -d postgres -c "CREATE DATABASE $DB_NAME;"
else
  echo "Database $DB_NAME already exists."
fi

echo "Database setup complete."

