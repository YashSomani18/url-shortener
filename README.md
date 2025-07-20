# URL Shortener Backend Application

A Spring Boot application that provides URL shortening functionality with Redis caching and MySQL database storage.

## Features

- **URL Shortening**: Create short URLs from long URLs
- **User Management**: Register and manage users
- **Analytics**: Track click statistics and user behavior
- **Redis Caching**: Fast URL lookups with Redis
- **Expiration**: Set expiration dates for URLs
- **Scheduled Cleanup**: Automatic cleanup of expired URLs
- **Click Tracking**: Detailed analytics for each URL
- **UUID Primary Keys**: Secure, globally unique identifiers

## Technology Stack

- **Spring Boot 3.5.3** - Main framework
- **Java 21** - Programming language
- **MySQL** - Primary database
- **Redis** - Caching layer
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **Lombok** - Reducing boilerplate code
- **Liquibase** - Database migrations

## Prerequisites

- Java 21 or higher
- MySQL 8.0 or higher
- Redis 6.0 or higher
- Maven 3.6 or higher

## Setup Instructions

### 1. Database Setup

Start MySQL and create a database:
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
spring.datasource.url=jdbc:mysql://localhost:3306/url_shortener?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password

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
```

#### Deactivate URL
```http
DELETE /api/urls/{urlKey}?username={username}
```

#### Get URL Statistics
```http
GET /api/urls/stats/{shortCode}
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
POST /api/auth/login?username=john_doe&password=password123
```

#### Get User Info
```http
GET /api/auth/user/{username}
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
- `expires_at` (DATETIME, Nullable)
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
- `clicked_at` (DATETIME)
- `ip_address` (VARCHAR(45), Nullable)
- `user_agent` (VARCHAR(500), Nullable)
- `referer` (VARCHAR(500), Nullable)
- `country` (VARCHAR(2), Nullable)
- `city` (VARCHAR(100), Nullable)
- `device_type` (VARCHAR(20), Nullable)
- `browser` (VARCHAR(50), Nullable)
- `created_on` (TIMESTAMP, Auditing)
- `modified_on` (TIMESTAMP, Auditing)
- `created_by` (VARCHAR(255), Auditing)
- `modified_by` (VARCHAR(255), Auditing)

## Database Migration

The application uses **Liquibase** for database schema management:

- **Migration Files**: Located in `src/main/resources/db/changelog/`
- **Master Changelog**: `db.changelog-master.xml`
- **Individual Changes**: `001-create-users-table.xml`, `002-create-urls-table.xml`, `003-create-url-clicks-table.xml`

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

## Caching Strategy

- **Redis Cache**: URLs are cached with 24-hour TTL
- **Cache Keys**: `url:{shortCode}`
- **Cache Invalidation**: Automatic on URL updates/deletion

## Scheduled Tasks

- **URL Cleanup**: Runs every hour to deactivate expired URLs
- **Cache Management**: Automatic cache invalidation for expired URLs

## Monitoring

The application includes Spring Boot Actuator for monitoring:
- Health checks: `/actuator/health`
- Application metrics: `/actuator/metrics`
- Application info: `/actuator/info`

## Development

### Project Structure
```
src/main/java/com/example/UrlShortner/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
├── model/          # Entity classes
├── repository/     # Data access layer
├── scheduler/      # Scheduled tasks
└── service/        # Business logic
```

### Running Tests
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License. 