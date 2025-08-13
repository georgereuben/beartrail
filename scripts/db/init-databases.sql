-- Initialize databases for all BearTrail microservices

-- Create the beartrail user if it doesn't exist
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'beartrail') THEN

      CREATE ROLE beartrail LOGIN PASSWORD 'beartrail123';
   END IF;
END
$do$;

-- User Management Service Database
CREATE DATABASE beartrail_user_management;

-- Market Data Service Database
CREATE DATABASE beartrail_market_data;

-- Order Simulation Service Database
CREATE DATABASE beartrail_order_simulation;

-- Portfolio Service Database
CREATE DATABASE beartrail_portfolio;

-- Leaderboard Service Database
CREATE DATABASE beartrail_leaderboard;

-- Notification Service Database
CREATE DATABASE beartrail_notification;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE beartrail_user_management TO beartrail;
GRANT ALL PRIVILEGES ON DATABASE beartrail_market_data TO beartrail;
GRANT ALL PRIVILEGES ON DATABASE beartrail_order_simulation TO beartrail;
GRANT ALL PRIVILEGES ON DATABASE beartrail_portfolio TO beartrail;
GRANT ALL PRIVILEGES ON DATABASE beartrail_leaderboard TO beartrail;
GRANT ALL PRIVILEGES ON DATABASE beartrail_notification TO beartrail;

-- Enable TimescaleDB extension for databases that need it
\c beartrail_market_data;
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;
ALTER SCHEMA public OWNER TO beartrail;
GRANT USAGE, CREATE ON SCHEMA public TO beartrail;

-- Grant additional permissions on the market data database
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO beartrail;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO beartrail;
GRANT USAGE ON SCHEMA public TO beartrail;

-- You can add TimescaleDB to other databases if needed
-- \c beartrail_order_simulation;
-- CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- \c beartrail_portfolio;
-- CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;
