# BearTrail - Paper Trading Platform

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Latest-blue.svg)](https://kafka.apache.org/)
[![React](https://img.shields.io/badge/React-18+-61DAFB.svg)](https://reactjs.org/)

A production-grade paper trading platform that enables simulated equity trading with virtual funds, real-time market data integration, and competitive leaderboards.

## 🎯 Project Overview

BearTrail is a comprehensive simulated trading platform designed to provide users with realistic trading experiences without financial risk. The platform integrates with Upstox API for real-time market data and implements a microservices architecture for scalability and maintainability.

### Key Features

- **Virtual Trading**: Simulate equity trades with virtual currency
- **Real-time Market Data**: Integration with Upstox API for live market prices
- **Portfolio Management**: Track holdings, P&L, and performance metrics
- **Competitive Leaderboards**: Rank users based on portfolio performance
- **Order Management**: Support for various order types and validations
- **Real-time Notifications**: Alerts for order fills and portfolio changes
- **Analytics & Reporting**: Comprehensive trading analytics and reports

### Technology Stack

- **Backend**: Spring Boot 3.x, Java 17+
- **Message Broker**: Apache Kafka
- **Frontend**: React 18+
- **Database**: PostgreSQL (primary), Redis (caching)
- **Market Data**: Upstox API
- **Authentication**: JWT, OAuth2
- **Containerization**: Docker, Docker Compose
- **Build Tool**: Gradle

## 🏗️ Architecture

BearTrail follows a microservices architecture pattern with event-driven communication through Kafka. The system is designed for high availability, scalability, and maintainability.

### Core Services
- **User Management Service**: User authentication, JWT tokens, OAuth2, profiles and account management
- **Market Data Service**: Real-time price feeds from Upstox API
- **Order Simulation Service**: Trade order processing and validation
- **Portfolio Service**: Holdings and P&L calculations
- **Leaderboard Service**: User ranking and competition features

### Supporting Services
- **API Gateway**: Single entry point and request routing
- **Notification Service**: Email and push notifications
- **Analytics Service**: Trading metrics and insights
- **Reporting Service**: Report generation and exports
- **Admin & Audit Service**: System monitoring and user support

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Docker and Docker Compose
- Node.js 18+ (for frontend development)
- Gradle 7+

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd beartrail
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d kafka postgres redis
   ```

3. **Run individual microservices**
   ```bash
   # User service example
   cd user
   ./gradlew bootRun
   ```

4. **Access the application**
   - API Gateway: http://localhost:8080
   - User Service: http://localhost:8081
   - Market Data Service: http://localhost:8082

## 📁 Project Structure

```
beartrail/
├── README.md                    # This file
├── ARCHITECTURE.md              # Detailed architecture documentation
├── API_CONTRACTS.md             # Service API specifications
├── docs/                        # Additional documentation
├── docker-compose.yml           # Infrastructure services
├── postman/                     # API testing collections
├── user/                        # User Management Service
├── market-data/                 # Market Data Service (planned)
├── order-simulation/            # Order Simulation Service (planned)
├── portfolio/                   # Portfolio Service (planned)
├── leaderboard/                 # Leaderboard Service (planned)
├── api-gateway/                 # API Gateway Service (planned)
└── frontend/                    # React frontend (planned)
```

## 📋 Development Status

### ✅ Completed
- [x] User Management Service (Authentication, CRUD operations)
- [x] Basic project structure and documentation
- [x] Docker configuration for development
- [x] Postman collections for API testing

### 🚧 In Progress
- [ ] Market Data Service integration with Upstox API
- [ ] Order Simulation Service
- [ ] Portfolio Service

### 📝 Planned
- [ ] Leaderboard Service
- [ ] API Gateway implementation
- [ ] Frontend React application
- [ ] Notification Service
- [ ] Analytics and Reporting Services
- [ ] Admin Dashboard

## 🔧 Configuration

Each microservice can be configured through:
- `application.properties` - Default configuration
- `application-docker.properties` - Docker environment
- Environment variables for sensitive data

Key configuration areas:
- Database connections
- Kafka broker settings
- Upstox API credentials
- JWT secret keys
- Service discovery endpoints

## 🧪 Testing

The project includes comprehensive testing at multiple levels:
- **Unit Tests**: Individual component testing
- **Integration Tests**: Service integration testing
- **Contract Tests**: API contract validation
- **End-to-End Tests**: Complete workflow testing

Run tests for a specific service:
```bash
cd user
./gradlew test
```

## 📖 Documentation

- [Architecture Documentation](docs/ARCHITECTURE.md) - Detailed system design
- [API Contracts](docs/API_CONTRACTS.md) - Service interfaces and contracts
- [Postman Collections](./postman/) - API testing and examples
- [Service-specific READMEs](./user/README.md) - Individual service documentation

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For questions or support, please create an issue in the repository or contact the development team.

---

**Note**: This is a paper trading platform for educational and simulation purposes only. No real money or securities are involved in transactions.
