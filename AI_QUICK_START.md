# AI-Assisted Development - Quick Start Guide

This guide helps you leverage AI assistants effectively in this project.

## 🚀 Quick Setup

### 1. Start Here
Read these files in order:
1. `AGENTS.md` - Main AI assistant guide (5 min read)
2. `.aiconfig.yml` - Quick reference (2 min scan)
3. `.abacus/README.md` - Agent overview (3 min read)

### 2. For GitHub Copilot Users
The project is already configured! Copilot will automatically read:
- `.github/copilot-instructions.md` (comprehensive instructions)
- All agent/skill definitions in `.github/agents/` and `.github/skills/`

**How to use:**
```
@workspace Create a new REST endpoint for notifications

@workspace Use the database architect pattern to add a new table

@workspace Review this code for pattern violations
```

### 3. For Abacus AI CLI Users
Agents are automatically available. Just reference them:

```bash
# Direct invocation
"Use the database_architect agent to create a user preferences table"

# By keywords (auto-triggers)
"Create a migration for..." → triggers database_architect
"Implement messaging..." → triggers messaging_specialist
"Review this code..." → triggers architecture_guardian
```

### 4. For Other AI Assistants (Cursor, Continue, etc.)
Add these files to your assistant's context:
- `AGENTS.md`
- `.aiconfig.yml`
- `.github/copilot-instructions.md`

## 🎯 Common Scenarios

### Scenario 1: Adding a New Feature
```
1. Ask: "Use database_architect to design schema for [feature]"
2. Ask: "Use service_developer to implement business logic"
3. Ask: "Use api_developer to create REST endpoints"
4. Ask: "Use test_engineer to write tests"
5. Ask: "Use architecture_guardian to validate code quality"
```

### Scenario 2: Fixing a Bug
```
1. Ask: "Use architecture_guardian to analyze [bug description]"
2. Ask: "Fix the issue in [ServiceClass] following project patterns"
3. Ask: "Use test_engineer to add regression test"
```

### Scenario 3: Code Review
```
Ask: "Use architecture_guardian to review [file/class] for:
- Constructor injection
- Encapsulation
- Business logic placement
- Transaction boundaries"
```

### Scenario 4: Database Changes
```
Ask: "Use database_architect to:
1. Create migration V1.0.X for [change]
2. Update [Entity]Model
3. Update repository methods"
```

### Scenario 5: REST API Creation
```
Ask: "Use api_developer to:
1. Design DTOs for [resource]
2. Create [Resource]Controller
3. Add validation and error handling"
```

## 🤖 Agent Quick Reference

| Agent | Use When | Example |
|-------|----------|---------|
| **database_architect** | Schema/migrations/entities | "Create migration for user_settings" |
| **messaging_specialist** | Messaging features | "Implement read receipts" |
| **architecture_guardian** | Code review/refactoring | "Review this service class" |
| **api_developer** | REST endpoints | "Create notification endpoint" |
| **service_developer** | Business logic | "Implement validation logic" |
| **test_engineer** | Writing tests | "Write tests for AuthService" |

## ⚠️ Critical Rules (MUST READ!)

### Database Safety
```bash
# ❌ NEVER
Connect to quarkus_db locally

# ✅ ALWAYS
Use quarkus_dev for local development
Verify: echo $QUARKUS_PROFILE
```

### Code Patterns
```java
// ❌ NEVER - Field injection
@Inject
private Logger log;

// ✅ ALWAYS - Constructor injection
private final Logger log;
public Service(Logger log) { this.log = log; }
```

```java
// ❌ NEVER - Public DTO fields
public class DTO {
    public String name;
}

// ✅ ALWAYS - Private with getters/setters
public class DTO {
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

```java
// ❌ NEVER - Hard delete
repository.delete(entity);

// ✅ ALWAYS - Soft delete
entity.deletedAt = LocalDateTime.now();
repository.persist(entity);
```

## 📋 Pre-Flight Checklist

Before asking AI to write code:
- [ ] Have you read `AGENTS.md`?
- [ ] Do you know which agent to use?
- [ ] Have you checked existing similar code?
- [ ] Is PostgreSQL running? (`docker ps | grep postgres`)
- [ ] Is `.env` loaded? (`source .env`)
- [ ] Is profile correct? (`echo $QUARKUS_PROFILE`)

After AI writes code:
- [ ] Does it compile? (`./mvnw compile`)
- [ ] Constructor injection used?
- [ ] Fields properly encapsulated?
- [ ] Business logic in service?
- [ ] Soft deletes used?
- [ ] Tests pass? (`./mvnw test`)

## 🔧 Common Commands

```bash
# Development
source .env && ./mvnw quarkus:dev    # Start dev server
./mvnw compile                        # Compile only
./mvnw test                           # Run tests
./mvnw verify                         # Tests + coverage

# Verification
echo $QUARKUS_PROFILE                # Check profile
grep DB_DEV_NAME .env                # Check database
docker ps | grep postgres            # Check PostgreSQL

# Quality
./mvnw verify                        # Generate coverage
open target/site/jacoco/index.html  # View coverage
```

## 📚 Templates

### Create Controller
```
Use api_developer to create a controller for [Resource]:
- Path: /api/v1/[resources]
- Methods: GET, POST, PUT, DELETE
- DTOs: [List required fields]
- Validation: [Specify rules]
```

### Create Service
```
Use service_developer to implement [Feature]Service:
- Business rules: [List rules]
- Validations: [Specify]
- Transactions: [Which methods need @Transactional]
```

### Create Migration
```
Use database_architect to create migration:
- Version: V1.0.X
- Description: [What changes]
- Tables: [List tables]
- Columns: [List columns with types]
```

## 🆘 Troubleshooting

### AI suggests wrong database
**Problem:** AI tries to connect to `quarkus_db`  
**Fix:** Remind: "Use quarkus_dev database, not quarkus_db"

### AI uses field injection
**Problem:** AI writes `@Inject private Logger log;`  
**Fix:** Remind: "Use constructor injection, not field injection"

### AI puts business logic in controller
**Problem:** Validation/logic in controller  
**Fix:** Remind: "Move business logic to service layer"

### AI uses hard delete
**Problem:** AI writes `repository.delete(entity)`  
**Fix:** Remind: "Use soft delete with deletedAt timestamp"

## 🎓 Learning Path

### Day 1: Basics
1. Read `AGENTS.md` (10 min)
2. Review `.aiconfig.yml` (5 min)
3. Try a simple task with an agent

### Day 2: Patterns
1. Read `.github/copilot-instructions.md` (20 min)
2. Study existing code in `src/main/java/`
3. Try creating a new endpoint with agents

### Day 3: Advanced
1. Review agent definitions in `.abacus/agents.yml`
2. Review skill definitions in `.github/skills/`
3. Try a complete feature with multiple agents

## 💡 Pro Tips

1. **Be Specific**: Instead of "create user feature", say "use database_architect to create migration for user_settings table with columns: theme (VARCHAR), language (VARCHAR)"

2. **Reference Patterns**: Say "following the pattern in UserController" to maintain consistency

3. **Chain Agents**: Use multiple agents in sequence for complex tasks

4. **Validate Often**: Run `./mvnw compile` after each significant change

5. **Read First**: Ask AI to "analyze existing [feature] implementation" before adding to it

6. **Use Checklists**: Ask AI to "validate against architecture checklist" before completion

## 🔗 Links

- **Main Guide:** [AGENTS.md](../AGENTS.md)
- **Configuration:** [.aiconfig.yml](../.aiconfig.yml)
- **Copilot Instructions:** [.github/copilot-instructions.md](../.github/copilot-instructions.md)
- **Agent Definitions:** [.abacus/agents.yml](.abacus/agents.yml)
- **Skills:** [.github/skills/](.github/skills/)

---

**Ready to start?** Pick a task and invoke the appropriate agent! 🚀
