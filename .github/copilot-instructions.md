# GitHub Copilot - Project Instructions

## Overview
This is a **Java 17+ with Quarkus 3.x** project following layered architecture (Controller → Service → Repository). Always use Quarkus CDI (`@Inject`, `@ApplicationScoped`) and RESTEasy Reactive for REST APIs.

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

# 2. Load environment variables
source .env

# 3. Run application (uses quarkus_dev)
./mvnw quarkus:dev
```

#### **Tests (MacBook)**
```bash
# Uses quarkus_test automatically (src/test/resources/application.properties)
./mvnw test
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
- ❌ Run `./mvnw quarkus:dev` without checking `.env`
- ❌ Assume the correct profile is active

#### ✅ ALWAYS do:
- ✅ Check `echo $QUARKUS_PROFILE` before running the application
- ✅ Use `source .env` before `./mvnw quarkus:dev`
- ✅ Confirm database with: `grep DB_DEV_NAME .env`
- ✅ Test locally with `quarkus_dev` before creating PR
- ✅ Run `validate-production-safety.sh` before merge

### 📋 Checklist Before Running Code

**Before `./mvnw quarkus:dev`:**
- [ ] Did I run `source .env`?
- [ ] Am I using `QUARKUS_PROFILE=dev`?
- [ ] Is local PostgreSQL running? (`docker ps | grep postgres`)
- [ ] Does `quarkus_dev` database exist? (not `quarkus_db`)

**Before `./mvnw test`:**
- [ ] Does `quarkus_test` database exist?
- [ ] Am I not pointing to `quarkus_db` or `quarkus_dev`

**Before creating PR/merge to main:**
- [ ] Does `application-prod.properties` have `clean-at-start=false`?
- [ ] Does `docker-compose.yml` use `QUARKUS_PROFILE=prod`?
- [ ] Did I run `validate-production-safety.sh`?

### 📖 Additional Documentation
- [.env.example](.env.example) - Configuration template
- [INCIDENT_PROD_DB_RESET_2026-02-09.md](a_error_log_temp/INCIDENT_PROD_DB_RESET_2026-02-09.md) - Incident that motivated these changes

---

## 🖥️ Development Environment (TECHNICAL DETAILS)

### LOCAL Environment (macOS/Linux)
- **DOES NOT use Docker** to run Quarkus application locally
- Application runs via **direct terminal**: `./mvnw quarkus:dev`
- PostgreSQL runs in **Docker** (container `quarkus_postgres`)
- Application connects to database via `jdbc:postgresql://localhost:5432/quarkus_db`
- Local port: `https://localhost:8443` (HTTPS with self-signed certificate)

### PRODUCTION Environment (VPS)
- **Uses Docker Compose** (`docker-compose.yml`)
- Application and PostgreSQL in separate containers
- Deploy via automatic Jenkins pipeline
- Bridge network for communication between containers

### ⚠️ IMPORTANT RULE
**NEVER assume** the application is running in Docker locally. Always ask or verify with `docker ps` and `ps aux | grep quarkus` to identify the environment before suggesting restart or debug commands.

## MANDATORY Package Structure
```
br.com.aguideptbr/
├── features/          # Business functionalities (organized by domain)
│   ├── auth/          # Authentication and security (feature)
│   ├── user/
│   │   ├── UserController.java
│   │   ├── UserService.java
│   │   ├── UserRepository.java
│   │   └── User.java (entity)
│   └── [other-feature]/
└── util/              # Shared utilities
```

---

### 📂 File and Directory Organization

- **Production and Structure Files:** The agent has full permission to create and edit essential files in the project root, such as `Dockerfile`, `Jenkinsfile`, `pom.xml`, `.gitignore`, and configuration files.
- **Source Code:** The `src/main/java/` folder is the project core. The agent should manipulate, create or refactor modules within this folder according to development requests.
- **Temporary and Draft Files (CRITICAL RULE):**
  - **Mandatory Location:** `a_error_log_temp/`
  - Test files should follow this pattern (`src/test/java/br/com/aguideptbr/features/[FEATURE_NAME]/[JAVA_FILE_NAME]Test.java`),
  that is, save tests in the correct structure within `src/test/java/...` respecting the project's organization by features.
  - Documentation drafts (`*.md`), text files for data manipulation or debug logs generated by the agent **MUST** be created exclusively within `a_error_log_temp/`.
  - **Prohibition:** Never create "reasoning support" or "quick test" files in the project root. If it's not an official configuration file or production code, it belongs to `a_error_log_temp/`.

  ## 🤖 Agent Behavior in File Creation

1. **Scope Identification:** Before creating a file, the agent should classify:
   - *Is it essential for pipeline or deploy operation?* (Ex: `pom.xml`, `Dockerfile`, `Jenkinsfile`) -> **Root**.
   - *Is it a test, draft, data dump or auxiliary file?* -> **a_error_log_temp/**.
2. **Automatic Cleanup:** When suggesting new test scripts, the agent should name them as `a_error_log_temp/test_resource_name.sh` by default.

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
- Use `@Path("/api/v1/resource")` on class
- Methods annotated with `@GET`, `@POST`, `@PUT`, `@DELETE`
- Return `Response` or `Uni<Response>` (reactive)
- Validate input with Bean Validation (`@Valid`)
- Mandatory logs: request entry and errors
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

### 2. Services
- Annotated with `@ApplicationScoped`
- Contains business logic
- Inject repositories with `@Inject`
- Transactions with `@Transactional` when needed
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
- Extend `PanacheRepository<Entity>` or use `PanacheEntity`
- Custom query methods follow `findByXxx` pattern
- Do not place business logic here
```java
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public User findByEmail(String email) {
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
public class User extends PanacheEntity {
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
}
```

## Exception Handling
- Use `@ServerExceptionMapper` for global handling
- Never expose stacktraces to client in production
- Return structured JSON:
```java
{
  "error": "User not found",
  "message": "User with ID 123 not found",
  "timestamp": "2026-01-31T10:30:00Z"
}
```

## Logging
- Inject JBoss `Logger`: `@Inject Logger log;`
- Levels: `log.info()` for normal operations, `log.error()` for errors, `log.debug()` for debugging
- Always log: start of important operations, errors with stacktrace, sensitive data should NOT be logged
- **Forbidden to use `System.out/err`** (Sonar: Replace this use of System.out by a logger)

## Configuration
- Use `application.properties` for common configurations
- Use `application-dev.properties` and `application-prod.properties` for environment-specific settings
- Access configs with `@ConfigProperty(name = "key") String value;`

---

## ⚠️ PRODUCTION DATABASE PROTECTION (CRITICAL)

### 🚨 INVIOLABLE RULES - MAIN DATABASE

The production database (`jdbc:postgresql://quarkus_postgres:5432/quarkus_db`) should **NEVER** be destroyed or recreated. This is an **ABSOLUTE** and **NON-NEGOTIABLE** rule.

#### 🔴 FORBIDDEN Configurations in Production:
```properties
# ❌ NEVER USE THIS IN PRODUCTION:
quarkus.flyway.clean-at-start=true
quarkus.hibernate-orm.database.generation=drop
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.database.generation=create
quarkus.hibernate-orm.database.generation=create-drop
```

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

### 🛡️ Branch Protection

#### Branch `main` (PRODUCTION):
- **ALWAYS** use `prod` profile in `docker-compose.yml`: `QUARKUS_PROFILE=prod`
- **NEVER** allow `clean-at-start=true` in merges to main
- **VERIFY** `application-prod.properties` before each merge
- **ONLY** incremental non-destructive migrations are allowed

#### Branch `develop-data-objects` (DEVELOPMENT):
- **ALLOWED** to use `clean-at-start=true` for development
- **ALLOWED** to recreate database locally for tests
- **MANDATORY** to review configurations before creating PR to main

### ✅ Checklist Before Merge develop → main

**BEFORE creating PR from develop to main, VERIFY:**

1. [ ] `application-prod.properties` has `quarkus.flyway.clean-at-start=false`
2. [ ] `application-prod.properties` has `quarkus.hibernate-orm.database.generation=none`
3. [ ] `docker-compose.yml` uses `QUARKUS_PROFILE=prod`
4. [ ] No migration contains `DROP DATABASE`, `DROP SCHEMA` or `TRUNCATE`
5. [ ] All migrations are incremental (only `ALTER TABLE ADD`, `CREATE INDEX`, etc.)
6. [ ] Tested the migration locally without `clean-at-start`

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

### 🚨 What Happens If This Rule Is Violated?

**CATASTROPHIC CONSEQUENCES:**
- Total loss of production data
- Application downtime
- Loss of user trust
- Impossibility of recovery (without backup)

### 🔧 How to Recover If Database Was Destroyed?

1. **Immediately stop** the application
2. **Restore** from the last available backup
3. **Verify** configurations before restarting
4. **Never** deploy without reviewing configs

### 📝 When Creating New Features

**ALWAYS ask:**
- "Is this migration incremental and non-destructive?"
- "Did I test without `clean-at-start=true`?"
- "Is the production configuration protected?"

**NEVER assume:**
- That Hibernate will "manage" the schema in production
- That `clean-at-start` is disabled by default
- That the correct profile will be used automatically

### 🤖 GitHub Actions and CI/CD (CRITICAL)

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

## Database Migrations
- Use Flyway in `src/main/resources/db/migration/`
- Naming: `V[major].[minor].[patch]__[Description].sql`
- Example: `V1.0.3__Add_user_role_column.sql`
- **NEVER modify already applied migrations**
- **PostgreSQL in Production and Tests**: Same migrations are used in both environments (quarkus_db and quarkus_test)
- **ALWAYS use `ON CONFLICT DO NOTHING`** for initial data INSERTs (idempotency)

## Tests
- Location: `src/test/java/br/com/aguideptbr/features/[feature]/`
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

# Use PostgreSQL with dedicated database for tests (quarkus_test)
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=${QUARKUS_DATASOURCE_JDBC_URL:jdbc:postgresql://quarkus_postgres:5432/quarkus_test}
quarkus.datasource.username=${QUARKUS_DATASOURCE_USERNAME:quarkus}
quarkus.datasource.password=${QUARKUS_DATASOURCE_PASSWORD:quarkus123}

# Flyway in tests - USES SAME MIGRATIONS AS PRODUCTION
quarkus.flyway.clean-at-start=true
quarkus.flyway.migrate-at-start=true
# Default location: classpath:db/migration (no need to specify)
```

**Important about Migrations:**
- Production and tests use **PostgreSQL** (quarkus_db and quarkus_test)
- **SAME migrations** are used in both environments
- Flyway executes `clean-at-start=true` in tests to ensure clean environment
- No need to create separate migrations or adapt syntax

**Important about JWT in Tests:**
- **ALWAYS** configure `quarkus.smallrye-jwt.enabled=false` in tests
- This completely disables the SmallRye JWT extension, avoiding attempts to load keys
- Combined with `quarkus.arc.exclude-types` from AuthFilter, ensures tests run without authentication

### Test Rules
✅ **ALLOWED:**
- Disable authentication filters via `quarkus.arc.exclude-types`
- Use PostgreSQL with dedicated database `quarkus_test` (isolated from production)
- RestAssured without authentication headers in tests
- Flyway `clean-at-start=true` to ensure clean environment at each test

❌ **FORBIDDEN:**
- Hardcoded tokens/passwords in test code
- Use `-DskipTests` in Jenkins/CI (tests are quality barrier)
- Skip tests to "quickly fix" authentication problems
- Connect to `quarkus_db` (production) during tests - ALWAYS use `quarkus_test`
- Create separate migrations for tests (use the same as production)

## Security

### JWT Authentication (CRITICAL - Lessons Learned)
- **MANUAL JWT Implementation**: Do not use SmallRye JWT Builder (`io.smallrye.jwt.build.Jwt`)
- **Reason**: SmallRye JWT has parsing problems with RSA PKCS#8 keys generated by OpenSSL
- **Current Solution**: Manual JWT signing using `java.security.Signature` in `JWTService.java`
- **Key Format**: PKCS#8 inline in `application.properties` via `mp.jwt.sign.key-content`

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

#### JWT Token Structure
- **Header:** `{"alg": "RS256", "typ": "JWT"}`
- **Payload:** Claims (iss, sub, upn, email, name, surname, groups, iat, exp)
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

## Docker
- Dockerfiles in `src/main/docker/`
- Prefer `Dockerfile.jvm` for development
- `Dockerfile.native` for production (GraalVM)

## CI/CD
- Jenkins configured (see `Jenkinsfile`)
- SonarQube integrated for code analysis
- Maven build: `./mvnw clean package`

## WHAT NOT TO DO
❌ Create temporary files in project root
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

## Quarkus Resources to Use
✅ Dev Mode: `./mvnw quarkus:dev` (automatic hot reload)
✅ Dev Services: databases automatically in containers
✅ Panache: JPA/Hibernate simplification
✅ RESTEasy Reactive: improved performance
✅ SmallRye Health: `/q/health` endpoints
✅ OpenAPI/Swagger: `/q/swagger-ui`

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
``bash
git add src/main/java/br/com/aguideptbr/features/user/UserService.java
git add src/test/java/br/com/aguideptbr/features/user/UserServiceTest.java
``

Now I'll commit your changes. This will modify the local branch history. Do you want to continue?

User: "Yes."

Agent:
``bash
git commit -m "feat(user): implement new feature X"
``

---
**Important:** When generating code, always check if you are following these guidelines. In case of doubt, consult the `DEVELOPMENT_GUIDE.md` file in the project root.
