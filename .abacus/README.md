# Abacus AI CLI - Agent Configuration

This directory contains specialized agent definitions for the Abacus AI CLI and other AI coding assistants.

## 📁 Files

- **`agents.yml`** - Main agent configuration file defining specialized agents, their expertise, and invocation patterns

## 🤖 Available Agents

### 1. **Database Architect** (`database_architect`)
Expert in PostgreSQL schema design, Flyway migrations, and JPA entity modeling.

**Use for:**
- Creating new database tables
- Writing Flyway migration files
- Modeling JPA entities and relationships
- Writing complex SQL queries
- Creating seed/fixture data

**Example invocation:**
```
Use the database_architect agent to create a migration for adding a user_preferences table
```

### 2. **Messaging Feature Specialist** (`messaging_specialist`)
Expert in the user messaging system (conversations, messages, participants).

**Use for:**
- Implementing messaging endpoints
- Debugging conversation flows
- Managing participants
- Implementing read receipts
- Message threading

**Example invocation:**
```
Use the messaging_specialist to implement read receipt functionality
```

### 3. **Architecture Guardian** (`architecture_guardian`)
Ensures code follows project patterns, best practices, and quality standards.

**Use for:**
- Code review
- Refactoring to meet standards
- Validating constructor injection
- Checking encapsulation
- Sonar rule compliance

**Example invocation:**
```
Ask the architecture_guardian to review this service class for pattern compliance
```

### 4. **REST API Developer** (`api_developer`)
Specialist in creating RESTful endpoints following project patterns.

**Use for:**
- Creating new REST controllers
- Designing request/response DTOs
- Implementing proper HTTP methods
- Adding validation
- Error handling

**Example invocation:**
```
Use the api_developer to create a new endpoint for user notifications
```

### 5. **Service Layer Developer** (`service_developer`)
Expert in implementing business logic following service layer patterns.

**Use for:**
- Implementing business logic
- Transaction management
- Domain validation
- Service orchestration
- Exception handling

**Example invocation:**
```
Use the service_developer to implement the validation logic for user registration
```

### 6. **Test Engineer** (`test_engineer`)
Specialist in writing comprehensive unit and integration tests.

**Use for:**
- Writing unit tests
- Writing integration tests
- Creating test fixtures
- Improving test coverage

**Example invocation:**
```
Use the test_engineer to write tests for the ConversationService
```

## 🔄 Common Workflows

### Implementing a New Feature
1. **database_architect**: Design schema and create migration
2. **service_developer**: Implement business logic
3. **api_developer**: Create REST endpoints
4. **test_engineer**: Write tests
5. **architecture_guardian**: Validate quality

### Fixing a Bug
1. **architecture_guardian**: Analyze code and identify issue
2. **service_developer**: Fix business logic
3. **test_engineer**: Add regression test

### Code Refactoring
1. **architecture_guardian**: Identify violations
2. **service_developer**: Refactor business logic
3. **api_developer**: Update endpoints if needed
4. **test_engineer**: Ensure tests still pass

## 🚀 How to Use

### With Abacus AI CLI
Agents are automatically available when working in this project. Reference them by name or trigger keywords:

```bash
# Direct agent reference
"Use the database_architect agent to..."

# Trigger by keywords
"Create a migration for..." (triggers database_architect)
"Implement messaging endpoint..." (triggers messaging_specialist)
"Review this code..." (triggers architecture_guardian)
```

### With Other AI Assistants

The agent definitions can also guide other AI tools:

**GitHub Copilot:**
- Read `.github/copilot-instructions.md` and agent definitions
- Use `@workspace` to reference project context

**Cursor:**
- Configure to read `AGENTS.md` and `.aiconfig.yml`
- Reference agent patterns in chat

**Continue:**
- Add agent files to context
- Use patterns as templates

## 📚 Additional Resources

- **Main Guide:** `../AGENTS.md` (Project-wide AI assistant guide)
- **Configuration:** `../.aiconfig.yml` (Project configuration)
- **Copilot Instructions:** `../.github/copilot-instructions.md` (Detailed instructions)
- **Skills:** `../.github/skills/` (Specialized skill definitions)

## ⚙️ Agent Configuration

Each agent in `agents.yml` defines:

- **Name & Description**: What the agent does
- **Trigger Keywords**: Words that invoke this agent
- **Context Files**: Files the agent should read
- **Expertise**: Areas of knowledge
- **Responsibilities**: What the agent can do
- **Constraints**: Rules the agent must follow
- **Workflow**: Steps the agent follows

## 🔒 Critical Rules for All Agents

All agents MUST follow these rules:

1. **Database Safety**
   - NEVER connect to `quarkus_db` (production) locally
   - ALWAYS use `quarkus_dev` for local development

2. **Code Patterns**
   - ALWAYS use constructor injection (NO field @Inject)
   - ALWAYS use private fields in DTOs with getters/setters
   - ALWAYS use soft delete (deletedAt) instead of hard delete

3. **Before Changes**
   - Read existing code first
   - Follow existing architecture patterns
   - Compile after changes: `./mvnw compile`

## ✅ Quality Gates

Before completing any task, all agents verify:

- ✅ Code compiles: `./mvnw compile`
- ✅ Tests pass: `./mvnw test` (if applicable)
- ✅ No field injection (constructor injection only)
- ✅ No public fields in DTOs
- ✅ Business logic in service layer only
- ✅ Soft deletes used (deletedAt)
- ✅ Proper logging (JBoss Logger, no System.out)

## 🆘 Need Help?

1. Read `../AGENTS.md` for comprehensive guide
2. Check `.aiconfig.yml` for patterns and templates
3. Review `.github/copilot-instructions.md` for detailed instructions
4. Look at existing code in `src/main/java/` for examples

---

**Last Updated:** 2026-06-09  
**Version:** 1.0  
**Compatible with:** Abacus AI CLI, GitHub Copilot, Cursor, Continue, and other AI coding assistants
