# BearTrail Architecture Documentation

## Overview

BearTrail implements a microservices architecture designed for scalability, maintainability, and fault tolerance. The system follows event-driven patterns with Apache Kafka as the central message broker, enabling loose coupling between services.

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                Frontend                                     │
│                            React Application                                │
└─────────────────────────┬───────────────────────────────────────────────────┘
                          │
                          │ HTTP/REST
                          │
┌─────────────────────────▼───────────────────────────────────────────────────┐
│                           API Gateway                                      │
│                    Rate Limiting, Routing, Auth                            │
└─────────────────────────┬───────────────────────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          │               │               │
          ▼               ▼               ▼
┌─────────────────┐ ┌─────────────┐ ┌─────────────────┐
│  User Management│ │  Market Data│ │  Order Sim      │
│    Service      │ │   Service   │ │    Service      │
│                 │ │             │ │                 │
│ Auth & JWT      │ │ Upstox API  │ │ Trade Execution │
│ User CRUD       │ │ Price Feeds │ │ Order Validation│
│ OAuth2 & Profiles│ │ WebSocket   │ │ Risk Checks     │
└─────────────────┘ └─────────────┘ └─────────────────┘
          │               │               │
          │               │               │
          └───────────────┼───────────────┘
                          │
          ┌───────────────┼───────────────┐
          │               │               │
          ▼               ▼               ▼
┌─────────────────┐ ┌─────────────┐ ┌─────────────────┐
│   Portfolio     │ │ Leaderboard │ │  Notification   │
│    Service      │ │   Service   │ │    Service      │
│                 │ │             │ │                 │
│ Holdings        │ │ User Rankings│ │ Multi-channel   │
│ P&L Calc        │ │ Competitions │ │ Alerts & Push   │
└─────────────────┘ └─────────────┘ └─────────────────┘
```

## Core Microservices

### 1. User Management Service
**Purpose**: User profile and account management, including authentication and authorization

**Responsibilities**:
- User profile CRUD operations
- Account settings and preferences
- User onboarding workflows
- Profile picture and document management
- JWT token issuance and validation
- OAuth2 integration (Google, GitHub)
- Role-based access control (RBAC)
- Session management
- Password policies and security

**Technology Stack**:
- Spring Boot
- Spring Data JPA
- File storage (AWS S3 or local)
- Image processing libraries
- Spring Security
- JWT libraries
- OAuth2 client libraries
- BCrypt for password hashing

**Database Schema**:
```sql
users_auth (
    user_id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255),
    roles TEXT[],
    is_active BOOLEAN,
    last_login TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
)

oauth_providers (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users_auth(user_id),
    provider VARCHAR(50),
    provider_user_id VARCHAR(100),
    access_token TEXT,
    refresh_token TEXT,
    expires_at TIMESTAMP
)

user_profiles (
    user_id UUID PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    date_of_birth DATE,
    country VARCHAR(50),
    avatar_url VARCHAR(255),
    preferences JSONB,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
)

user_settings (
    user_id UUID PRIMARY KEY REFERENCES user_profiles(user_id),
    email_notifications BOOLEAN,
    push_notifications BOOLEAN,
    trading_notifications BOOLEAN,
    theme VARCHAR(20),
    language VARCHAR(10),
    timezone VARCHAR(50)
)
```

### 2. Market Data Service
**Purpose**: Real-time market data ingestion and distribution

**Responsibilities**:
- Upstox API integration
- Real-time price feed processing
- Market data normalization
- Data caching and storage
- WebSocket connections for live updates

**Technology Stack**:
- Spring Boot
- WebSocket support
- Upstox Java SDK
- Redis for caching
- Kafka for event publishing

**Data Flow**:
1. Fetch market data from Upstox API
2. Normalize and validate data
3. Cache in Redis for fast access
4. Publish events to Kafka topics
5. Serve data via REST API and WebSocket

**Kafka Topics**:
- `market-data.prices` - Real-time price updates
- `market-data.orders` - Order book updates
- `market-data.trades` - Trade executions

### 3. Order Simulation Service
**Purpose**: Simulated trade order processing

**Responsibilities**:
- Order validation and business rules
- Order matching simulation
- Trade execution logic
- Order status management
- Risk management checks

**Technology Stack**:
- Spring Boot
- State machine for order lifecycle
- Kafka for event processing
- Redis for order book simulation

**Order Types Supported**:
- Market Orders
- Limit Orders
- Stop Loss Orders
- Stop Limit Orders

**Order States**:
```
PENDING → VALIDATED → QUEUED → PARTIALLY_FILLED → FILLED
                    ↘         ↘
                    REJECTED   CANCELLED
```

### 4. Portfolio Service
**Purpose**: Portfolio management and P&L calculations

**Responsibilities**:
- Holdings tracking
- Profit/Loss calculations
- Portfolio valuation
- Position management
- Risk metrics computation

**Technology Stack**:
- Spring Boot
- Complex event processing
- Time series data handling
- Mathematical libraries for calculations

**Database Schema**:
```sql
portfolios (
    user_id UUID PRIMARY KEY,
    total_value DECIMAL(15,2),
    cash_balance DECIMAL(15,2),
    invested_amount DECIMAL(15,2),
    total_pnl DECIMAL(15,2),
    day_pnl DECIMAL(15,2),
    updated_at TIMESTAMP
)

holdings (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES portfolios(user_id),
    symbol VARCHAR(20),
    quantity INTEGER,
    average_price DECIMAL(10,2),
    current_price DECIMAL(10,2),
    market_value DECIMAL(15,2),
    unrealized_pnl DECIMAL(15,2),
    updated_at TIMESTAMP
)
```

### 5. Leaderboard Service
**Purpose**: User ranking and competition management

**Responsibilities**:
- Portfolio performance ranking
- Competition management
- Leaderboard calculations
- Achievement tracking
- Social features

**Technology Stack**:
- Spring Boot
- Redis for leaderboard storage
- Scheduled jobs for ranking updates
- WebSocket for real-time updates

## Supporting Services

### API Gateway
**Purpose**: Single entry point for all client requests

**Features**:
- Request routing to microservices
- Rate limiting and throttling
- Authentication middleware
- Request/response transformation
- Load balancing
- Circuit breaker pattern

**Technology**: Spring Cloud Gateway

### Notification Service
**Purpose**: Multi-channel notification delivery

**Channels**:
- Email notifications
- Push notifications
- In-app notifications
- SMS (future)

**Events**:
- Order fill notifications
- Portfolio milestone alerts
- Leaderboard position changes
- System announcements

### Analytics Service
**Purpose**: Trading analytics and insights

**Features**:
- Trading pattern analysis
- Performance metrics
- Risk assessment
- Market correlation analysis
- Behavioral analytics

### Admin & Audit Service
**Purpose**: System administration and compliance

**Features**:
- User management dashboard
- System health monitoring
- Audit trail tracking
- Feature flag management
- Support ticket system

## Data Management

### Database Strategy
- **PostgreSQL**: Primary transactional data
- **Redis**: Caching and session storage
- **InfluxDB**: Time-series market data
- **Elasticsearch**: Logging and search

### Event Sourcing
Key entities use event sourcing pattern:
- Order lifecycle events
- Portfolio changes
- User actions

### CQRS Implementation
Separate read and write models for:
- Portfolio queries vs updates
- Order history vs active orders
- User profiles vs authentication

## Security Architecture

### Authentication Flow
1. Client requests authentication
2. Authentication service validates credentials
3. JWT token issued with claims
4. Token validation on each request
5. Role-based authorization

### Data Protection
- Encryption at rest and in transit
- PII data anonymization
- Secure API key management
- Regular security audits

## Scalability Patterns

### Horizontal Scaling
- Stateless service design
- Database sharding strategies
- Load balancer configuration
- Auto-scaling policies

### Performance Optimization
- Redis caching layers
- Database connection pooling
- Async processing with Kafka
- CDN for static assets

## Monitoring and Observability

### Metrics Collection
- Application metrics (Micrometer)
- System metrics (Prometheus)
- Business metrics (custom)

### Logging Strategy
- Structured logging (JSON)
- Centralized log aggregation
- Log level management
- Sensitive data masking

### Distributed Tracing
- Request correlation IDs
- Service-to-service tracing
- Performance bottleneck identification

## Deployment Architecture

### Container Strategy
- Docker containers for each service
- Multi-stage builds for optimization
- Health check implementations
- Resource limit configurations

### Orchestration
- Docker Compose for development
- Kubernetes for production (planned)
- Service mesh for advanced traffic management

### CI/CD Pipeline
1. Code commit triggers build
2. Automated testing execution
3. Container image creation
4. Security scanning
5. Deployment to staging
6. Production deployment approval

## Disaster Recovery

### Backup Strategy
- Database daily backups
- Point-in-time recovery capability
- Cross-region backup replication
- Configuration backup

### High Availability
- Multi-zone deployment
- Database clustering
- Load balancer redundancy
- Circuit breaker patterns

This architecture provides a solid foundation for a production-grade paper trading platform that can scale with user growth while maintaining high availability and performance.
