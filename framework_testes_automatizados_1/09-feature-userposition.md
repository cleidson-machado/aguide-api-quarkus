# 🏆 Feature: UserPosition (Ranking) — Plano de Ação

## Análise de Testabilidade

### Estrutura Atual
```
userposition/
├── UserRankingController.java       # REST endpoints
├── UserRankingService.java          # Orquestração (bem modularizada)
├── UserRankingRepository.java       # ✅ Data access (já existe)
├── UserRankingAuditRepository.java  # ✅ Data access (já existe)
├── UserRankingModel.java
├── UserRankingAuditModel.java
├── services/
│   ├── UserRankingMetricsService.java    # Cálculos de engagement/conversion
│   ├── UserRankingMilestoneService.java  # Detecção de milestones
│   └── UserRankingValidationService.java # Validações de entrada
├── dto/ (5 DTOs)
└── enuns/ (4 enums)
```

### Más Práticas Identificadas

#### 🟡 1. Chamada estática para UserModel.findByIdActive não extraída
**Severidade:** Média | **Impacto:** Testabilidade

Esta é a feature **mais modularizada** do projeto, com services especializados (SRP), mas ainda tem dependência de chamada estática em `UserRankingValidationService` ou similar. Verificar.

#### ✅ 2. Excelente: Arquitetura SOLID aplicada
- Responsabilidades extraídas em 3 services especializados
- Repository pattern já implementado
- Tratamento de auditoria
- Pessimistic lock para concorrência
- Idempotência via requestId
- Score cap implementado

#### ✅ 3. Constructor injection em todos os services
**Severidade:** N/A | **Impacto:** Positivo

---

## 🔧 Plano de Refatoração

### Passo 1: Extrair dependência de UserModel (1h)

Se `UserRankingService` ou `UserRankingValidationService` usam `UserModel.findById()` ou similar, extrair para UserRepository.

### Passo 2: Melhorar cobertura de testes (2h)

A feature já tem `UserRankingServiceTest` existente. Adicionar testes para:
- Services especializados (MetricsService, MilestoneService, ValidationService)
- Cenários de concorrência (pessimistic lock)
- Idempotência via requestId
- Score cap

---

## 🧪 Estratégia de Testes

### Testes Unitários (com mocks)

| Método | Cenário | Mocks |
|--------|---------|-------|
| UserRankingService.create() | Sucesso | userRankingRepo, metricsService |
| UserRankingService.create() | Já existe → 409 | userRankingRepo |
| UserRankingService.addPoints() | Sucesso com auditoria | userRankingRepo, auditRepo, metricsService |
| UserRankingService.addPoints() | RequestId duplicado → idempotente | auditRepo |
| UserRankingService.addPoints() | Pontos excedem máximo → 400 | userRankingRepo |
| UserRankingService.addPoints() | Score cap ativado (milestone) | userRankingRepo, metricsService |
| UserRankingService.update() | Sucesso com recálculo de métricas | userRankingRepo, validationService, metricsService, milestoneService |
| UserRankingService.update() | Milestone de perfil detectado | userRankingRepo, milestoneService |
| UserRankingValidationService.validateNonNegative() | Valor negativo → exception | — |
| UserRankingValidationService.validateProfileCompletionPercentage() | >100 → exception | — |
| UserRankingMetricsService.calculateEngagementLevel() | Cálculo correto | — |
| UserRankingMilestoneService.checkContentViewsMilestones() | Milestone atingido | auditRepo |

### Testes de Integração

| Teste | Status |
|-------|--------|
| UserRankingServiceTest | ✅ Já existe |

### Testes de Regressão

- **Cenário:** Criar ranking → 201
- **Cenário:** Criar ranking duplicado → 409
- **Cenário:** Adicionar pontos → score atualizado
- **Cenário:** Buscar top 10 → lista ordenada
- **Cenário:** Atualizar métricas → engagement level recalculado
- **Cenário:** Soft delete → 204
- **Cenário:** Histórico de pontos → auditoria correta

---

## 📊 Impacto na Pipeline

**Nenhum.** Feature já bem estruturada.

---

## ✅ Checklist de Implementação

- [ ] Verificar dependências estáticas de UserModel
- [ ] Extrair para UserRepository se aplicável
- [ ] Escrever testes unitários para MetricsService
- [ ] Escrever testes unitários para MilestoneService
- [ ] Escrever testes unitários para ValidationService
- [ ] Adicionar testes de concorrência (pessimistic lock)
- [ ] Adicionar teste de idempotência (requestId)
- [ ] Rodar `./mvnw verify` completo
