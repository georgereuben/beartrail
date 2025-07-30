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

## Market Data Service

**Base URL**: `http://localhost:8082/market`

### REST Endpoints

#### GET /market/instruments
**Description**: Get list of available trading instruments

**Query Parameters**:
- `exchange` (optional): Filter by exchange
- `sector` (optional): Filter by sector
- `limit` (optional): Number of results (default: 100)
- `offset` (optional): Pagination offset

**Response (200)**:
```json
{
  "instruments": [
    {
      "symbol": "AAPL",
      "name": "Apple Inc.",
      "exchange": "NASDAQ",
      "sector": "Technology",
      "currency": "USD",
      "lotSize": 1,
      "tickSize": 0.01,
      "isActive": true
    }
  ],
  "totalCount": 1000,
  "hasMore": true
}
```

#### GET /market/quote/{symbol}
**Description**: Get current quote for a symbol

**Path Parameters**:
- `symbol`: Trading symbol (e.g., AAPL)

**Response (200)**:
```json
{
  "symbol": "AAPL",
  "lastPrice": 150.25,
  "bid": 150.20,
  "ask": 150.30,
  "volume": 1000000,
  "change": 2.50,
  "changePercent": 1.69,
  "high": 152.00,
  "low": 148.50,
  "open": 149.00,
  "previousClose": 147.75,
  "timestamp": "2025-07-30T15:30:00Z"
}
```

#### GET /market/quotes
**Description**: Get quotes for multiple symbols

**Query Parameters**:
- `symbols`: Comma-separated list of symbols

**Response (200)**:
```json
{
  "quotes": [
    { /* quote object for each symbol */ }
  ]
}
```

#### GET /market/history/{symbol}
**Description**: Get historical price data

**Path Parameters**:
- `symbol`: Trading symbol

**Query Parameters**:
- `interval`: Time interval (1m, 5m, 15m, 1h, 1d)
- `from`: Start date (ISO 8601)
- `to`: End date (ISO 8601)

**Response (200)**:
```json
{
  "symbol": "AAPL",
  "interval": "1d",
  "data": [
    {
      "timestamp": "2025-07-30T00:00:00Z",
      "open": 149.00,
      "high": 152.00,
      "low": 148.50,
      "close": 150.25,
      "volume": 1000000
    }
  ]
}
```

### WebSocket Endpoints

#### WS /market/live
**Description**: Real-time price updates

**Subscribe Message**:
```json
{
  "action": "subscribe",
  "symbols": ["AAPL", "GOOGL", "MSFT"]
}
```

**Price Update Message**:
```json
{
  "type": "price_update",
  "symbol": "AAPL",
  "price": 150.25,
  "change": 2.50,
  "changePercent": 1.69,
  "volume": 1000000,
  "timestamp": "2025-07-30T15:30:00Z"
}
```

---

## Order Simulation Service

**Base URL**: `http://localhost:8083/orders`

### REST Endpoints

#### POST /orders
**Description**: Place a new order

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "symbol": "AAPL",
  "side": "BUY",
  "orderType": "LIMIT",
  "quantity": 100,
  "price": 150.00,
  "stopPrice": null,
  "timeInForce": "DAY",
  "clientOrderId": "string"
}
```

**Response (201)**:
```json
{
  "orderId": "uuid",
  "clientOrderId": "string",
  "status": "PENDING",
  "message": "Order placed successfully",
  "estimatedFees": 1.50
}
```

#### GET /orders
**Description**: Get user's orders

**Headers**: `Authorization: Bearer <token>`

**Query Parameters**:
- `status` (optional): Filter by order status
- `symbol` (optional): Filter by symbol
- `from` (optional): Start date
- `to` (optional): End date
- `limit` (optional): Number of results
- `offset` (optional): Pagination offset

**Response (200)**:
```json
{
  "orders": [
    {
      "orderId": "uuid",
      "clientOrderId": "string",
      "symbol": "AAPL",
      "side": "BUY",
      "orderType": "LIMIT",
      "quantity": 100,
      "filledQuantity": 50,
      "remainingQuantity": 50,
      "price": 150.00,
      "averageFillPrice": 149.95,
      "status": "PARTIALLY_FILLED",
      "timeInForce": "DAY",
      "createdAt": "2025-07-30T10:30:00Z",
      "updatedAt": "2025-07-30T10:35:00Z"
    }
  ],
  "totalCount": 25,
  "hasMore": true
}
```

#### GET /orders/{orderId}
**Description**: Get order details

**Headers**: `Authorization: Bearer <token>`

**Path Parameters**:
- `orderId`: Order UUID

**Response (200)**:
```json
{
  "orderId": "uuid",
  "clientOrderId": "string",
  "symbol": "AAPL",
  "side": "BUY",
  "orderType": "LIMIT",
  "quantity": 100,
  "filledQuantity": 100,
  "remainingQuantity": 0,
  "price": 150.00,
  "averageFillPrice": 149.98,
  "status": "FILLED",
  "timeInForce": "DAY",
  "fees": 1.50,
  "fills": [
    {
      "fillId": "uuid",
      "quantity": 50,
      "price": 149.95,
      "timestamp": "2025-07-30T10:32:00Z"
    },
    {
      "fillId": "uuid",
      "quantity": 50,
      "price": 150.01,
      "timestamp": "2025-07-30T10:33:00Z"
    }
  ],
  "createdAt": "2025-07-30T10:30:00Z",
  "updatedAt": "2025-07-30T10:33:00Z"
}
```

#### DELETE /orders/{orderId}
**Description**: Cancel an order

**Headers**: `Authorization: Bearer <token>`

**Path Parameters**:
- `orderId`: Order UUID

**Response (200)**:
```json
{
  "message": "Order cancelled successfully",
  "orderId": "uuid",
  "status": "CANCELLED"
}
```

#### GET /orders/trades
**Description**: Get user's trade history

**Headers**: `Authorization: Bearer <token>`

**Query Parameters**: Same as orders endpoint

**Response (200)**:
```json
{
  "trades": [
    {
      "tradeId": "uuid",
      "orderId": "uuid",
      "symbol": "AAPL",
      "side": "BUY",
      "quantity": 100,
      "price": 149.98,
      "fees": 1.50,
      "timestamp": "2025-07-30T10:33:00Z"
    }
  ],
  "totalCount": 50,
  "hasMore": true
}
```

---

## Portfolio Service

**Base URL**: `http://localhost:8084/portfolio`

### REST Endpoints

#### GET /portfolio
**Description**: Get user's portfolio summary

**Headers**: `Authorization: Bearer <token>`

**Response (200)**:
```json
{
  "userId": "uuid",
  "totalValue": 105000.00,
  "cashBalance": 25000.00,
  "investedAmount": 80000.00,
  "totalPnL": 5000.00,
  "dayPnL": 250.00,
  "totalPnLPercent": 5.00,
  "dayPnLPercent": 0.25,
  "updatedAt": "2025-07-30T15:30:00Z"
}
```

#### GET /portfolio/holdings
**Description**: Get user's current holdings

**Headers**: `Authorization: Bearer <token>`

**Response (200)**:
```json
{
  "holdings": [
    {
      "symbol": "AAPL",
      "quantity": 100,
      "averagePrice": 149.50,
      "currentPrice": 150.25,
      "marketValue": 15025.00,
      "unrealizedPnL": 75.00,
      "unrealizedPnLPercent": 0.50,
      "dayPnL": 25.00,
      "dayPnLPercent": 0.17,
      "updatedAt": "2025-07-30T15:30:00Z"
    }
  ],
  "totalMarketValue": 80000.00,
  "totalUnrealizedPnL": 5000.00
}
```

#### GET /portfolio/performance
**Description**: Get portfolio performance metrics

**Headers**: `Authorization: Bearer <token>`

**Query Parameters**:
- `period`: Performance period (1d, 1w, 1m, 3m, 6m, 1y, all)

**Response (200)**:
```json
{
  "period": "1m",
  "startValue": 100000.00,
  "endValue": 105000.00,
  "totalReturn": 5000.00,
  "totalReturnPercent": 5.00,
  "annualizedReturn": 60.00,
  "volatility": 15.50,
  "sharpeRatio": 3.87,
  "maxDrawdown": -2.50,
  "winRate": 65.00,
  "profitFactor": 1.85,
  "dailyReturns": [
    {
      "date": "2025-07-01",
      "value": 100000.00,
      "return": 0.00,
      "returnPercent": 0.00
    }
  ]
}
```

#### GET /portfolio/analytics
**Description**: Get detailed portfolio analytics

**Headers**: `Authorization: Bearer <token>`

**Response (200)**:
```json
{
  "diversification": {
    "sectorAllocation": [
      {
        "sector": "Technology",
        "value": 40000.00,
        "percentage": 50.00
      }
    ],
    "concentrationRisk": "MEDIUM",
    "herfindahlIndex": 0.35
  },
  "riskMetrics": {
    "portfolioBeta": 1.15,
    "valueAtRisk": -2500.00,
    "expectedShortfall": -3200.00,
    "correlation": 0.75
  },
  "tradingMetrics": {
    "totalTrades": 125,
    "winningTrades": 81,
    "losingTrades": 44,
    "averageWin": 150.00,
    "averageLoss": -75.00,
    "largestWin": 850.00,
    "largestLoss": -320.00
  }
}
```

---

## Leaderboard Service

**Base URL**: `http://localhost:8085/leaderboard`

### REST Endpoints

#### GET /leaderboard/global
**Description**: Get global leaderboard

**Query Parameters**:
- `period`: Ranking period (daily, weekly, monthly, all-time)
- `limit`: Number of results (default: 100)
- `offset`: Pagination offset

**Response (200)**:
```json
{
  "period": "monthly",
  "leaderboard": [
    {
      "rank": 1,
      "userId": "uuid",
      "username": "TraderPro",
      "totalReturn": 15000.00,
      "totalReturnPercent": 15.00,
      "portfolioValue": 115000.00,
      "isCurrentUser": false
    }
  ],
  "currentUser": {
    "rank": 25,
    "totalReturn": 5000.00,
    "totalReturnPercent": 5.00,
    "portfolioValue": 105000.00
  },
  "totalParticipants": 1000,
  "updatedAt": "2025-07-30T15:30:00Z"
}
```

#### GET /leaderboard/competitions
**Description**: Get active competitions

**Response (200)**:
```json
{
  "competitions": [
    {
      "competitionId": "uuid",
      "name": "Monthly Trading Challenge",
      "description": "Best monthly return wins",
      "startDate": "2025-07-01T00:00:00Z",
      "endDate": "2025-07-31T23:59:59Z",
      "participants": 500,
      "prizePool": "$1000",
      "isActive": true,
      "userParticipating": true,
      "userRank": 15
    }
  ]
}
```

#### POST /leaderboard/competitions/{competitionId}/join
**Description**: Join a competition

**Headers**: `Authorization: Bearer <token>`

**Path Parameters**:
- `competitionId`: Competition UUID

**Response (200)**:
```json
{
  "message": "Successfully joined competition",
  "competitionId": "uuid",
  "startingRank": 250
}
```

---

## Notification Service

**Base URL**: `http://localhost:8086/notifications`

### REST Endpoints

#### GET /notifications
**Description**: Get user notifications

**Headers**: `Authorization: Bearer <token>`

**Query Parameters**:
- `type`: Filter by notification type
- `read`: Filter by read status (true/false)
- `limit`: Number of results
- `offset`: Pagination offset

**Response (200)**:
```json
{
  "notifications": [
    {
      "notificationId": "uuid",
      "type": "ORDER_FILLED",
      "title": "Order Executed",
      "message": "Your buy order for 100 AAPL shares has been filled at $150.25",
      "data": {
        "orderId": "uuid",
        "symbol": "AAPL",
        "quantity": 100,
        "price": 150.25
      },
      "isRead": false,
      "createdAt": "2025-07-30T10:33:00Z"
    }
  ],
  "unreadCount": 5,
  "totalCount": 25
}
```

#### PUT /notifications/{notificationId}/read
**Description**: Mark notification as read

**Headers**: `Authorization: Bearer <token>`

**Path Parameters**:
- `notificationId`: Notification UUID

**Response (200)**:
```json
{
  "message": "Notification marked as read"
}
```

#### PUT /notifications/read-all
**Description**: Mark all notifications as read

**Headers**: `Authorization: Bearer <token>`

**Response (200)**:
```json
{
  "message": "All notifications marked as read",
  "count": 5
}
```

---

## Kafka Event Contracts

### Market Data Events

#### Topic: `market-data.prices`
**Description**: Real-time price updates

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "PRICE_UPDATE",
  "timestamp": "2025-07-30T15:30:00Z",
  "source": "market-data-service",
  "data": {
    "symbol": "AAPL",
    "price": 150.25,
    "bid": 150.20,
    "ask": 150.30,
    "volume": 1000000,
    "change": 2.50,
    "changePercent": 1.69
  }
}
```

#### Topic: `market-data.instruments`
**Description**: Instrument lifecycle events

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "INSTRUMENT_ADDED|INSTRUMENT_UPDATED|INSTRUMENT_DELISTED",
  "timestamp": "2025-07-30T15:30:00Z",
  "source": "market-data-service",
  "data": {
    "symbol": "AAPL",
    "name": "Apple Inc.",
    "exchange": "NASDAQ",
    "status": "ACTIVE"
  }
}
```

### Order Events

#### Topic: `orders.lifecycle`
**Description**: Order state changes

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "ORDER_CREATED|ORDER_FILLED|ORDER_CANCELLED|ORDER_REJECTED",
  "timestamp": "2025-07-30T15:30:00Z",
  "source": "order-simulation-service",
  "data": {
    "orderId": "uuid",
    "userId": "uuid",
    "symbol": "AAPL",
    "side": "BUY",
    "orderType": "LIMIT",
    "quantity": 100,
    "price": 150.00,
    "status": "FILLED",
    "fillPrice": 149.98,
    "fillQuantity": 100,
    "fees": 1.50
  }
}
```

### Portfolio Events

#### Topic: `portfolio.updates`
**Description**: Portfolio value changes

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "PORTFOLIO_UPDATED",
  "timestamp": "2025-07-30T15:30:00Z",
  "source": "portfolio-service",
  "data": {
    "userId": "uuid",
    "totalValue": 105000.00,
    "cashBalance": 25000.00,
    "totalPnL": 5000.00,
    "dayPnL": 250.00,
    "holdings": [
      {
        "symbol": "AAPL",
        "quantity": 100,
        "marketValue": 15025.00,
        "unrealizedPnL": 75.00
      }
    ]
  }
}
```

### User Events

#### Topic: `users.lifecycle`
**Description**: User account events

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "USER_REGISTERED|USER_VERIFIED|USER_UPDATED|USER_DELETED",
  "timestamp": "2025-07-30T15:30:00Z",
  "source": "user-management-service",
  "data": {
    "userId": "uuid",
    "username": "string",
    "email": "string"
  }
}
```

### Notification Events

#### Topic: `notifications.outbound`
**Description**: Notification delivery requests

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "SEND_NOTIFICATION",
  "timestamp": "2025-07-30T15:30:00Z",
  "source": "order-simulation-service",
  "data": {
    "userId": "uuid",
    "type": "ORDER_FILLED",
    "channels": ["EMAIL", "PUSH"],
    "title": "Order Executed",
    "message": "Your buy order for 100 AAPL shares has been filled",
    "data": {
      "orderId": "uuid",
      "symbol": "AAPL"
    }
  }
}
```

## Error Response Format

All services use a standardized error response format:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters",
    "details": [
      {
        "field": "quantity",
        "message": "Quantity must be greater than 0"
      }
    ],
    "timestamp": "2025-07-30T15:30:00Z",
    "path": "/orders",
    "requestId": "uuid"
  }
}
```

## Common HTTP Status Codes

- `200 OK`: Successful request
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request parameters
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict (e.g., duplicate order)
- `422 Unprocessable Entity`: Validation error
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server error
- `503 Service Unavailable`: Service temporarily unavailable

This API contract serves as the definitive guide for service integration and client development.
