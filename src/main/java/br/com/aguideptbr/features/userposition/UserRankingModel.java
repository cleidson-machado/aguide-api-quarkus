package br.com.aguideptbr.features.userposition;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade representando o ranking/pontuação de um usuário.
 *
 * OBJETIVO: Classificar usuários por POTENCIAL DE CONVERSÃO (likelihood de
 * pagar por serviços).
 *
 * CRITÉRIOS DE CLASSIFICAÇÃO:
 * 1. Engajamento com conteúdo (visualizações, tempo de uso, frequência)
 * 2. Interação social (mensagens enviadas, conversas iniciadas)
 * 3. Completude do perfil (telefones cadastrados, apps de mensagem)
 * 4. Recência e frequência de uso (última atividade, dias consecutivos)
 *
 * RELACIONAMENTOS SUGERIDOS (NÃO IMPLEMENTADOS AINDA):
 * - @ManyToOne UserModel user (1 ranking por usuário, relacionamento 1-1)
 * - Possível auditar histórico: criar UserRankingHistoryModel para rastrear
 * evolução do score
 *
 * MODELOS DE PERSISTÊNCIA POSSÍVEIS:
 * 1. Uma linha por usuário (atualizada periodicamente via batch job)
 * 2. Histórico de snapshots (1 linha por dia/semana para rastrear evolução)
 * 3. Híbrido: tabela atual + tabela de histórico separada
 *
 * RECOMENDAÇÃO: Começar com opção 1 (1 linha por usuário), migrar para 3 se
 * precisar de histórico.
 *
 * USO TÍPICO:
 * - Dashboard admin: listar top 100 usuários com maior score
 * - Recomendações: priorizar usuários com score alto para campanhas
 * - Análise de churn: identificar usuários com score baixo e decrescente
 */
@Entity
@Table(name = "app_user_ranking")
public class UserRankingModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    // ========== RELACIONAMENTO COM USUÁRIO (NÃO IMPLEMENTADO) ==========

    /**
     * ID do usuário (relacionamento manual por enquanto).
     *
     * FUTURO: Transformar em @ManyToOne UserModel quando definir modelagem.
     * Exemplo:
     *
     * @ManyToOne(fetch = FetchType.LAZY)
     * @JoinColumn(name = "user_id", nullable = false, unique = true)
     *                  public UserModel user;
     *
     *                  IMPORTANTE: Adicionar constraint UNIQUE se for 1-1 (1
     *                  ranking por usuário).
     */
    @Column(name = "user_id", nullable = false)
    public UUID userId;

    // ========== SCORE E CLASSIFICAÇÃO ==========

    /**
     * Score TOTAL do usuário (0-100).
     * Calculado com base em múltiplos fatores (ver método calculateScore()).
     *
     * FÓRMULA SUGERIDA (ajustar pesos conforme necessidade):
     * - Engajamento com conteúdo: 40% (contentEngagementScore)
     * - Interação social: 30% (socialEngagementScore)
     * - Completude de perfil: 20% (profileCompletenessScore)
     * - Recência de uso: 10% (recencyScore)
     */
    @Column(name = "total_score", nullable = false)
    public Integer totalScore = 0;

    /**
     * Nível de engajamento do usuário (LOW, MEDIUM, HIGH, VERY_HIGH).
     * Derivado do totalScore:
     * - LOW: 0-25
     * - MEDIUM: 26-50
     * - HIGH: 51-75
     * - VERY_HIGH: 76-100
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "engagement_level", length = 20, nullable = false)
    public EngagementLevel engagementLevel = EngagementLevel.LOW;

    /**
     * Potencial de conversão (VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH).
     * Baseado em análise preditiva ou regras de negócio.
     *
     * SUGESTÃO DE REGRAS:
     * - VERY_HIGH: totalScore >= 80 AND hasPhones = true AND messagesSent >= 10
     * - HIGH: totalScore >= 60 AND (hasPhones = true OR messagesSent >= 5)
     * - MEDIUM: totalScore >= 40
     * - LOW: totalScore >= 20
     * - VERY_LOW: totalScore < 20
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "conversion_potential", length = 20, nullable = false)
    public ConversionPotential conversionPotential = ConversionPotential.VERY_LOW;

    // ========== ENGAJAMENTO COM CONTEÚDO ==========

    /**
     * Total de visualizações de conteúdo (ContentRecordModel).
     *
     * FUTURO: Calcular via query:
     * SELECT COUNT(*) FROM user_content_views WHERE user_id = ?
     *
     * RELACIONAMENTO SUGERIDO: Criar tabela user_content_views (user_id,
     * content_id, viewed_at)
     */
    @Column(name = "total_content_views", nullable = false)
    public Long totalContentViews = 0L;

    /**
     * Total de conteúdos únicos visualizados.
     * Diferencia usuário que vê 100x o mesmo vídeo vs 100 vídeos diferentes.
     *
     * FUTURO: SELECT COUNT(DISTINCT content_id) FROM user_content_views WHERE
     * user_id = ?
     */
    @Column(name = "unique_content_views", nullable = false)
    public Long uniqueContentViews = 0L;

    /**
     * Tempo médio diário de uso do app (em minutos).
     *
     * CÁLCULO SUGERIDO:
     * 1. Registrar eventos de "app opened" e "app closed" (tabela user_sessions)
     * 2. Calcular média dos últimos 30 dias
     * 3. Atualizar este campo via batch job noturno
     *
     * RELACIONAMENTO SUGERIDO: Criar UserSessionModel (user_id, session_start,
     * session_end, duration_minutes)
     */
    @Column(name = "avg_daily_usage_minutes", nullable = false)
    public Integer avgDailyUsageMinutes = 0;

    /**
     * Total de dias consecutivos de uso (streak atual).
     * Usuário que acessa diariamente tem maior engajamento.
     *
     * CÁLCULO: Contar dias consecutivos até hoje com pelo menos 1 atividade.
     */
    @Column(name = "consecutive_days_streak", nullable = false)
    public Integer consecutiveDaysStreak = 0;

    /**
     * Total de dias com pelo menos 1 acesso (lifetime).
     * Diferencia usuário antigo com baixa frequência vs novo com alta frequência.
     */
    @Column(name = "total_active_days", nullable = false)
    public Long totalActiveDays = 0L;

    // ========== INTERAÇÃO SOCIAL ==========

    /**
     * Total de mensagens enviadas pelo usuário (UserMessageModel).
     *
     * FUTURO: SELECT COUNT(*) FROM app_user_message WHERE sender_id = ? AND
     * deleted_at IS NULL
     *
     * RELACIONAMENTO SUGERIDO: Já existe UserMessageModel.sender (FK para
     * UserModel)
     */
    @Column(name = "total_messages_sent", nullable = false)
    public Long totalMessagesSent = 0L;

    /**
     * Total de conversas iniciadas pelo usuário (ConversationModel onde user é
     * criador).
     * Proatividade em iniciar conversas indica maior engajamento.
     *
     * FUTURO: SELECT COUNT(*) FROM app_conversation_participant
     * WHERE user_id = ? AND is_creator = true AND deleted_at IS NULL
     *
     * RELACIONAMENTO SUGERIDO: Já existe ConversationParticipantModel.isCreator
     */
    @Column(name = "total_conversations_started", nullable = false)
    public Long totalConversationsStarted = 0L;

    /**
     * Total de contatos únicos com quem o usuário trocou mensagens.
     * Maior rede = maior potencial de conversão.
     *
     * FUTURO: Query complexa cruzando conversas e participantes.
     */
    @Column(name = "unique_contacts_messaged", nullable = false)
    public Long uniqueContactsMessaged = 0L;

    /**
     * Total de conversas ativas (onde usuário ainda é participante).
     *
     * FUTURO: SELECT COUNT(*) FROM app_conversation_participant
     * WHERE user_id = ? AND left_at IS NULL AND deleted_at IS NULL
     */
    @Column(name = "active_conversations", nullable = false)
    public Integer activeConversations = 0;

    // ========== COMPLETUDE DO PERFIL ==========

    /**
     * Se o usuário tem pelo menos 1 telefone cadastrado (PhoneNumberModel).
     * Usuário com telefone = maior probabilidade de conversão.
     *
     * FUTURO: SELECT COUNT(*) > 0 FROM phone_numbers WHERE user_id = ? AND
     * deleted_at IS NULL
     *
     * RELACIONAMENTO SUGERIDO: Já existe PhoneNumberModel.user (FK para UserModel)
     */
    @Column(name = "has_phones", nullable = false)
    public Boolean hasPhones = false;

    /**
     * Total de telefones cadastrados.
     * Mais telefones = usuário mais engajado/confiável.
     *
     * FUTURO: SELECT COUNT(*) FROM phone_numbers WHERE user_id = ? AND deleted_at
     * IS NULL
     */
    @Column(name = "total_phones", nullable = false)
    public Integer totalPhones = 0;

    /**
     * Se o usuário tem WhatsApp cadastrado.
     * WhatsApp = canal direto de conversão.
     *
     * FUTURO: SELECT COUNT(*) > 0 FROM phone_numbers
     * WHERE user_id = ? AND has_whatsapp = true AND deleted_at IS NULL
     */
    @Column(name = "has_whatsapp", nullable = false)
    public Boolean hasWhatsapp = false;

    /**
     * Se o usuário tem Telegram cadastrado.
     *
     * FUTURO: SELECT COUNT(*) > 0 FROM phone_numbers
     * WHERE user_id = ? AND has_telegram = true AND deleted_at IS NULL
     */
    @Column(name = "has_telegram", nullable = false)
    public Boolean hasTelegram = false;

    // ========== RECÊNCIA E ATIVIDADE ==========

    /**
     * Data e hora da última atividade do usuário no app.
     * Usuário recente = maior potencial de conversão.
     *
     * CÁLCULO: MAX(last_login, last_content_view, last_message_sent)
     */
    @Column(name = "last_activity_at")
    public LocalDateTime lastActivityAt;

    /**
     * Data e hora do último conteúdo visualizado.
     *
     * FUTURO: SELECT MAX(viewed_at) FROM user_content_views WHERE user_id = ?
     */
    @Column(name = "last_content_view_at")
    public LocalDateTime lastContentViewAt;

    /**
     * Data e hora da última mensagem enviada.
     *
     * FUTURO: SELECT MAX(sent_at) FROM app_user_message WHERE sender_id = ? AND
     * deleted_at IS NULL
     */
    @Column(name = "last_message_sent_at")
    public LocalDateTime lastMessageSentAt;

    /**
     * Data e hora do último login.
     *
     * FUTURO: Registrar em UserSessionModel ou atualizar em UserModel ao fazer
     * login.
     */
    @Column(name = "last_login_at")
    public LocalDateTime lastLoginAt;

    // ========== PREFERÊNCIAS E COMPORTAMENTO ==========

    /**
     * Categoria de conteúdo favorita do usuário.
     * Derivada de análise de histórico de visualizações.
     *
     * FUTURO: Calcular via GROUP BY categoryId em user_content_views.
     */
    @Column(name = "favorite_category", length = 100)
    public String favoriteCategory;

    /**
     * Tipo de conteúdo preferido (VIDEO, ARTICLE, PODCAST, etc).
     *
     * FUTURO: Calcular via GROUP BY type em user_content_views.
     */
    @Column(name = "favorite_content_type", length = 50)
    public String favoriteContentType;

    /**
     * Horário preferido de uso (MORNING, AFTERNOON, EVENING, NIGHT).
     * Útil para timing de campanhas/notificações.
     *
     * CÁLCULO: Agrupar sessions por hora do dia, identificar pico.
     */
    @Column(name = "preferred_usage_time", length = 20)
    public String preferredUsageTime;

    // ========== AUDITORIA E METADADOS ==========

    /**
     * Data e hora da última atualização do score.
     * Útil para saber se o score está atualizado.
     *
     * SUGESTÃO: Rodar batch job diário/semanal para recalcular scores.
     */
    @Column(name = "score_updated_at")
    public LocalDateTime scoreUpdatedAt;

    /**
     * Data de criação do registro de ranking.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    /**
     * Data da última atualização do registro.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    /**
     * Data de exclusão lógica (soft delete).
     * Null significa que o ranking está ativo.
     *
     * NOTA: Raramente usado, pois ranking está vinculado ao usuário.
     * Se usuário for deletado, ranking também deve ser.
     */
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    // ========== MÉTODOS DE NEGÓCIO (SUGESTÕES) ==========

    /**
     * Calcula o score total com base nos sub-scores.
     *
     * FÓRMULA SUGERIDA (ajustar pesos conforme necessidade):
     * - Engajamento com conteúdo: 40%
     * - Interação social: 30%
     * - Completude de perfil: 20%
     * - Recência de uso: 10%
     *
     * IMPLEMENTAÇÃO FUTURA:
     * public void calculateTotalScore() {
     * int contentScore = calculateContentEngagementScore(); // 0-100
     * int socialScore = calculateSocialEngagementScore(); // 0-100
     * int profileScore = calculateProfileCompletenessScore(); // 0-100
     * int recencyScore = calculateRecencyScore(); // 0-100
     *
     * this.totalScore = (int) (
     * contentScore * 0.4 +
     * socialScore * 0.3 +
     * profileScore * 0.2 +
     * recencyScore * 0.1
     * );
     *
     * this.engagementLevel = deriveEngagementLevel(this.totalScore);
     * this.conversionPotential = deriveConversionPotential();
     * this.scoreUpdatedAt = LocalDateTime.now();
     * }
     */

    /**
     * Deriva o nível de engajamento com base no score total.
     *
     * IMPLEMENTAÇÃO FUTURA:
     * private EngagementLevel deriveEngagementLevel(int score) {
     * if (score >= 76) return EngagementLevel.VERY_HIGH;
     * if (score >= 51) return EngagementLevel.HIGH;
     * if (score >= 26) return EngagementLevel.MEDIUM;
     * return EngagementLevel.LOW;
     * }
     */

    /**
     * Deriva o potencial de conversão com base em múltiplos fatores.
     *
     * IMPLEMENTAÇÃO FUTURA:
     * private ConversionPotential deriveConversionPotential() {
     * // Regras de negócio customizadas
     * if (totalScore >= 80 && hasPhones && totalMessagesSent >= 10) {
     * return ConversionPotential.VERY_HIGH;
     * }
     * if (totalScore >= 60 && (hasPhones || totalMessagesSent >= 5)) {
     * return ConversionPotential.HIGH;
     * }
     * if (totalScore >= 40) {
     * return ConversionPotential.MEDIUM;
     * }
     * if (totalScore >= 20) {
     * return ConversionPotential.LOW;
     * }
     * return ConversionPotential.VERY_LOW;
     * }
     */

    /**
     * Verifica se o ranking está ativo.
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * Marca o ranking como deletado (soft delete).
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restaura o ranking deletado.
     */
    public void restore() {
        this.deletedAt = null;
    }

    // ========== QUERIES ÚTEIS (IMPLEMENTAR EM UserRankingRepository) ==========

    /**
     * QUERY: Buscar ranking de um usuário
     * - find("userId = ?1 and deletedAt is null", userId).firstResult()
     *
     * QUERY: Top 100 usuários com maior score
     * - list("deletedAt is null order by totalScore desc", Page.ofSize(100))
     *
     * QUERY: Usuários com alto potencial de conversão
     * - list("conversionPotential = ?1 and deletedAt is null order by totalScore
     * desc", ConversionPotential.VERY_HIGH)
     *
     * QUERY: Usuários inativos (sem atividade nos últimos 30 dias)
     * - list("lastActivityAt < ?1 and deletedAt is null order by lastActivityAt",
     * LocalDateTime.now().minusDays(30))
     *
     * QUERY: Usuários com streak alto (gamificação)
     * - list("consecutiveDaysStreak >= ?1 and deletedAt is null order by
     * consecutiveDaysStreak desc", 7)
     */
}
