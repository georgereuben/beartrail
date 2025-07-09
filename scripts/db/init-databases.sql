-- Initialize databases for all BearTrail microservices

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
