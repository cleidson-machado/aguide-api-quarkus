---
name: usermessage-implementation
description: 'Complete user messaging feature (conversations, messages, participants) in Java Quarkus backend. Analyzes existing code, identifies gaps, implements missing components following project architecture. Use for: finishing usermessage package, fixing messaging bugs, validating business logic, ensuring API contract compliance.'
argument-hint: 'Specific task (e.g., "implement read receipts", "fix conversation creation", "complete all endpoints")'
---

# UserMessage Feature Implementation Workflow

Multi-step procedure to analyze, complete, and validate the messaging system in `src/main/java/br/com/aguideptbr/features/usermessage/`.

## When to Use

Invoke this skill when:
- ✅ Implementing new messaging endpoints or business logic
- ✅ Debugging conversation or message flows
- ✅ Validating API implementation against documentation
- ✅ Fixing architecture inconsistencies in usermessage feature
- ✅ Completing missing DTOs, services, repositories, or controllers
- ✅ Ensuring business rules are properly enforced

**DO NOT** use this skill for:
- ❌ Features outside the usermessage package
- ❌ Database configuration or profile setup
- ❌ Frontend implementation
- ❌ Infrastructure or deployment issues

---

## Phase 1: Discovery & Analysis

### 1.1 Scan Existing Implementation

**Goal:** Understand what's already built before making changes.

```
Step 1: Inventory all files in features/usermessage/
→ grep_search for package structure
→ List all *Controller.java, *Service.java, *Repository.java, *Model.java
→ Check dto/ subfolder for request/response objects

Step 2: Load API specification
→ Read a_error_log_temp/USERMESSAGE_API_DOCUMENTATION.md
→ Extract all 17 endpoint definitions (9 conversation + 8 message)
→ Note required request/response structures

Step 3: Map existing endpoints
→ Read ConversationController.java
→ Read MessageController.java
→ List all @Path, @GET, @POST, @PUT, @DELETE annotations
→ Compare with API docs to find missing endpoints
```

**Output:** Checklist of implemented vs missing endpoints.

---

### 1.2 Validate Entity Model

**Goal:** Ensure database entities correctly model the domain.

```
Step 1: Review entity relationships
→ Read ConversationModel.java
→ Read UserMessageModel.java
→ Read ConversationParticipantModel.java
→ Check @OneToMany, @ManyToOne, @ManyToMany annotations

Step 2: Verify required fields
→ Conversation: id, type, name?, createdAt, updatedAt, deletedAt, lastMessageAt
→ Message: id, conversationId, senderId, content, messageType, sentAt, isRead, readAt, parentMessageId
→ Participant: conversationId, userId, isAdmin, isCreator, lastReadAt, isPinned, isArchived

Step 3: Check soft delete implementation
→ Confirm deletedAt field exists
→ Verify queries filter deletedAt IS NULL

Step 4: Validate audit timestamps
→ @CreationTimestamp on createdAt
→ @UpdateTimestamp on updatedAt
```

**Decision Point:**
- ✅ All fields present → Proceed to Phase 2
- ❌ Missing fields → Create Flyway migration before continuing

---

### 1.3 Review Business Logic

**Goal:** Identify missing business rules in service layer.

```
Step 1: Read ConversationService.java
→ Check methods: createDirect, createGroup, findAll, archive, pin, addParticipant, removeParticipant
→ Verify each has @Transactional where needed
→ Look for validation logic (e.g., "cannot create conversation with self")

Step 2: Read MessageService.java
→ Check methods: send, findByConversation, markAsRead, edit, delete, search, getReplies
→ Verify sender authorization checks
→ Look for lastMessageAt update logic

Step 3: Validate against business rules
→ Cross-reference with API_DOCUMENTATION.md business rules section
→ Create gaps list (missing validations, missing logic)
```

**Output:** List of missing business logic implementations.

---

## Phase 2: Implementation

### 2.1 Missing Controllers (REST Endpoints)

**When:** API endpoints don't exist yet.

**Reference:** See [Controller Template](./templates/ControllerTemplate.java) for complete example.

```
Template Pattern:
@Path("/api/v1/[resource]")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class [Resource]Controller {
    private final [Resource]Service service;
    private final Logger log;

    // CONSTRUCTOR INJECTION (mandatory)
    public [Resource]Controller([Resource]Service service, Logger log) {
        this.service = service;
        this.log = log;
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        log.infof("GET /api/v1/[resource]/%s", id);
        return Response.ok(service.findById(id)).build();
    }
}
```

**Checklist for each endpoint:**
- [ ] Constructor injection (not field injection)
- [ ] Logger statement at start of method
- [ ] Input validation with @Valid
- [ ] Proper HTTP status codes (200, 201, 204, 400, 404)
- [ ] Exception handling for business logic errors
- [ ] API versioning (/api/v1/)

---

### 2.2 Missing DTOs (Request/Response)

**When:** Endpoint exists but DTOs are incomplete.

**Reference:** See [DTO Templates](./templates/DTOTemplates.java) for complete examples.

```
Request DTO Pattern:
public class Create[Resource]Request {
    @NotBlank(message = "Field is required")
    private String field;

    // Default constructor (for Jackson)
    public Create[Resource]Request() {}

    // Getters and Setters (private fields!)
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
}

Response DTO Pattern:
public class [Resource]Response {
    private UUID id;
    private String name;

    // Constructor for easy mapping
    public [Resource]Response(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters only (immutable)
    public UUID getId() { return id; }
    public String getName() { return name; }
}
```

**Validation Rules:**
- [ ] All fields are private (never public)
- [ ] Bean Validation annotations (@NotBlank, @Email, @Size)
- [ ] Getters for all fields (Jackson needs them)
- [ ] Setters for request DTOs (Jackson deserialization)
- [ ] Meaningful error messages in validation annotations

---

### 2.3 Missing Service Layer Logic

**When:** Business rules not enforced.

**Reference:** See [Service Template](./templates/ServiceTemplate.java) for complete example.

```
Service Pattern:
@ApplicationScoped
public class [Resource]Service {
    private final [Resource]Repository repository;
    private final Logger log;

    // CONSTRUCTOR INJECTION (mandatory)
    public [Resource]Service([Resource]Repository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    @Transactional  // CUD operations only
    public [Resource]Response create([Resource]Request request) {
        // 1. Validate business rules
        if (violation) {
            throw new WebApplicationException("Error message", 400);
        }

        // 2. Map to entity
        var entity = new [Resource]Model();
        entity.field = request.getField();

        // 3. Persist
        repository.persist(entity);

        // 4. Map to response
        return new [Resource]Response(entity.id, entity.field);
    }

    // Read operations - no @Transactional
    public List<[Resource]Response> findAll() {
        return repository.findAll().stream()
            .map(e -> new [Resource]Response(e.id, e.field))
            .collect(Collectors.toList());
    }
}
```

**Business Rule Implementation Checklist:**
- [ ] Constructor injection for all dependencies
- [ ] @Transactional on create/update/delete methods ONLY
- [ ] Input validation before persistence
- [ ] Authorization checks (e.g., user is participant)
- [ ] Proper exception types (WebApplicationException with status)
- [ ] Soft delete (set deletedAt, not hard delete)
- [ ] Logging at key decision points

---

### 2.4 Missing Repository Methods

**When:** Custom queries needed.

```
Repository Pattern:
@ApplicationScoped
public class [Resource]Repository implements PanacheRepositoryBase<[Resource]Model, UUID> {

    // Custom finder methods
    public [Resource]Model findByField(String field) {
        return find("field = ?1 AND deletedAt IS NULL", field).firstResult();
    }

    public List<[Resource]Model> findByUserId(UUID userId) {
        return list("userId = ?1 AND deletedAt IS NULL", userId);
    }

    // Pagination
    public List<[Resource]Model> findPaginated(int page, int size) {
        return find("deletedAt IS NULL")
            .page(Page.of(page, size))
            .list();
    }
}
```

**Repository Rules:**
- [ ] Extend PanacheRepositoryBase<Entity, UUID>
- [ ] NO @Transactional annotations (use in service)
- [ ] NO business logic (pure data access)
- [ ] Always filter soft deletes (deletedAt IS NULL)
- [ ] Use parameterized queries (?1, ?2) to avoid SQL injection

---

### 2.5 Critical Business Rules to Implement

Based on API documentation, ensure these are enforced:

**Conversation Rules:**
```java
// DIRECT conversations
- Only 2 participants allowed
- Cannot have name, description, or icon
- Cannot add/remove participants after creation
- Auto-created if already exists between users

// GROUP conversations
- Creator automatically becomes isAdmin=true
- Name is required (max 255 chars)
- Only admins can add/remove participants
- Participants can remove themselves (leave)

// Archive/Pin
- Per-user setting (ConversationParticipant.isArchived/isPinned)
- Doesn't affect other participants
```

**Message Rules:**
```java
// Sending
- User must be participant in conversation
- Update conversation.lastMessageAt on send
- TEXT messages must have non-empty content
- IMAGE/VIDEO/FILE content should be URL

// Read Receipts
- Update message.isRead and message.readAt
- Update participant.lastReadAt
- Don't mark own messages as read

// Edit/Delete
- Only sender can edit or delete their messages
- Edit sets isEdited=true and editedAt timestamp
- Delete is soft (sets deletedAt)

// Threading
- parentMessageId must exist in same conversation
- Replies sorted by sentAt ASC (chronological)
```

---

## Phase 3: Validation

### 3.1 Compile Check

```
Step 1: Build the project
→ ./mvnw clean compile

Step 2: Review errors
→ Fix compilation errors immediately
→ Check for missing imports
→ Verify constructor injection syntax

Step 3: Verify no Sonar violations
→ NO field injection (@Inject on fields)
→ NO public fields (except Panache entities)
→ All local variables use camelCase
```

**Stop if:** Compilation fails. Fix before proceeding.

---

### 3.2 API Contract Validation

```
Step 1: Compare implemented endpoints with documentation
→ Read USERMESSAGE_API_DOCUMENTATION.md
→ List all 17 endpoints (9 conversation + 8 message)
→ Verify each has matching @Path in controller

Step 2: Validate request/response structures
→ For each endpoint, check DTO fields match API docs
→ Verify HTTP methods match (GET, POST, PUT, DELETE)
→ Confirm status codes are correct (200, 201, 204, 400, 404)

Step 3: Test critical flows manually
→ Create conversation (POST /api/v1/conversations/direct)
→ Send message (POST /api/v1/messages)
→ Mark as read (PUT /api/v1/messages/{id}/read)
→ Verify unread count decreases
```

**Output:** List of any mismatches between implementation and API docs.

---

### 3.3 Business Logic Validation

```
Step 1: Test business rule enforcement
→ Try creating conversation with self → Should fail 400
→ Try editing other user's message → Should fail 403
→ Try adding participant to DIRECT → Should fail 400
→ Try removing participant as non-admin → Should fail 403

Step 2: Verify data integrity
→ Send message → Check conversation.lastMessageAt updated
→ Mark as read → Check participant.lastReadAt updated
→ Archive conversation → Check only current user affected
→ Delete message → Check deletedAt set (not hard delete)

Step 3: Check relationship integrity
→ Delete conversation → Messages cascade or remain?
→ Remove participant → Messages remain visible?
→ Reply to deleted parent → Allowed or blocked?
```

**Quality Gate:** All business rules must pass before completion.

---

### 3.4 Code Quality Checklist

Run through this final checklist:

**Architecture Compliance:**
- [ ] All controllers use constructor injection
- [ ] All services use constructor injection
- [ ] No field injection anywhere (@Inject on fields)
- [ ] Business logic only in service layer
- [ ] Controllers are thin (just delegation)
- [ ] Repositories have no business logic

**Encapsulation:**
- [ ] All DTO fields are private with getters/setters
- [ ] Entity fields follow Panache convention (public OK)
- [ ] No public fields in non-entity classes

**Error Handling:**
- [ ] All exceptions have meaningful messages
- [ ] HTTP status codes match error types
- [ ] No unhandled exceptions escape service layer

**Logging:**
- [ ] Logger injected via constructor (not field)
- [ ] All endpoints log at entry
- [ ] Errors logged with context
- [ ] No System.out.println or System.err

**Database:**
- [ ] Soft deletes used everywhere (deletedAt)
- [ ] Audit fields present (createdAt, updatedAt)
- [ ] Migrations are idempotent
- [ ] No destructive migrations for production

**Testing:**
- [ ] Profile 'dev' used for local development
- [ ] No connection to quarkus_db (production) locally
- [ ] Tests use quarkus_test database

---

## Phase 4: Documentation & Handoff

### 4.1 Update Documentation (if needed)

```
If implementation differs from API docs:
→ Update USERMESSAGE_API_DOCUMENTATION.md
→ Note any intentional deviations
→ Document new endpoints or fields

If new migrations created:
→ Document schema changes
→ Add comments in migration file
```

---

### 4.2 Completion Summary

Provide a structured summary:

```markdown
## Implementation Summary

### ✅ Completed
- [List of implemented endpoints]
- [List of implemented business rules]
- [List of created/updated files]

### ⚠️ Known Limitations
- [Any incomplete features]
- [Any temporary workarounds]
- [Any technical debt]

### 🔄 Remaining Work
- [Missing endpoints]
- [Incomplete business logic]
- [Required migrations]

### 📋 Next Steps
1. [Immediate next action]
2. [Follow-up tasks]
3. [Testing recommendations]
```

---

## Decision Tree

Use this to determine what to do:

```
┌─ Need to add endpoint?
│  └─ YES → Phase 2.1 (Create Controller)
│          → Phase 2.2 (Create DTOs)
│          → Phase 2.3 (Implement Service)
│
├─ Endpoint exists but broken?
│  └─ YES → Phase 1.3 (Review Business Logic)
│          → Phase 2.3 (Fix Service)
│
├─ Database schema incomplete?
│  └─ YES → Phase 1.2 (Validate Entity Model)
│          → Create Flyway migration
│          → Update entities
│
├─ Architecture violations?
│  └─ YES → Phase 3.4 (Code Quality Checklist)
│          → Fix field injection
│          → Fix public fields
│
└─ Everything works?
   └─ YES → Phase 3 (Validation)
           → Phase 4 (Documentation)
```

---

## Quick Reference Commands

```bash
# Compile project
./mvnw clean compile

# Run tests
./mvnw test

# Start dev server (uses quarkus_dev database)
source .env && ./mvnw quarkus:dev

# Create new migration
touch src/main/resources/db/migration/V1.0.X__Description.sql

# Check for Sonar issues
./mvnw sonar:sonar
```

---

## Common Pitfalls

**❌ Field Injection (Sonar: java:S6813)**
```java
// WRONG
@Inject
Logger log;

// CORRECT
private final Logger log;
public Service(Logger log) { this.log = log; }
```

**❌ Public Fields (Sonar: java:S1104)**
```java
// WRONG
public class DTO {
    public String name;
}

// CORRECT
public class DTO {
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

**❌ Business Logic in Controller**
```java
// WRONG
@POST
public Response create(Request req) {
    if (validate(req)) {  // Business logic!
        var entity = new Entity();
        repository.persist(entity);  // Direct persistence!
    }
}

// CORRECT
@POST
public Response create(Request req) {
    return Response.ok(service.create(req)).build();
}
```

**❌ Hard Delete**
```java
// WRONG
repository.delete(entity);

// CORRECT
entity.deletedAt = LocalDateTime.now();
repository.persist(entity);
```

---

## Success Criteria

The usermessage feature is complete when:

1. ✅ All 17 endpoints from API docs exist and work
2. ✅ All business rules are enforced in service layer
3. ✅ No Sonar violations (constructor injection, encapsulation)
4. ✅ Code compiles without errors
5. ✅ Entities correctly model the domain
6. ✅ DTOs match API documentation contracts
7. ✅ Error handling covers all edge cases
8. ✅ Soft deletes used throughout
9. ✅ Logging is comprehensive
10. ✅ Database schema supports all operations

---

## Related Resources

- **API Specification:** `a_error_log_temp/USERMESSAGE_API_DOCUMENTATION.md`
- **Project Instructions:** `.github/copilot-instructions.md`
- **Quarkus Guides:** https://quarkus.io/guides/
- **Panache Guide:** https://quarkus.io/guides/hibernate-orm-panache
