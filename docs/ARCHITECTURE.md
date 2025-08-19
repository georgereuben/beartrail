# BearTrail Architecture Documentation - Updated for Decoupled Design

## Overview

BearTrail implements a **hybrid microservices architecture** combining **event-driven real-time processing** (Apache Kafka) with **decoupled database services** for optimal performance and reliability. The system is designed for scalability, maintainability, and fault tolerance, providing realistic paper trading simulation with production-grade capabilities.

## System Architecture Decision: Decoupled Hybrid Approach

**Architecture**: Event-driven architecture (Kafka) for real-time processing + dedicated storage services for persistence and analytics.

### Core Architecture Benefits
- **Real-time order execution** via Kafka events (100-500ms latency)
- **Decoupled data persistence** via dedicated storage services
- **Loose coupling** between microservices
- **Independent scaling** of data fetching and storage layers
- **Event audit trail** for compliance and debugging
- **Separation of concerns** between data fetching and storage
- **Optimized performance** through specialized services

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                Frontend                                     │
│                     React Application + ApexCharts                         │
└─────────────────────────┬───────────────────────────────────────────────────┘
                          │
                          │ HTTP/REST + WebSocket
                          │
┌─────────────────────────▼───────────────────────────────────────────────────┐
│                           API Gateway                                      │
│                    Rate Limiting, Routing, Auth                            │
└─────────────────────────┬───────────────────────────────────────────────────┘
                          │
                          │
    ┌─────────────────────┼─────────────────────┬─────────────────────────────┐
    │                     │                     │                             │
    ▼                     ▼                     ▼                             ▼
┌─────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐
│ User        │  │ Market Data     │  │ Market Data     │  │ Order               │
│ Service     │  │ Client Service  │  │ Service         │  │ Service             │
│             │  │                 │  │                 │  │                     │
│ Auth & JWT  │  │ Upstox API      │  │ DB Operations   │  │ Trade Execution     │
│ User CRUD   │  │ Data Fetching   │  │ Query API       │  │ Order Types         │
│ OAuth2      │  │ Kafka Producer  │  │ TimescaleDB     │  │ Risk Checks         │
└─────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────────┘
    │                     │                     │                             │
    │                     │                     │                             │
    └─────────────────────┼─────────────────────┼─────────────────────────────┘
                          │                     │
    ┌─────────────────────┼─────────────────────┼─────────────────────┐
    │                     │                     │                     │
    ▼                     ▼                     ▼                     ▼
┌─────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ Portfolio   │  │ Leaderboard     │  │ Notification    │  │ Query Aggregator│
│ Service     │  │ Service         │  │ Service         │  │ Service         │
│             │  │                 │  │                 │  │                 │
│ Holdings    │  │ User Rankings   │  │ Multi-channel   │  │ Multi-DB Query  │
│ P&L Calc    │  │ Competitions    │  │ Alerts & Push   │  │ Caching Layer   │
└─────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘

                          │
                          ▼
          ┌───────────────────────────────────────┐
          │            Apache Kafka               │
          │         Event Streaming Hub           │
          │                                       │
          │  Topics: market-data-updates,         │
          │  market-data-persistence,             │
          │  order-events, portfolio-updates,     │
          │  user-notifications, leaderboard-     │
          │  updates, system-events               │
          └───────────────────────────────────────┘
```

## Kafka Event Streaming Architecture - Updated

### Core Topics Structure

| Topic | Producers | Consumers | Purpose |
|-------|-----------|-----------|---------|
| `market-data-updates` | Market Data Client Service | Order Service, Portfolio Service, Leaderboard Service | Real-time price updates |
| `market-data-persistence` | Market Data Client Service | Market Data Service | Candle data for persistence |
| `order-events` | Order Service | Portfolio Service, Leaderboard Service, User Service | Order lifecycle events |
| `portfolio-updates` | Portfolio Service | Leaderboard Service, User Service | Portfolio value changes |
| `user-notifications` | User Service, Order Service, Portfolio Service | User Service | User alerts and notifications |
| `leaderboard-updates` | Leaderboard Service | User Service | Ranking changes |
| `system-events` | All Services | Monitoring Service, Audit Service | System-wide events |
| `market-session-events` | Market Data Client Service | Order Service, Portfolio Service, Leaderboard Service | Market open/close events |

### Service-Level Event Flow - Updated

**Market Data Client Service**:
- Produces: `market-data-updates`, `market-data-persistence`, `market-session-events`, `system-events`
- Consumes: `system-events`

**Market Data Service**:
- Produces: `system-events`
- Consumes: `market-data-persistence`, `system-events`

**Order Service**:
- Produces: `order-events`, `user-notifications`, `system-events`
- Consumes: `market-data-updates`, `market-session-events`, `system-events`

**Portfolio Service**:
- Produces: `portfolio-updates`, `user-notifications`, `system-events`
- Consumes: `order-events`, `market-data-updates`, `system-events`

## Core Microservices - Updated

### 1. Market Data Client Service (New - Lightweight API Client)
**Purpose**: Pure Upstox API interfacing and real-time data distribution

**Responsibilities**:
- Upstox API integration for OHLCV candlestick data
- Parallel API processing and optimization
- Real-time price feed processing via Kafka events
- Market session management (open/close events)
- Data caching for immediate access
- **No database operations**

**Key Features**:
- **Focus**: Pure data fetching and real-time distribution
- **Performance**: Optimized for speed without database I/O
- **Scalability**: Can scale independently based on API throughput
- **Reliability**: Simplified with single responsibility

### 2. Market Data Service (Main Service - Database & Queries)
**Purpose**: Central market data repository and query serving

**Responsibilities**:
- Consuming market data from `market-data-persistence` topic
- Batch database writes for optimal performance
- Historical data queries and API endpoints
- TimescaleDB optimization and maintenance
- Data aggregation and continuous aggregates
- Query caching and optimization

**Data Format**: **Candlestick OHLCV** (Open, High, Low, Close, Volume)
- Industry-standard financial data format
- Optimal storage efficiency
- Perfect integration with Upstox API
- Enables realistic order execution simulation

**Storage Strategy**:
| Interval | Retention | Primary Use Case | Storage Impact |
|----------|-----------|------------------|----------------|
| **1 minute** | 30 days | Order execution validation | High |
| **5 minutes** | 90 days | Intraday analysis | Medium |
| **1 hour** | 1 year | Daily trading patterns | Low |
| **Daily** | 5+ years | Long-term backtesting | Very Low |

**Query Endpoints**:
- `GET /api/candles/{symbol}?interval={interval}&from={timestamp}&to={timestamp}`
- `GET /api/latest/{symbol}?interval={interval}`
- `GET /api/bulk/latest?symbols={symbol1,symbol2}&interval={interval}`
- `GET /api/aggregates/{symbol}?type={type}&period={period}`

### 3. Order Service (Updated)
**Purpose**: Real-time order execution simulation with conditional logic

**Order Types Supported**:
- **Market Orders**: Execute immediately at current market price
- **Limit Orders**: Execute when market price reaches user's specified price
- **Stop-Loss Orders**: Trigger when price falls below stop price
- **Take-Profit Orders**: Execute when price rises above target price

**Real-time Execution Logic**:
1. Market Data Client Service publishes price update to `market-data-updates`
2. Order Service consumes event and checks pending orders for the symbol
3. Orders meeting execution criteria are processed using candlestick high/low data
4. `order-events` published to trigger portfolio updates and notifications

**Updated Dependencies**:
- **Consumes**: `market-data-updates` (for real-time execution)
- **Queries**: Market Data Service (for historical validation)

## Benefits of Decoupled Architecture

### Performance Benefits
1. **Parallel Processing**: Market data fetching and storage happen independently
2. **Optimized Database Operations**: Main service can batch writes efficiently
3. **Reduced Latency**: Real-time updates don't wait for database operations
4. **Independent Scaling**: Scale services based on specific bottlenecks

### Maintainability Benefits
1. **Single Responsibility**: Each service has a clear, focused purpose
2. **Independent Development**: Teams can work on services independently
3. **Easier Testing**: Smaller, focused services are easier to test
4. **Technology Flexibility**: Can optimize each service with different technologies

### Reliability Benefits
1. **Fault Isolation**: Database issues don't affect real-time data flow
2. **Independent Recovery**: Services can restart independently
3. **Graceful Degradation**: System can continue with limited functionality
4. **Monitoring Simplicity**: Easier to identify bottlenecks and issues

## Data Flow Example

### Minute Candle Update Process
```
1. Market Data Client Service fetches from Upstox API
2. Market Data Client Service publishes to:
   - `market-data-updates` (real-time consumers)
   - `market-data-persistence` (storage)
3. Market Data Service:
   - Consumes from `market-data-persistence`
   - Batches writes to TimescaleDB
   - Updates continuous aggregates
4. Other services consume from `market-data-updates` for real-time processing
```

### Query Process
```
1. Frontend/Client requests historical data
2. API Gateway routes to Market Data Service
3. Market Data Service queries TimescaleDB with optimized indexes
4. Cached results returned to client
```

## Service Directory Structure

### Market Data Client Service
```
market-data-client/
├── src/main/java/com/beartrail/marketdataclient/
│   ├── MarketDataClientApplication.java
│   ├── client/upstox/UpstoxApiClient.java
│   ├── service/MarketDataFetchService.java
│   ├── producer/MarketDataKafkaProducer.java
│   ├── config/HttpClientConfig.java
│   └── scheduler/CandleUpdateScheduler.java
└── src/main/resources/
    └── application.yml
```

### Market Data Service (Main)
```
market-data/
├── src/main/java/com/beartrail/marketdata/
│   ├── MarketDataApplication.java
│   ├── consumer/KafkaMarketDataConsumer.java
│   ├── service/CandleStorageService.java
│   ├── controller/MarketDataQueryController.java
│   ├── repository/MarketDataRepository.java
│   └── config/DatabaseConfig.java
└── src/main/resources/
    ├── application.yml
    └── db/migration/
```

This decoupled architecture provides the foundation for the optimizations outlined in the updated gameplan, focusing each service on its core competency while maintaining system-wide performance and reliability.
