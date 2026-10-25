# 👤 Feature: User — Plano de Ação

## Análise de Testabilidade

### Estrutura Atual
```
user/
├── UserController.java     # REST endpoints + CRUD direto
├── UserModel.java          # JPA Entity (PanacheEntityBase)
├── UserRole.java           # Enum
└── dto/
    ├── PhoneSummaryDTO.java
    └── UserDetailResponse.java
```

**⚠️ NOTA:** User não tem Service nem Repository. Toda lógica está no Controller e nas chamadas estáticas do UserModel.

### Más Práticas Identificadas

#### 🔴 1. Zero camada de Service (CRÍTICO)
**Severidade:** Alta | **Impacto:** Impossível testar lógica isoladamente

```java
// UserController.java - TODO no Controller:
UserModel.find("deletedAt is null").page(Page.of(0, 50)).list();  // L47
UserModel.count("deletedAt is null");  // L82
UserModel.find("deletedAt is null").page(Page.of(page, size)).list();  // L88
UserModel.findByIdActive(id);  // L100, L125, L149
UserModel.findById(id);  // L163
userModel.persist();  // L137
user.softDelete();  // L155
user.restore();  // L173
```

**8+ chamadas diretas ao modelo no Controller.** Sem Service para encapsular regras de negócio.

#### 🔴 2. `@Transactional` no Controller
**Severidade:** Alta | **Impacto:** Viola arquitetura em camadas

```java
@POST @Transactional
public Response createUser(UserModel userModel) {
    userModel.persist();
}

@DELETE @Path("/{id}") @Transactional
public Response deleteUser(@PathParam("id") UUID id) { ... }

@PUT @Path("/{id}/restore") @Transactional
public Response restoreUser(@PathParam("id") UUID id) { ... }

@PUT @Path("/{id}") @Transactional
public Response updateUser(@PathParam("id") UUID id, UserModel dataFromRequest) { ... }
```

#### 🟡 3. Método createUser sem validação
**Severidade:** Média | **Impacto:** Dados inconsistentes

```java
@POST @Transactional
public Response createUser(UserModel userModel) {
    userModel.persist();  // ← Sem validação, sem verificação de duplicidade
}
```

#### 🟡 4. UserController sem Service injetado
**Severidade:** Média | **Impacto:** Testabilidade

```java
public UserController(Logger log) {
    this.log = log;
    // NENHUM SERVICE INJETADO!
}
```

---

## 🔧 Plano de Refatoração

### Passo 1: Criar UserRepository (2h)

```java
// NOVO: br.com/aguideptbr/features/user/UserRepository.java
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserModel, UUID> {
    public UserModel findByIdActive(UUID id) {
        return find("id = ?1 and deletedAt is null", id).firstResult();
    }
    public List<UserModel> findAllActive() { ... }
    public long countActive() { ... }
    public List<UserModel> findAllActivePaginated(int page, int size) { ... }
}
```

### Passo 2: Criar UserService (3h)

Mover toda lógica CRUD do Controller para Service:

```java
@ApplicationScoped
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository, Logger log) { ... }
    
    public List<UserDetailResponse> listActive(int limit) { ... }
    public PaginatedResponse<UserDetailResponse> listPaginated(int page, int size) { ... }
    public UserModel findById(UUID id) { ... }
    
    @Transactional
    public UserModel create(UserModel user) {
        // validar email único
        // validar campos obrigatórios
        user.persist();
        return user;
    }
    
    @Transactional
    public UserModel update(UUID id, UserModel data) { ... }
    
    @Transactional
    public void softDelete(UUID id) { ... }
    
    @Transactional
    public UserModel restore(UUID id) { ... }
}
```

### Passo 3: Simplificar Controller (30min)

```java
public class UserController {
    private final UserService userService;
    private final Logger log;
    
    public UserController(UserService userService, Logger log) {
        this.userService = userService;
        this.log = log;
    }
    
    @GET
    public Response list() {
        return Response.ok(userService.listActive(50)).build();
    }
    
    @POST @Transactional  // ← Remover @Transactional daqui
    public Response createUser(UserModel userModel) {
        return Response.status(201).entity(userService.create(userModel)).build();
    }
    // ...
}
```

---

## 🧪 Estratégia de Testes

### Testes Unitários (com mocks)

| Método | Cenário | Mocks |
|--------|---------|-------|
| UserService.listActive() | Lista paginada com limite | UserRepository |
| UserService.findById() | Usuário encontrado | UserRepository |
| UserService.findById() | Usuário não encontrado → 404 | UserRepository |
| UserService.create() | Sucesso | UserRepository |
| UserService.create() | Email duplicado → 409 | UserRepository |
| UserService.update() | Sucesso | UserRepository |
| UserService.softDelete() | Sucesso | UserRepository |
| UserService.softDelete() | Usuário não encontrado → 404 | UserRepository |
| UserService.restore() | Sucesso | UserRepository |
| UserService.restore() | Usuário já ativo → 400 | UserRepository |

### Testes de Integração

| Teste | Status |
|-------|--------|
| UserResourceTest | ✅ Já existe |

### Testes de Regressão

- **Cenário:** Listar usuários → 200
- **Cenário:** Buscar por ID → 200
- **Cenário:** Buscar por ID inexistente → 404
- **Cenário:** Criar usuário → 201
- **Cenário:** Atualizar usuário → 200
- **Cenário:** Soft delete → 204
- **Cenário:** Restaurar usuário deletado → 200
- **Cenário:** Buscar detalhes com telefones → 200

---

## 📊 Impacto na Pipeline

**Nenhum.** Criação de nova camada não altera contratos de API. Testes existentes continuam válidos.

---

## ✅ Checklist de Implementação

- [ ] Criar `UserRepository`
- [ ] Criar `UserService` com métodos CRUD
- [ ] Remover `@Transactional` do Controller
- [ ] Remover chamadas Panache diretas do Controller
- [ ] Escrever testes unitários para UserService
- [ ] Rodar `./mvnw verify` completo
