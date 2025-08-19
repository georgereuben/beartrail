# BearTrail API Contracts

This document defines the API contracts for all microservices in the BearTrail platform. Each service exposes REST APIs and communicates via Kafka events.

## Table of Contents
- [User Management Service](#user-management-service)
- [Market Data Service](#market-data-service)
- [Order Simulation Service](#order-simulation-service)
- [Portfolio Service](#portfolio-service)
- [Leaderboard Service](#leaderboard-service)
- [Notification Service](#notification-service)
- [Kafka Event Contracts](#kafka-event-contracts)

---

## User Management Service

**Base URL**: `http://localhost:8081/users`

The User Management Service handles both authentication and user profile management, providing a unified API for all user-related operations.

### Authentication Endpoints

#### POST /users/auth/login
**Description**: Authenticate user with credentials

**Request Body**:
```json
{
  "username": "string",
  "password": "string"
}
```

**Response (200)**:
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "username": "string",
    "email": "string",
    "roles": ["USER", "TRADER"]
  }
}
```

#### POST /users/auth/register
**Description**: Register new user account

**Request Body**:
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "confirmPassword": "string"
}
```

**Response (201)**:
```json
{
  "message": "User registered successfully",
  "userId": "uuid"
}
```

#### POST /users/auth/refresh
**Description**: Refresh access token

**Request Body**:
```json
{
  "refreshToken": "string"
}
```

**Response (200)**:
```json
{
  "accessToken": "string",
  "expiresIn": 3600
}
```

#### POST /users/auth/logout
**Description**: Invalidate user session

**Headers**: `Authorization: Bearer <token>`

**Response (200)**:
```json
{
  "message": "Logged out successfully"
}
```

#### GET /users/auth/me
**Description**: Get current user authentication info

**Headers**: `Authorization: Bearer <token>`

**Response (200)**:
```json
{
  "id": "uuid",
  "username": "string",
  "email": "string",
  "roles": ["USER", "TRADER"],
  "isActive": true,
  "lastLogin": "2025-07-30T10:30:00Z"
}
```

#### POST /users/auth/oauth/google
**Description**: OAuth2 authentication with Google

**Request Body**:
```json
{
  "code": "string",
  "redirectUri": "string"
}
```

**Response (200)**:
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "username": "string",
    "email": "string",
    "roles": ["USER", "TRADER"]
  }
}
```

#### PUT /users/auth/password
**Description**: Change user password

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "currentPassword": "string",
  "newPassword": "string",
  "confirmPassword": "string"
}
```

**Response (200)**:
```json
{
  "message": "Password updated successfully"
}
```

### Profile Management Endpoints

#### GET /users/profile
**Description**: Get user profile

**Headers**: `Authorization: Bearer <token>`

**Response (200)**:
```json
{
  "userId": "uuid",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "phone": "string",
  "dateOfBirth": "1990-01-01",
  "country": "string",
  "avatarUrl": "string",
  "preferences": {
    "theme": "dark",
    "language": "en",
    "timezone": "UTC"
  },
  "createdAt": "2025-07-30T10:30:00Z",
  "updatedAt": "2025-07-30T10:30:00Z"
}
```

#### PUT /users/profile
**Description**: Update user profile

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "firstName": "string",
  "lastName": "string",
  "phone": "string",
  "dateOfBirth": "1990-01-01",
  "country": "string"
}
```

**Response (200)**:
```json
{
  "message": "Profile updated successfully",
  "profile": { /* updated profile object */ }
}
```

#### GET /users/settings
**Description**: Get user settings

**Headers**: `Authorization: Bearer <token>`

**Response (200)**:
```json
{
  "emailNotifications": true,
  "pushNotifications": true,
  "tradingNotifications": true,
  "theme": "dark",
  "language": "en",
  "timezone": "UTC"
}
```

#### PUT /users/settings
**Description**: Update user settings

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "emailNotifications": true,
  "pushNotifications": false,
  "tradingNotifications": true,
  "theme": "light",
  "language": "en",
  "timezone": "EST"
}
```

#### POST /users/avatar
**Description**: Upload profile picture

**Headers**: `Authorization: Bearer <token>`

**Content-Type**: `multipart/form-data`

**Request**: File upload

**Response (200)**:
```json
{
  "message": "Avatar uploaded successfully",
  "avatarUrl": "string"
}
```

---

## Market Data Services (Decoupled Architecture)

### Market Data Client Service

**Base URL**: `http://localhost:8082/market-client` (Internal Backend Service)

**Purpose**: Lightweight service dedicated to Upstox API interfacing and real-time data distribution

**Service Architecture**:
- **Upstox API Integration**: Fetches OHLCV candlestick data with parallel processing
- **Kafka Event Publishing**: Publishes to both `market-data-updates` and `market-data-persistence` topics
- **Redis Caching**: Caches recent API responses for performance
- **No Database Operations**: Purely focused on data fetching and real-time distribution

### Data Fetching Schedule

| Interval | Cron Schedule | Source | Description |
|----------|---------------|--------|-------------|
| **1 minute** | `0 0/1 * * * ?` | Upstox API | Parallel batch processing of 20,000+ stocks |

### Performance Optimizations

**Parallel Processing**:
- **Batch Size**: 500 stocks per API call
- **Thread Pool**: 12 concurrent threads
- **Connection Pooling**: 100 total connections, 25 per route
- **Timeouts**: 3s connect, 8s read

**Kafka Publishing**:
```java
// Dual topic publishing for separation of concerns
kafkaProducer.sendAsync("market-data-updates", candle);     // Real-time consumers
kafkaProducer.sendAsync("market-data-persistence", candle); // Storage service
```

### Market Data Service (Main)

**Base URL**: `http://localhost:8083/market-data` (Internal API Service)

**Purpose**: Central market data repository with optimized database operations and query serving

**Service Architecture**:
- **Kafka Consumer**: Consumes from `market-data-persistence` topic with batch processing
- **TimescaleDB Storage**: Optimized time-series storage with compression and partitioning
- **Query API**: REST endpoints for historical data retrieval
- **Continuous Aggregates**: Automatic generation of higher timeframe candles

### Database Operations

**Batch Processing**:
- **Batch Size**: 1000 candles per database write
- **Flush Interval**: Every 5 seconds or when batch is full
- **Connection Pooling**: Optimized for high-throughput writes

**TimescaleDB Integration**:
- **Hypertable Configuration**: 1-day chunks with compression
- **Retention Policy**: 2 years automatic cleanup
- **Indexing**: Optimized for symbol + timeframe + timestamp queries

### REST API Endpoints

#### GET /api/candles/{symbol}
**Description**: Get historical candle data for a symbol

**Parameters**:
- `symbol` (path): Stock symbol (e.g., "NSE_EQ|INE002A01018")
- `interval` (query): Time interval ("I1", "I30", "1d")
- `from` (query): Start timestamp (ISO 8601)
- `to` (query): End timestamp (ISO 8601)
- `limit` (query, optional): Maximum number of candles (default: 1000)

**Response (200)**:
```json
{
  "symbol": "NSE_EQ|INE002A01018",
  "interval": "I1",
  "candles": [
    {
      "timestamp": "2025-08-13T09:15:00Z",
      "open": 150.00,
      "high": 152.50,
      "low": 149.75,
      "close": 151.25,
      "volume": 125000
    }
  ],
  "count": 1,
  "fromTimestamp": "2025-08-13T09:15:00Z",
  "toTimestamp": "2025-08-13T15:30:00Z"
}
```

#### GET /api/latest/{symbol}
**Description**: Get the latest candle for a symbol

**Parameters**:
- `symbol` (path): Stock symbol
- `interval` (query): Time interval

**Response (200)**:
```json
{
  "symbol": "NSE_EQ|INE002A01018",
  "interval": "I1",
  "candle": {
    "timestamp": "2025-08-13T15:30:00Z",
    "open": 151.00,
    "high": 151.75,
    "low": 150.50,
    "close": 151.25,
    "volume": 45000
  }
}
```

#### GET /api/bulk/latest
**Description**: Get latest candles for multiple symbols

**Parameters**:
- `symbols` (query): Comma-separated list of symbols
- `interval` (query): Time interval

**Response (200)**:
```json
{
  "interval": "I1",
  "candles": {
    "NSE_EQ|INE002A01018": {
      "timestamp": "2025-08-13T15:30:00Z",
      "open": 151.00,
      "high": 151.75,
      "low": 150.50,
      "close": 151.25,
      "volume": 45000
    },
    "NSE_EQ|INE009A01021": {
      "timestamp": "2025-08-13T15:30:00Z",
      "open": 2750.00,
      "high": 2765.50,
      "low": 2745.25,
      "close": 2760.75,
      "volume": 12500
    }
  },
  "count": 2
}
```

#### GET /api/aggregates/{symbol}
**Description**: Get aggregated data (from continuous aggregates)

**Parameters**:
- `symbol` (path): Stock symbol
- `type` (query): Aggregate type ("5m", "30m", "1h", "1d")
- `from` (query): Start timestamp
- `to` (query): End timestamp

**Response (200)**:
```json
{
  "symbol": "NSE_EQ|INE002A01018",
  "aggregateType": "5m",
  "candles": [
    {
      "bucket": "2025-08-13T09:15:00Z",
      "open": 150.00,
      "high": 152.50,
      "low": 149.75,
      "close": 151.25,
      "volume": 625000,
      "candleCount": 5
    }
  ]
}
```

### Continuous Aggregates

**5-minute candles** (from 1-minute data):
```sql
CREATE MATERIALIZED VIEW ohlc_5m_candles
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('5 minutes', timestamp) AS bucket,
    stock_id,
    FIRST(open_price, timestamp) AS open_price,
    MAX(high_price) AS high_price,
    MIN(low_price) AS low_price,
    LAST(close_price, timestamp) AS close_price,
    SUM(volume) AS volume,
    COUNT(*) AS candle_count
FROM ohlc_candles
GROUP BY bucket, stock_id;
```

### Kafka Event Flow

**Market Data Client Service Produces**:
- `market-data-updates`: Real-time price updates for immediate consumption
- `market-data-persistence`: Candle data for database storage

**Market Data Service Consumes**:
- `market-data-persistence`: Batch processes and stores candle data

**Event Structure**:
```json
{
  "symbol": "NSE_EQ|INE002A01018",
  "lastPrice": 150.25,
  "openPrice": 149.00,
  "highPrice": 152.00,
  "lowPrice": 148.50,
  "closePrice": 150.25,
  "volume": 1000000,
  "timestamp": "2025-08-13T15:30:00Z",
  "timeInterval": "I1",
  "instrumentToken": "151064324"
}
```

### Performance Characteristics

**Market Data Client Service**:
- **Target Processing Time**: <25 seconds for 20,000 stocks
- **API Throughput**: 40+ parallel batches of 500 stocks
- **Kafka Publishing**: Asynchronous dual-topic publishing
- **Memory Usage**: Optimized for minimal footprint

**Market Data Service**:
- **Database Write Throughput**: 1000+ candles per batch
- **Query Performance**: Sub-second response for historical data
- **Storage Efficiency**: TimescaleDB compression for long-term storage
- **Cache Hit Ratio**: 90%+ for frequently accessed data

### Service Health Endpoints

#### GET /actuator/health (Both Services)
**Description**: Service health status

**Response (200)**:
```json
{
  "status": "UP",
  "components": {
    "kafka": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    },
    "db": {
      "status": "UP"
    }
  }
}
```

#### GET /actuator/metrics (Both Services)
**Description**: Service metrics for monitoring

**Key Metrics**:
- `market.data.fetch.duration`: API fetch time per batch
- `market.data.kafka.publish.rate`: Kafka publishing throughput
- `market.data.db.write.duration`: Database write performance
- `market.data.cache.hit.ratio`: Cache performance

---

