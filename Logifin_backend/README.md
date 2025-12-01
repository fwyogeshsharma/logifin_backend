# Logifin Backend

A production-grade Spring Boot backend with Docker-based deployment.

## Tech Stack

- **Java 8** with Spring Boot 2.7.x
- **Spring Security** with JWT Authentication
- **Spring Data JPA** + Hibernate
- **PostgreSQL** database
- **Flyway** for database versioning
- **Docker** + Docker Compose
- **pgAdmin** for database management
- **JUnit 5** + Mockito for testing

## Project Structure

```
Logifin_backend/
├── src/
│   └── main/
│       ├── java/com/logifin/
│       │   ├── config/         # Configuration classes
│       │   ├── controller/     # REST controllers
│       │   ├── dto/            # Data Transfer Objects
│       │   ├── entity/         # JPA entities
│       │   ├── exception/      # Exception handling
│       │   ├── repository/     # JPA repositories
│       │   └── service/        # Business logic
│       └── resources/
│           ├── db/migration/   # Flyway migrations
│           ├── application.yml
│           ├── application-local.yml
│           └── application-prod.yml
├── Dockerfile
├── docker-compose.yml
├── .env
├── .env.prod
└── pom.xml
```

## Quick Start

### Prerequisites

- Docker & Docker Compose installed
- No Java or Maven installation required (Docker handles everything)

### One-Command Setup

```bash
docker compose up --build
```

This will automatically:
1. Start PostgreSQL database
2. Create the database
3. Run Flyway migrations
4. Start Spring Boot application
5. Start pgAdmin UI

### Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| Backend API | http://localhost:8080 | - |
| Health Check | http://localhost:8080/actuator/health | - |
| pgAdmin | http://localhost:5050 | admin@logifin.com / admin123 |

### Default Users

| Email | Password | Role |
|-------|----------|------|
| admin@logifin.com | admin123 | SUPER_ADMIN |
| test@logifin.com | admin123 | CSR |

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/login` | User login | No |
| POST | `/api/v1/auth/register` | User registration | No |

### Users (Requires Authentication)

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | `/api/v1/users` | Get all users | CSR, ADMIN, SUPER_ADMIN |
| GET | `/api/v1/users/{id}` | Get user by ID | CSR, ADMIN, SUPER_ADMIN |
| GET | `/api/v1/users/email/{email}` | Get user by email | CSR, ADMIN, SUPER_ADMIN |
| GET | `/api/v1/users/active` | Get active users | CSR, ADMIN, SUPER_ADMIN |
| GET | `/api/v1/users/search?name=` | Search users by name | CSR, ADMIN, SUPER_ADMIN |
| POST | `/api/v1/users` | Create new user | ADMIN, SUPER_ADMIN |
| PUT | `/api/v1/users/{id}` | Update user | ADMIN, SUPER_ADMIN |
| DELETE | `/api/v1/users/{id}` | Delete user | SUPER_ADMIN |
| PATCH | `/api/v1/users/{id}/activate` | Activate user | ADMIN, SUPER_ADMIN |
| PATCH | `/api/v1/users/{id}/deactivate` | Deactivate user | ADMIN, SUPER_ADMIN |

### Roles (Requires SUPER_ADMIN)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/roles` | Get all roles |
| GET | `/api/v1/roles/{id}` | Get role by ID |
| GET | `/api/v1/roles/name/{roleName}` | Get role by name |
| POST | `/api/v1/roles` | Create new role |
| PUT | `/api/v1/roles/{id}` | Update role |
| DELETE | `/api/v1/roles/{id}` | Delete role |

### Health (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/health` | Application health |
| GET | `/api/v1/info` | Application info |
| GET | `/actuator/health` | Actuator health |

## Configuration

### Environment Variables

Edit `.env` file to customize:

```env
# Database
DB_NAME=LogifinLocal
DB_USER=postgres
DB_PASSWORD=postgres
DB_PORT=5432

# Application
SPRING_PROFILE=local
APP_PORT=8080

# pgAdmin
PGADMIN_EMAIL=admin@logifin.com
PGADMIN_PASSWORD=admin123
PGADMIN_PORT=5050
```

### Profiles

- **local**: Development mode with debug logging
- **prod**: Production mode with optimized settings

Switch profiles:
```bash
SPRING_PROFILE=prod docker compose up --build
```

## Database Migrations

Migrations are in `src/main/resources/db/migration/`:

- `V1__init.sql` - Initial schema and users table
- `V2__add_audit_tables.sql` - Audit logging table
- `V3__add_user_roles.sql` - User roles and authentication

### Adding New Migrations

1. Create file: `V{N}__description.sql`
2. Restart application (Flyway runs automatically)

## pgAdmin Setup

1. Open http://localhost:5050
2. Login with credentials from `.env`
3. Add new server:
   - **Host**: `postgres` (container name)
   - **Port**: `5432`
   - **Database**: `LogifinLocal`
   - **Username**: `postgres`
   - **Password**: `postgres`

## Docker Commands

```bash
# Start all services
docker compose up --build

# Start in background
docker compose up -d --build

# Stop all services
docker compose down

# Stop and remove volumes (clears database)
docker compose down -v

# View logs
docker compose logs -f backend

# Rebuild only backend
docker compose up --build backend
```

## Production Deployment

1. Copy `.env.prod` to `.env`
2. Update credentials and settings
3. Run:
```bash
docker compose up -d --build
```

### Production Considerations

- Change all default passwords
- Consider disabling pgAdmin or securing it
- Use proper SSL/TLS
- Configure proper logging
- Set up monitoring and alerting

## Development

### Running Locally Without Docker

```bash
# Start PostgreSQL separately, then:
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Building JAR

```bash
./mvnw clean package -DskipTests
```

## API Examples

### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@logifin.com",
    "password": "admin123"
  }'
```

### Login Response

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "email": "admin@logifin.com",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ROLE_SUPER_ADMIN"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### Create User (with JWT Token)

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "roleName": "ROLE_CSR"
  }'
```

### Response

```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 3,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "active": true,
    "roleId": 4,
    "roleName": "ROLE_CSR",
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

## Available Roles

| Role | Description |
|------|-------------|
| ROLE_LENDER | Lender role with access to lending operations |
| ROLE_TRANSPORTER | Transporter role with access to transport operations |
| ROLE_TRUST_ACCOUNT | Trust account role with access to trust account operations |
| ROLE_CSR | Customer Service Representative role |
| ROLE_ADMIN | Administrator role with elevated privileges |
| ROLE_SUPER_ADMIN | Super Administrator role with full system access |
