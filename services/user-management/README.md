# User Service

A Spring Boot microservice for user authentication and management in the TraderSim application.

## Overview

The User Service provides secure user registration, authentication, and management capabilities with JWT-based authentication. It serves as the identity provider for the TraderSim ecosystem.

## API Endpoints

### Authentication Endpoints

#### POST `/api/auth/register`
Register a new user account.

**Request Body:**
```json
{
  "firstName": "string (required)",
  "lastName": "string (required)", 
  "email": "string (required, valid email)",
  "password": "string (required)"
}
```

**Response:**
- **200 OK** - Registration successful
- **409 CONFLICT** - User already exists
- **400 BAD REQUEST** - Invalid input data

**Success Response Body:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "userId": 123,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}
```

#### POST `/api/auth/login`
Authenticate user and receive JWT tokens.

**Request Body:**
```json
{
  "email": "string (required, valid email)",
  "password": "string (required)"
}
```

**Response:**
- **200 OK** - Authentication successful
- **401 UNAUTHORIZED** - Invalid credentials

**Success Response Body:**
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "expiresAt": "2025-07-31T12:00:00",
  "userId": 123,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "roles": ["ROLE_USER"],
  "emailVerified": false,
  "enabled": true
}
```

## Data Models

### User Roles
- `ROLE_USER` - Standard user permissions
- `ROLE_ADMIN` - Administrative permissions

### User Properties
- **id** - Unique user identifier (Long)
- **firstName** - User's first name
- **lastName** - User's last name  
- **email** - Unique email address (used for login)
- **roles** - Set of assigned roles
- **enabled** - Account status (boolean)
- **locked** - Account lock status (boolean)
- **emailVerified** - Email verification status (boolean)
- **credentialsExpired** - Password expiry status (boolean)

## Authentication & Security

### JWT Configuration
- **Access Token Expiry:** 24 hours (86400 seconds)
- **Refresh Token Expiry:** 7 days (604800 seconds)
- **Token Type:** Bearer
- **Algorithm:** HS256

### Security Features
- Password encryption using Spring Security
- JWT-based stateless authentication
- Email validation
- Input validation with Bean Validation
- Global exception handling

## Database

**Technology:** PostgreSQL
**Default Connection:**
- Host: localhost:5432
- Database: tradersim
- Schema: Auto-managed via Hibernate DDL

### Tables
- `users` - Main user information
- `roles` - Available system roles
- `user_roles` - Many-to-many relationship between users and roles

## Configuration

### Environment Profiles
- **Default** - Local development with PostgreSQL
- **Docker** - Containerized deployment configuration
- **Test** - In-memory H2 database for testing

### Key Configuration Properties
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/tradersim
spring.datasource.username=admin
spring.datasource.password=admin

# JWT
jwt.secret=myVerySecretKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm
jwt.expiration=86400
jwt.refresh-expiration=604800
```

## Technology Stack

- **Framework:** Spring Boot 3.5.3
- **Java:** JDK 21
- **Database:** PostgreSQL (Production), H2 (Testing)
- **Security:** Spring Security with JWT
- **Build Tool:** Gradle
- **Container:** Docker support included

## Integration Points

### Outbound Dependencies
- PostgreSQL database for user persistence
- Kafka for event publishing (configured but not actively used)

### Inbound Dependencies
External services can authenticate users by:
1. Validating JWT tokens issued by this service
2. Using the provided authentication endpoints

## Deployment

### Docker
```bash
# Build the application
./gradlew build

# Run with Docker Compose
docker-compose up
```

### Standalone
```bash
# Run locally
./gradlew bootRun
```

## Health & Monitoring

Spring Boot Actuator endpoints are available for health checks and monitoring.

## Error Handling

The service provides standardized error responses:
- **400** - Bad Request (validation errors)
- **401** - Unauthorized (authentication failed)
- **409** - Conflict (user already exists)
- **500** - Internal Server Error

All error responses include descriptive messages for debugging and user feedback.
