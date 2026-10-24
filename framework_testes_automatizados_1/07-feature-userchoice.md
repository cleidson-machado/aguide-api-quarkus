# 🎯 Feature: UserChoice — Plano de Ação

## Análise de Testabilidade

### Estrutura Atual
```
userchoice/
├── UserChoiceController.java  # REST endpoints (boa delegação)
├── UserChoiceService.java     # Lógica de negócio + validações
├── UserChoiceRepository.java  # Data access (JÁ EXISTE!)
├── UserChoiceModel.java       # JPA Entity
├── dto/
│   ├── CreateUserChoiceRequest.java
│   ├── UpdateUserChoiceRequest.java
│   └── UserChoiceResponse.java
└── enuns/ (16 enums!)
```

### Más Práticas Identificadas

#### 🟡 1. Lógica de validação de enums duplicada
**Severidade:** Média | **Impacto:** Manutenibilidade

```java
// UserChoiceService.create() - L112
if (userChoice.profileType == UserProfileType.CREATOR) {
    if (userChoice.contentFormats != null && !userChoice.contentFormats.isEmpty()) {
        if (!ContentFormat.isValidStringList(userChoice.contentFormats)) { ... }
    }
}

// UserChoiceService.update() - L225 (EXATAMENTE A MESMA LÓGICA DUPLICADA)
if (existing.profileType == UserProfileType.CREATOR) {
    if (existing.contentFormats != null && !existing.contentFormats.isEmpty()) {
        if (!ContentFormat.isValidStringList(existing.contentFormats)) { ... }
    }
}
```

**Solução:** Extrair método privado `validateContentFormats()` e `validateInfoSources()`.

#### 🟡 2. Conversão manual DTO→Model duplicada
**Severidade:** Baixa | **Impacto:** Manutenibilidade

```java
// UserChoiceController.java
private UserChoiceModel toModel(CreateUserChoiceRequest request) { ... }     // 30+ linhas
private UserChoiceModel toModelFromUpdate(UpdateUserChoiceRequest request) { ... } // 30+ linhas (quase idêntico)
private UserChoiceResponse toResponse(UserChoiceModel model) { ... }          // 40+ linhas
```

3 métodos de conversão manual. Poderiam usar MapStruct ou similar, mas para o escopo pragmático, apenas unificar os dois toModel.

#### ✅ 3. Boas práticas
- Controller bem delegado ao Service
- Repository já existe e é injetado
- Validação via `isValid()` no Model
- Soft delete implementado

---

## 🔧 Plano de Refatoração

### Passo 1: Extrair métodos de validação (1h)

```java
private void validateContentFormats(List<String> contentFormats, UserProfileType profileType) {
    if (profileType == UserProfileType.CREATOR 
        && contentFormats != null && !contentFormats.isEmpty()
        && !ContentFormat.isValidStringList(contentFormats)) {
        throw new WebApplicationException("Invalid content formats", 400);
    }
}

private void validateInfoSources(List<String> infoSources, UserProfileType profileType) {
    // similar
}
```

### Passo 2: Unificar toModel (30min)

```java
private UserChoiceModel toModel(Object request, UserChoiceModel model) {
    if (request instanceof CreateUserChoiceRequest) {
        model.userId = ((CreateUserChoiceRequest) request).getUserId();
    }
    // campos comuns
    model.profileType = getProfileType(request);
    model.nicheContext = getNicheContext(request);
    // ... etc
}
```

---

## 🧪 Estratégia de Testes

### Testes Unitários (com mocks)

| Método | Cenário | Mocks |
|--------|---------|-------|
| create() | Sucesso CREATOR com todos os campos | userChoiceRepository |
| create() | Sucesso CONSUMER com todos os campos | userChoiceRepository |
| create() | User já tem escolha → 409 | userChoiceRepository |
| create() | CREATOR com contentFormats inválidos → 400 | — |
| create() | CONSUMER com infoSources inválidos → 400 | — |
| update() | Sucesso | userChoiceRepository |
| update() | Escolha deletada → 410 | userChoiceRepository |
| update() | Escolha não encontrada → 404 | userChoiceRepository |
| findByUserId() | Encontrada | userChoiceRepository |
| findByUserId() | Não encontrada → 404 | userChoiceRepository |
| softDelete() | Sucesso | userChoiceRepository |
| softDelete() | Já deletada → 410 | userChoiceRepository |

### Testes de Integração

- Fluxo completo: criar → buscar → atualizar → deletar
- Filtros por profileType e nicheContext
- Endpoints específicos (creators/monetized, consumers/visa)

### Testes de Regressão

- **Cenário:** Criar escolha CREATOR → 201
- **Cenário:** Criar escolha duplicada → 409
- **Cenário:** Criar com campos inválidos → 400
- **Cenário:** Atualizar escolha → 200
- **Cenário:** Soft delete → 204
- **Cenário:** Listar com filtros → 200
- **Cenário:** Listar criadores monetizados → 200

---

## 📊 Impacto na Pipeline

**Nenhum.** Refatoração puramente estrutural e de validação.

---

## ✅ Checklist de Implementação

- [ ] Extrair `validateContentFormats()` e `validateInfoSources()` no UserChoiceService
- [ ] Remover duplicação de validação em create()/update()
- [ ] Unificar métodos de conversão DTO→Model (opcional, baixo impacto)
- [ ] Escrever testes unitários para UserChoiceService
- [ ] Rodar `./mvnw verify` completo
