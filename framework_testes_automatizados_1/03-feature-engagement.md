# 📊 Feature: Engagement — Plano de Ação

## Análise de Testabilidade

### Estrutura Atual
```
engagement/
├── ContentEngagementController.java  # REST endpoints
├── ContentEngagementService.java     # Lógica de negócio
├── ContentEngagementRepository.java  # Data access (já existe!)
├── ContentEngagementModel.java       # JPA Entity
├── EngagementStatus.java             # Enum
├── EngagementType.java               # Enum
└── dto/
    ├── CreateEngagementDTO.java
    ├── EngagementResponseDTO.java
    ├── UpdateEngagementDTO.java
    └── UserTopContentsDTO.java
```

### Más Práticas Identificadas

#### 🟡 1. Chamadas Panache estáticas no Service (médio impacto)
**Severidade:** Média | **Impacto:** Testabilidade

```java
// ContentEngagementService.java
UserModel user = UserModel.findById(dto.getUserId());           // L48
ContentRecordModel content = ContentRecordModel.findById(dto.getContentId());  // L62
ContentRecordModel content = ContentRecordModel.findById(dto.getContentId());  // L257 (getUserTopContents)
```

**Por que é 🟡 e não 🔴:** O EngagementService já usa `ContentEngagementRepository` injetado para a maioria das operações. Apenas 3 pontos usam chamadas estáticas, todas para validação de existência de usuário/conteúdo.

#### 🟡 2. WebApplicationException com Response montada manualmente
**Severidade:** Baixa | **Impacto:** Boilerplate

```java
throw new WebApplicationException(
    Response.status(Response.Status.NOT_FOUND)
        .entity(Map.of(
            "error", "User not found",
            "message", "User with ID " + dto.getUserId() + " does not exist",
            "timestamp", LocalDateTime.now()))
        .build());
```

Repetido 8+ vezes no Service. Poderia ser um método auxiliar ou exception mapper.

#### ✅ 3. Boa prática: Repository injetado
**Severidade:** N/A | **Impacto:** Positivo

`ContentEngagementRepository` já existe e é injetado. Bom exemplo a ser seguido por outras features.

---

## 🔧 Plano de Refatoração

### Passo 1: Extrair verificação de entidades para UserRepository/ContentRepository (2h)

Criar/estender repositories para validar existência:

```java
// Em UserRepository (assumindo que será criado)
public boolean existsById(UUID id) {
    return count("id = ?1 and deletedAt is null", id) > 0;
}
```

```java
// Em ContentRecordRepository
public boolean existsById(UUID id) {
    return count("id = ?1 and deletedAt is null", id) > 0;
}
```

Depois substituir em ContentEngagementService:

```java
// ANTES:
UserModel user = UserModel.findById(dto.getUserId());
if (user == null) { throw ... }

// DEPOIS:
if (!userRepository.existsById(dto.getUserId())) { throw ... }
```

### Passo 2: Simplificar WebApplicationException com helper (30min)

```java
// Helper no próprio service ou em classe utilitária
private WebApplicationException notFound(String entity, Object id) {
    return new WebApplicationException(
        Response.status(404)
            .entity(Map.of("error", entity + " not found", 
                          "message", entity + " with ID " + id + " does not exist"))
            .build());
}
```

### Passo 3: Melhorar ContentEngagementRepository (1h)

O repository já existe, mas pode ser estendido com métodos que hoje estão inline no Service:

```java
// Já existe: findActiveEngagement(), findByUserId(), findByContentId()
// Adicionar:
public boolean existsByUserAndContent(UUID userId, UUID contentId) { ... }
public long countActiveByUser(UUID userId) { ... }
```

---

## 🧪 Estratégia de Testes

### Testes Unitários (com mocks)

| Método | Cenário | Mocks |
|--------|---------|-------|
| createEngagement() | Sucesso com todos os campos | engagementRepository, userRepository, contentRepository |
| createEngagement() | Usuário não existe → 404 | userRepository |
| createEngagement() | Conteúdo não existe → 404 | userRepository, contentRepository |
| createEngagement() | Engagement reversível duplicado → 409 | engagementRepository |
| updateEngagement() | Sucesso | engagementRepository |
| updateEngagement() | Engagement não encontrado → 404 | engagementRepository |
| deleteEngagement() | Soft delete → REMOVED | engagementRepository |
| getEngagement() | Sucesso | engagementRepository |
| getUserTopContents() | Enriquecimento com dados de conteúdo | engagementRepository, contentRepository |

### Testes de Integração

- Fluxo completo: criar engajamento → buscar → atualizar → deletar
- Stats de conteúdo
- Stats de usuário
- Top contents

### Testes de Regressão

- **Cenário:** Criar LIKE → buscar → 200
- **Cenário:** Criar LIKE duplicado → 409
- **Cenário:** Atualizar status de engajamento
- **Cenário:** Soft delete → REMOVED
- **Cenário:** Stats aggregation

---

## 📊 Impacto na Pipeline

**Nenhum.** A refatoração é cirúrgica e não altera contratos de API.

---

## ✅ Checklist de Implementação

- [ ] Criar `userRepository.existsById()` (ou similar)
- [ ] Criar `contentRepository.existsById()` (ou similar)
- [ ] Substituir `UserModel.findById()` por chamadas de repository
- [ ] Simplificar WebApplicationException com helper
- [ ] Estender `ContentEngagementRepository` com métodos de verificação
- [ ] Escrever testes unitários para ContentEngagementService
- [ ] Rodar `./mvnw verify` completo
