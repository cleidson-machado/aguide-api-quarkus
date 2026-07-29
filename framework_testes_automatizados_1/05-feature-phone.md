# 📞 Feature: Phone — Plano de Ação

## Análise de Testabilidade

### Estrutura Atual
```
phone/
├── PhoneNumberController.java   # REST endpoints (FIELD INJECTION!)
├── PhoneNumberService.java      # Lógica de negócio + validação (FIELD INJECTION!)
├── PhoneNumberRepository.java   # Data access (já existe!)
├── PhoneNumberModel.java        # JPA Entity
└── dto/
    ├── PhoneNumberRequest.java
    └── PhoneNumberResponse.java
```

### Más Práticas Identificadas

#### 🔴 1. Field Injection no Controller e Service (CRÍTICO)
**Severidade:** Alta | **Impacto:** Impossível testar com mocks puros

```java
// PhoneNumberController.java
@Inject PhoneNumberService phoneService;   // ← Field injection!
@Inject Logger log;                         // ← Field injection!

// PhoneNumberService.java
@Inject PhoneNumberRepository phoneRepository;  // ← Field injection!
@Inject Logger log;                              // ← Field injection!
```

**Consequência:** Não é possível instanciar o Service em testes unitários sem framework CDI. Viola java:S6813.

#### 🟡 2. Padrão inconsistente com o resto do projeto
**Severidade:** Média | **Impacto:** Manutenibilidade

Todas as outras classes do projeto usam **constructor injection**. Phone é a única feature que usa field injection.

#### ✅ 3. Boa prática: Repository bem estruturado
**Severidade:** N/A | **Impacto:** Positivo

`PhoneNumberRepository` já existe e tem métodos especializados (findByUser, findPrimaryByUser, etc.). É um exemplo de boa separação.

---

## 🔧 Plano de Refatoração

### Passo 1: Corrigir Field Injection no PhoneNumberService (1h)

```java
// ANTES:
@ApplicationScoped
public class PhoneNumberService {
    @Inject PhoneNumberRepository phoneRepository;
    @Inject Logger log;
    // ...
}

// DEPOIS:
@ApplicationScoped
public class PhoneNumberService {
    private final PhoneNumberRepository phoneRepository;
    private final Logger log;
    
    public PhoneNumberService(PhoneNumberRepository phoneRepository, Logger log) {
        this.phoneRepository = phoneRepository;
        this.log = log;
    }
    // ...
}
```

### Passo 2: Corrigir Field Injection no PhoneNumberController (30min)

```java
// ANTES:
public class PhoneNumberController {
    @Inject PhoneNumberService phoneService;
    @Inject Logger log;

// DEPOIS:
public class PhoneNumberController {
    private final PhoneNumberService phoneService;
    private final Logger log;
    
    public PhoneNumberController(PhoneNumberService phoneService, Logger log) {
        this.phoneService = phoneService;
        this.log = log;
    }
```

### Passo 3: Extrair UserRepository para validação (1h)

```java
// PhoneNumberService.java - Substituir:
UserModel user = UserModel.findById(userId);
// Por:
if (!userRepository.existsById(userId)) { throw new NotFoundException("Usuário não encontrado"); }
```

---

## 🧪 Estratégia de Testes

### Testes Unitários (com mocks)

| Método | Cenário | Mocks |
|--------|---------|-------|
| create() | Sucesso: primeiro telefone → primary | phoneRepository, userRepository |
| create() | Usuário não existe → 404 | userRepository |
| create() | Número duplicado → 400 | phoneRepository, userRepository |
| create() | Número inválido Brasil → 400 | — (validação direta) |
| create() | Número inválido Portugal → 400 | — (validação direta) |
| update() | Sucesso com mudança de número | phoneRepository |
| update() | Número não encontrado → 404 | phoneRepository |
| setPrimary() | Sucesso | phoneRepository |
| setPrimary() | Telefone não pertence ao usuário → 400 | phoneRepository |
| delete() | Soft delete com promoção de primary | phoneRepository |
| validatePhoneNumber() | Brasil mobile válido → true | — |
| validatePhoneNumber() | Brasil fixo válido → true | — |
| validatePhoneNumber() | Portugal mobile válido → true | — |
| validatePhoneNumber() | Inválido → false | — |

### Testes de Integração

| Teste | Status |
|-------|--------|
| PhoneNumberServiceIntegrationTest | ✅ Já existe |

### Testes de Regressão

- **Cenário:** Adicionar telefone → 201
- **Cenário:** Adicionar telefone duplicado → 400
- **Cenário:** Marcar como principal → 204
- **Cenário:** Deletar telefone → 204
- **Cenário:** Listar telefones do usuário → 200
- **Cenário:** Validar formato Brasil (+55 67 9xxxx-xxxx)
- **Cenário:** Validar formato Portugal (+351 9x xxx-xxxx)
- **Cenário:** Validar formato genérico E.164

---

## 📊 Impacto na Pipeline

**Nenhum.** A correção de field injection para constructor injection não altera comportamento em runtime. Testes existentes continuam passando.

---

## ✅ Checklist de Implementação

- [ ] Converter `PhoneNumberService` de field para constructor injection
- [ ] Converter `PhoneNumberController` de field para constructor injection
- [ ] Extrair verificação de existência de usuário para UserRepository
- [ ] Escrever testes unitários para validação de formato (PhoneNumberValidationTest)
- [ ] Escrever testes unitários para PhoneNumberService com mocks
- [ ] Rodar `./mvnw verify` completo
