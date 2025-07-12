# BearTrail Architecture Documentation

## Overview

BearTrail implements a **hybrid microservices architecture** combining **event-driven real-time processing** (Apache Kafka) with **database-centric persistence** for optimal performance and reliability. The system is designed for scalability, maintainability, and fault tolerance, providing realistic paper trading simulation with production-grade capabilities.

## System Architecture Decision: Hybrid Approach

**Architecture**: Event-driven architecture (Kafka) for real-time processing + database-centric storage for persistence and analytics.

### Core Architecture Benefits
- **Real-time order execution** via Kafka events (100-500ms latency)
- **Data persistence** via database for historical analysis and safety nets
- **Loose coupling** between microservices
- **Scalability** through independent service scaling
- **Event audit trail** for compliance and debugging

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
    ┌─────────────────────┼─────────────────────┐
    │                     │                     │
    ▼                     ▼                     ▼
┌─────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ User        │  │ Market Data     │  │ Order           │
│ Service     │  │ Service         │  │ Service         │
│             │  │                 │  │                 │
│ Auth & JWT  │  │ Upstox API      │  │ Trade Execution │
��� User CRUD   │  │ Candlestick     │  │ Order Types     │
│ OAuth2      │  │ Data Storage    │  │ Risk Checks     │
└─────────────┘  └─────────────────┘  └─────────────────┘
    │                     │                     │
    │                     │                     │
    └─────────────────────┼─────────────────────┘
                          │
    ┌─────────────────────┼─────────────────────┐
    │                     │                     │
    ▼                     ▼                     ▼
┌─────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ Portfolio   │  │ Leaderboard     │  │ Notification    │
│ Service     │  │ Service         │  │ Service         │
│             │  │                 │  │                 │
│ Holdings    │  │ User Rankings   │  │ Multi-channel   │
│ P&L Calc    │  │ Competitions    │  │ Alerts & Push   │
└─────────────┘  └─────────────────┘  └─────────────────┘

                          │
                          ▼
          ┌───────────────────────────────────────┐
          │            Apache Kafka               │
          │         Event Streaming Hub           │
          │                                       │
          │  Topics: market-data-updates,         │
          │  order-events, portfolio-updates,     │
          │  user-notifications, leaderboard-     │
          │  updates, system-events               │
          └───────────────────────────────────────┘
```

## Kafka Event Streaming Architecture

### Core Topics Structure

| Topic | Producers | Consumers | Purpose |
|-------|-----------|-----------|---------|
| `market-data-updates` | Market Data Service | Order Service, Portfolio Service, Leaderboard Service | Real-time price updates |
| `order-events` | Order Service | Portfolio Service, Leaderboard Service, User Service | Order lifecycle events |
| `portfolio-updates` | Portfolio Service | Leaderboard Service, User Service | Portfolio value changes |
| `user-notifications` | User Service, Order Service, Portfolio Service | User Service | User alerts and notifications |
| `leaderboard-updates` | Leaderboard Service | User Service | Ranking changes |
| `system-events` | All Services | Monitoring Service, Audit Service | System-wide events |
| `market-session-events` | Market Data Service | Order Service, Portfolio Service, Leaderboard Service | Market open/close events |
| `dead-letter-queue` | All Services | Dead Letter Queue Handler | Failed message processing |

### Service-Level Event Flow

**Market Data Service**:
- Produces: `market-data-updates`, `market-session-events`, `system-events`
- Consumes: `system-events`

**Order Service**:
- Produces: `order-events`, `user-notifications`, `system-events`
- Consumes: `market-data-updates`, `market-session-events`, `system-events`

**Portfolio Service**:
- Produces: `portfolio-updates`, `user-notifications`, `system-events`
- Consumes: `order-events`, `market-data-updates`, `system-events`

**Leaderboard Service**:
- Produces: `leaderboard-updates`, `system-events`
- Consumes: `portfolio-updates`, `order-events`, `market-data-updates`, `market-session-events`, `system-events`

**User Service**:
- Produces: `user-notifications`, `system-events`
- Consumes: `order-events`, `portfolio-updates`, `leaderboard-updates`, `user-notifications`, `system-events`

## Core Microservices

### 1. Market Data Service
**Purpose**: Real-time market data ingestion, candlestick data storage, and distribution

**Responsibilities**:
- Upstox API integration for OHLCV candlestick data
- Multi-tier storage (1min, 5min, 1hr, daily intervals)
- Real-time price feed processing via Kafka events
- Market session management (open/close events)
- Data caching and intelligent query routing
- WebSocket connections for live frontend updates

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

### 2. Order Service
**Purpose**: Real-time order execution simulation with conditional logic

**Order Types Supported**:
- **Market Orders**: Execute immediately at current market price
- **Limit Orders**: Execute when market price reaches user's specified price
- **Stop-Loss Orders**: Trigger when price falls below stop price
- **Take-Profit Orders**: Execute when price rises above target price

**Real-time Execution Logic**:
1. Market Data Service publishes price update to `market-data-updates`
2. Order Service consumes event and checks pending orders for the symbol
3. Orders meeting execution criteria are processed using candlestick high/low data
4. `order-events` published to trigger portfolio updates and notifications

### 3. Portfolio Service
**Purpose**: Portfolio management, P&L calculations, and position tracking

**Responsibilities**:
- Holdings tracking and position management
- Real-time P&L calculations based on market data events
- Portfolio valuation using latest market prices
- Risk metrics computation
- Event-driven portfolio snapshots

### 4. Leaderboard Service
**Purpose**: Real-time user ranking and competition management

**Responsibilities**:
- Real-time portfolio performance ranking via portfolio events
- Competition management and achievement tracking
- Social features and leaderboard calculations
- Event-driven ranking updates

### 5. User Service
**Purpose**: User management, authentication, and notification orchestration

**Responsibilities**:
- User profile CRUD operations and authentication
- JWT token issuance and validation
- OAuth2 integration (Google, GitHub)
- Event-driven notification routing
- User preference management

## Data Storage Strategy

### Candlestick Data Management
**Primary Storage**: 1-minute candlestick intervals as optimal balance between granularity and storage efficiency.

**Data Lifecycle Management**:
- **Automated aggregation** from 1-minute to higher timeframes
- **Scheduled cleanup** of old data with archival to cold storage
- **Database partitioning** by trading date for query performance
- **Compression** for historical data partitions

### Query Optimization Strategy
**Intelligent query routing** based on time range:
- ≤1 day: Query 1-minute candles
- ≤7 days: Query 5-minute candles
- ≤30 days: Query hourly candles
- >30 days: Query daily candles

### Database Strategy
- **PostgreSQL**: Primary transactional data and candlestick storage
- **Redis**: Caching and real-time data
- **Kafka**: Event streaming and audit trail

## Hybrid Implementation Strategy

### Event-Driven Components
- **Real-time order execution** via Kafka event consumption
- **Live leaderboard updates** via portfolio event streams
- **User notifications** via event-triggered messaging
- **Market session management** via market open/close events

### Database-Driven Components
- **Historical candlestick data storage** in OHLCV format
- **Complex analytical queries** for portfolio analysis
- **Batch safety nets** for missed event processing
- **End-of-day settlements** and report generation

## Production-Grade Configuration

### Kafka Configuration
- **ACKS=all** for guaranteed message delivery
- **Idempotence enabled** to prevent duplicate processing
- **Topic partitioning** by symbol for parallel processing
- **Dead letter queues** for failed message handling

### Database Optimization
- **Table partitioning** by trading date
- **Composite indexes** on (symbol, timestamp)
- **Connection pooling** for concurrent access
- **Read replicas** for analytical workloads

### Monitoring and Observability
- **Event processing latency** metrics
- **Order execution timing** tracking
- **Database query performance** monitoring
- **Kafka lag** and throughput metrics

## Integration Points

### Upstox API Integration
- **Fetch candlestick data** in standard OHLCV format
- **Real-time price updates** published to Kafka
- **Market session events** for trading hours management
- **Error handling** for API rate limits and downtime

### Frontend Integration
- **Candlestick chart rendering** using ApexCharts
- **Real-time price updates** via WebSocket connections
- **Order status updates** via event-driven notifications
- **Portfolio performance** charts with multi-timeframe support

## Key Architectural Principles Applied

1. **Event-Driven Design**: Real-time processing via Kafka event streams
2. **Hybrid Persistence**: Events for speed, database for reliability
3. **Service Decoupling**: Independent microservice evolution
4. **Data Lifecycle Management**: Automated aggregation and archival
5. **Industry Standards**: Candlestick format for financial data
6. **Production Readiness**: Monitoring, error handling, scalability
7. **Query Performance**: Intelligent routing based on time ranges
8. **Realistic Simulation**: Order execution logic using candle high/low data

This architecture provides BearTrail with a **production-grade foundation** that balances real-time performance with data reliability, enabling realistic paper trading simulation while maintaining scalability and observability for future growth.
