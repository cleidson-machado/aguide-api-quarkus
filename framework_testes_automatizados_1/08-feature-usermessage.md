# 💬 Feature: UserMessage — Plano de Ação

## Análise de Testabilidade

### Estrutura Atual
```
usermessage/
├── ConversationController.java  # REST endpoints (boa delegação)
├── ConversationService.java     # Lógica de negócio (injeta repositories)
├── MessageController.java       # REST endpoints (boa delegação)
├── MessageService.java          # Lógica de negócio (injeta repositories)
├── UserBlockController.java     # REST endpoints (boa delegação)
├── UserBlockService.java        # Lógica de negócio (injeta repositories)
├── ConversationModel.java
├── ConversationParticipantModel.java
├── ConversationRepository.java  # ✅ Já existe
├── ConversationParticipantRepository.java # ✅ Já existe
├── UserMessageModel.java
├── UserMessageRepository.java   # ✅ Já existe
├── UserBlockModel.java
├── UserBlockRepository.java     # ✅ Já existe
├── ConversationType.java
├── MessageType.java
└── dto/ (10 DTOs)
```

### Más Práticas Identificadas

#### 🟡 1. Chamadas estáticas Panache no ConversationService
**Severidade:** Média | **Impacto:** Testabilidade

```java
// ConversationService.java
UserModel user1 = UserModel.findByIdActive(user1Id);  // L117
UserModel user2 = UserModel.findByIdActive(user2Id);  // L118
UserModel creator = UserModel.findByIdActive(creatorId);  // L173
UserModel user = UserModel.findByIdActive(userId);  // L240
```

Apesar de ter repositories injetados para Conversation, Participant, Message e Block, ainda há chamadas estáticas para `UserModel.findByIdActive()`.

#### 🟡 2. MessageController tem endpoint não implementado
**Severidade:** Baixa | **Impacto:** UX

```java
// MessageController.java
@GET @Path("/{messageId}")
public Response getMessageById(...) {
    throw new jakarta.ws.rs.NotSupportedException("Endpoint not yet implemented");
}
```

#### ✅ 3. Excelente arquitetura de testes
**Severidade:** N/A | **Impacto:** Referência

```java
// ConversationServiceTest.java - Modelo de teste unitário
@BeforeEach
void setUp() {
    conversationRepository = Mockito.mock(ConversationRepository.class);
    participantRepository = Mockito.mock(ConversationParticipantRepository.class);
    messageRepository = Mockito.mock(UserMessageRepository.class);
    blockRepository = Mockito.mock(UserBlockRepository.class);
    Logger log = Mockito.mock(Logger.class);
    service = new ConversationService(..., log);
}
```

**Esta é a abordagem correta:** Mockito puro, sem @QuarkusTest, constructor injection.

#### ✅ 4. Constructor injection em todos os Services
**Severidade:** N/A | **Impacto:** Positivo

Todas as classes de usermessage usam constructor injection. Referência para o resto do projeto.

---

## 🔧 Plano de Refatoração

### Passo 1: Extrair UserRepository para chamadas estáticas (1h)

```java
// ConversationService.java - Substituir:
UserModel user1 = UserModel.findByIdActive(user1Id);
UserModel user2 = UserModel.findByIdActive(user2Id);
// Por:
UserModel user1 = userRepository.findByIdActive(user1Id);
UserModel user2 = userRepository.findByIdActive(user2Id);
```

### Passo 2: Implementar endpoint faltante (1h)

```java
@GET @Path("/{messageId}")
public Response getMessageById(
        @PathParam("messageId") UUID messageId,
        @HeaderParam("Authorization") String authHeader) {
    
    UUID userId = SecurityUtils.extractUserIdFromToken(authHeader);
    // Buscar mensagem + validar permissão
    UserMessageModel message = messageService.getMessageById(messageId, userId);
    return Response.ok(new MessageResponse(message)).build();
}
```

---

## 🧪 Estratégia de Testes

### Testes Unitários (Já existem como referência)

| Teste | Status | Tipo |
|-------|--------|------|
| ConversationServiceTest | ✅ Completo | Mockito puro |
| MessageServiceTest | ✅ Completo | Mockito puro |
| UserBlockServiceTest | ✅ Completo | Mockito puro |

### Novos Testes Unitários a Adicionar

| Método | Cenário | Mocks |
|--------|---------|-------|
| ConversationService.createDirectConversation() | Sucesso (primeira vez) | conversationRepo, participantRepo, blockRepo, userRepo |
| ConversationService.createDirectConversation() | Conversa já existe → retorna existente | conversationRepo, blockRepo |
| ConversationService.createDirectConversation() | Usuário não encontrado → 404 | blockRepo, userRepo |
| ConversationService.createGroupConversation() | Sucesso com participantes | conversationRepo, participantRepo, userRepo |
| ConversationService.addParticipant() | Admin adiciona → sucesso | conversationRepo, participantRepo, userRepo |
| ConversationService.removeParticipant() | Usuário sai → sucesso | conversationRepo, participantRepo |
| MessageService.sendMessage() | Sucesso | messageRepo, conversationRepo, participantRepo, blockRepo |
| MessageService.sendMessage() | Bloqueio detectado → 409 | messageRepo, conversationRepo, participantRepo, blockRepo |
| MessageService.editMessage() | Sucesso (autor da mensagem) | messageRepo |
| MessageService.editMessage() | Outro usuário → 403 | messageRepo |
| MessageService.markAsRead() | Sucesso (não é o sender) | messageRepo, participantRepo |
| UserBlockService.blockUser() | Sucesso | blockRepo, userRepo |
| UserBlockService.blockUser() | Já bloqueado → 409 | blockRepo |
| UserBlockService.unblockUser() | Sucesso | blockRepo |
| UserBlockService.unblockUser() | Bloqueio não existe → 404 | blockRepo |

### Testes de Regressão

- **Cenário:** Criar conversa direta → 201
- **Cenário:** Criar conversa duplicada → 200 (idempotente)
- **Cenário:** Enviar mensagem → 201
- **Cenário:** Marcar como lida → 204
- **Cenário:** Editar mensagem → 200
- **Cenário:** Deletar mensagem → 204 (soft delete)
- **Cenário:** Bloquear usuário → 200
- **Cenário:** Desbloquear usuário → 204
- **Cenário:** Conversa bloqueada → 409 ao enviar mensagem

---

## 📊 Impacto na Pipeline

**Nenhum.** Esta feature já segue as melhores práticas do projeto.

---

## ✅ Checklist de Implementação

- [ ] Extrair `userRepository` para substituir chamadas estáticas
- [ ] Implementar endpoint GET /messages/{messageId}
- [ ] Escrever testes unitários adicionais para MessageService
- [ ] Escrever testes unitários para ConversationService.createDirectConversation() com userRepo mockado
- [ ] Escrever testes unitários para UserBlockService
- [ ] Rodar `./mvnw verify` completo
