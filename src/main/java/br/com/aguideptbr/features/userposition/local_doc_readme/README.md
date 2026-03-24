# User Ranking & Conversion Potential - Feature Documentation

## 📋 Visão Geral

Esta feature implementa um sistema de **classificação de usuários por potencial de conversão**, permitindo identificar quais usuários têm maior probabilidade de pagar por serviços (consultoria, premium, contatos).

**Localização:** `src/main/java/br/com/aguideptbr/features/userposition/`

**Componentes:**
- `UserRankingModel.java` - Entidade principal de ranking/pontuação
- `EngagementLevel.java` - Enum de níveis de engajamento
- `ConversionPotential.java` - Enum de potencial de conversão

---

## 🎯 Objetivo

Classificar usuários por **POTENCIAL DE CONVERSÃO** (likelihood de pagar por serviços), baseado em:

1. **Engajamento com conteúdo** (40%) - Visualizações, tempo de uso, frequência
2. **Interação social** (30%) - Mensagens enviadas, conversas iniciadas
3. **Completude do perfil** (20%) - Telefones cadastrados, apps de mensagem
4. **Recência e frequência de uso** (10%) - Última atividade, dias consecutivos

---

## 📊 UserRankingModel - Entidade Principal

### Relacionamentos

#### Relacionamento com Usuário (NÃO IMPLEMENTADO)
```java
// FUTURO: Transformar em @ManyToOne UserModel
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false, unique = true)
public UserModel user;
```

**Por enquanto:** Campo manual `userId` (UUID)

**IMPORTANTE:** Adicionar constraint UNIQUE se for 1-1 (1 ranking por usuário).

### Score e Classificação

#### `totalScore` (Integer, 0-100)
Score TOTAL do usuário calculado com base em múltiplos fatores.

**Fórmula sugerida:**
- Engajamento com conteúdo: 40% (`contentEngagementScore`)
- Interação social: 30% (`socialEngagementScore`)
- Completude de perfil: 20% (`profileCompletenessScore`)
- Recência de uso: 10% (`recencyScore`)

#### `engagementLevel` (EngagementLevel)
Nível de engajamento do usuário derivado do `totalScore`:
- **LOW**: 0-25
- **MEDIUM**: 26-50
- **HIGH**: 51-75
- **VERY_HIGH**: 76-100

#### `conversionPotential` (ConversionPotential)
Potencial de conversão baseado em análise preditiva ou regras de negócio.

**Regras sugeridas:**
- **VERY_HIGH**: `totalScore >= 80 AND hasPhones = true AND messagesSent >= 10`
- **HIGH**: `totalScore >= 60 AND (hasPhones = true OR messagesSent >= 5)`
- **MEDIUM**: `totalScore >= 40`
- **LOW**: `totalScore >= 20`
- **VERY_LOW**: `totalScore < 20`

### Engajamento com Conteúdo

#### `totalContentViews` (Long)
Total de visualizações de conteúdo (ContentRecordModel).

**Cálculo futuro:**
```sql
SELECT COUNT(*) FROM user_content_views WHERE user_id = ?
```

**Relacionamento sugerido:** Criar tabela `user_content_views` (user_id, content_id, viewed_at)

#### `uniqueContentViews` (Long)
Total de conteúdos únicos visualizados.

Diferencia:
- Usuário que vê 100x o mesmo vídeo
- Usuário que vê 100 vídeos diferentes

**Cálculo futuro:**
```sql
SELECT COUNT(DISTINCT content_id) FROM user_content_views WHERE user_id = ?
```

#### `avgDailyUsageMinutes` (Integer)
Tempo médio diário de uso do app (em minutos).

**Cálculo sugerido:**
1. Registrar eventos de "app opened" e "app closed" (tabela `user_sessions`)
2. Calcular média dos últimos 30 dias
3. Atualizar este campo via batch job noturno

**Relacionamento sugerido:** Criar `UserSessionModel` (user_id, session_start, session_end, duration_minutes)

#### `consecutiveDaysStreak` (Integer)
Total de dias consecutivos de uso (streak atual).

Usuário que acessa diariamente tem maior engajamento.

**Cálculo:** Contar dias consecutivos até hoje com pelo menos 1 atividade.

#### `totalActiveDays` (Long)
Total de dias com pelo menos 1 acesso (lifetime).

Diferencia usuário antigo com baixa frequência vs novo com alta frequência.

### Interação Social

#### `totalMessagesSent` (Long)
Total de mensagens enviadas pelo usuário (UserMessageModel).

**Cálculo futuro:**
```sql
SELECT COUNT(*) FROM app_user_message
WHERE sender_id = ? AND deleted_at IS NULL
```

**Relacionamento sugerido:** Já existe `UserMessageModel.sender` (FK para UserModel)

#### `totalConversationsStarted` (Long)
Total de conversas iniciadas pelo usuário (ConversationModel onde user é criador).

Proatividade em iniciar conversas indica maior engajamento.

**Cálculo futuro:**
```sql
SELECT COUNT(*) FROM app_conversation_participant
WHERE user_id = ? AND is_creator = true AND deleted_at IS NULL
```

**Relacionamento sugerido:** Já existe `ConversationParticipantModel.isCreator`

#### `uniqueContactsMessaged` (Long)
Total de contatos únicos com quem o usuário trocou mensagens.

Maior rede = maior potencial de conversão.

**Cálculo futuro:** Query complexa cruzando conversas e participantes.

#### `activeConversations` (Integer)
Total de conversas ativas (onde usuário ainda é participante).

**Cálculo futuro:**
```sql
SELECT COUNT(*) FROM app_conversation_participant
WHERE user_id = ? AND left_at IS NULL AND deleted_at IS NULL
```

### Completude do Perfil

#### `hasPhones` (Boolean)
Se o usuário tem pelo menos 1 telefone cadastrado (PhoneNumberModel).

Usuário com telefone = maior probabilidade de conversão.

**Cálculo futuro:**
```sql
SELECT COUNT(*) > 0 FROM phone_numbers
WHERE user_id = ? AND deleted_at IS NULL
```

**Relacionamento sugerido:** Já existe `PhoneNumberModel.user` (FK para UserModel)

#### `totalPhones` (Integer)
Total de telefones cadastrados.

Mais telefones = usuário mais engajado/confiável.

**Cálculo futuro:**
```sql
SELECT COUNT(*) FROM phone_numbers
WHERE user_id = ? AND deleted_at IS NULL
```

#### `hasWhatsapp` (Boolean)
Se o usuário tem WhatsApp cadastrado.

WhatsApp = canal direto de conversão.

**Cálculo futuro:**
```sql
SELECT COUNT(*) > 0 FROM phone_numbers
WHERE user_id = ? AND has_whatsapp = true AND deleted_at IS NULL
```

#### `hasTelegram` (Boolean)
Se o usuário tem Telegram cadastrado.

**Cálculo futuro:**
```sql
SELECT COUNT(*) > 0 FROM phone_numbers
WHERE user_id = ? AND has_telegram = true AND deleted_at IS NULL
```

### Recência e Atividade

#### `lastActivityAt` (LocalDateTime)
Data e hora da última atividade do usuário no app.

Usuário recente = maior potencial de conversão.

**Cálculo:** `MAX(last_login, last_content_view, last_message_sent)`

#### `lastContentViewAt` (LocalDateTime)
Data e hora do último conteúdo visualizado.

**Cálculo futuro:**
```sql
SELECT MAX(viewed_at) FROM user_content_views WHERE user_id = ?
```

#### `lastMessageSentAt` (LocalDateTime)
Data e hora da última mensagem enviada.

**Cálculo futuro:**
```sql
SELECT MAX(sent_at) FROM app_user_message
WHERE sender_id = ? AND deleted_at IS NULL
```

#### `lastLoginAt` (LocalDateTime)
Data e hora do último login.

**Futuro:** Registrar em `UserSessionModel` ou atualizar em `UserModel` ao fazer login.

### Preferências e Comportamento

#### `favoriteCategory` (String)
Categoria de conteúdo favorita do usuário.

Derivada de análise de histórico de visualizações.

**Cálculo futuro:** Calcular via `GROUP BY categoryId` em `user_content_views`.

#### `favoriteContentType` (String)
Tipo de conteúdo preferido (VIDEO, ARTICLE, PODCAST, etc).

**Cálculo futuro:** Calcular via `GROUP BY type` em `user_content_views`.

#### `preferredUsageTime` (String)
Horário preferido de uso (MORNING, AFTERNOON, EVENING, NIGHT).

Útil para timing de campanhas/notificações.

**Cálculo:** Agrupar sessions por hora do dia, identificar pico.

### Auditoria e Metadados

#### `scoreUpdatedAt` (LocalDateTime)
Data e hora da última atualização do score.

Útil para saber se o score está atualizado.

**Sugestão:** Rodar batch job diário/semanal para recalcular scores.

#### `createdAt` (LocalDateTime)
Data de criação do registro de ranking.

#### `updatedAt` (LocalDateTime)
Data da última atualização do registro.

#### `deletedAt` (LocalDateTime)
Data de exclusão lógica (soft delete).

Null significa que o ranking está ativo.

**Nota:** Raramente usado, pois ranking está vinculado ao usuário. Se usuário for deletado, ranking também deve ser.

---

## 🔢 EngagementLevel - Níveis de Engajamento

Enum representando o nível de engajamento de um usuário, derivado do `totalScore` em `UserRankingModel`.

### Valores

| Nível | Range | Descrição | Ação Recomendada |
|-------|-------|-----------|------------------|
| **LOW** | 0-25 | Engajamento muito baixo. Usuário pouco ativo, em risco de churn. | Campanhas de reengajamento |
| **MEDIUM** | 26-50 | Engajamento médio. Usuário ocasional, pode ser estimulado. | Notificações de conteúdo relevante |
| **HIGH** | 51-75 | Engajamento alto. Usuário frequente, bom candidato para conversão. | Ofertas personalizadas |
| **VERY_HIGH** | 76-100 | Engajamento muito alto. Usuário super ativo, prioridade para campanhas premium. | Contato direto, ofertas VIP |

### Casos de Uso

- **Segmentação de campanhas:** Enviar promoções para HIGH e VERY_HIGH
- **Dashboard admin:** Filtrar usuários por nível de engajamento
- **Análise de churn:** Usuários LOW podem estar em risco
- **Gamificação:** Recompensar usuários VERY_HIGH com badges/pontos

---

## 💰 ConversionPotential - Potencial de Conversão

Enum representando o potencial de conversão de um usuário.

**CONVERSÃO** = Likelihood de pagar por serviços (consultoria, premium, contatos).

### Critérios de Classificação

| Potencial | Critérios | Foco estratégico |
|-----------|-----------|------------------|
| **VERY_LOW** | `totalScore < 20` | Engajamento básico (não vender ainda) |
| **LOW** | `totalScore >= 20` | Educação e engajamento (não vender ainda) |
| **MEDIUM** | `totalScore >= 40` | Ofertas de entrada (trials, descontos) |
| **HIGH** | `totalScore >= 60 AND (hasPhones OR messagesSent >= 5)` | Contato direto, ofertas personalizadas |
| **VERY_HIGH** | `totalScore >= 80 AND hasPhones AND messagesSent >= 10` | Prioridade máxima, contato imediato do time de vendas |

### Descrição dos Níveis

#### VERY_LOW
Potencial MUITO BAIXO de conversão.

**Perfil:** Usuário pouco engajado, sem telefone, sem interações.

**Foco:** Engajamento básico (não vender ainda).

#### LOW
Potencial BAIXO de conversão.

**Perfil:** Usuário com algum engajamento, mas faltam indicadores-chave.

**Foco:** Educação e engajamento (não vender ainda).

#### MEDIUM
Potencial MÉDIO de conversão.

**Perfil:** Usuário engajado, pode converter com abordagem certa.

**Foco:** Ofertas de entrada (trials, descontos).

#### HIGH
Potencial ALTO de conversão.

**Perfil:** Usuário muito engajado, tem telefone OU mensagens.

**Foco:** Contato direto, ofertas personalizadas.

#### VERY_HIGH
Potencial MUITO ALTO de conversão.

**Perfil:** Usuário super engajado, tem telefone E muitas mensagens.

**Foco:** Prioridade máxima, contato imediato do time de vendas.

### Casos de Uso

- **Priorização de leads:** Time de vendas foca em VERY_HIGH e HIGH
- **Automação de marketing:** Enviar ofertas personalizadas por segmento
- **Previsão de receita:** Estimar quantos usuários converterão
- **A/B testing:** Testar diferentes abordagens por potencial
- **CRM Integration:** Sincronizar com Salesforce/HubSpot usando este campo

---

## 🛠️ Métodos de Negócio (Implementação Futura)

### `calculateTotalScore()`

Calcula o score total com base nos sub-scores.

**Fórmula sugerida (ajustar pesos conforme necessidade):**
```java
public void calculateTotalScore() {
    int contentScore = calculateContentEngagementScore(); // 0-100
    int socialScore = calculateSocialEngagementScore();   // 0-100
    int profileScore = calculateProfileCompletenessScore(); // 0-100
    int recencyScore = calculateRecencyScore();           // 0-100

    this.totalScore = (int) (
        contentScore * 0.4 +
        socialScore * 0.3 +
        profileScore * 0.2 +
        recencyScore * 0.1
    );

    this.engagementLevel = deriveEngagementLevel(this.totalScore);
    this.conversionPotential = deriveConversionPotential();
    this.scoreUpdatedAt = LocalDateTime.now();
}
```

### `deriveEngagementLevel(int score)`

Deriva o nível de engajamento com base no score total.

```java
private EngagementLevel deriveEngagementLevel(int score) {
    if (score >= 76) return EngagementLevel.VERY_HIGH;
    if (score >= 51) return EngagementLevel.HIGH;
    if (score >= 26) return EngagementLevel.MEDIUM;
    return EngagementLevel.LOW;
}
```

### `deriveConversionPotential()`

Deriva o potencial de conversão com base em múltiplos fatores.

```java
private ConversionPotential deriveConversionPotential() {
    if (totalScore >= 80 && hasPhones && totalMessagesSent >= 10) {
        return ConversionPotential.VERY_HIGH;
    }
    if (totalScore >= 60 && (hasPhones || totalMessagesSent >= 5)) {
        return ConversionPotential.HIGH;
    }
    if (totalScore >= 40) {
        return ConversionPotential.MEDIUM;
    }
    if (totalScore >= 20) {
        return ConversionPotential.LOW;
    }
    return ConversionPotential.VERY_LOW;
}
```

### Métodos Auxiliares

```java
public boolean isActive() {
    return deletedAt == null;
}

public void softDelete() {
    this.deletedAt = LocalDateTime.now();
}

public void restore() {
    this.deletedAt = null;
}
```

---

## 📊 Modelos de Persistência

### Opção 1: Uma linha por usuário (RECOMENDADO)
- Atualizada periodicamente via batch job
- Tabela: `app_user_ranking`
- Performance: Rápida para leitura
- Limitação: Não mantém histórico

### Opção 2: Histórico de snapshots
- 1 linha por dia/semana para rastrear evolução
- Tabela: `app_user_ranking_history`
- Performance: Mais registros, queries mais lentas
- Benefício: Análise temporal completa

### Opção 3: Híbrido (RECOMENDADO PARA ESCALAR)
- Tabela atual: `app_user_ranking` (snapshot mais recente)
- Tabela histórico: `app_user_ranking_history` (snapshots antigos)
- Batch job: Copia ranking atual para histórico diariamente
- Benefício: Performance + Histórico

**Recomendação:** Começar com **Opção 1**, migrar para **Opção 3** quando precisar de histórico.

---

## 🔍 Queries Úteis (Implementar em UserRankingRepository)

### Buscar ranking de um usuário
```java
find("userId = ?1 and deletedAt is null", userId).firstResult()
```

### Top 100 usuários com maior score
```java
list("deletedAt is null order by totalScore desc", Page.ofSize(100))
```

### Usuários com alto potencial de conversão
```java
list("conversionPotential = ?1 and deletedAt is null order by totalScore desc",
    ConversionPotential.VERY_HIGH)
```

### Usuários inativos (sem atividade nos últimos 30 dias)
```java
list("lastActivityAt < ?1 and deletedAt is null order by lastActivityAt",
    LocalDateTime.now().minusDays(30))
```

### Usuários com streak alto (gamificação)
```java
list("consecutiveDaysStreak >= ?1 and deletedAt is null order by consecutiveDaysStreak desc", 7)
```

### Distribuição por nível de engajamento
```sql
SELECT engagement_level, COUNT(*) as total
FROM app_user_ranking
WHERE deleted_at IS NULL
GROUP BY engagement_level
ORDER BY total DESC
```

### Potencial de conversão por categoria favorita
```sql
SELECT favorite_category, conversion_potential, COUNT(*) as total
FROM app_user_ranking
WHERE deleted_at IS NULL
GROUP BY favorite_category, conversion_potential
ORDER BY total DESC
```

---

## 🚀 Próximos Passos

### 1. Implementar Repository
Criar `UserRankingRepository.java` com queries customizadas:
```java
@ApplicationScoped
public class UserRankingRepository implements PanacheRepositoryBase<UserRankingModel, UUID> {
    public UserRankingModel findByUserId(UUID userId) {
        return find("userId = ?1 and deletedAt is null", userId).firstResult();
    }

    public List<UserRankingModel> findTopUsers(int limit) {
        return find("deletedAt is null", Sort.descending("totalScore"))
            .page(Page.ofSize(limit))
            .list();
    }
}
```

### 2. Implementar Service
Criar `UserRankingService.java` com lógica de cálculo:
```java
@ApplicationScoped
public class UserRankingService {
    @Inject UserRankingRepository repository;
    @Inject Logger log;

    @Transactional
    public void calculateAndUpdateScore(UUID userId) {
        // Implementar lógica de cálculo
    }
}
```

### 3. Criar Migration Flyway
Arquivo: `V1.0.X__Create_user_ranking_table.sql`
```sql
CREATE TABLE app_user_ranking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    total_score INTEGER NOT NULL DEFAULT 0,
    engagement_level VARCHAR(20) NOT NULL DEFAULT 'LOW',
    conversion_potential VARCHAR(20) NOT NULL DEFAULT 'VERY_LOW',
    -- ... outros campos
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    UNIQUE(user_id)
);

CREATE INDEX idx_user_ranking_user_id ON app_user_ranking(user_id);
CREATE INDEX idx_user_ranking_total_score ON app_user_ranking(total_score DESC);
CREATE INDEX idx_user_ranking_conversion ON app_user_ranking(conversion_potential);
```

### 4. Implementar Batch Job
Criar job para calcular scores periodicamente:
```java
@ApplicationScoped
public class UserRankingBatchJob {
    @Scheduled(cron = "0 0 2 * * ?") // 2h AM diariamente
    public void recalculateAllScores() {
        // Implementar lógica
    }
}
```

### 5. Criar Endpoints REST
```java
@Path("/api/v1/rankings")
public class UserRankingController {
    @GET
    @Path("/top")
    public Response getTopUsers(@QueryParam("limit") int limit) {
        // Retornar top usuários
    }

    @GET
    @Path("/user/{userId}")
    public Response getUserRanking(@PathParam("userId") UUID userId) {
        // Retornar ranking de um usuário
    }
}
```

---

## 📈 Métricas e KPIs

### Métricas de Engajamento
- **DAU (Daily Active Users):** Usuários com `lastActivityAt` nas últimas 24h
- **WAU (Weekly Active Users):** Usuários com `lastActivityAt` nos últimos 7 dias
- **MAU (Monthly Active Users):** Usuários com `lastActivityAt` nos últimos 30 dias
- **Retention Rate:** % de usuários que voltam após N dias
- **Churn Rate:** % de usuários com `lastActivityAt` > 30 dias

### Métricas de Conversão
- **Conversion Rate:** % de usuários que passam de VERY_LOW para MEDIUM+
- **Lead Quality Score:** Avg(`totalScore`) do segmento HIGH/VERY_HIGH
- **Revenue Prediction:** Soma de `conversionPotential.weight * avgTicket`
- **Sales Pipeline:** Contagem de usuários por `conversionPotential`

### Métricas de Produto
- **Feature Adoption:** % de usuários que usam feature X (via `totalContentViews`)
- **Social Virality:** Avg(`totalConversationsStarted`) / Avg(`totalMessagesSent`)
- **Profile Completeness:** % de usuários com `hasPhones = true`
- **Power Users:** Usuários com `consecutiveDaysStreak >= 30`

---

## ⚠️ Considerações Importantes

### Performance
- Calcular scores via batch job (não em tempo real)
- Indexar campos `totalScore`, `userId`, `conversionPotential`
- Usar cache (Redis) para top rankings
- Limitar queries com `Page.ofSize()`

### Privacidade
- Não expor `totalScore` para usuários (só admin)
- Logs devem respeitar LGPD/GDPR
- Pseudonimizar dados em relatórios

### Escalabilidade
- Particionar tabela por `createdAt` se histórico crescer muito
- Considerar Data Warehouse separado para analytics
- Usar async processing para cálculos pesados

### Monitoramento
- Alertar se `scoreUpdatedAt` > 48h (score desatualizado)
- Dashboards: distribuição de `engagementLevel` e `conversionPotential`
- Logs: tempo de execução do batch job

---

## 📚 Referências

- [Panache Documentation](https://quarkus.io/guides/hibernate-orm-panache)
- [Quarkus Scheduler](https://quarkus.io/guides/scheduler)
- [User Engagement Metrics](https://mixpanel.com/blog/user-engagement-metrics/)
- [Lead Scoring Best Practices](https://www.salesforce.com/resources/articles/lead-scoring/)
