---
name: quarkus-db-specialist
description: 'Senior PostgreSQL + Java Quarkus backend specialist. Use when: designing or evolving PostgreSQL schemas via Flyway migrations; modeling JPA/Panache entities; defining entity relationships (OneToOne, OneToMany, ManyToMany); writing advanced SQL (CTEs, window functions, indexes); creating seed/fixture SQL scripts; writing safe targeted DELETE scripts respecting FK constraints; diagnosing entity mapping or migration conflicts; designing REST API layers (controllers, DTOs, mappers, services) aligned with the DB model.'
argument-hint: 'Describe the schema change, entity, SQL task, or API layer you need'
---

# Quarkus DB Specialist

## Scope
This skill applies to all Java/Quarkus backend work in this project — database schema evolution, entity modeling, and REST API design aligned with the PostgreSQL model.

Project conventions:
- **Package root:** `br.com.aguideptbr.features.<feature>/`
- **Entities:** `PanacheEntityBase` + `UUID` PK with `@GeneratedValue(strategy = GenerationType.UUID)`
- **Migrations:** `src/main/resources/db/migration/V{major}.{minor}.{patch}__{Description}.sql`
- **Table prefix:** `app_` (e.g. `app_user`, `app_conversation`)
- **Audit columns:** `created_at`, `updated_at` (Hibernate `@CreationTimestamp`/`@UpdateTimestamp`), `deleted_at` (soft delete)
- **Injection:** Constructor injection — never field `@Inject` (except `@Provider`)
- **Encapsulation:** Private fields + getters/setters in DTOs; public fields only in Panache entities

---

## 1. Flyway Migration Files

### Naming
```
V{major}.{minor}.{patch}__{PascalCase_Description}.sql
```
Examples: `V1.0.24__Add_user_preferences.sql`, `V1.0.25__Create_notification_table.sql`

### Safe Migration Template
```sql
-- ========================================
-- DESCRIPTION
-- Version: X.Y.Z
-- Date: YYYY-MM-DD
-- Author: <name>
-- ========================================

-- ✅ NON-DESTRUCTIVE ONLY:
--   ALTER TABLE ... ADD COLUMN
--   CREATE INDEX CONCURRENTLY
--   UPDATE ... WHERE ...
--   INSERT ... ON CONFLICT DO NOTHING

-- ❌ FORBIDDEN IN PRODUCTION:
--   DROP TABLE / DROP COLUMN
--   TRUNCATE
--   DROP SCHEMA
```

### New Table Template
```sql
CREATE TABLE app_<entity> (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Business columns
    <column> <TYPE> [NOT NULL] [DEFAULT <value>],

    -- Foreign keys
    <parent>_id UUID NOT NULL,

    -- Audit
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP,

    -- Referential integrity
    CONSTRAINT fk_<entity>_<parent> FOREIGN KEY (<parent>_id)
        REFERENCES app_<parent>(id) ON DELETE CASCADE
);

-- Performance indexes
CREATE INDEX idx_<entity>_<column> ON app_<entity>(<column>) WHERE deleted_at IS NULL;

-- Documentation
COMMENT ON TABLE  app_<entity>           IS '<description>';
COMMENT ON COLUMN app_<entity>.<column>  IS '<description>';
```

### Add Column Template
```sql
ALTER TABLE app_<entity> ADD COLUMN IF NOT EXISTS <column> <TYPE>;

-- Backfill before NOT NULL constraint
UPDATE app_<entity> SET <column> = <default_value> WHERE <column> IS NULL;

-- Then add constraint (only if needed)
ALTER TABLE app_<entity> ALTER COLUMN <column> SET NOT NULL;
```

### Initial Data Insert (Idempotent)
```sql
INSERT INTO app_<entity> (id, <col1>, <col2>)
VALUES (gen_random_uuid(), <val1>, <val2>)
ON CONFLICT (<unique_column>) DO NOTHING;
```

---

## 2. JPA/Panache Entity Modeling

### Entity Template
```java
@Entity
@Table(name = "app_<entity>")
public class <Entity>Model extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    // --- Business fields ---

    @Column(nullable = false, length = <n>)
    public String <field>;

    // Enum stored as String
    @Enumerated(EnumType.STRING)
    @Column(name = "<col>", length = 20, nullable = false)
    public <Enum> <field> = <Enum>.DEFAULT;

    // --- Relationships ---

    // ManyToOne (owner side)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "<parent>_id", nullable = false)
    @JsonIgnore
    public <Parent>Model <parent>;

    // OneToMany (inverse side)
    @OneToMany(mappedBy = "<parent>", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    public List<<Child>Model> <children> = new ArrayList<>();

    // --- Audit ---

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    // --- Soft-delete helper ---
    public boolean isActive() {
        return deletedAt == null;
    }
}
```

### Relationship Decision Table

| Cardinality | Owning Side | Annotation | FK Column |
|-------------|-------------|------------|-----------|
| `@ManyToOne` | Child (FK holder) | `@JoinColumn(name = "<parent>_id")` | On child table |
| `@OneToMany` | Parent (inverse) | `mappedBy = "<field_on_child>"` | No extra column |
| `@ManyToMany` | Pick one | `@JoinTable` | Join table |
| `@OneToOne` | FK holder | `@JoinColumn(name = "<partner>_id", unique = true)` | On FK side |

**Fetch strategy rules:**
- Default to `LAZY` for all relationships
- Use `EAGER` only for `@ManyToOne` to tiny/stable reference tables (e.g. Role)
- Never `EAGER` on collections

---

## 3. Repository Pattern

```java
@ApplicationScoped
public class <Entity>Repository implements PanacheRepositoryBase<<Entity>Model, UUID> {

    // Active records only
    public List<<Entity>Model> findActive() {
        return find("deletedAt is null order by createdAt desc").list();
    }

    // Find by FK
    public List<<Entity>Model> findBy<Parent>Id(UUID <parent>Id) {
        return find("<parent>.id = ?1 and deletedAt is null", <parent>Id).list();
    }

    // Paginated active records
    public PanacheQuery<<Entity>Model> queryActive() {
        return find("deletedAt is null order by createdAt desc");
    }

    // Soft delete — NEVER use hard delete
    public void softDelete(<Entity>Model entity) {
        entity.deletedAt = LocalDateTime.now();
        persist(entity);
    }
}
```

---

## 4. Service Layer

```java
@ApplicationScoped
public class <Entity>Service {

    private final <Entity>Repository repository;
    private final Logger log;

    public <Entity>Service(<Entity>Repository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    @Transactional
    public <Entity>Model create(<CreateDTO> dto) {
        // Business validation
        if (<duplicate check>) {
            throw new WebApplicationException(
                Response.status(409)
                    .entity(Map.of("error", "Conflict", "message", "<message>"))
                    .build());
        }
        var entity = new <Entity>Model();
        // map dto → entity
        repository.persist(entity);
        return entity;
    }

    // @Transactional on CUD only — never on read-only methods
    public List<<Entity>Model> findAll(int page, int size) {
        return repository.queryActive()
                .page(Page.of(page, size))
                .list();
    }
}
```

---

## 5. REST Controller

```java
@Path("/api/v1/<resource>")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER"})
public class <Entity>Controller {

    private final <Entity>Service service;
    private final Logger log;

    public <Entity>Controller(<Entity>Service service, Logger log) {
        this.service = service;
        this.log = log;
    }

    @POST
    public Response create(@Valid <CreateDTO> dto,
                           @HeaderParam("Authorization") String authHeader) {
        log.infof("POST /api/v1/<resource>");
        var result = service.create(dto);
        return Response.status(201).entity(result).build();
    }

    @GET
    public Response findAll(@QueryParam("page") @DefaultValue("0") int page,
                            @QueryParam("size") @DefaultValue("20") int size) {
        log.infof("GET /api/v1/<resource>?page=%d&size=%d", page, size);
        var items = service.findAll(page, size);
        return Response.ok(items).build();
    }
}
```

---

## 6. Seed / Fixture SQL Scripts

Use for dev (`quarkus_dev`) and test (`quarkus_test`) environments only. **Never seed directly into `quarkus_db` (production).**

### Header Template
```sql
-- ================================================================
-- SEED: <Feature> Test Data
-- Environment: DEV / TEST only (never production)
-- Purpose: <describe the test scenario>
-- Dependencies: <list tables/rows required before this runs>
-- Cleanup: See <CLEANUP_SCRIPT>.sql
-- Last updated: YYYY-MM-DD
-- ================================================================
```

### Seed Pattern (Idempotent)
```sql
-- Use fixed UUIDs so re-runs are deterministic
DO $$
DECLARE
    v_user1_id UUID := 'aaaaaaaa-1111-1111-1111-000000000001';
    v_user2_id UUID := 'aaaaaaaa-2222-2222-2222-000000000002';
    v_conv_id  UUID := 'bbbbbbbb-1111-1111-1111-000000000001';
BEGIN
    -- Insert users (skip if exists)
    INSERT INTO app_user (id, name, surname, email, password_hash, role, created_at, updated_at)
    VALUES
        (v_user1_id, 'Alice', 'Test', 'alice@test.com', '$2a$10$...', 'FREE', NOW(), NOW()),
        (v_user2_id, 'Bob',   'Test', 'bob@test.com',   '$2a$10$...', 'FREE', NOW(), NOW())
    ON CONFLICT (email) DO NOTHING;

    -- Insert dependent records
    INSERT INTO app_conversation (id, conversation_type, created_at, updated_at)
    VALUES (v_conv_id, 'DIRECT', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO app_conversation_participant (id, conversation_id, user_id, joined_at)
    VALUES
        (gen_random_uuid(), v_conv_id, v_user1_id, NOW()),
        (gen_random_uuid(), v_conv_id, v_user2_id, NOW())
    ON CONFLICT (conversation_id, user_id) DO NOTHING;
END $$;
```

---

## 7. Safe Targeted Deletion Scripts

Always delete in reverse FK dependency order. Never use `TRUNCATE` on tables with FK dependents unless all dependents are also truncated in order.

### Deletion Template
```sql
-- ================================================================
-- CLEANUP: Remove <Feature> test data
-- Respects FK constraints (delete leaf → root order)
-- Safe: SELECT preview before DELETE (swap comments)
-- ================================================================

BEGIN;

-- Step 1: Leaf tables first
-- SELECT * FROM app_conversation_participant WHERE conversation_id = '<uuid>';
DELETE FROM app_conversation_participant WHERE conversation_id = '<uuid>';

-- Step 2: Messages
-- SELECT * FROM app_user_message WHERE conversation_id = '<uuid>';
DELETE FROM app_user_message WHERE conversation_id = '<uuid>';

-- Step 3: Root table last
-- SELECT * FROM app_conversation WHERE id = '<uuid>';
DELETE FROM app_conversation WHERE id = '<uuid>';

COMMIT;
-- Use ROLLBACK to preview without committing
```

### Soft Delete (Preferred)
```sql
-- Mark as deleted without removing rows (preserves audit trail)
UPDATE app_<entity>
SET    deleted_at = NOW()
WHERE  id = '<uuid>'
  AND  deleted_at IS NULL;
```

---

## 8. Advanced SQL Patterns

### Unread Count (Window Function)
```sql
SELECT
    c.id,
    COUNT(m.id) FILTER (WHERE m.sent_at > COALESCE(cp.last_read_at, '1970-01-01')) AS unread_count
FROM app_conversation c
JOIN app_conversation_participant cp ON cp.conversation_id = c.id AND cp.user_id = :userId
LEFT JOIN app_user_message m ON m.conversation_id = c.id AND m.deleted_at IS NULL
WHERE c.deleted_at IS NULL
GROUP BY c.id, cp.last_read_at;
```

### CTE for Complex Aggregation
```sql
WITH ranked AS (
    SELECT
        user_id,
        score,
        RANK() OVER (ORDER BY score DESC) AS position
    FROM app_user_ranking
    WHERE deleted_at IS NULL
)
SELECT u.id, u.name, r.score, r.position
FROM ranked r
JOIN app_user u ON u.id = r.user_id
WHERE r.position <= 100;
```

### Index Guidelines
```sql
-- Partial index (most queries filter soft-deleted rows)
CREATE INDEX idx_<table>_<col> ON app_<table>(<col>) WHERE deleted_at IS NULL;

-- Composite index when WHERE and ORDER BY differ
CREATE INDEX idx_<table>_<a>_<b> ON app_<table>(<a>, <b> DESC) WHERE deleted_at IS NULL;

-- Never index low-cardinality columns (boolean, enum with <5 values) alone
```

---

## 9. Migration Conflict Resolution

| Symptom | Cause | Fix |
|---------|-------|-----|
| `FlywayException: Validate failed. Migration checksum mismatch` | Applied migration was modified | Create a new migration; never edit applied files |
| `org.postgresql.util.PSQLException: column does not exist` | Hibernate model references column not yet in DB | Add missing Flyway migration |
| `could not execute JPQL query` | Entity field name differs from column mapping | Verify `@Column(name=...)` matches DB column |
| Flyway timeout in CI (Jenkins) | `clean-at-start=true` with large DB | Set `clean-at-start=false`, reset DB externally |

**MANDATORY production config:**
```properties
quarkus.flyway.clean-at-start=false
quarkus.flyway.migrate-at-start=true
quarkus.flyway.baseline-on-migrate=true
quarkus.hibernate-orm.database.generation=none
```

---

## 10. Workflow Checklist

When implementing a new feature end-to-end:

- [ ] Identify next migration version (`V1.0.X`)
- [ ] Write migration SQL: new tables → constraints → indexes → comments
- [ ] Create `*Model.java` entity with all columns, relationships, audit fields
- [ ] Create `*Repository.java` with named query methods (`findByXxx`, soft-delete aware)
- [ ] Create DTO(s): private fields + getters/setters + Bean Validation annotations
- [ ] Create `*Service.java` with `@Transactional` on CUD, business rules, proper exceptions
- [ ] Create `*Controller.java` with versioned path `/api/v1/...`, constructor injection, logs
- [ ] If seed data needed: create `a_error_log_temp/<feature>_seed.sql` with fixed UUIDs + `ON CONFLICT DO NOTHING`
- [ ] Verify `quarkus.flyway.clean-at-start=false` is set in `application-prod.properties`

---

## References
- [Flyway Migrations](./references/flyway-patterns.md)
- [Entity Relationships](./references/entity-relationships.md)
- [Panache Queries](https://quarkus.io/guides/hibernate-orm-panache)
- [Quarkus REST](https://quarkus.io/guides/resteasy-reactive)
