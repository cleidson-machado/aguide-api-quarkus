# ✅ Feature: Ownership — Plano de Ação

## Análise de Testabilidade

### Estrutura Atual
```
ownership/
├── ContentOwnershipController.java  # REST endpoints (bem delegado ao Service)
├── ContentOwnershipService.java     # Lógica de negócio + HMAC
├── ContentOwnershipModel.java       # JPA Entity
├── OwnershipStatus.java             # Enum
└── dto/
    ├── OwnershipStatusResponse.java
    ├── UserContentResponse.java
    ├── ValidateOwnershipRequest.java
    └── ValidateOwnershipResponse.java
```

### Más Práticas Identificadas

#### 🟡 1. Chamadas Panache estáticas no Service
**Severidade:** Média | **Impacto:** Testabilidade

```java
// ContentOwnershipService.java
UserModel user = UserModel.findById(request.getUserId());              // L99
ContentRecordModel content = ContentRecordModel.findById(request.getContentId());  // L108
ContentOwnershipModel ownership = ContentOwnershipModel.findByUserAndContent(...);  // L115
ContentRecordModel content = ContentRecordModel.findById(ownership.contentId);     // L259 (getUserVerifiedContent)
```

- 4 pontos de acoplamento estático
- `ContentOwnershipModel.findByUserAndContent()` é método estático da entidade

#### 🟡 2. Lógica de negócio com dependência de ConfigProperty
**Severidade:** Baixa | **Impacto:** Teste de integração com perfil específico

```java
@ConfigProperty(name = "ownership.validation.secret") String ownershipSecretKey
```

- Precisa de configuração específica em testes
- HMAC é testável unitariamente com valor fixo da secret

#### ✅ 3. Boa prática: Controller bem delegado
**Severidade:** N/A | **Impacto:** Positivo

Controller chama Service para todas as operações. Exception handling padronizado (deixa propagar).

---

## 🔧 Plano de Refatoração

### Passo 1: Extrair ContentOwnershipRepository (1h)

```java
// NOVO: br.com/aguideptbr/features/ownership/ContentOwnershipRepository.java
@ApplicationScoped
public class ContentOwnershipRepository implements PanacheRepositoryBase<ContentOwnershipModel, UUID> {
    public ContentOwnershipModel findByUserAndContent(UUID userId, UUID contentId) { ... }
    public List<ContentOwnershipModel> findVerifiedByUserId(UUID userId) { ... }
    public List<ContentOwnershipModel> findPending() { ... }
}
```

### Passo 2: Substituir chamadas estáticas no Service (1h)

Injetar `ContentOwnershipRepository`, `UserRepository` e `ContentRecordRepository`.

### Passo 3: Testar HMAC com valor fixo (30min)

```java
@Test
void testCalculateHMAC_ShouldReturnConsistentHash() {
    // Usando ReflectionTestUtils ou constructor overloading para injetar secret fixa
    ContentOwnershipService service = new ContentOwnershipService(log, "test-secret-key-12345");
    String hash1 = service.calculateHMAC(userId, contentId, "yt-channel", "yt-channel");
    String hash2 = service.calculateHMAC(userId, contentId, "yt-channel", "yt-channel");
    assertEquals(hash1, hash2, "HMAC deve ser consistente");
}
```

---

## 🧪 Estratégia de Testes

### Testes Unitários (com mocks)

| Método | Cenário | Mocks |
|--------|---------|-------|
| validateOwnership() | Sucesso: channel match → VERIFIED | userRepo, contentRepo, ownershipRepo |
| validateOwnership() | Usuário sem YouTube channel → REJECTED | userRepo, contentRepo |
| validateOwnership() | Channel mismatch → REJECTED | userRepo, contentRepo |
| validateOwnership() | Usuário não encontrado → 404 | userRepo |
| validateOwnership() | Conteúdo não encontrado → 404 | userRepo, contentRepo |
| validateOwnership() | Retry tracking incrementa retryCount | userRepo, contentRepo, ownershipRepo |
| cancelOwnershipClaim() | Cancelamento bem-sucedido | ownershipRepo |
| cancelOwnershipClaim() | Ownership não encontrada → 404 | ownershipRepo |
| getOwnershipStatus() | Status retornado corretamente | ownershipRepo |
| getUserVerifiedContent() | Lista de conteúdos verificados | userRepo, ownershipRepo, contentRepo |
| calculateHMAC() | Hash consistente para mesmos inputs | — (teste direto) |

### Testes de Integração

| Teste | Status |
|-------|--------|
| ContentOwnershipServiceTest | ✅ Já existe (integração) |
| Fluxo: validate → status → cancel | ✅ |

### Testes de Regressão

- **Cenário:** Validar ownership com canais correspondentes → VERIFIED
- **Cenário:** Validar ownership com canais diferentes → REJECTED
- **Cenário:** Validar ownership sem YouTube channel → REJECTED
- **Cenário:** Cancelar ownership → USER_CANCELLED
- **Cenário:** Idempotência: mesma requisição 2x atualiza retryCount

---

## 📊 Impacto na Pipeline

**Nenhum.** A refatoração não altera APIs. A adição de `ContentOwnershipRepository` é transparente.

---

## ✅ Checklist de Implementação

- [ ] Criar `ContentOwnershipRepository`
- [ ] Injetar repository em `ContentOwnershipService`
- [ ] Substituir chamadas estáticas
- [ ] Escrever testes unitários com mocks
- [ ] Verificar HMAC tests com secret fixa
- [ ] Rodar `./mvnw verify` completo
