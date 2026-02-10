# GitHub Copilot - Project Instructions

## Overview
This is a **Java 17+ with Quarkus 3.x** project following layered architecture (Controller → Service → Repository). Always use Quarkus CDI (`@Inject`, `@ApplicationScoped`) and RESTEasy Reactive for REST APIs. Multi-environment setup (dev/test/prod) with strict database isolation.

---

## 🚨 DATABASE SEPARATION (CRITICAL - READ FIRST!)

### 🔴 HISTORICAL PROBLEM: Production Data Loss
This project suffered **MULTIPLE LOSSES** of the production database due to incorrect configurations. The problem occurred when:
- Running `./mvnw quarkus:dev` locally → **DESTROYED** `quarkus_db` (production)
- Running `./mvnw test` → **DESTROYED** `quarkus_db` (production)
- Flyway with `clean-at-start=true` pointing to wrong database

### ✅ IMPLEMENTED SOLUTION: 3 Separate Databases

#### 1️⃣ **quarkus_db** (PRODUCTION - VPS)
- **NEVER** connect to this database locally!
- Used ONLY on VPS via Docker Compose
- Profile: `QUARKUS_PROFILE=prod`
- Flyway: `clean-at-start=false` (MANDATORY)
- Configuration: [application-prod.properties](src/main/resources/application-prod.properties)

#### 2️⃣ **quarkus_dev** (LOCAL DEVELOPMENT)
- Isolated database for development on MacBook
- Can be cleaned/reset without risks
- Profile: `QUARKUS_PROFILE=dev`
- Flyway: `clean-at-start=true` (allowed)
- Configuration: [application-dev.properties](src/main/resources/application-dev.properties)

#### 3️⃣ **quarkus_test** (TESTS)
- Dedicated database for tests (`./mvnw test`)
- Cleaned before each test execution
- Flyway: `clean-at-start=false` (avoids timeout, but recreated externally)
- Configuration: [src/test/resources/application.properties](src/test/resources/application.properties)

### 📦 Environment Configuration

#### **Local Development (MacBook)**
```bash
# 1. Check if PostgreSQL is running (quarkus_postgres container)
docker ps | grep quarkus_postgres

# 2. Load environment variables (CRITICAL - ALWAYS DO THIS FIRST!)
source .env

# 3. Verify profile (must be 'dev')
echo $QUARKUS_PROFILE

# 4. Run application (uses quarkus_dev)
./mvnw quarkus:dev
```
Access: `https://localhost:8443` (SSL enabled in dev). Hot reload active. Flyway cleans/migrates on start.

#### **Tests (MacBook)**
```bash
# Uses quarkus_test automatically (src/test/resources/application.properties)
./mvnw test

# Pretty output similar to Jest/NestJS
./test.sh
```

#### **Production (VPS)**
```bash
# Docker Compose creates and manages everything (uses quarkus_db)
docker compose up -d

# Verify it's using QUARKUS_PROFILE=prod
docker compose exec aguide-api env | grep QUARKUS_PROFILE
```

### 🔒 Implemented Protections

#### ✅ Environment Variables (.env)
- **NEVER committed to Git** (protected by `.gitignore`)
- Defines which database to use in each environment
- Example: `DB_DEV_NAME=quarkus_dev`, `DB_PROD_NAME=quarkus_db`

#### ✅ Quarkus Profiles
- **dev**: Points to `quarkus_dev`, allows `clean-at-start=true`
- **prod**: Points to `quarkus_db`, **FORBIDS** `clean-at-start=true`
- **test**: Points to `quarkus_test`, controlled by external scripts

#### ✅ Docker Compose
- `docker-compose.yml`: **Production VPS** (creates only the application, external PostgreSQL)
- Local PostgreSQL managed separately (already exists in Docker Desktop)

#### ✅ Security Validations
- Script: [validate-production-safety.sh](validate-production-safety.sh)
- Verifies `application-prod.properties` before deploy
- Blocks merge if it detects `clean-at-start=true` in prod

### ⚠️ INVIOLABLE RULES

#### 🔴 NEVER do:
- ❌ Connect to `quarkus_db` locally (only on VPS!)
- ❌ Use `clean-at-start=true` with `QUARKUS_PROFILE=prod`
- ❌ Commit `.env` file to Git
- ❌ Run `./mvnw quarkus:dev` without `source .env` first
- ❌ Assume the correct profile is active
- ❌ Modify existing Flyway migrations (causes checksum errors)
- ❌ Use `quarkus.hibernate-orm.database.generation` different from `none` in production
- ❌ Create destructive migrations (`DROP TABLE`, `TRUNCATE`) for production
- ❌ Merge develop→main without checking database configurations

#### ✅ ALWAYS do:
- ✅ Run `source .env` before any development operation
- ✅ Check `echo $QUARKUS_PROFILE` before running the application
- ✅ Confirm database with: `grep DB_DEV_NAME .env`
- ✅ Test locally with `quarkus_dev` before creating PR
- ✅ Run `validate-production-safety.sh` before merge
- ✅ Use incremental migrations only (ALTER TABLE ADD, CREATE INDEX, etc.)

### 📋 Checklist Before Running Code

**Before `./mvnw quarkus:dev`:**
- [ ] Did I run `source .env`?
- [ ] Am I using `QUARKUS_PROFILE=dev`?
- [ ] Is local PostgreSQL running? (`docker ps | grep postgres`)
- [ ] Does `quarkus_dev` database exist? (not `quarkus_db`)

**Before `./mvnw test`:**
- [ ] Does `quarkus_test` database exist?
- [ ] Am I not pointing to `quarkus_db` or `quarkus_dev`?

**Before creating PR/merge to main:**
- [ ] Does `application-prod.properties` have `clean-at-start=false`?
- [ ] Does `docker-compose.yml` use `QUARKUS_PROFILE=prod`?
- [ ] Did I run `validate-production-safety.sh`?
- [ ] Are all migrations incremental and non-destructive?

### 📖 Additional Documentation
- [QUICK_START.md](QUICK_START.md) - Database setup and daily workflows
- [.env.example](.env.example) - Configuration template
- [INCIDENT_PROD_DB_RESET_2026-02-09.md](a_error_log_temp/INCIDENT_PROD_DB_RESET_2026-02-09.md) - Incident that motivated these changes

---

## 🖥️ Development Environment (TECHNICAL DETAILS)

### LOCAL Environment (macOS/Linux)
- **DOES NOT use Docker** to run Quarkus application locally
- Application runs via **direct terminal**: `./mvnw quarkus:dev`
- PostgreSQL runs in **Docker** (container `quarkus_postgres`)
- Application connects to database via `jdbc:postgresql://localhost:5432/quarkus_dev`
- Local port: `https://localhost:8443` (HTTPS with self-signed certificate)

### PRODUCTION Environment (VPS)
- **Uses Docker Compose** (`docker-compose.yml`)
- Application and PostgreSQL in separate containers
- Deploy via automatic Jenkins pipeline
- Bridge network for communication between containers

### ⚠️ IMPORTANT RULE
**NEVER assume** the application is running in Docker locally. Always ask or verify with `docker ps` and `ps aux | grep quarkus` to identify the environment before suggesting restart or debug commands.

---

## Architecture Patterns

### Layered Structure (DDD by Feature)
```
src/main/java/br/com/aguideptbr/
├── features/          # Business functionalities (organized by domain)
│   ├── auth/          # Authentication and security (AuthenticationFilter)
│   ├── user/          # User management
│   │   ├── UserController.java
│   │   ├── UserService.java
│   │   ├── UserRepository.java
│   │   ├── UserModel.java (entity)
│   │   └── dto/
│   ├── content/       # Content records
│   ├── phone/         # Phone numbers (nested under users)
│   └── [other-feature]/
└── util/              # Shared utilities
```

### Repository Pattern
Use Panache active record pattern. Example from [PhoneNumberRepository.java](src/main/java/br/com/aguideptbr/features/phone/PhoneNumberRepository.java):
```java
@ApplicationScoped
public class PhoneNumberRepository implements PanacheRepositoryBase<PhoneNumberModel, UUID> {
    public List<PhoneNumberModel> findByUserId(UUID userId) {
        return find("userId", userId).list();
    }
}
```

### Project-Specific Conventions

#### UUID Primary Keys
All entities use UUID, not Long:
```java
@Entity
public class UserModel extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "UUID")
    public UUID id;
    // ...
}
```

#### Soft Deletes
Models include `deletedAt` timestamp for soft deletion:
```java
@Column(name = "deleted_at")
public LocalDateTime deletedAt;
```
Query active records: `find("deletedAt is null")`

#### API Versioning
Always include `/api/v1/` in paths. Future versions will be `/api/v2/`, etc.

#### Pagination
Use Quarkus Panache Page API:
```java
List<UserModel> users = UserModel.find("deletedAt is null")
    .page(Page.of(pageNumber, pageSize))
    .list();
```
Return `PaginatedResponse<T>` wrapper (see [util/PaginatedResponse.java](src/main/java/br/com/aguideptbr/util/PaginatedResponse.java)).

---

## 📂 File and Directory Organization

- **Production and Structure Files:** The agent has full permission to create and edit essential files in the project root, such as `Dockerfile`, `Jenkinsfile`, `pom.xml`, `.gitignore`, and configuration files.
- **Source Code:** The `src/main/java/` folder is the project core. The agent should manipulate, create or refactor modules within this folder according to development requests.
- **Temporary and Draft Files (CRITICAL RULE):**
  - **Mandatory Location:** `a_error_log_temp/`
  - Test files should follow this pattern (`src/test/java/br/com/aguideptbr/features/[FEATURE_NAME]/[JAVA_FILE_NAME]Test.java`),
  that is, save tests in the correct structure within `src/test/java/...` respecting the project's organization by features.
  - Documentation drafts (`*.md`), text files for data manipulation or debug logs generated by the agent **MUST** be created exclusively within `a_error_log_temp/`.
  - **Prohibition:** Never create "reasoning support" or "quick test" files in the project root. If it's not an official configuration file or production code, it belongs to `a_error_log_temp/`.

---

## Code Conventions

### ✅ Field Encapsulation (Sonar: java:S1104) - CRITICAL

**FUNDAMENTAL RULE:** Class fields should **NEVER** be `public` (except in Panache entities).

#### ❌ FORBIDDEN (violates java:S1104):
```java
public class ErrorResponse {
    public String error;        // ❌ Public field
    public String message;      // ❌ Public field
    public LocalDateTime timestamp; // ❌ Public field
}

public class LoginRequest {
    public String email;        // ❌ Public field
    public String password;     // ❌ Public field
}
```

#### ✅ CORRECT (proper encapsulation):

**For DTOs and Utility Classes:**
```java
public class ErrorResponse {
    private String error;       // ✅ Private
    private String message;     // ✅ Private
    private LocalDateTime timestamp; // ✅ Private

    // Constructor
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Mandatory getters (Jackson needs them for JSON serialization)
    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;       // ✅ Private

    @NotBlank(message = "Password is required")
    private String password;    // ✅ Private

    // Getters and Setters (needed for Bean Validation and Jackson)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

**For Constants:**
```java
public class Constants {
    // ✅ Constants can be public static final
    public static final String TOKEN_TYPE = "Bearer";
    public static final int MAX_ATTEMPTS = 3;
}
```

**Exception - Panache Entities:**
```java
@Entity
@Table(name = "users")
public class UserModel extends PanacheEntity {
    // ✅ Panache allows public fields by framework convention
    public String name;
    public String email;

    // But methods with logic must exist
    public boolean isActive() {
        return deletedAt == null;
    }
}
```

#### 🎯 Benefits of Encapsulation:
- **Access Control:** Defines who can read/write data
- **Validation:** Allows adding logic in setters
- **Debugging:** Facilitates tracking changes via breakpoints
- **Maintainability:** Internal changes don't affect external code
- **Sonar Compliance:** Meets java:S1104 and improves code quality

#### 📋 Class Creation Checklist:
- [ ] All fields are `private` (except `static final` constants and Panache entities)?
- [ ] Getters are present for all fields that need external access?
- [ ] Setters are present only for mutable fields?
- [ ] Bean Validation works with getters/setters (`@NotBlank`, `@Email`, etc.)?
- [ ] Jackson can serialize/deserialize with getters/setters?

### ✅ Naming Convention (Sonar: java:S117)
- **Local variables and parameters** should use **camelCase** (e.g.: `titleText`).
- **Avoid snake_case** in variables and parameters (e.g.: `title_txt`).
- **Constants** can use **UPPER_SNAKE_CASE** (e.g.: `TOKEN_TYPE`).

### 1. REST Controllers
- Use `@Path("/api/v1/resource")` on class (always version APIs)
- Methods annotated with `@GET`, `@POST`, `@PUT`, `@DELETE`
- Return `Response` or `Uni<Response>` (reactive)
- Validate input with Bean Validation (`@Valid`)
- Mandatory logs: request entry (`log.infof("GET /api/v1/users/%s", id)`)
- Return proper HTTP status (200, 201, 204, 400, 404)
- **Never** put business logic in controllers
```java
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {
    @Inject UserService userService;
    @Inject Logger log;

    @GET
    public Response findAll() {
        log.info("GET /api/v1/users - Listing all users");
        return Response.ok(userService.findAll()).build();
    }
}
```

Example pattern from [UserController.java](src/main/java/br/com/aguideptbr/features/user/UserController.java#L36-L70).

### 2. Services
- Annotated with `@ApplicationScoped` (singleton)
- Contains business logic
- Inject repositories with `@Inject`
- Use `@Transactional` for CUD operations (CREATE, UPDATE, DELETE)
- Validate business rules here
- Throw specific exceptions (e.g., `WebApplicationException` with proper status)
```java
@ApplicationScoped
public class UserService {
    @Inject UserRepository userRepository;
    @Inject Logger log;

    @Transactional
    public User create(User user) {
        // business logic
    }
}
```

### 3. Repositories
- Extend `PanacheRepository<Entity>` or `PanacheRepositoryBase<Entity, UUID>` for custom IDs
- Custom query methods follow `findByXxx` pattern
- Do not place business logic here
- **Never** use `@Transactional` in repository (use in service)
```java
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserModel, UUID> {
    public UserModel findByEmail(String email) {
        return find("email", email).firstResult();
    }
}
```

### 4. Entities
- Inherit from `PanacheEntity` (auto-generated id) OR use `PanacheEntityBase` with custom `@Id`
- Use `@Entity`, `@Table`, `@Column`
- Always include audit fields:
```java
@Entity
@Table(name = "users")
public class UserModel extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "UUID")
    public UUID id;

    @Column(nullable = false, length = 100)
    public String name;

    @Column(unique = true, nullable = false)
    public String email;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;  // Soft delete
}
```

---

## Authentication & Security

### JWT Authentication (CRITICAL - Lessons Learned)
Custom JWT filter in [AuthenticationFilter.java](src/main/java/br/com/aguideptbr/features/auth/AuthenticationFilter.java):
- **Intercepts all requests** (`@Provider`, `@Priority(Priorities.AUTHENTICATION)`)
- **Public paths:** `/api/v1/auth/*`, `/q/health`, `/q/swagger-ui`
- **Validates JWT** with granular exceptions (TokenMissingException, TokenExpiredException, TokenMalformedException, TokenInvalidException)
- **JWT keys** in `security/` directory (git-ignored, must exist locally)

#### JWT Configuration
JWT config in [application.properties](src/main/resources/application.properties#L67-L91):
- `mp.jwt.verify.publickey.location=security/jwt-public.pem`
- `mp.jwt.sign.key.location=security/jwt-private.pem`
- `quarkus.smallrye-jwt.enabled=false` (we use custom filter)

#### JWT Key Generation (Correct Command)
```bash
# Generate RSA 2048-bit private key in PKCS#8 format
openssl genpkey -algorithm RSA -out security/jwt-private.pem -pkeyopt rsa_keygen_bits:2048

# Extract public key
openssl rsa -pubout -in security/jwt-private.pem -out security/jwt-public.pem

# Set correct permissions
chmod 600 security/jwt-private.pem
chmod 644 security/jwt-public.pem
```

Or use the provided script: `./generate-jwt-keys.sh`

#### JWT Token Structure
- **Header:** `{"alg": "RS256", "typ": "JWT"}`
- **Payload:** Claims (iss, sub, upn, email, name, surname, role, iat, exp)
- **Signature:** SHA256withRSA using private key
- **Final Format:** `base64url(header).base64url(payload).base64url(signature)`

#### Security Configuration
- `AuthenticationFilter` validates JWT tokens in requests
- `@RolesAllowed` for role-based access control
- **Never commit:** private keys, credentials, tokens
- **Keys in Production:** Use environment variables or secrets manager

#### Test Credentials (Development)
- Email: `contato@aguide.space`
- Password: `admin123`
- Role: `ADMIN`
- BCrypt Hash: `$2a$10$1b.v1jTmdr.c1XJXM10bsO.YwcpgZkXszAivtIL6VgfUQF2RhMIBy`

**Complete Documentation:** See `a_error_log_temp/SAGA_JWT_AUTHENTICATION_FIX.md`

---

## Database Management

### Flyway Migrations (STRICT)
Location: `src/main/resources/db/migration/`
Naming: `V{major}.{minor}.{patch}__{description}.sql` (e.g., `V1.0.0__Create_tables.sql`)

**Rules**:
- **NEVER modify already applied migrations** (causes checksum errors, use repair or create new migration)
- Each migration is immutable
- Repair mode auto-enabled: `quarkus.flyway.repair-at-start=true`
- Dev profile cleans on start: `quarkus.flyway.clean-at-start=true` (dev only!)
- Prod never cleans: See [application-prod.properties](src/main/resources/application-prod.properties)
- **PostgreSQL in Production and Tests**: Same migrations are used in both environments (quarkus_db and quarkus_test)
- **ALWAYS use `ON CONFLICT DO NOTHING`** for initial data INSERTs (idempotency)

### 📋 Safe Migration Format

✅ **ALLOWED** (non-destructive):
```sql
-- V1.0.5__Add_status_column.sql
ALTER TABLE content_records ADD COLUMN status VARCHAR(20);
UPDATE content_records SET status = 'ACTIVE' WHERE status IS NULL;
ALTER TABLE content_records ALTER COLUMN status SET NOT NULL;

CREATE INDEX idx_content_status ON content_records(status);
```

❌ **FORBIDDEN** (destructive):
```sql
-- ❌ NEVER DO THIS IN PRODUCTION:
DROP TABLE content_records;
TRUNCATE TABLE users;
DROP SCHEMA public CASCADE;
ALTER TABLE content_records DROP COLUMN important_data;
```

### Hibernate ORM
Schema generation **disabled**: `quarkus.hibernate-orm.database.generation=none`
Flyway manages all schema changes. **Never use Hibernate to generate/update schema.**

#### ✅ MANDATORY Configurations for Production:
```properties
# ✅ ALWAYS USE IN PRODUCTION (application-prod.properties):
quarkus.hibernate-orm.database.generation=none
quarkus.flyway.clean-at-start=false
quarkus.flyway.migrate-at-start=true
quarkus.flyway.baseline-on-migrate=true
```

#### ✅ ALLOWED Configurations for Development:
```properties
# ✅ ALLOWED IN application-dev.properties:
quarkus.hibernate-orm.database.generation=none
quarkus.flyway.clean-at-start=true  # OK for develop branch
quarkus.flyway.migrate-at-start=true
```

---

## Exception Handling & Error Handling
- Use `@ServerExceptionMapper` for global handling
- Never expose stacktraces to client in production
- Throw `WebApplicationException` with proper status and JSON body
- Return structured JSON:
```java
throw new WebApplicationException(
    Response.status(404)
        .entity(Map.of(
            "error", "User not found",
            "message", "User with ID 123 not found",
            "timestamp", LocalDateTime.now()
        ))
        .build()
);
```

---

## Logging
- Inject JBoss `Logger`: `@Inject Logger log;`
- Levels: `log.info()` for normal operations, `log.error()` for errors, `log.debug()` for debugging
- Always log: start of important operations, errors with stacktrace
- Sensitive data should NOT be logged (passwords, tokens)
- **Forbidden to use `System.out/err`** (Sonar: Replace this use of System.out by a logger)

---

## Configuration
- Use `application.properties` for common configurations
- Use `application-dev.properties` and `application-prod.properties` for environment-specific settings
- Access configs with `@ConfigProperty(name = "key") String value;`

---

## Tests

### Location
- Tests: `src/test/java/br/com/aguideptbr/features/[feature]/`
- Use `@QuarkusTest` for integration tests
- Use `RestAssured` to test endpoints
- Desired minimum coverage: 80%

### Unit Testing Best Practices (FOCUS)
- **Focus on business rules** (Service) and critical flows.
- **Isolate dependencies** with mocks (Repository, external gateways).
- **Negative tests are mandatory**: validate expected errors/exceptions.
- **Avoid weak tests** (getters/setters without logic and implementation duplication).
- **Determinism**: no dependency on real date/time, network, execution order.
- **If the test needs `@QuarkusTest`**, it's probably integration, not unit.

### When to Create Unit Tests
- Rules with multiple branches (if/else, validations, authorization).
- Calculations, transformations and normalizations.
- Recurring bugs (tests prevent regression).
- Expected error cases (e.g.: invalid password, non-existent resource).

### Test Configuration (CRITICAL)
**ALWAYS create `src/test/resources/application.properties` with:**
```properties
# Disable AuthenticationFilter in tests
quarkus.arc.exclude-types=br.com.aguideptbr.features.auth.AuthenticationFilter

# Disable JWT in tests (avoids public key not found error)
quarkus.smallrye-jwt.enabled=false

# WORKAROUND: Mesmo com JWT desabilitado, o Quarkus valida a config
mp.jwt.sign.key-content=-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDUBQ==\n-----END PRIVATE KEY-----
mp.jwt.verify.publickey.location=META-INF/resources/publicKey.pem

# Use PostgreSQL with dedicated database for tests (quarkus_test)
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://${DB_TEST_HOST:localhost}:${DB_TEST_PORT:5432}/${DB_TEST_NAME:quarkus_test}
quarkus.datasource.username=${DB_TEST_USERNAME:quarkus}
quarkus.datasource.password=${DB_TEST_PASSWORD:quarkus123}

# Flyway in tests - USES SAME MIGRATIONS AS PRODUCTION
quarkus.flyway.clean-at-start=false
quarkus.flyway.migrate-at-start=true
quarkus.flyway.baseline-on-migrate=true

# Hibernate in tests
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.log.sql=false
```

Test config: [src/test/resources/application.properties](src/test/resources/application.properties)
- Authentication disabled (`quarkus.arc.exclude-types=...AuthenticationFilter`)
- JWT disabled in tests (`quarkus.smallrye-jwt.enabled=false`)

### Test Rules
✅ **ALLOWED:**
- Disable authentication filters via `quarkus.arc.exclude-types`
- Use PostgreSQL with dedicated database `quarkus_test` (isolated from production)
- RestAssured without authentication headers in tests
- Flyway `clean-at-start=false` to ensure clean environment at each test

❌ **FORBIDDEN:**
- Hardcoded tokens/passwords in test code
- Use `-DskipTests` in Jenkins/CI (tests are quality barrier)
- Skip tests to "quickly fix" authentication problems
- Connect to `quarkus_db` (production) during tests - ALWAYS use `quarkus_test`
- Create separate migrations for tests (use the same as production)

---

## CI/CD Pipeline

### Jenkins
Jenkins pipelines in workspace root:
- `Jenkinsfile` - Main dev/staging pipeline
- `Jenkinsfile.test` - Test branch
- `Jenkinsfile.production` - Production deployment

Uses SonarQube for code quality. Health checks via validation scripts (`validate-*.sh`).

### 🚨 GitHub Actions and CI/CD (CRITICAL)

**IDENTIFIED PROBLEM:**
GitHub Actions can cause data loss if it doesn't validate the profile before deploy!

**MANDATORY Verifications in deploy workflow:**
```yaml
- name: ⚠️ Verify production configuration
  run: |
    grep -q "quarkus.flyway.clean-at-start=false" src/main/resources/application-prod.properties || exit 1
    grep -q "quarkus.hibernate-orm.database.generation=none" src/main/resources/application-prod.properties || exit 1
    echo "✅ Production configurations verified"

- name: ⚠️ Validate docker-compose.yml on VPS
  script: |
    cd /opt/apps/aguide-api-quarkus
    grep -q "QUARKUS_PROFILE=prod" docker-compose.yml || (echo "❌ INCORRECT PROFILE!" && exit 1)
    echo "✅ Production profile confirmed"
```

**NEVER in production deploy:**
- ❌ `docker compose down` without checking persistent volumes
- ❌ `docker compose build --no-cache` without validating configurations
- ❌ Deploy without confirming `QUARKUS_PROFILE=prod`
- ❌ Database rebuild (use only migrations)

**SAFE command for deploy:**
```bash
cd /opt/apps/aguide-api-quarkus
git pull origin main
# Verify profile before any operation
grep -q "QUARKUS_PROFILE=prod" docker-compose.yml || exit 1
# Only update the application service (don't touch postgres)
docker compose up -d --no-deps --build aguide-api
docker system prune -f
```

---

## Docker
- Dockerfiles in `src/main/docker/`
- Prefer `Dockerfile.jvm` for development
- `Dockerfile.native` for production (GraalVM)
- `docker-compose.yml`: VPS production deployment

---

## Git Commands and User Interaction

- Whenever the agent is about to suggest Git commands that can alter the state of local or remote branch, such as `git commit`, `git push`, `git reset`, `git rebase`, `git pull --rebase`, `git push --force` or similar, it must **mandatorily ask the developer user** if it can proceed with executing these commands.
- The agent should warn the user about the potential risk of "messing up" the current branch, explaining that these commands can modify the history or content of local and remote branch.
- Only after explicit confirmation from the user, should the agent suggest or execute Git commands that change the local or remote branch.
- For Git commands that do not change the branch state (like `git status`, `git log`, `git diff`), the agent can suggest or execute without needing confirmation.

### Adding Files to Stage (git add)

- **Under no circumstances** should the agent suggest batch addition commands like `git add .`, `git add -A`, or `git add --all`.
- All files must be added individually using `git add <file-path>` after being explicitly listed and reviewed with the user.
- This prevents accidental inclusion of temporary files, logs, credentials or other unwanted artifacts in the commit.

Expected behavior example:

User: "Add my changes and commit."

Agent: "I'll add the following files individually:
- `src/main/java/br/com/aguideptbr/features/user/UserService.java`
- `src/test/java/br/com/aguideptbr/features/user/UserServiceTest.java`

Confirm adding these files to stage?"

User: "Yes."

Agent:
```bash
git add src/main/java/br/com/aguideptbr/features/user/UserService.java
git add src/test/java/br/com/aguideptbr/features/user/UserServiceTest.java
```

Now I'll commit your changes. This will modify the local branch history. Do you want to continue?

User: "Yes."

Agent:
```bash
git commit -m "feat(user): implement new feature X"
```

---

## WHAT NOT TO DO (COMPREHENSIVE)
❌ Create temporary files in project root (use `a_error_log_temp/`)
❌ Put business logic in Controllers or Repositories
❌ Use Spring annotations (use Quarkus CDI)
❌ Forget `@Transactional` in methods that modify data
❌ Create packages outside `br.com.aguideptbr`
❌ Ignore exception handling
❌ Log sensitive information (passwords, tokens)
❌ Hardcoded credentials/tokens in tests
❌ Skip tests in CI/CD with `-DskipTests`
❌ Use real database (PostgreSQL) in unit tests
❌ **NEVER** use `quarkus.flyway.clean-at-start=true` in production
❌ **NEVER** use `quarkus.hibernate-orm.database.generation` different from `none` in production
❌ **NEVER** create destructive migrations (`DROP TABLE`, `TRUNCATE`) for production
❌ **NEVER** merge develop→main without checking database configurations
❌ **NEVER** assume the correct profile will be used automatically
❌ **NEVER** run `./mvnw quarkus:dev` without `source .env` first
❌ **NEVER** commit `.env` file to Git
❌ **NEVER** modify existing Flyway migrations
❌ Using `QUARKUS_PROFILE=prod` locally (connects to production DB!)
❌ Putting business logic in controllers (violates separation of concerns)
❌ Direct repository access from controllers (always go through services)
❌ Use batch git add commands (`git add .`, `git add -A`)

---

## Quarkus Resources to Use
✅ Dev Mode: `./mvnw quarkus:dev` (automatic hot reload)
✅ Dev Services: databases automatically in containers
✅ Panache: JPA/Hibernate simplification
✅ RESTEasy Reactive: improved performance
✅ SmallRye Health: `/q/health` endpoints
✅ OpenAPI/Swagger: `/q/swagger-ui` (dev only)

---

## Key Configuration Files
- [application.properties](src/main/resources/application.properties) - Base config
- [application-dev.properties](src/main/resources/application-dev.properties) - Dev overrides
- [application-prod.properties](src/main/resources/application-prod.properties) - Prod overrides
- [src/test/resources/application.properties](src/test/resources/application.properties) - Test config
- [docker-compose.yml](docker-compose.yml) - VPS production deployment
- [pom.xml](pom.xml) - Maven dependencies (Quarkus 3.23.3, Java 17)

---

## Reference Documentation
- [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) - Comprehensive dev guide with code templates
- [QUICK_START.md](QUICK_START.md) - Database setup and daily workflows
- [README.md](README.md) - Quarkus basics and troubleshooting
- Quarkus guides: https://quarkus.io/guides/

---

**Important:** When generating code, always check if you are following these guidelines. In case of doubt, consult the referenced documentation files in the project root.
