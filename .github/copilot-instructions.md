# GitHub Copilot - Instruções do Projeto

## Visão Geral
Este é um projeto **Java 17+ com Quarkus 3.x** seguindo arquitetura de camadas (Controller → Service → Repository). Use sempre CDI do Quarkus (`@Inject`, `@ApplicationScoped`) e RESTEasy Reactive para APIs REST.

## Estrutura de Pacotes OBRIGATÓRIA
```
br.com.aguideptbr/
├── auth/              # Autenticação e segurança
├── features/          # Funcionalidades de negócio (organizadas por domínio)
│   ├── user/
│   │   ├── UserController.java
│   │   ├── UserService.java
│   │   ├── UserRepository.java
│   │   └── User.java (entidade)
│   └── [outra-feature]/
└── util/              # Utilitários compartilhados
```

## Convenções de Código

### 1. Controllers REST
- Usar `@Path("/api/v1/recurso")` na classe
- Métodos anotados com `@GET`, `@POST`, `@PUT`, `@DELETE`
- Retornar `Response` ou `Uni<Response>` (reactive)
- Validar entrada com Bean Validation (`@Valid`)
- Logs obrigatórios: entrada de request e erros
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
- Anotados com `@ApplicationScoped`
- Contém lógica de negócio
- Injeta repositories com `@Inject`
- Transações com `@Transactional` quando necessário
```java
@ApplicationScoped
public class UserService {
    @Inject UserRepository userRepository;
    @Inject Logger log;

    @Transactional
    public User create(User user) {
        // lógica de negócio
    }
}
```

### 3. Repositories
- Estender `PanacheRepository<Entity>` ou usar `PanacheEntity`
- Métodos de consulta customizados seguem padrão `findByXxx`
- Não colocar lógica de negócio aqui
```java
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public User findByEmail(String email) {
        return find("email", email).firstResult();
    }
}
```

### 4. Entidades
- Herdar de `PanacheEntity` (id gerado automaticamente) OU usar `PanacheEntityBase` com `@Id` customizado
- Usar `@Entity`, `@Table`, `@Column`
- Sempre incluir campos de auditoria:
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

## Tratamento de Exceções
- Usar `@ServerExceptionMapper` para tratamento global
- Nunca expor stacktraces para o cliente em produção
- Retornar JSON estruturado:
```java
{
  "error": "User not found",
  "message": "Usuário com ID 123 não encontrado",
  "timestamp": "2026-01-31T10:30:00Z"
}
```

## Logging
- Injetar `Logger` do JBoss: `@Inject Logger log;`
- Níveis: `log.info()` para operações normais, `log.error()` para erros, `log.debug()` para debug
- Sempre logar: início de operações importantes, erros com stacktrace, dados sensíveis NÃO devem ser logados

## Configurações
- Usar `application.properties` para configurações comuns
- Usar `application-dev.properties` e `application-prod.properties` para ambientes específicos
- Acessar configs com `@ConfigProperty(name = "key") String value;`

## Migrations de Banco de Dados
- Usar Flyway em `src/main/resources/db/migration/`
- Nomenclatura: `V[major].[minor].[patch]__[Description].sql`
- Exemplo: `V1.0.3__Add_user_role_column.sql`
- **NUNCA modificar migrations já aplicadas**
- **H2 vs PostgreSQL**: Migrations para testes ficam em `db/migration/h2/` (sintaxe compatível)

## Testes
- Localização: `src/test/java/br/com/aguideptbr/features/[feature]/`
- Usar `@QuarkusTest` para testes de integração
- Usar `RestAssured` para testar endpoints
- Cobertura mínima desejada: 80%

### Dependência Obrigatória para Testes
**Adicionar H2 no pom.xml:**
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jdbc-h2</artifactId>
    <scope>test</scope>
</dependency>
```

### Configuração de Testes (CRÍTICO)
**SEMPRE criar `src/test/resources/application.properties` com:**
```properties
# Desabilita AuthenticationFilter em testes
quarkus.arc.exclude-types=br.com.aguideptbr.auth.AuthenticationFilter

# Usa H2 em memória para testes rápidos
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
quarkus.datasource.username=sa
quarkus.datasource.password=

# Flyway em testes - USA MIGRATIONS ESPECÍFICAS DO H2
quarkus.flyway.clean-at-start=true
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=classpath:db/migration/h2
```

**Diferenças H2 vs PostgreSQL nas Migrations:**
- PostgreSQL: `DEFAULT gen_random_uuid()` → H2: `DEFAULT RANDOM_UUID()`
- PostgreSQL: `ADD COLUMN x, ADD COLUMN y` → H2: Separar em múltiplos `ALTER TABLE`
- PostgreSQL: `COMMENT ON COLUMN` → H2: Não suportado (remover)
- PostgreSQL: `USING gin(to_tsvector(...))` → H2: Índice simples sem gin

### Regras de Testes
✅ **PERMITIDO:**
- Desabilitar filtros de autenticação via `quarkus.arc.exclude-types`
- Usar H2 em memória para testes
- RestAssured sem headers de autenticação em testes

❌ **PROIBIDO:**
- Hardcoded tokens/senhas no código de teste
- Usar `-DskipTests` no Jenkins/CI (testes são barreira de qualidade)
- Pular testes para "resolver rápido" problemas de autenticação
- Usar banco PostgreSQL real em testes (usar H2)

## Segurança
- Autenticação implementada via `AuthenticationFilter`
- Nunca comitar credenciais, tokens ou senhas
- Usar `@RolesAllowed` para controle de acesso

## Docker
- Dockerfiles em `src/main/docker/`
- Preferir `Dockerfile.jvm` para desenvolvimento
- `Dockerfile.native` para produção (GraalVM)

## CI/CD
- Jenkins configurado (ver `Jenkinsfile`)
- SonarQube integrado para análise de código
- Build Maven: `./mvnw clean package`

## O QUE NÃO FAZER
❌ Criar arquivos temporários na raiz do projeto
❌ Colocar lógica de negócio em Controllers ou Repositories
❌ Usar anotações do Spring (usar Quarkus CDI)
❌ Esquecer `@Transactional` em métodos que modificam dados
❌ Criar packages fora de `br.com.aguideptbr`
❌ Ignorar tratamento de exceções
❌ Logar informações sensíveis (senhas, tokens)
❌ Hardcoded credenciais/tokens em testes
❌ Pular testes no CI/CD com `-DskipTests`
❌ Usar banco real (PostgreSQL) em testes unitários

## Recursos do Quarkus a Utilizar
✅ Dev Mode: `./mvnw quarkus:dev` (hot reload automático)
✅ Dev Services: bancos de dados automaticamente em containers
✅ Panache: simplificação de JPA/Hibernate
✅ RESTEasy Reactive: performance melhorada
✅ SmallRye Health: endpoints `/q/health`
✅ OpenAPI/Swagger: `/q/swagger-ui`

---
**Importante:** Ao gerar código, sempre verificar se está seguindo estas diretrizes. Em caso de dúvida, consultar o arquivo `DEVELOPMENT_GUIDE.md` na raiz do projeto.
