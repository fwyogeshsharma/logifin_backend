# Logifin Project Context - AI Assistant Onboarding Guide

> **Quick Start for New Sessions**: Read this file to understand the complete project context, business domain, architecture, and current implementation status.

> **Visual Learner?** Check out these diagram files:
> - [PROJECT_DIAGRAMS.md](PROJECT_DIAGRAMS.md) - Technical flow diagrams (system architecture, authentication, database ER)
> - [USER_JOURNEY_DIAGRAMS.md](USER_JOURNEY_DIAGRAMS.md) - User interface flows with forms, buttons, and screens

**Last Updated**: 2025-12-19
**Current Migration Version**: V2
**Spring Boot Version**: 2.7.18
**Java Version**: 8

---

## Table of Contents
1. [Business Domain Overview](#business-domain-overview)
2. [Core Concepts](#core-concepts)
3. [Tech Stack](#tech-stack)
4. [Project Structure](#project-structure)
5. [Key Entities & Relationships](#key-entities--relationships)
6. [Important Implementation Patterns](#important-implementation-patterns)
7. [Current Feature Status](#current-feature-status)
8. [Common Gotchas & Conventions](#common-gotchas--conventions)
9. [Database Migrations](#database-migrations)
10. [API Endpoints](#api-endpoints)
11. [How to Build & Deploy](#how-to-build--deploy)

---

## Business Domain Overview

**Logifin** is a logistics financing platform that connects three main actors:

1. **Lenders** - Financial institutions or individuals who provide funding
2. **Transporters** - Companies/individuals who own vehicles and execute trips
3. **Consigners/Senders** - Companies/individuals who need goods transported

### Core Business Flow

#### Trip Financing Workflow
1. **Transporter** creates a Trip with pickup, destination, and estimated costs
2. **Trip** has a sender/consigner attached to it
3. **Lender** browses available trips and marks interest in financing them (can select multiple trips at once)
4. System automatically finds the **Contract** between:
   - The Lender
   - The Transporter (trip creator)
   - The Consigner/Sender (from trip)
5. If no valid contract exists, the specific trip is skipped with a clear error message
6. **Transporter** sees all interested lenders with contract terms for their trip
7. **Transporter** accepts ONE lender - this action:
   - Links the trip to the contract
   - Copies contract terms (interest rate, maturity days) to the trip
   - Auto-rejects all other pending proposals for that trip
8. Trip is now financed and ready to execute

#### Contract System
- Contracts are **tripartite agreements** between Lender, Transporter, and Consigner
- A Contract defines:
  - Interest rate
  - Maturity days (repayment period)
  - LTV (Loan-to-Value ratio)
  - Loan percentage
  - Penalty ratio
  - Expiry date
- Contracts are stored once in the `contracts` table
- `contract_parties` junction table tracks which users are parties to which contracts
- Contracts can have status: DRAFT, ACTIVE, EXPIRED, TERMINATED
- Only ACTIVE contracts with future expiry dates are used for trip financing

---

## Core Concepts

### Data Normalization Principle
**CRITICAL**: We follow strict data normalization. Contract terms (interest rate, maturity days, etc.) are stored ONLY in the `contracts` table. The `trip_finance_proposals` table only stores relationships (IDs) and status information. When contract terms are needed in responses, they are fetched via JPA relationships.

### Automatic Contract Discovery
When a lender marks interest in a trip:
- System does NOT ask for contract ID in request payload
- System automatically queries to find active contracts where all three parties are members:
  - Lender ID (from authentication context)
  - Transporter ID (from `trip.createdByUser`)
  - Consigner ID (from `trip.sender`)
- Uses the contract with the furthest expiry date
- If no contract found, returns descriptive error message

### Batch Operations with Partial Success
- Lenders can select 1-100 trips in a single API call
- Each trip is processed individually
- Response includes per-trip success/failure results
- Some trips can succeed while others fail (e.g., no contract exists for specific trips)

---

## Tech Stack

### Core Technologies
- **Java 8** (JDK 1.8)
- **Spring Boot 2.7.18**
- **Spring Data JPA** with Hibernate
- **Spring Security** with JWT authentication
- **PostgreSQL** database
- **Flyway** for database version control
- **Docker** + Docker Compose for containerization
- **Maven** for dependency management

### Testing
- **JUnit 5** (Jupiter)
- **Mockito** for mocking
- **Spring Boot Test**

### Additional Libraries
- **Lombok** - Reduce boilerplate code
- **Springdoc OpenAPI** - API documentation (Swagger)
- **Jackson** - JSON serialization
- **Validation API** - Request validation

---

## Project Structure

```
LogifinProductionReady/
├── Logifin_backend/
│   ├── src/main/java/com/logifin/
│   │   ├── config/              # Security, CORS, beans, Swagger
│   │   ├── controller/          # REST endpoints
│   │   ├── dto/                 # Request/Response DTOs
│   │   ├── entity/              # JPA entities
│   │   ├── exception/           # Custom exceptions & handlers
│   │   ├── repository/          # Spring Data JPA repositories
│   │   ├── service/             # Business logic interfaces
│   │   │   └── impl/            # Service implementations
│   │   ├── security/            # JWT, UserDetails, etc.
│   │   └── LogifinApplication.java
│   ├── src/main/resources/
│   │   ├── db/migration/        # Flyway SQL migrations
│   │   ├── application.yml      # Base config
│   │   ├── application-local.yml
│   │   └── application-prod.yml
│   ├── src/test/java/           # Unit and integration tests
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── pom.xml
│   ├── .env                     # Docker environment variables
│   └── README.md
├── PROJECT_CONTEXT.md           # This file
└── TRIP_FINANCING_IMPLEMENTATION_STEPS.md
```

---

## Key Entities & Relationships

### User
- **Table**: `users`
- **Key Fields**: id, firstName, lastName, email, phone, password, active, company_id
- **Relationships**:
  - ManyToOne with Company
  - ManyToMany with Role
  - OneToMany with Trip (as createdByUser)
  - OneToMany with ContractParty
  - OneToMany with TripFinanceProposal (as lender)

### Company
- **Table**: `companies`
- **Key Fields**: id, name, gstin, pan, address, active
- **Relationships**: OneToMany with User

### Role
- **Table**: `roles`
- **Key Fields**: id, roleName, description
- **Values**: ROLE_LENDER, ROLE_TRANSPORTER, ROLE_TRUST_ACCOUNT, ROLE_CSR, ROLE_ADMIN, ROLE_SUPER_ADMIN

### Trip
- **Table**: `trips`
- **Key Fields**: id, pickup, destination, loanAmount, interestRate, maturityDays, sender_id, created_by_user_id, contract_id
- **Relationships**:
  - ManyToOne with User (as createdByUser) - the transporter
  - ManyToOne with User (as sender) - the consigner
  - ManyToOne with Contract - populated when transporter accepts a lender
  - OneToMany with TripFinanceProposal

### Contract
- **Table**: `contracts`
- **Key Fields**: id, interestRate, maturityDays, loanPercent, ltv, penaltyRatio, expiryDate, status
- **Relationships**:
  - OneToMany with ContractParty
  - OneToMany with TripFinanceProposal
  - OneToMany with Trip

### ContractParty (Junction Table)
- **Table**: `contract_parties`
- **Purpose**: Tracks which users are parties to which contracts
- **Key Fields**: id, contract_id, user_id, partyType, signedAt
- **Relationships**:
  - ManyToOne with Contract
  - ManyToOne with User
- **Important Query**: `findActiveContractsByThreeParties(userId1, userId2, userId3)` - finds contracts where all three users are parties

### TripFinanceProposal
- **Table**: `trip_finance_proposals`
- **Purpose**: Tracks lender interest in financing trips
- **Key Fields**: id, trip_id, lender_id, contract_id, status, proposedAt, respondedAt
- **Status Values**: PENDING, ACCEPTED, REJECTED, WITHDRAWN
- **Unique Constraint**: (trip_id, lender_id, contract_id)
- **Relationships**:
  - ManyToOne with Trip
  - ManyToOne with User (as lender)
  - ManyToOne with Contract

---

## Important Implementation Patterns

### 1. Service Layer Pattern
- All business logic in service implementations
- Controllers are thin - only handle HTTP concerns
- Use `@Transactional` annotation on service methods
- Read-only queries use `@Transactional(readOnly = true)`

### 2. DTO Pattern
- Separate DTOs for requests and responses
- Never expose entities directly in APIs
- Use Builder pattern with Lombok (`@Builder`)
- DTOs include validation annotations (`@NotNull`, `@NotEmpty`, etc.)

### 3. Exception Handling
- Custom exceptions: `ResourceNotFoundException`, `BadRequestException`
- Global exception handler with `@ControllerAdvice`
- Consistent error response format

### 4. Repository Pattern
- Spring Data JPA repositories
- Custom queries using `@Query` annotation
- Use JPQL for complex queries
- Named query methods following Spring Data conventions

### 5. Security Pattern
- JWT-based authentication
- Role-based access control (`@PreAuthorize`)
- User ID extracted from authentication context, NOT from request body

### 6. Audit Trail
- `BaseEntity` with createdAt, updatedAt, createdBy, updatedBy
- Automatically managed by JPA listeners

---

## Current Feature Status

### ✅ Completed Features

#### Authentication & Authorization
- JWT-based login/logout
- Role-based access control
- User management (CRUD)
- Role management

#### Company Management
- Company CRUD operations
- User-Company relationships

#### Trip Management
- Trip creation and management
- Trip-Transporter relationship
- Trip-Consigner relationship

#### Contract System
- Contract CRUD operations
- ContractParty junction table
- Multi-party contract support
- Contract status management

#### Trip Financing (Latest Feature)
- **Lender Operations**:
  - Mark interest in multiple trips (batch operation)
  - Automatic contract discovery based on lender, transporter, consigner
  - View my interests (with optional status filter)
  - View specific interest details
  - Withdraw pending interest

- **Transporter Operations**:
  - View all interested lenders for a specific trip
  - View all interests across all my trips
  - Accept lender (auto-rejects other proposals, links trip to contract)
  - Reject lender

- **Key Implementation Details**:
  - No contractId in request - system auto-finds it
  - Partial success in batch operations
  - Descriptive error messages when no contract found
  - Automatic population of trip financial terms when lender accepted

### 🚧 Pending/Planned Features
- Document management
- Payment tracking
- Trip execution workflow
- Notifications system
- Reporting and analytics

---

## Common Gotchas & Conventions

### Entity Field Names (CRITICAL)
These are common mistakes - memorize these:

1. **Trip creator is accessed via `createdByUser`, NOT `createdBy`**
   - ❌ Wrong: `trip.getCreatedBy()`
   - ✅ Correct: `trip.getCreatedByUser()`

2. **User has firstName and lastName, NOT name**
   - ❌ Wrong: `user.getName()`
   - ✅ Correct: `user.getFirstName() + " " + user.getLastName()`

3. **Transporter is the trip creator**
   - Transporter: `trip.getCreatedByUser()`
   - Consigner: `trip.getSender()`

### Naming Conventions
- **Entities**: Singular, PascalCase (e.g., `TripFinanceProposal`)
- **Tables**: Plural, snake_case (e.g., `trip_finance_proposals`)
- **Columns**: snake_case (e.g., `created_at`, `lender_id`)
- **DTOs**: Descriptive names ending with DTO (e.g., `CreateFinanceInterestRequest`, `FinanceInterestForLenderDTO`)
- **Repositories**: EntityName + Repository (e.g., `TripFinanceProposalRepository`)
- **Services**: EntityName + Service (interface) and EntityName + ServiceImpl (implementation)
- **Controllers**: Descriptive name + Controller (e.g., `LenderFinanceController`, `TransporterFinanceController`)

### Code Conventions
- Always use `@Slf4j` for logging
- Use `@RequiredArgsConstructor` instead of `@Autowired`
- Validate user ownership before operations (e.g., verify trip belongs to transporter)
- Extract user ID from `Authentication` object, never from request
- Use proper HTTP status codes (200 OK, 201 Created, 204 No Content, 400 Bad Request, 404 Not Found)

### Query Conventions
- Use repository method names for simple queries
- Use `@Query` with JPQL for complex queries
- Use `@Modifying` for UPDATE/DELETE queries
- Always use named parameters (`:paramName`) in JPQL

### Testing Conventions
- Test class name: EntityNameServiceTest or EntityNameControllerTest
- Use `@MockBean` for mocking dependencies
- Use `@WebMvcTest` for controller tests
- Use `@DataJpaTest` for repository tests

---

## Database Migrations

### Current Migration Status
```
✅ V1__init.sql                  - Base schema (users, roles, companies)
✅ V2__create_trip_finance_proposals.sql - Trip financing feature
```

### Migration File Naming
- Format: `V{version}__{description}.sql`
- Example: `V3__add_payment_tracking.sql`
- Version number must be sequential
- Description should be concise and descriptive

### Adding New Migrations
1. Create file in `src/main/resources/db/migration/`
2. Follow naming convention
3. Write idempotent SQL (use IF NOT EXISTS where applicable)
4. Restart application or run `docker compose up --build`
5. Flyway automatically applies new migrations
6. Check logs for migration success: `Successfully applied N migration(s)`

### Migration Best Practices
- Never modify existing migrations
- Always add new migrations for schema changes
- Include both UP and DOWN logic where applicable
- Add comments for complex logic
- Test migrations on local database first

---

## API Endpoints

### Trip Financing Endpoints

#### Lender APIs
Base Path: `/api/v1/lenders`

| Method | Endpoint | Description | Request Body | Auth |
|--------|----------|-------------|--------------|------|
| POST | `/finance-interests` | Mark interest in multiple trips | `CreateFinanceInterestRequest` | ROLE_LENDER |
| GET | `/finance-interests` | Get my interests | Query param: `status` (optional) | ROLE_LENDER |
| GET | `/finance-interests/{id}` | Get specific interest | - | ROLE_LENDER |
| DELETE | `/finance-interests/{id}` | Withdraw interest | - | ROLE_LENDER |

**CreateFinanceInterestRequest**:
```json
{
  "tripIds": [101, 102, 103]
}
```

**BatchFinanceInterestResponse**:
```json
{
  "totalRequested": 3,
  "successCount": 2,
  "failureCount": 1,
  "results": [
    {
      "tripId": 101,
      "tripNumber": "TRP-101",
      "success": true,
      "message": "Interest marked successfully (Contract ID: 5)",
      "interestId": 25
    },
    {
      "tripId": 102,
      "tripNumber": "TRP-102",
      "success": false,
      "message": "No active contract found between you (lender), transporter (John Doe), and consigner (ABC Corp)"
    }
  ]
}
```

#### Transporter APIs
Base Path: `/api/v1/transporters`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/trips/{tripId}/finance-interests` | View interested lenders for trip | ROLE_TRANSPORTER |
| GET | `/finance-interests` | View all interests for my trips | ROLE_TRANSPORTER |
| POST | `/finance-interests/{id}/accept` | Accept lender | ROLE_TRANSPORTER |
| POST | `/finance-interests/{id}/reject` | Reject lender | ROLE_TRANSPORTER |

**FinanceInterestForTransporterDTO** (Response):
```json
{
  "id": 25,
  "status": "PENDING",
  "interestedAt": "2025-12-19T10:00:00",
  "respondedAt": null,
  "lenderId": 10,
  "lenderName": "Jane Smith",
  "lenderCompanyName": "Finance Corp",
  "contractId": 5,
  "interestRate": 12.5,
  "maturityDays": 30,
  "contractExpiryDate": "2026-12-31",
  "loanPercent": 80.0,
  "ltv": 75.0,
  "penaltyRatio": 2.0
}
```

### Authentication Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | User login |
| POST | `/api/v1/auth/register` | User registration |

### Other Endpoints
See README.md for complete list of User, Role, and other endpoints.

---

## How to Build & Deploy

### Prerequisites
- Docker & Docker Compose installed
- No Java or Maven installation required

### Local Development

#### Start All Services
```bash
cd C:\Anupam\Faber\Projects\LogifinProductionReady\Logifin_backend
docker compose up --build
```

This starts:
- PostgreSQL database (port 5432)
- Spring Boot application (port 8080)
- pgAdmin (port 5050)

#### Access Points
- **Backend API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **pgAdmin**: http://localhost:5050 (admin@logifin.com / admin123)

#### Rebuild After Code Changes
```bash
docker compose up --build backend
```

#### View Logs
```bash
docker compose logs -f backend
```

#### Stop Services
```bash
docker compose down
```

#### Clean Database (Reset)
```bash
docker compose down -v
docker compose up --build
```

### Compilation (Without Docker)
```bash
cd Logifin_backend
mvn clean compile
```

### Running Tests
```bash
mvn test
mvn test -Dtest=TripFinanceProposalServiceTest
```

### Building JAR
```bash
mvn clean package -DskipTests
```

---

## Working with Code

### Reading Files
Always read the entity files first to understand field names and relationships before making changes.

### Making Changes
1. Read relevant entity/repository/service files
2. Make changes following existing patterns
3. Compile to check for errors: `docker compose up --build backend`
4. Check logs for any runtime errors
5. Test the API endpoints

### Common Commands
```bash
# Compile only (quick check)
docker exec -it logifin-backend mvn compile -q

# Full rebuild
docker compose up --build backend

# Check migration status
docker compose logs backend | grep "Flyway"

# Access database
docker exec -it logifin-postgres psql -U postgres -d LogifinLocal
```

### Debugging Tips
1. Check logs: `docker compose logs -f backend`
2. Check database: Use pgAdmin at http://localhost:5050
3. Verify migrations: Look for "Successfully applied N migration" in logs
4. Test API: Use Postman or curl with proper JWT token

---

## Current Focus Areas

### Recently Completed (2025-12-19)
- ✅ Trip Finance Proposal feature with automatic contract discovery
- ✅ Batch interest marking with partial success handling
- ✅ Lender and Transporter APIs for finance management
- ✅ Database migration V2 for trip_finance_proposals table

### Next Potential Features
- Document upload and management for trips
- Payment tracking and disbursement
- Trip status workflow (CREATED → IN_PROGRESS → COMPLETED)
- Notifications for proposal acceptance/rejection
- Dashboard analytics for lenders and transporters

---

## Questions to Ask When Starting New Work

When user requests new features, consider:

1. **Does this require new entities?**
   - If yes, create entity, repository, service, controller, DTOs, migration

2. **Does this modify existing entities?**
   - If yes, create new Flyway migration, update entity, update related code

3. **Who are the actors?**
   - Lender, Transporter, Consigner, Admin, etc.
   - Create separate controllers if different actors have different operations

4. **Are there relationships to existing entities?**
   - Use ManyToOne, OneToMany, ManyToMany as appropriate
   - Consider junction tables for complex many-to-many relationships

5. **Should data be duplicated or normalized?**
   - Default: Normalize (store once, fetch via relationships)
   - Exception: Only duplicate if there's a strong business reason (e.g., historical snapshot)

6. **What validations are needed?**
   - Ownership (e.g., trip belongs to transporter)
   - Status checks (e.g., only PENDING proposals can be withdrawn)
   - Duplicates (e.g., lender can't mark interest twice for same trip+contract)

7. **What error scenarios exist?**
   - Not found (404)
   - Already exists (400)
   - Invalid state transition (400)
   - No permission (403)

---

## Emergency Reference

### If Build Fails
1. Check for syntax errors in Java files
2. Verify all imports are present
3. Check entity field names match (e.g., `createdByUser` not `createdBy`)
4. Look at logs: `docker compose logs backend`

### If Migration Fails
1. Check migration file syntax
2. Verify version number is sequential
3. Check for conflicting schema changes
4. Look at Flyway logs: `docker compose logs backend | grep Flyway`

### If API Returns 500
1. Check backend logs
2. Verify database constraints
3. Check for null pointer exceptions
4. Verify all required relationships are loaded (check for LazyInitializationException)

### If Tests Fail
1. Check mocking setup
2. Verify expected vs actual values
3. Check for proper authentication/authorization in test context
4. Review test data setup

---

**For Quick Onboarding**:
1. Read [Business Domain Overview](#business-domain-overview)
2. Review [Key Entities & Relationships](#key-entities--relationships)
3. Check [Common Gotchas & Conventions](#common-gotchas--conventions)
4. Understand [Current Feature Status](#current-feature-status)

**For Making Changes**:
1. Read relevant entity files first
2. Follow [Important Implementation Patterns](#important-implementation-patterns)
3. Create migration if schema changes
4. Follow [Naming Conventions](#naming-conventions)
5. Test with `docker compose up --build`

---

*This file should be updated regularly as new features are added or architectural decisions are made.*
