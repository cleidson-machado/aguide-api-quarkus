# Guia de Desenvolvimento - Mobile REST API (Java + Quarkus)

## 📋 Índice
1. [Visão Geral do Projeto](#visão-geral-do-projeto)
2. [Arquitetura e Padrões](#arquitetura-e-padrões)
3. [Estrutura de Pacotes](#estrutura-de-pacotes)
4. [Convenções de Código](#convenções-de-código)
5. [Gestão de Banco de Dados](#gestão-de-banco-de-dados)
6. [Testes](#testes)
7. [Segurança](#segurança)
8. [Deploy e Containerização](#deploy-e-containerização)
9. [Troubleshooting](#troubleshooting)

---

## 🎯 Visão Geral do Projeto

### Stack Tecnológica
- **Framework:** Quarkus 3.x (Supersonic Subatomic Java)
- **Java:** 17+ (LTS)
- **Build Tool:** Maven
- **Persistence:** Hibernate ORM com Panache
- **Migration:** Flyway
- **REST:** RESTEasy Reactive
- **Dependency Injection:** CDI (Contexts and Dependency Injection)
- **Containerização:** Docker / Podman
- **CI/CD:** Jenkins + SonarQube

### Características do Quarkus
- **Dev Mode:** Hot reload automático (`./mvnw quarkus:dev`)
- **Dev Services:** Containers automáticos para bancos de dados e outros serviços
- **Native Compilation:** GraalVM para executáveis nativos ultra-rápidos
- **Cloud Native:** Otimizado para Kubernetes e ambientes serverless

---

## 🏗️ Arquitetura e Padrões

### Arquitetura em Camadas
```
┌─────────────────────┐
│   REST Controller   │  → Endpoints HTTP, validação de entrada
├─────────────────────┤
│      Service        │  → Lógica de negócio, orquestração
├─────────────────────┤
│     Repository      │  → Acesso a dados, queries
├─────────────────────┤
│      Entity         │  → Modelo de domínio (JPA/Hibernate)
└─────────────────────┘
```

### Princípios de Design
- **Single Responsibility:** Cada classe tem uma única responsabilidade
- **Separation of Concerns:** Controllers não têm lógica de negócio, repositories não têm lógica de negócio
- **Dependency Injection:** Sempre usar `@Inject`, nunca `new` para componentes gerenciados
- **Fail Fast:** Validar entrada o mais cedo possível
- **Idempotência:** Operações PUT e DELETE devem ser idempotentes

---

## 📁 Estrutura de Pacotes

### Organização por Feature (Domain-Driven Design)
```
src/main/java/br/com/aguideptbr/
│
├── auth/                           # Módulo de autenticação
│   ├── AuthenticationFilter.java  # Filtro de autenticação JWT/OAuth
│   ├── AuthService.java            # Serviço de autenticação
│   └── TokenProvider.java          # Geração/validação de tokens
│
├── features/                       # Funcionalidades de negócio
│   │
│   ├── user/                       # Módulo de usuários
│   │   ├── User.java               # Entidade
│   │   ├── UserController.java    # REST endpoints
│   │   ├── UserService.java       # Lógica de negócio
│   │   ├── UserRepository.java    # Acesso a dados
│   │   ├── dto/
│   │   │   ├── UserRequestDTO.java
│   │   │   └── UserResponseDTO.java
│   │   └── exception/
│   │       └── UserNotFoundException.java
│   │
│   ├── content/                    # Módulo de conteúdo
│   │   ├── Content.java
│   │   ├── ContentController.java
│   │   ├── ContentService.java
│   │   └── ContentRepository.java
│   │
│   └── [nova-feature]/            # Template para novas features
│       ├── [Entity].java
│       ├── [Entity]Controller.java
│       ├── [Entity]Service.java
│       └── [Entity]Repository.java
│
└── util/                           # Utilitários compartilhados
    ├── DateUtil.java
    ├── ValidationUtil.java
    ├── Constants.java
    └── exception/
        ├── GlobalExceptionHandler.java
        ├── BusinessException.java
        └── ErrorResponse.java
```

### Nomenclatura de Classes
- **Entity:** `User`, `Product`, `Order`
- **Controller:** `UserController`, `ProductController`
- **Service:** `UserService`, `ProductService`
- **Repository:** `UserRepository`, `ProductRepository`
- **DTO:** `UserRequestDTO`, `UserResponseDTO`
- **Exception:** `UserNotFoundException`, `InvalidEmailException`

---

## 💻 Convenções de Código

### 1. REST Controllers

#### Template Padrão
```java
package br.com.aguideptbr.features.user;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.net.URI;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    UserService userService;

    @Inject
    Logger log;

    @GET
    public Response findAll() {
        log.info("GET /api/v1/users - Listing all users");
        return Response.ok(userService.findAll()).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        log.infof("GET /api/v1/users/%d - Finding user by ID", id);
        var user = userService.findById(id);
        return Response.ok(user).build();
    }

    @POST
    public Response create(@Valid UserRequestDTO dto) {
        log.info("POST /api/v1/users - Creating new user");
        var created = userService.create(dto);
        return Response.created(URI.create("/api/v1/users/" + created.id))
                       .entity(created)
                       .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid UserRequestDTO dto) {
        log.infof("PUT /api/v1/users/%d - Updating user", id);
        var updated = userService.update(id, dto);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        log.infof("DELETE /api/v1/users/%d - Deleting user", id);
        userService.delete(id);
        return Response.noContent().build();
    }
}
```

#### Convenções de Controllers
- ✅ Sempre usar versioning na URL (`/api/v1/`)
- ✅ Usar substantivos no plural para coleções (`/users`, não `/user`)
- ✅ Retornar códigos HTTP apropriados:
  - `200 OK` - Sucesso com retorno de dados
  - `201 Created` - Recurso criado (com header `Location`)
  - `204 No Content` - Sucesso sem retorno de dados (DELETE)
  - `400 Bad Request` - Erro de validação
  - `404 Not Found` - Recurso não encontrado
  - `500 Internal Server Error` - Erro do servidor
- ✅ Validar entrada com Bean Validation (`@Valid`)
- ✅ Logar início de cada operação
- ❌ **NUNCA** colocar lógica de negócio no controller
- ❌ **NUNCA** acessar repository diretamente do controller

### 2. Services

#### Template Padrão
```java
package br.com.aguideptbr.features.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    Logger log;

    public List<User> findAll() {
        return userRepository.listAll();
    }

    public User findById(Long id) {
        return userRepository.findByIdOptional(id)
            .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }

    @Transactional
    public User create(UserRequestDTO dto) {
        // Validações de negócio
        if (userRepository.findByEmail(dto.email) != null) {
            throw new BusinessException("Email already registered");
        }

        var user = new User();
        user.name = dto.name;
        user.email = dto.email;
        
        userRepository.persist(user);
        log.infof("User created with ID: %d", user.id);
        
        return user;
    }

    @Transactional
    public User update(Long id, UserRequestDTO dto) {
        var user = findById(id);
        user.name = dto.name;
        user.email = dto.email;
        
        userRepository.persist(user);
        log.infof("User updated with ID: %d", user.id);
        
        return user;
    }

    @Transactional
    public void delete(Long id) {
        var user = findById(id);
        userRepository.delete(user);
        log.infof("User deleted with ID: %d", id);
    }
}
```

#### Convenções de Services
- ✅ Anotar com `@ApplicationScoped` (singleton)
- ✅ Usar `@Transactional` em métodos que modificam dados (CREATE, UPDATE, DELETE)
- ✅ Validar regras de negócio aqui
- ✅ Orquestrar chamadas a múltiplos repositories
- ✅ Lançar exceções de negócio quando apropriado
- ❌ **NUNCA** retornar `null` - preferir `Optional` ou lançar exceção

### 3. Repositories

#### Template Padrão (Panache Repository)
```java
package br.com.aguideptbr.features.user;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public List<User> findByNameContaining(String name) {
        return find("LOWER(name) LIKE LOWER(?1)", "%" + name + "%").list();
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }
}
```

#### Convenções de Repositories
- ✅ Implementar `PanacheRepository<Entity>`
- ✅ Métodos customizados seguem padrão `findByXxx`, `existsByXxx`, `deleteByXxx`
- ✅ Usar queries JPQL ou SQL nativas quando necessário
- ❌ **NUNCA** colocar lógica de negócio no repository
- ❌ **NUNCA** usar `@Transactional` no repository (usar no service)

### 4. Entidades

#### Template Padrão
```java
package br.com.aguideptbr.features.user;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {

    @Column(nullable = false, length = 100)
    public String name;

    @Column(unique = true, nullable = false, length = 100)
    public String email;

    @Column(name = "is_active")
    public Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Order> orders;
}
```

#### Convenções de Entidades
- ✅ Estender `PanacheEntity` (id gerado automaticamente) OU `PanacheEntityBase` (id customizado)
- ✅ Usar campos públicos (Panache gera getters/setters automaticamente)
- ✅ Sempre incluir `createdAt` e `updatedAt` para auditoria
- ✅ Nomear tabelas e colunas explicitamente com `@Table` e `@Column`
- ✅ Usar snake_case para nomes de tabelas e colunas (`user_name`, não `userName`)
- ✅ Definir constraints (`nullable`, `unique`, `length`)
- ❌ **NUNCA** expor entidades JPA diretamente na API (usar DTOs)

### 5. DTOs (Data Transfer Objects)

#### Template Padrão
```java
package br.com.aguideptbr.features.user.dto;

import jakarta.validation.constraints.*;

public class UserRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    public String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    public String email;

    @NotNull
    public Boolean isActive;
}
```

```java
package br.com.aguideptbr.features.user.dto;

import java.time.LocalDateTime;

public class UserResponseDTO {
    public Long id;
    public String name;
    public String email;
    public Boolean isActive;
    public LocalDateTime createdAt;

    // Factory method
    public static UserResponseDTO from(User user) {
        var dto = new UserResponseDTO();
        dto.id = user.id;
        dto.name = user.name;
        dto.email = user.email;
        dto.isActive = user.isActive;
        dto.createdAt = user.createdAt;
        return dto;
    }
}
```

#### Convenções de DTOs
- ✅ Separar Request e Response DTOs
- ✅ Usar Bean Validation em Request DTOs
- ✅ Incluir factory methods para conversão (`from`, `toEntity`)
- ✅ **NUNCA** expor senha ou dados sensíveis em Response DTOs

---

## 🗄️ Gestão de Banco de Dados

### Flyway Migrations

#### Estrutura
```
src/main/resources/db/migration/
├── V1.0.0__Create_tables.sql
├── V1.0.1__Insert_test_data.sql
├── V1.0.2__Add_audit_timestamps.sql
└── V1.0.3__Add_user_role_column.sql
```

#### Nomenclatura
```
V[major].[minor].[patch]__[Description].sql
  ↑                        ↑
  Obrigatório             Dois underscores
```

#### Exemplo de Migration
```sql
-- V1.0.3__Add_user_role_column.sql

ALTER TABLE users
ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'USER';

CREATE INDEX idx_users_role ON users(role);
```

#### Regras de Migrations
- ✅ **NUNCA** modificar migrations já aplicadas
- ✅ Sempre criar nova migration para alterações
- ✅ Incluir rollback manual se necessário (em comentário)
- ✅ Testar migrations em ambiente de desenvolvimento primeiro
- ✅ Usar transações quando possível

### Configuração de Banco de Dados

#### application.properties
```properties
# Database
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USERNAME:postgres}
quarkus.datasource.password=${DB_PASSWORD:postgres}
quarkus.datasource.jdbc.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:mobile_api}

# Hibernate
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.log.sql=false

# Flyway
quarkus.flyway.migrate-at-start=true
quarkus.flyway.baseline-on-migrate=true
```

---

## 🧪 Testes

### Estrutura de Testes
```
src/test/java/br/com/aguideptbr/features/user/
├── UserControllerTest.java     # Testes de integração (API)
├── UserServiceTest.java        # Testes unitários (lógica de negócio)
└── UserRepositoryTest.java     # Testes de repository (queries)
```

### Template de Teste de Controller
```java
package br.com.aguideptbr.features.user;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class UserControllerTest {

    @Test
    void testFindAll() {
        given()
            .when().get("/api/v1/users")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$.size()", is(notNullValue()));
    }

    @Test
    void testCreate() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name": "John Doe",
                    "email": "john@example.com",
                    "isActive": true
                }
                """)
            .when().post("/api/v1/users")
            .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("id", notNullValue())
                .body("name", is("John Doe"));
    }

    @Test
    void testFindById_NotFound() {
        given()
            .when().get("/api/v1/users/999999")
            .then()
                .statusCode(404);
    }
}
```

### Convenções de Testes
- ✅ Usar `@QuarkusTest` para testes de integração
- ✅ Usar RestAssured para testar endpoints
- ✅ Nomear testes descritivamente: `testMethodName_Scenario_ExpectedResult`
- ✅ Usar transações em testes para rollback automático
- ✅ Mock beans quando necessário com `@InjectMock`

---

## 🔒 Segurança

### Autenticação
- Implementada via `AuthenticationFilter` em `br.com.aguideptbr.auth`
- Usar JWT ou OAuth2 conforme configuração

### Autorização
```java
@RolesAllowed("ADMIN")
@GET
@Path("/admin")
public Response adminOnlyEndpoint() {
    // ...
}
```

### Boas Práticas
- ✅ **NUNCA** comitar credenciais, tokens ou senhas
- ✅ Usar variáveis de ambiente para secrets
- ✅ Validar e sanitizar toda entrada do usuário
- ✅ Usar HTTPS em produção
- ✅ Implementar rate limiting em endpoints públicos
- ❌ **NUNCA** logar senhas ou tokens

---

## 🐳 Deploy e Containerização

### Docker Build (JVM)
```bash
./mvnw clean package
docker build -f src/main/docker/Dockerfile.jvm -t mobile-rest-api:jvm .
docker run -p 8080:8080 mobile-rest-api:jvm
```

### Docker Build (Native)
```bash
./mvnw clean package -Pnative -Dquarkus.native.container-build=true
docker build -f src/main/docker/Dockerfile.native -t mobile-rest-api:native .
docker run -p 8080:8080 mobile-rest-api:native
```

### Docker Compose
```yaml
# docker-compose.yml
version: '3.8'
services:
  api:
    build:
      context: .
      dockerfile: src/main/docker/Dockerfile.jvm
    ports:
      - "8080:8080"
    environment:
      DB_HOST: postgres
      DB_USERNAME: postgres
      DB_PASSWORD: postgres
    depends_on:
      - postgres
  
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: mobile_api
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
```

---

## 🛠️ Troubleshooting

### Comandos Úteis

#### Dev Mode
```bash
./mvnw quarkus:dev
# Acesse http://localhost:8080/q/dev
```

#### Build
```bash
./mvnw clean package
```

#### Testes
```bash
./mvnw test
./mvnw verify  # Testes de integração
```

#### Verificar Dependências
```bash
./mvnw dependency:tree
```

#### Flyway Info
```bash
./mvnw flyway:info
```

### Erros Comuns

#### "Could not find or load main class"
- Verificar `application.properties` e `quarkus.package.main-class`

#### "Transaction already active"
- Remover `@Transactional` duplicado (service + repository)

#### "Connection refused to database"
- Verificar se database está rodando
- Verificar configurações em `application.properties`

#### "Constraint violation"
- Verificar Bean Validation no DTO
- Verificar constraints no banco de dados

---

## 📚 Recursos Adicionais

- [Quarkus Guides](https://quarkus.io/guides/)
- [Panache Documentation](https://quarkus.io/guides/hibernate-orm-panache)
- [RESTEasy Reactive](https://quarkus.io/guides/resteasy-reactive)
- [CDI Reference](https://quarkus.io/guides/cdi-reference)

---

**Última atualização:** Janeiro 2026
