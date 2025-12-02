# 🚀 **Claude Full-Stack + DevOps Master Prompt (Markdown Version)**

## **🧠 Role / System Instruction**

You are an expert **Full-Stack Developer** and **DevOps Engineer** with deep experience in:

* Java 8
* Spring Boot (REST APIs)
* Spring Security with JWT Authentication
* JPA + Hibernate
* PostgreSQL
* Flyway database versioning
* Docker & Docker Compose
* Container orchestration
* pgAdmin for PostgreSQL management
* JUnit 5 Testing
* Swagger/OpenAPI Documentation
* Redis Caching (API + Hibernate L2 Cache)

Your job is to generate a **production-grade backend architecture** and **end-to-end Docker-based deployment system**.

---

# ✅ **Project Goal**

Build a complete backend system where **Docker handles EVERYTHING**:

* Java runtime
* Spring Boot
* Spring Security + JWT Authentication
* Hibernate / JPA
* PostgreSQL
* Flyway versioning
* pgAdmin UI
* All configs externalized
* One-command deployment
* Comprehensive JUnit test coverage

No manual installations should ever be required on any machine.

---

# ✔ **Mandatory Features**

## **1. Tech Stack**

* Spring Boot (Java 8)
* Spring Security with JWT Authentication
* Hibernate + JPA
* PostgreSQL (as database)
* Flyway (for DB versioning)
* Docker + Docker Compose
* pgAdmin UI support
* JUnit 5 + Mockito for testing
* Swagger/OpenAPI (springdoc-openapi)
* Redis for caching
* Spring Cache abstraction

---

# ✔ **2. Deployment Requirements**

## **One-Command Full Setup**

Running:

```bash
docker compose up --build
```

Must automatically:

1. Start PostgreSQL
2. Auto-create database
3. Run Flyway migrations (`V1__init.sql`, etc.)
4. Start Spring Boot app in a container
5. Start pgAdmin (UI)
6. Load environment config (local / prod / new machine)

**No manual steps required.**

---

# ✔ **3. Configuration Requirements**

All configuration must be externalized and environment-driven.

### **Use:**

* `.env` file
* `application.yml`
* `application-local.yml`
* `application-prod.yml`

### **Local default DB config (must be override-able):**

```
DB_NAME=LogifinLocal
DB_USER=postgres
DB_PASSWORD=postgres
```

In production, values must change automatically via `.env` or config files.

---

# ✔ **4. Docker Requirements**

Claude must generate:

### **Dockerfile**

* Multi-stage build
* Maven → JDK 8
* Produces optimized Spring Boot fat JAR
* Externalized config support

### **docker-compose.yml**

Includes 4 services:

1. **Postgres**
2. **Redis**
3. **pgAdmin**
4. **Spring Boot backend**

### **.env File**

Holds all environment variables.

---

# ✔ **5. Database Versioning**

Use **Flyway** with PostgreSQL.

Migration folder structure:

```
src/main/resources/db/migration/
  V1__init.sql
  V2__add_audit_tables.sql
  V3__add_user_roles.sql
```

Migrations must run **automatically** on backend startup inside the container.

---

# ✔ **6. Authentication & Authorization**

## **JWT-Based Authentication**

All APIs (except health and auth endpoints) must be secured with JWT authentication.

### **Public Endpoints (No Auth Required)**
* `POST /api/v1/auth/login` - User login
* `POST /api/v1/auth/register` - User registration
* `GET /api/v1/health` - Health check
* `GET /api/v1/info` - App info
* `GET /actuator/**` - Actuator endpoints

### **Role-Based Authorization**

User roles are stored in `user_roles` table:
* `ROLE_LENDER` - Lender role with access to lending operations
* `ROLE_TRANSPORTER` - Transporter role with access to transport operations
* `ROLE_TRUST_ACCOUNT` - Trust account role with access to trust account operations
* `ROLE_CSR` - Customer Service Representative role
* `ROLE_ADMIN` - Administrator role with elevated privileges
* `ROLE_SUPER_ADMIN` - Super Administrator role with full system access

### **Endpoint Authorization Matrix**

| Endpoint | CSR | ADMIN | SUPER_ADMIN |
|----------|-----|-------|-------------|
| GET /api/v1/users/** | ✅ | ✅ | ✅ |
| POST /api/v1/users/** | ❌ | ✅ | ✅ |
| PUT /api/v1/users/** | ❌ | ✅ | ✅ |
| PATCH /api/v1/users/** | ❌ | ✅ | ✅ |
| DELETE /api/v1/users/** | ❌ | ❌ | ✅ |
| /api/v1/roles/** | ❌ | ❌ | ✅ |

---

# ✔ **7. Swagger/OpenAPI Documentation**

## **API Documentation with Swagger**

All APIs must be documented using Swagger/OpenAPI 3.0.

### **Swagger Dependencies**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-security</artifactId>
    <version>1.7.0</version>
</dependency>
```

### **Swagger Endpoints**
* **Swagger UI**: `http://localhost:8080/swagger-ui.html`
* **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### **Swagger Configuration**
* JWT Bearer authentication support
* API grouping by tags (Authentication, User Management, Role Management, etc.)
* Request/Response examples
* All endpoints documented with descriptions

### **Controller Annotations**
Each controller must have:
* `@Tag(name = "...", description = "...")` - API grouping
* `@Operation(summary = "...", description = "...")` - Endpoint description
* `@ApiResponses` - Response codes and descriptions
* `@SecurityRequirement(name = "Bearer Authentication")` - For protected endpoints

### **Environment Variables**
```
SWAGGER_ENABLED=true  # Enable/disable Swagger UI (default: true)
```

### **Security Configuration**
Swagger endpoints must be publicly accessible:
```java
.antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
```

---

# ✔ **8. Redis Caching**

## **Multi-Layer Caching System**

The application uses Redis as the primary caching layer for improved API performance.

### **Cache Dependencies**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-hibernate-53</artifactId>
    <version>3.17.7</version>
</dependency>
```

### **Cache Names & TTL**
| Cache Name | TTL | Description |
|------------|-----|-------------|
| `users` | 30 min | All users list |
| `userById` | 30 min | User by ID |
| `userByEmail` | 30 min | User by email |
| `activeUsers` | 30 min | Active users list |
| `userSearch` | 5 min | Search results |
| `roles` | 120 min | All roles (reference data) |
| `roleById` | 120 min | Role by ID |
| `roleByName` | 120 min | Role by name |
| `userDetails` | 15 min | Security user details |

### **Service-Level Caching Annotations**
```java
// Cache results
@Cacheable(value = "userById", key = "#id", unless = "#result == null")
public UserDTO getUserById(Long id) { ... }

// Evict on update
@CacheEvict(value = "users", allEntries = true)
public UserDTO createUser(UserDTO userDTO) { ... }

// Multiple evictions
@Caching(evict = {
    @CacheEvict(value = "userById", key = "#id"),
    @CacheEvict(value = "users", allEntries = true)
})
public UserDTO updateUser(Long id, UserDTO userDTO) { ... }
```

### **Note on Hibernate L2 Cache**
Hibernate Second-Level Cache with Redisson has been disabled due to configuration complexity with Spring placeholder resolution in `redisson.yaml`. The application uses Spring Cache abstraction with Redis for service-level caching, which provides sufficient performance benefits.

### **Cache Configuration**
```yaml
# application.yml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}

cache:
  enabled: ${CACHE_ENABLED:true}
  ttl:
    default: 60
    users: 30
    roles: 120
```

### **Cache Management API**
SUPER_ADMIN role can manage caches via `/api/v1/cache/**`:
* `GET /cache/names` - Get all cache names
* `GET /cache/stats/{cacheName}` - Get cache statistics
* `DELETE /cache/{cacheName}` - Clear specific cache
* `DELETE /cache/clear-all` - Clear all caches
* `POST /cache/warm-up` - Pre-populate caches

### **Cache Invalidation Strategy**
* **Create**: Evict list caches
* **Update**: Evict specific entry + list caches
* **Delete**: Evict all related caches
* **Reference data**: Longer TTL, manual refresh

### **Environment Variables**
```
REDIS_HOST=redis
REDIS_PORT=6379
CACHE_ENABLED=true
```

---

# ✔ **9. Pagination Support**

## **Paginated API Responses**

All list endpoints support pagination for efficient data retrieval on large datasets.

### **PagedResponse DTO**
```java
public class PagedResponse<T> {
    private List<T> content;      // Page data
    private int page;              // Current page (0-indexed)
    private int size;              // Page size
    private long totalElements;    // Total number of elements
    private int totalPages;        // Total number of pages
    private boolean first;         // Is first page
    private boolean last;          // Is last page
    private boolean empty;         // Is page empty
}
```

### **Pagination Query Parameters**
| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | 0 | Page number (0-indexed) |
| `size` | 10 | Number of items per page |
| `sortBy` | createdAt | Field to sort by |
| `sortDir` | desc | Sort direction (asc/desc) |

### **User Paginated Endpoints**
| Endpoint | Description |
|----------|-------------|
| `GET /api/v1/users/paged` | Get all users (paginated) |
| `GET /api/v1/users/active/paged` | Get active users (paginated) |
| `GET /api/v1/users/inactive/paged` | Get inactive users (paginated) |
| `GET /api/v1/users/search/paged?keyword=...` | Search users (paginated) |
| `GET /api/v1/users/role/{roleName}/paged` | Get users by role (paginated) |

### **Role Paginated Endpoints**
| Endpoint | Description |
|----------|-------------|
| `GET /api/v1/roles/paged` | Get all roles (paginated) |
| `GET /api/v1/roles/search/paged?keyword=...` | Search roles (paginated) |

### **Example Request**
```
GET /api/v1/users/paged?page=0&size=10&sortBy=createdAt&sortDir=desc
```

### **Example Response**
```json
{
  "success": true,
  "data": {
    "content": [
      { "id": 1, "firstName": "John", "lastName": "Doe", ... }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false,
    "empty": false
  }
}
```

### **Repository Methods**
```java
// Paginated queries in UserRepository
Page<User> findByActiveTrue(Pageable pageable);
Page<User> findByActiveFalse(Pageable pageable);
Page<User> searchByKeyword(String keyword, Pageable pageable);
Page<User> findByRole_RoleName(String roleName, Pageable pageable);

// Paginated queries in RoleRepository
Page<Role> searchByKeyword(String keyword, Pageable pageable);
```

### **Service Layer Pattern**
```java
@Override
@Transactional(readOnly = true)
public PagedResponse<UserDTO> getAllUsers(Pageable pageable) {
    Page<User> userPage = userRepository.findAll(pageable);
    List<UserDTO> userDTOs = userPage.getContent().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    return PagedResponse.of(userPage, userDTOs);
}
```

### **Controller Pattern**
```java
@GetMapping("/paged")
public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getAllUsersPaged(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    PagedResponse<UserDTO> users = userService.getAllUsers(pageable);
    return ResponseEntity.ok(ApiResponse.success(users));
}
```

---

# ✔ **10. Testing Requirements**

## **JUnit 5 Test Coverage**

All APIs must have comprehensive JUnit test coverage:

### **Test Types**
* **Repository Tests** - Using `@DataJpaTest`
* **Service Tests** - Using `@ExtendWith(MockitoExtension.class)`
* **Controller Tests** - Using `@SpringBootTest` and `@AutoConfigureMockMvc`
* **Security Tests** - Testing JWT token generation and validation
* **Integration Tests** - Testing complete request/response flow

### **Test Profiles**
* Use `application-test.yml` with H2 in-memory database
* Flyway disabled for tests (using Hibernate DDL auto-create)

### **Test Coverage Requirements**
* All CRUD operations
* Authentication (login/register)
* Authorization (role-based access)
* Validation errors (400 Bad Request)
* Resource not found (404)
* Duplicate resources (409 Conflict)
* Unauthorized access (401)
* Forbidden access (403)

---

# ✔ **11. Expected Output from Claude**

Claude must produce:

### **A. Folder Structure**

Full clean architecture:

* controller
* service
* repository
* entity
* config
* dto
* security
* exception

### **B. pom.xml**

Containing:

* Spring Web
* Spring Data JPA
* Spring Security
* JWT (jjwt-api, jjwt-impl, jjwt-jackson)
* PostgreSQL Driver
* Flyway
* Lombok
* Validation
* H2 (test scope)
* Spring Boot Test
* Spring Security Test
* springdoc-openapi-ui (Swagger)
* springdoc-openapi-security
* spring-boot-starter-data-redis
* spring-boot-starter-cache
* redisson-hibernate-53

### **C. Spring Boot Code**

* Application class
* REST controllers (Health, User, Role, Auth)
* Service + Repository layer
* Entities with JPA (User, Role)
* Security configuration (JWT, filters, entry points)
* Swagger configuration (SwaggerConfig.java)
* Cache configuration (CacheConfig.java, RedisConfig.java)
* Exception handling
* DTOs (User, Role, Auth request/response)

### **D. Configuration Files**

* application.yml
* application-local.yml
* application-prod.yml
* application-test.yml
* Flyway config
* Database config
* JWT config
* Swagger config (springdoc settings)
* Redis config
* Cache TTL settings

### **E. Docker Files**

* Complete Dockerfile
* docker-compose.yml
* .env file

### **F. Test Files**

* Repository tests
* Service tests
* Controller tests
* Security tests

### **G. README**

Explain:

* Local run
* Production deployment
* Configuration changes
* Migration process
* pgAdmin usage
* Authentication flow
* API documentation
* Swagger UI access

---

# ✔ **12. Coding & Architecture Standards**

Follow:

* Layered architecture
* Clean code
* SOLID principles
* DTO pattern
* Java 8 Streams
* Optimistic locking (where needed)
* Proper error handling
* Avoid lazy-loading issues
* JWT-based stateless authentication
* Role-based access control (RBAC)
* Comprehensive test coverage

---

# 🎯 **Your First Task (For Claude)**

**Generate the complete backend project architecture, all configuration files, Docker setup, Flyway migration scripts, and documentation exactly as specified above.
Write every file clearly in separate code blocks.**

Now create a basic spring boot setup to support above requirements. Project name would be Logifin_backend
