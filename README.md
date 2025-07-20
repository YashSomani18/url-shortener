# URL Shortener Backend Application

A Spring Boot application that provides URL shortening functionality with Redis caching and PostgreSQL database storage.

## Features

- **URL Shortening**: Create short URLs from long URLs
- **User Management**: Register and manage users with JWT authentication
- **Advanced Analytics**: Track click statistics, device info, geolocation, and user behavior
- **Redis Caching**: Fast URL lookups with Redis
- **URL Expiration**: Set expiration dates for URLs
- **Scheduled Cleanup**: Automatic cleanup of expired URLs
- **Comprehensive Click Tracking**: Detailed analytics including device type, browser, location, UTM parameters
- **UUID Primary Keys**: Secure, globally unique identifiers
- **Database Migrations**: Liquibase-managed schema evolution
- **Comprehensive Testing**: Unit tests for controllers, repositories, models, and DTOs

## Technology Stack

- **Spring Boot 3.5.3** - Main framework
- **Java 21** - Programming language
- **PostgreSQL** - Primary database
- **Redis** - Caching layer
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **Lombok** - Reducing boilerplate code
- **Liquibase** - Database migrations
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **H2 Database** - In-memory database for testing

## Prerequisites

- Java 21 or higher
- PostgreSQL 12 or higher
- Redis 6.0 or higher
- Maven 3.6 or higher

## Setup Instructions

### 1. Database Setup

Start PostgreSQL and create a database:
```sql
CREATE DATABASE url_shortener;
```

### 2. Redis Setup

Start Redis server:
```bash
redis-server
```

### 3. Application Configuration

Update `src/main/resources/application.properties` with your database and Redis credentials:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/url_shortener
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### 4. Build and Run

```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### URL Management

#### Create Short URL
```http
POST /api/urls
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "originalUrl": "https://example.com/very/long/url",
  "title": "My Short URL",
  "description": "Description of the URL",
  "expiresAt": "2024-12-31T23:59:59"
}
```

#### Get URL Information
```http
GET /api/urls/{shortCode}
```

#### Get User's URLs
```http
GET /api/urls/user/{username}
Authorization: Bearer {jwt_token}
```

#### Deactivate URL
```http
DELETE /api/urls/{urlKey}?username={username}
Authorization: Bearer {jwt_token}
```

#### Get URL Statistics
```http
GET /api/urls/stats/{shortCode}
Authorization: Bearer {jwt_token}
```

### User Management

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

#### Get User Info
```http
GET /api/auth/user/{username}
Authorization: Bearer {jwt_token}
```

#### Update Password
```http
PUT /api/auth/password
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "username": "john_doe",
  "oldPassword": "oldpassword",
  "newPassword": "newpassword"
}
```

#### Delete User
```http
DELETE /api/auth/user/{username}
Authorization: Bearer {jwt_token}
```

### URL Redirection

#### Redirect to Original URL
```http
GET /{shortCode}
```

## Database Schema

### Users Table
- `user_key` (Primary Key, VARCHAR(36), UUID)
- `username` (VARCHAR(64), Unique)
- `email` (VARCHAR(255), Unique)
- `password` (VARCHAR(255), Encrypted)
- `created_on` (TIMESTAMP, Auditing)
- `modified_on` (TIMESTAMP, Auditing)
- `created_by` (VARCHAR(255), Auditing)
- `modified_by` (VARCHAR(255), Auditing)

### URLs Table
- `url_key` (Primary Key, VARCHAR(36), UUID)
- `original_url` (TEXT)
- `short_code` (VARCHAR(10), Unique)
- `expires_at` (TIMESTAMP, Nullable)
- `click_count` (BIGINT, Default: 0)
- `user_key` (VARCHAR(36), Foreign Key to users.user_key, Nullable)
- `is_active` (BOOLEAN, Default: true)
- `title` (VARCHAR(255), Nullable)
- `description` (TEXT, Nullable)
- `created_on` (TIMESTAMP, Auditing)
- `modified_on` (TIMESTAMP, Auditing)
- `created_by` (VARCHAR(255), Auditing)
- `modified_by` (VARCHAR(255), Auditing)

### URL Clicks Table
- `url_click_key` (Primary Key, VARCHAR(36), UUID)
- `url_key` (VARCHAR(36), Foreign Key to urls.url_key)
- `clicked_at` (TIMESTAMP)
- `ip_address` (VARCHAR(45), Nullable)
- `user_agent` (VARCHAR(500), Nullable)
- `referer` (VARCHAR(500), Nullable)
- `country` (VARCHAR(2), Nullable)
- `city` (VARCHAR(100), Nullable)
- `device_type` (VARCHAR(20), Nullable)
- `browser` (VARCHAR(50), Nullable)
- `operating_system` (VARCHAR(50), Nullable)
- `browser_version` (VARCHAR(20), Nullable)
- `language` (VARCHAR(10), Nullable)
- `session_id` (VARCHAR(36), Nullable)
- `is_unique_visitor` (BOOLEAN, Default: false)
- `is_bot` (BOOLEAN, Default: false)
- `region` (VARCHAR(100), Nullable)
- `country_code` (VARCHAR(3), Nullable)
- `timezone` (VARCHAR(50), Nullable)
- `utm_source` (VARCHAR(100), Nullable)
- `utm_medium` (VARCHAR(100), Nullable)
- `utm_campaign` (VARCHAR(100), Nullable)
- `utm_term` (VARCHAR(100), Nullable)
- `utm_content` (VARCHAR(100), Nullable)
- `response_time_ms` (INTEGER, Nullable)
- `is_suspicious` (BOOLEAN, Default: false)
- `click_source` (VARCHAR(20), Nullable)
- `previous_url` (VARCHAR(500), Nullable)
- `is_mobile` (BOOLEAN, Default: false)
- `screen_resolution` (VARCHAR(20), Nullable)
- `connection_type` (VARCHAR(20), Nullable)
- `geo_enriched` (BOOLEAN, Default: false)
- `device_enriched` (BOOLEAN, Default: false)
- `created_on` (TIMESTAMP, Auditing)
- `modified_on` (TIMESTAMP, Auditing)
- `created_by` (VARCHAR(255), Auditing)
- `modified_by` (VARCHAR(255), Auditing)

## Database Migration

The application uses **Liquibase** for database schema management:

- **Migration Files**: Located in `src/main/resources/db/changelog/`
- **Master Changelog**: `db.changelog-master.xml`
- **Individual Changes**: 
  - `001-create-users-table.xml`
  - `002-create-urls-table.xml`
  - `003-create-url-clicks-table.xml` (includes multiple changesets for analytics)

### Key Features:
- **UUID Primary Keys**: Secure, globally unique identifiers
- **Snake Case Naming**: All database fields use snake_case convention
- **Auditing Fields**: Automatic tracking of creation and modification
- **Foreign Key Constraints**: Proper relationships between tables
- **Indexes**: Optimized for common queries
- **Cascade Deletes**: URL clicks are deleted when URLs are deleted
- **Nullable Foreign Keys**: URLs can exist without users (anonymous URLs)
- **Preconditions**: Checks before applying migrations
- **Rollback Support**: Proper rollback scripts for each change
- **PostgreSQL Compatibility**: Optimized for PostgreSQL database

## Analytics Features

### Click Tracking
- **Device Information**: Device type, browser, operating system
- **Geolocation**: Country, city, region, timezone
- **User Behavior**: Session tracking, unique visitors, bot detection
- **Performance**: Response time tracking
- **Marketing**: UTM parameter tracking
- **Security**: Suspicious activity detection

### Analytics Data
- Click counts and trends
- Geographic distribution
- Device and browser statistics
- Referrer analysis
- Performance metrics

## Caching Strategy

- **Redis Cache**: URLs are cached with 24-hour TTL
- **Cache Keys**: `url:{shortCode}`
- **Cache Invalidation**: Automatic on URL updates/deletion

## Scheduled Tasks

- **URL Cleanup**: Runs every hour to deactivate expired URLs
- **Cache Management**: Automatic cache invalidation for expired URLs

## Testing

The application includes comprehensive test coverage:

### Test Structure
```
src/test/java/com/example/UrlShortner/
├── controller/      # Controller tests with MockMvc
├── repository/      # Repository tests with H2 database
├── model/          # Entity tests
├── dto/            # DTO tests
└── config/         # Configuration tests
```

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserControllerTest

# Run with coverage
mvn test jacoco:report
```

### Test Coverage
- **Controllers**: REST endpoint testing with MockMvc
- **Repositories**: Data access layer testing with H2
- **Models**: Entity validation and behavior testing
- **DTOs**: Data transfer object validation
- **Configuration**: Security and JWT configuration testing

## Monitoring

The application includes Spring Boot Actuator for monitoring:
- Health checks: `/actuator/health`
- Application metrics: `/actuator/metrics`
- Application info: `/actuator/info`

## Development

### Project Structure
```
src/main/java/com/example/UrlShortner/
├── config/          # Configuration classes (Security, JWT, Redis, Async)
├── controller/      # REST controllers (User, URL, Redirect)
├── dto/            # Data Transfer Objects
├── enums/          # Enumeration classes (Browser, Device, OS)
├── model/          # Entity classes (User, URL, UrlClick)
├── repository/     # Data access layer
├── scheduler/      # Scheduled tasks (URL cleanup)
└── service/        # Business logic (User, URL, Analytics, Cache, etc.)
```

### Key Design Patterns
- **Layered Architecture**: Controllers → Services → Repositories
- **DTO Pattern**: Separate data transfer objects from entities
- **Builder Pattern**: Lombok-generated builders for entities and DTOs
- **Repository Pattern**: Data access abstraction
- **Service Layer**: Business logic encapsulation

### Code Quality
- **Lombok**: Reduces boilerplate code
- **Validation**: Jakarta Validation annotations
- **Auditing**: Automatic creation/modification tracking
- **Error Handling**: Comprehensive exception handling
- **Security**: JWT-based authentication

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License. 