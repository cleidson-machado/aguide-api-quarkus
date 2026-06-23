# AI Assistant Configuration

This file provides instructions and context for AI assistants (Abacus AI CLI, GitHub Copilot, Cursor, etc.) working on this project.

---

## 🎯 Project Overview

**Type:** REST API Backend for Mobile Applications  
**Framework:** Quarkus 3.23.3 (Java 17+)  
**Database:** PostgreSQL  
**Architecture:** Layered (Controller → Service → Repository → Entity)  
**ORM:** Hibernate ORM with Panache  
**Build Tool:** Maven (with wrapper `./mvnw`)

---

## 🚨 CRITICAL: Database Isolation (READ THIS FIRST!)

This project uses **3 separate databases** to prevent production data loss:

### Databases

1. **`quarkus_db` (PRODUCTION - VPS ONLY)**
   - ❌ NEVER connect to this database locally
   - ✅ Only used on VPS via Docker Compose
   - Profile: `QUARKUS_PROFILE=prod`
   - Flyway: `clean-at-start=false` (MANDATORY)

2. **`quarkus_dev` (LOCAL DEVELOPMENT)**
   - ✅ Use for all local development
   - Profile: `QUARKUS_PROFILE=dev` (default)
   - Flyway: `clean-at-start=true` allowed
   - Connection: `localhost:5432/quarkus_dev`

3. **`quarkus_test` (TESTING)**
   - ✅ Used by `./mvnw test`
   - Automatically configured in `src/test/resources/application.properties`
   - Isolated from dev and prod

### ⚠️ INVIOLABLE RULES

**NEVER:**
- ❌ Connect to `quarkus_db` locally (production only!)
- ❌ Use `clean-at-start=true` with `QUARKUS_PROFILE=prod`
- ❌ Commit `.env` file to Git
- ❌ Execute code without checking `QUARKUS_PROFILE`

**ALWAYS:**
- ✅ Run `source .env` before `./mvnw quarkus:dev`
- ✅ Verify profile with `echo $QUARKUS_PROFILE`
- ✅ Confirm database with `grep DB_DEV_NAME .env`
- ✅ Use `quarkus_dev` for local development

---

## 🏗️ Project Architecture

### Package Structure
```
br.com.aguideptbr.features.<feature>/
├── *Controller.java     # REST endpoints (@Path, @GET, @POST, etc.)
├── *Service.java        # Business logic (@ApplicationScoped, @Transactional)
├── *Repository.java     # Data access (PanacheRepositoryBase)
├── *Model.java          # JPA entities (@Entity)
└── dto/
    ├── *Request.java    # API request DTOs
    └── *Response.java   # API response DTOs
```

### Dependency Injection
**MANDATORY:** Use constructor injection everywhere
```java
// ✅ CORRECT
public class MyService {
    private final MyRepository repository;
    private final Logger log;
    
    public MyService(MyRepository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }
}

// ❌ WRONG - Field injection (Sonar violation)
public class MyService {
    @Inject
    private MyRepository repository;
    @Inject
    private Logger log;
}
```

### Encapsulation Rules
- **DTOs:** Private fields + getters/setters (NO public fields)
- **Entities:** Public fields allowed (Panache convention)
- **Services/Controllers:** Private fields + constructor injection

---

## 📋 Development Workflow

### Starting the Application
```bash
# 1. Ensure PostgreSQL is running
docker ps | grep postgres

# 2. Load environment variables
source .env

# 3. Verify profile
echo $QUARKUS_PROFILE  # Should output: dev

# 4. Start application
./mvnw quarkus:dev
```

### Running Tests
```bash
# Uses quarkus_test database automatically
./mvnw test
```

### Building
```bash
# Compile only
./mvnw compile

# Full build with tests
./mvnw clean verify

# Package for deployment
./mvnw package
```

### Code Quality
```bash
# Generate JaCoCo coverage report
./mvnw verify

# View report at: target/site/jacoco/index.html
```

---

## 🤖 AI Assistant Guidelines

### Code Style Conventions

1. **Constructor Injection Always**
   - Never use `@Inject` on fields (Sonar: java:S6813)
   - Use constructor injection for all dependencies

2. **Encapsulation**
   - DTOs: private fields with getters/setters (Sonar: java:S1104)
   - Never public fields except in Panache entities

3. **Transaction Management**
   - Add `@Transactional` to service methods that modify data (CUD)
   - DO NOT add `@Transactional` to read-only methods
   - DO NOT add `@Transactional` to repository methods

4. **Soft Deletes**
   - Always use soft delete (set `deletedAt` timestamp)
   - Never use hard delete (`repository.delete()`)
   - Filter queries with `WHERE deletedAt IS NULL`

5. **Logging**
   - Use JBoss Logger (inject via constructor)
   - Never use `System.out.println` or `System.err`
   - Log at entry of controller methods
   - Log errors with context

6. **REST API**
   - Versioned paths: `/api/v1/...`
   - Proper HTTP status codes (200, 201, 204, 400, 404, 409)
   - Use `@Valid` for request validation
   - Return meaningful error messages

### Database Migrations

**Flyway Migration Naming:**
```
V{major}.{minor}.{patch}__{PascalCase_Description}.sql
Example: V1.0.25__Add_user_preferences.sql
```

**Migration Rules:**
- ✅ NON-DESTRUCTIVE ONLY: `ALTER TABLE ADD COLUMN`, `CREATE INDEX`, `UPDATE`
- ❌ FORBIDDEN IN PRODUCTION: `DROP TABLE`, `DROP COLUMN`, `TRUNCATE`
- Always use `IF NOT EXISTS` and `ON CONFLICT` for idempotency
- Add comments to tables and columns
- Create indexes with `WHERE deleted_at IS NULL`

### Entity Pattern
```java
@Entity
@Table(name = "app_<entity>")
public class <Entity>Model extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    @Column(nullable = false)
    public String name;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;
}
```

### Before Making Changes

**Pre-flight Checklist:**
1. Search existing code for similar patterns
2. Follow the existing architecture (don't create parallel approaches)
3. Check entity relationships before modifying
4. Verify business rules in documentation
5. Ensure changes are minimal and focused
6. Compile after changes: `./mvnw compile`
7. Run tests if modifying business logic: `./mvnw test`

---

## 🎓 Specialized AI Agents/Skills

### Available Agents

This project has specialized configurations for focused tasks. Agents can be invoked for specific domains:

#### 1. **Quarkus DB Specialist**
Use for: Database schema design, Flyway migrations, JPA entity modeling, SQL queries, REST API layer design

**Invocation:** Reference `.github/skills/quarkus-db-specialist/SKILL.md`

**Expertise:**
- PostgreSQL schema evolution
- Flyway migration files (safe, idempotent)
- JPA/Panache entity relationships
- Advanced SQL (CTEs, window functions, indexes)
- Seed/fixture data scripts
- REST layer aligned with DB model

#### 2. **UserMessage Backend Specialist**
Use for: Implementing/debugging the messaging feature (conversations, messages, participants)

**Invocation:** Reference `.github/agents/usermessage-backend.agent.md`

**Expertise:**
- Message persistence and retrieval
- Conversation flow (DIRECT/GROUP types)
- Read receipts and unread counts
- Participant management
- Message threading
- Business logic enforcement

#### 3. **UserMessage Implementation Workflow**
Use for: Complete end-to-end implementation of messaging features

**Invocation:** Reference `.github/skills/usermessage-implementation/SKILL.md`

**Provides:**
- Multi-phase workflow (Discovery → Implementation → Validation)
- Architecture compliance checks
- Business logic validation
- Code quality checklists
- API contract verification

### How to Use Agents

**For GitHub Copilot Chat:**
```
@workspace Use the Quarkus DB Specialist to create a new migration for user preferences table
```

**For Abacus AI CLI:**
When working on specific domains, AI assistants should:
1. Read the relevant agent/skill documentation first
2. Follow the patterns and templates provided
3. Validate against the checklists
4. Ensure architectural consistency

---

## 🔍 Key Documentation Files

- **`.github/copilot-instructions.md`** - Comprehensive instructions for GitHub Copilot
- **`.env.example`** - Environment variable template
- **`README.md`** - Project setup and usage
- **`DEVELOPMENT_GUIDE.md`** - Detailed development guidelines
- **`QUICK_START.md`** - Quick start guide
- **`SETUP_GUIDE_DATABASES.md`** - Database setup instructions

---

## 🚀 Common Tasks

### Adding a New Feature

1. **Design Database Schema**
   - Create Flyway migration: `V1.0.X__Create_feature_table.sql`
   - Use template from `.github/skills/quarkus-db-specialist/SKILL.md`

2. **Create Entity**
   - Extend `PanacheEntityBase`
   - Add audit fields (`createdAt`, `updatedAt`, `deletedAt`)
   - Define relationships

3. **Create Repository**
   - Implement `PanacheRepositoryBase<Entity, UUID>`
   - Add custom query methods
   - Filter soft deletes

4. **Create Service**
   - `@ApplicationScoped`
   - Constructor injection
   - `@Transactional` on CUD methods only
   - Business logic and validation

5. **Create DTOs**
   - Private fields
   - Bean Validation annotations
   - Getters/setters

6. **Create Controller**
   - `@Path("/api/v1/...")`
   - Constructor injection
   - Logging
   - Proper HTTP status codes

7. **Compile and Test**
   ```bash
   ./mvnw compile
   ./mvnw test
   ```

### Fixing a Bug

1. **Understand the Issue**
   - Read existing code in the feature package
   - Check entity relationships
   - Review business rules

2. **Locate the Problem**
   - Check logs in `quarkus-startup.log`
   - Review error messages
   - Use `git blame` for context

3. **Fix at Root Cause**
   - Don't apply surface-level patches
   - Maintain architectural consistency
   - Follow existing patterns

4. **Verify the Fix**
   - Compile: `./mvnw compile`
   - Run related tests: `./mvnw test -Dtest=FeatureTest`
   - Test manually if needed

---

## ⚠️ Common Pitfalls

### Field Injection (Sonar: java:S6813)
```java
// ❌ WRONG
@Inject
Logger log;

// ✅ CORRECT
private final Logger log;
public Service(Logger log) { this.log = log; }
```

### Public Fields (Sonar: java:S1104)
```java
// ❌ WRONG
public class DTO {
    public String name;
}

// ✅ CORRECT
public class DTO {
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

### Business Logic in Controller
```java
// ❌ WRONG
@POST
public Response create(Request req) {
    if (validate(req)) {
        var entity = new Entity();
        repository.persist(entity);
    }
}

// ✅ CORRECT
@POST
public Response create(Request req) {
    return Response.status(201).entity(service.create(req)).build();
}
```

### Hard Delete
```java
// ❌ WRONG
repository.delete(entity);

// ✅ CORRECT
entity.deletedAt = LocalDateTime.now();
repository.persist(entity);
```

---

## 📞 Environment Detection

**Local Development (macOS/Linux):**
- Application runs directly via `./mvnw quarkus:dev` (NOT in Docker)
- PostgreSQL runs in Docker container
- Port: `https://localhost:8443` (HTTPS with self-signed cert)

**Production (VPS):**
- Application runs in Docker via `docker-compose.yml`
- Deployed automatically via Jenkins pipeline
- Port: Configured in Docker Compose

**IMPORTANT:** Always verify environment before suggesting restart/debug commands:
```bash
# Check if app is in Docker
docker ps | grep mobile-rest-api

# Check if app is running locally
ps aux | grep quarkus
```

---

## 🎯 Success Criteria

Code is ready when:
- ✅ Follows existing architecture patterns
- ✅ Uses constructor injection everywhere
- ✅ All fields properly encapsulated
- ✅ Business logic in service layer only
- ✅ Soft deletes used throughout
- ✅ Compiles without errors: `./mvnw compile`
- ✅ Tests pass: `./mvnw test`
- ✅ No Sonar violations (run locally or check CI)
- ✅ Proper logging with JBoss Logger
- ✅ Meaningful error messages with correct HTTP status

---

## 📚 Additional Resources

- **Quarkus Guides:** https://quarkus.io/guides/
- **Panache Guide:** https://quarkus.io/guides/hibernate-orm-panache
- **RESTEasy Guide:** https://quarkus.io/guides/resteasy-reactive
- **Flyway Guide:** https://quarkus.io/guides/flyway

---

## 🤝 Contributing

When contributing code:
1. Follow all patterns in this document
2. Reference existing implementations
3. Use specialized agents/skills for domain-specific work
4. Validate against checklists
5. Ensure backward compatibility
6. Document significant changes

---

**Last Updated:** 2026-06-09  
**Maintained by:** Development Team  
**AI Assistants:** This file is your primary reference for working on this project
