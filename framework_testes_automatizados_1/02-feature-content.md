# 📹 Feature: Content — Plano de Ação

## Análise de Testabilidade

### Estrutura Atual
```
content/
├── ContentRecordController.java   # REST endpoints + lógica CRUD
├── ContentRecordModel.java         # JPA Entity (PanacheEntityBase)
├── ContentService.java             # Lógica de paginação e ordenação
└── ContentType.java                # Enum
```

### Más Práticas Identificadas

#### 🔴 1. Regras de Negócio e Persistência no Controller (CRÍTICO)
**Severidade:** Alta | **Impacto:** Impossível testar isoladamente

```java
// ContentRecordController.java - Lógica de negócio no Controller

@POST
@Transactional  // ← Transactional no Controller! (deveria estar no Service)
public Response create(@Valid ContentRecordModel contentRecordModel) {
    contentRecordModel.persist();  // ← Persistência direta!
    return Response.status(Status.CREATED).entity(contentRecordModel).build();
}

@PUT
@Path("/{id}")
@Transactional
public Response update(@PathParam("id") UUID id, @Valid ContentRecordModel dataFromRequest) {
    ContentRecordModel existing = ContentRecordModel.findById(id);  // ← Query estática
    // ... mapeamento manual de campos ...
    return Response.ok(existing).build();
}

@DELETE
@Path("/{id}")
@Transactional
public Response delete(@PathParam("id") UUID id) {
    ContentRecordModel existing = ContentRecordModel.findById(id);  // ← Query estática
    boolean deleted = ContentRecordModel.deleteById(id);  // ← HARD DELETE!
    // ...
}
```

**3 métodos com lógica CRUD no Controller.** Deveriam delegar tudo ao ContentService.

#### 🔴 2. Chama `ContentRecordModel` diretamente no Controller
**Severidade:** Alta | **Impacto:** Impossível mockar

```java
// ContentRecordController.java
var query = ContentRecordModel.findAll();  // GET /paged
ContentRecordModel result = ContentRecordModel.findByTitle(title);  // GET /find-first-title
List<ContentRecordModel> results = ContentRecordModel.searchByTitle(query);  // GET /search
ContentRecordModel result = ContentRecordModel.findById(idHash);  // GET /{id}
```

#### 🔴 3. Hard Delete (Violação de regra do projeto)
**Severidade:** Alta | **Impacto:** Perda de dados

```java
boolean deleted = ContentRecordModel.deleteById(id);  // ← HARD DELETE
```

Regra do projeto: **sempre soft delete**. Este código remove fisicamente o registro.

#### 🟡 4. Método redundante no Controller
**Severidade:** Baixa | **Impacto:** Manutenibilidade

`listPaginatedWithMeta()` duplica a funcionalidade paginada de `listContents()`. Já documentado como "redundant" no próprio código.

#### 🟡 5. Exception handling genérico
**Severidade:** Baixa | **Impacto:** Debugging

```java
catch (Exception e) {
    return Response.status(Status.INTERNAL_SERVER_ERROR)
        .entity("Erro interno do servidor: " + e.getMessage())
        .build();
}
```

---

## 🔧 Plano de Refatoração

### Passo 1: Extrair ContentRepository (2h)

```java
// NOVO: br.com/aguideptbr/features/content/ContentRecordRepository.java
@ApplicationScoped
public class ContentRecordRepository implements PanacheRepositoryBase<ContentRecordModel, UUID> {
    public ContentRecordModel findByTitle(String title) { ... }
    public List<ContentRecordModel> searchByTitle(String query) { ... }
    public List<ContentRecordModel> findAllSorted(Sort sort) { ... }
    public void softDelete(UUID id) {  // ← Soft delete!
        ContentRecordModel existing = findById(id);
        if (existing != null) {
            existing.deletedAt = LocalDateTime.now();
            persist(existing);
        }
    }
}
```

### Passo 2: Mover CRUD do Controller para ContentService (3h)

```java
@ApplicationScoped
public class ContentService {
    private final ContentRecordRepository repository;
    
    // JÁ EXISTE: getPaginatedContents(), getLimitedContents()
    
    // NOVOS:
    @Transactional
    public ContentRecordModel create(ContentRecordModel model) {
        model.persist();
        return model;
    }
    
    @Transactional
    public ContentRecordModel update(UUID id, ContentRecordModel data) {
        ContentRecordModel existing = repository.findById(id);
        if (existing == null) throw new NotFoundException("Content not found");
        existing.title = data.title;
        existing.description = data.description;
        // ... demais campos ...
        return existing;
    }
    
    @Transactional
    public void delete(UUID id) {
        repository.softDelete(id);  // ← Soft delete!
    }
}
```

### Passo 3: Simplificar Controller (1h)

```java
@POST
public Response create(@Valid ContentRecordModel request) {
    var created = contentService.create(request);
    return Response.status(Status.CREATED).entity(created).build();
}

@PUT @Path("/{id}")
public Response update(@PathParam("id") UUID id, @Valid ContentRecordModel request) {
    return Response.ok(contentService.update(id, request)).build();
}

@DELETE @Path("/{id}")
public Response delete(@PathParam("id") UUID id) {
    contentService.delete(id);
    return Response.noContent().build();
}
```

---

## 🧪 Estratégia de Testes

### Testes Unitários (com mocks)

| Método | Cenário | Mock |
|--------|---------|------|
| ContentService.create() | Content criado com sucesso | ContentRecordRepository |
| ContentService.update() | Content não encontrado → 404 | ContentRecordRepository |
| ContentService.update() | Update bem-sucedido | ContentRecordRepository |
| ContentService.delete() | Soft delete bem-sucedido | ContentRecordRepository |
| ContentService.delete() | Content não encontrado → 404 | ContentRecordRepository |
| ContentService.getPaginatedContents() | Ordenação válida | ContentRecordRepository (count, findAll) |

### Testes de Integração (@QuarkusTest)

| Teste | Status |
|-------|--------|
| ContentRecordResourceTest | ✅ Já existe |
| listContents() paginado | ✅ |
| listContents() limitado | ✅ |
| CRUD via REST | ✅ |

### Testes de Regressão

- **Cenário:** Criar conteúdo → 201
- **Cenário:** Buscar por ID → 200
- **Cenário:** Atualizar conteúdo → 200
- **Cenário:** Deletar conteúdo → 204 (soft delete)
- **Cenário:** Buscar conteúdo deletado → 404
- **Cenário:** Paginação com sort inválido → 400

---

## 📊 Impacto na Pipeline

**Baixo.** A refatoração é puramente estrutural. A adição de `softDelete` muda comportamento do DELETE — necessário atualizar testes de integração que esperam hard delete.

### Mudança Requerida no Jenkinsfile
Nenhuma. Testes unitários novos rodam mais rápido.

---

## ✅ Checklist de Implementação

- [ ] Criar `ContentRecordRepository` com métodos find/query e softDelete
- [ ] Mover CRUD para `ContentService` (create, update, delete)
- [ ] Corrigir DELETE para soft delete
- [ ] Atualizar Controller para delegar ao Service
- [ ] Escrever testes unitários para ContentService
- [ ] Atualizar testes de integração existentes
- [ ] Rodar `./mvnw verify` completo
