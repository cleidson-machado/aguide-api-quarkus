package br.com.aguideptbr.features.userposition;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.userposition.enuns.ConversionPotential;
import br.com.aguideptbr.features.userposition.enuns.EngagementLevel;
import br.com.aguideptbr.features.userposition.services.UserRankingMetricsService;
import br.com.aguideptbr.features.userposition.services.UserRankingMilestoneService;
import br.com.aguideptbr.features.userposition.services.UserRankingValidationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Service para lógica de negócio relacionada a rankings de usuários.
 * Gerencia criação, atualização, busca e remoção de dados de ranking.
 *
 * REFATORAÇÃO SOLID (2026-04-05):
 * Responsabilidades extraídas para services especializados:
 * - UserRankingValidationService: validações de entrada
 * - UserRankingMetricsService: cálculos de engagement e conversion
 * - UserRankingMilestoneService: detecção e premiação de milestones
 *
 * Responsabilidades mantidas:
 * - Orquestração de CRUD operations
 * - Gerenciamento de transações
 * - Coordenação entre services
 *
 * @see UserRankingModel
 * @see UserRankingRepository
 * @see UserRankingValidationService
 * @see UserRankingMetricsService
 * @see UserRankingMilestoneService
 */
@ApplicationScoped
public class UserRankingService {

    private final Logger log;
    private final UserRankingRepository userRankingRepository;
    private final UserRankingAuditRepository auditRepository;
    private final UserRankingValidationService validationService;
    private final UserRankingMetricsService metricsService;
    private final UserRankingMilestoneService milestoneService;

    // Constantes de validação (mantidas para compatibilidade com addPoints)
    private static final int MAX_TOTAL_SCORE = 9_999_999;
    private static final int MAX_POINTS_PER_REQUEST = 1000;

    public UserRankingService(
            Logger log,
            UserRankingRepository userRankingRepository,
            UserRankingAuditRepository auditRepository,
            UserRankingValidationService validationService,
            UserRankingMetricsService metricsService,
            UserRankingMilestoneService milestoneService) {
        this.log = log;
        this.userRankingRepository = userRankingRepository;
        this.auditRepository = auditRepository;
        this.validationService = validationService;
        this.metricsService = metricsService;
        this.milestoneService = milestoneService;
    }

    /**
     * Cria um novo ranking para um usuário.
     *
     * Validações:
     * - Verifica se o usuário já possui ranking (unicidade)
     * - Inicializa valores padrão se não fornecidos
     * - Calcula engagement level e conversion potential iniciais
     *
     * @param userRanking Modelo de ranking a ser criado
     * @return UserRankingModel persistido com UUID gerado
     * @throws WebApplicationException (409) se usuário já tiver ranking
     */
    @Transactional
    public UserRankingModel create(UserRankingModel userRanking) {
        log.infof("📝 Creating user ranking: userId=%s", userRanking.getUserId());

        // Verificar se usuário já tem ranking
        if (userRankingRepository.existsByUserId(userRanking.getUserId())) {
            log.warnf("⚠️ User already has a ranking: userId=%s", userRanking.getUserId());
            throw new WebApplicationException(
                    "User already has a ranking. Use PUT to update.",
                    Response.Status.CONFLICT);
        }

        // Calcular métricas iniciais (delegado para MetricsService)
        metricsService.calculateEngagementLevel(userRanking);
        metricsService.calculateConversionPotential(userRanking);
        userRanking.setScoreUpdatedAt(LocalDateTime.now(ZoneOffset.UTC)); // Correction #3: UTC timestamp

        userRankingRepository.persist(userRanking);

        log.infof("✅ User ranking created successfully: id=%s, userId=%s, totalScore=%d",
                userRanking.getId(), userRanking.getUserId(), userRanking.getTotalScore());

        return userRanking;
    }

    /**
     * Busca um ranking por ID.
     *
     * @param id ID do ranking
     * @return Optional contendo o ranking ou vazio se não encontrado
     */
    public Optional<UserRankingModel> findById(UUID id) {
        log.infof("🔍 Finding ranking by id: %s", id);
        return userRankingRepository.findByIdOptional(id)
                .filter(UserRankingModel::isActive);
    }

    /**
     * Busca o ranking de um usuário específico.
     *
     * @param userId ID do usuário
     * @return Optional contendo o ranking ou vazio se não encontrado
     */
    public Optional<UserRankingModel> findByUserId(UUID userId) {
        log.infof("🔍 Finding ranking by userId: %s", userId);
        return userRankingRepository.findByUserId(userId);
    }

    /**
     * Busca o histórico de adição de pontos de um usuário.
     *
     * @param userId ID do usuário
     * @param limit  Quantidade máxima de registros (default: 10)
     * @return Lista de registros de pontos ordenados por data (mais recente
     *         primeiro)
     */
    public List<UserRankingAuditModel> findPointsHistory(UUID userId, int limit) {
        log.infof("📜 Finding points history for userId=%s, limit=%d", userId, limit);
        return auditRepository.findPointsHistoryByUserId(userId, limit);
    }

    /**
     * Busca todos os rankings ativos.
     *
     * @return Lista de rankings ativos
     */
    public List<UserRankingModel> findAllActive() {
        log.info("🔍 Finding all active rankings");
        return userRankingRepository.findAllActive();
    }

    /**
     * Busca rankings por nível de engajamento.
     *
     * @param engagementLevel Nível de engajamento
     * @return Lista de rankings com o nível especificado
     */
    public List<UserRankingModel> findByEngagementLevel(EngagementLevel engagementLevel) {
        log.infof("🔍 Finding rankings by engagement level: %s", engagementLevel);
        return userRankingRepository.findByEngagementLevel(engagementLevel);
    }

    /**
     * Busca rankings por potencial de conversão.
     *
     * @param conversionPotential Potencial de conversão
     * @return Lista de rankings com o potencial especificado
     */
    public List<UserRankingModel> findByConversionPotential(ConversionPotential conversionPotential) {
        log.infof("🔍 Finding rankings by conversion potential: %s", conversionPotential);
        return userRankingRepository.findByConversionPotential(conversionPotential);
    }

    /**
     * Busca top N usuários por pontuação.
     *
     * @param limit Número máximo de resultados (padrão: 10)
     * @return Lista dos usuários com maior pontuação
     */
    public List<UserRankingModel> findTopByScore(int limit) {
        int effectiveLimit = limit > 0 && limit <= 100 ? limit : 10;
        log.infof("🔍 Finding top %d users by score", effectiveLimit);
        return userRankingRepository.findTopByScore(effectiveLimit);
    }

    /**
     * Atualiza um ranking existente.
     *
     * REFATORAÇÃO (2026-04-05):
     * - Validações delegadas para ValidationService
     * - Métricas delegadas para MetricsService
     * - Milestones delegados para MilestoneService (incluindo NOVO: phone
     * milestones)
     * - Timestamps com UTC (Correction #3)
     * - Remoção de persist() redundantes (Correction #6)
     *
     * @param id          ID do ranking
     * @param updatedData Dados atualizados
     * @return UserRankingModel atualizado
     * @throws WebApplicationException (404) se ranking não encontrado
     */
    @Transactional
    public UserRankingModel update(UUID id, UserRankingModel updatedData) {
        log.infof("🔄 Updating ranking: id=%s", id);

        UserRankingModel existing = userRankingRepository.findByIdOptional(id)
                .filter(UserRankingModel::isActive)
                .orElseThrow(() -> {
                    log.warnf("⚠️ Ranking not found: id=%s", id);
                    return new WebApplicationException(
                            "Ranking not found",
                            Response.Status.NOT_FOUND);
                });

        // Score é gerenciado separadamente (addPoints, milestones) - não atualizar
        // diretamente

        // Capturar valores anteriores para detecção de milestones
        Long previousContentViews = existing.getTotalContentViews();
        Integer previousProfileCompletion = existing.getProfileCompletionPercentage() != null
                ? existing.getProfileCompletionPercentage()
                : 0;
        Integer previousTotalPhones = existing.getTotalPhones() != null ? existing.getTotalPhones() : 0;
        Boolean previousHasWhatsapp = existing.getHasWhatsapp() != null ? existing.getHasWhatsapp() : false;
        Boolean previousHasTelegram = existing.getHasTelegram() != null ? existing.getHasTelegram() : false;

        // Atualizar campos com validação delegada
        if (updatedData.getTotalContentViews() != null) {
            validationService.validateNonNegative(updatedData.getTotalContentViews(), "totalContentViews");
            existing.setTotalContentViews(updatedData.getTotalContentViews());
        }
        if (updatedData.getUniqueContentViews() != null) {
            validationService.validateNonNegative(updatedData.getUniqueContentViews(), "uniqueContentViews");
            existing.setUniqueContentViews(updatedData.getUniqueContentViews());
        }
        if (updatedData.getAvgDailyUsageMinutes() != null) {
            validationService.validateDailyUsageMinutes(updatedData.getAvgDailyUsageMinutes());
            existing.setAvgDailyUsageMinutes(updatedData.getAvgDailyUsageMinutes());
        }
        if (updatedData.getConsecutiveDaysStreak() != null) {
            validationService.validateConsecutiveDaysStreak(updatedData.getConsecutiveDaysStreak());
            existing.setConsecutiveDaysStreak(updatedData.getConsecutiveDaysStreak());
        }
        if (updatedData.getTotalActiveDays() != null) {
            existing.setTotalActiveDays(updatedData.getTotalActiveDays());
        }
        if (updatedData.getTotalMessagesSent() != null) {
            existing.setTotalMessagesSent(updatedData.getTotalMessagesSent());
        }
        if (updatedData.getTotalConversationsStarted() != null) {
            existing.setTotalConversationsStarted(updatedData.getTotalConversationsStarted());
        }
        if (updatedData.getUniqueContactsMessaged() != null) {
            existing.setUniqueContactsMessaged(updatedData.getUniqueContactsMessaged());
        }
        if (updatedData.getActiveConversations() != null) {
            existing.setActiveConversations(updatedData.getActiveConversations());
        }
        if (updatedData.getHasPhones() != null) {
            existing.setHasPhones(updatedData.getHasPhones());
        }
        if (updatedData.getTotalPhones() != null) {
            existing.setTotalPhones(updatedData.getTotalPhones());
        }
        if (updatedData.getHasWhatsapp() != null) {
            existing.setHasWhatsapp(updatedData.getHasWhatsapp());
        }
        if (updatedData.getHasTelegram() != null) {
            existing.setHasTelegram(updatedData.getHasTelegram());
        }
        if (updatedData.getLastActivityAt() != null) {
            validationService.validateTimestamp(updatedData.getLastActivityAt(), "lastActivityAt");
            existing.setLastActivityAt(updatedData.getLastActivityAt());
        }
        if (updatedData.getLastContentViewAt() != null) {
            validationService.validateTimestamp(updatedData.getLastContentViewAt(), "lastContentViewAt");
            existing.setLastContentViewAt(updatedData.getLastContentViewAt());
        }
        if (updatedData.getLastMessageSentAt() != null) {
            validationService.validateTimestamp(updatedData.getLastMessageSentAt(), "lastMessageSentAt");
            existing.setLastMessageSentAt(updatedData.getLastMessageSentAt());
        }
        if (updatedData.getLastLoginAt() != null) {
            validationService.validateTimestamp(updatedData.getLastLoginAt(), "lastLoginAt");
            existing.setLastLoginAt(updatedData.getLastLoginAt());
        }
        if (updatedData.getFavoriteCategory() != null) {
            validationService.validateStringLength(updatedData.getFavoriteCategory(), 100, "favoriteCategory");
            existing.setFavoriteCategory(updatedData.getFavoriteCategory());
        }
        if (updatedData.getFavoriteContentType() != null) {
            validationService.validateStringLength(updatedData.getFavoriteContentType(), 50, "favoriteContentType");
            validationService.validateContentType(updatedData.getFavoriteContentType());
            existing.setFavoriteContentType(updatedData.getFavoriteContentType());
        }
        if (updatedData.getPreferredUsageTime() != null) {
            existing.setPreferredUsageTime(updatedData.getPreferredUsageTime());
        }
        if (updatedData.getProfileCompletionPercentage() != null) {
            validationService.validateProfileCompletionPercentage(updatedData.getProfileCompletionPercentage());
            existing.setProfileCompletionPercentage(updatedData.getProfileCompletionPercentage());
        }

        // Recalcular métricas (delegado para MetricsService)
        metricsService.calculateEngagementLevel(existing);
        metricsService.calculateConversionPotential(existing);
        existing.setScoreUpdatedAt(LocalDateTime.now(ZoneOffset.UTC)); // Correction #3: UTC timestamp

        // Correction #6: Remover persist() redundante - JPA dirty checking automático
        // em entidade gerenciada
        // userRankingRepository.persist(existing); <- REMOVIDO

        // Verificar milestones de visualização de conteúdo (delegado para
        // MilestoneService)
        if (updatedData.getTotalContentViews() != null
                && !updatedData.getTotalContentViews().equals(previousContentViews)) {
            milestoneService.checkContentViewsMilestones(existing.getUserId(), previousContentViews,
                    existing.getTotalContentViews());
        }

        // Verificar milestones de completude de perfil (delegado para MilestoneService)
        if (updatedData.getProfileCompletionPercentage() != null) {
            int totalPointsAdded = milestoneService.checkProfileCompletionMilestones(
                    existing,
                    previousProfileCompletion,
                    updatedData.getProfileCompletionPercentage());

            if (totalPointsAdded > 0) {
                existing.setScoreUpdatedAt(LocalDateTime.now(ZoneOffset.UTC)); // Correction #3: UTC timestamp
                // Correction #6: Remover persist() redundante
                log.infof("🎯 Profile completion milestones applied: +%d total points", totalPointsAdded);
            }
        }

        // Correction #7: Verificar milestones de telefones (NOVO - delegado para
        // MilestoneService)
        boolean phoneFieldsChanged = updatedData.getTotalPhones() != null
                || updatedData.getHasWhatsapp() != null
                || updatedData.getHasTelegram() != null;

        if (phoneFieldsChanged) {
            Integer currentTotalPhones = existing.getTotalPhones() != null ? existing.getTotalPhones() : 0;
            Boolean currentHasWhatsapp = existing.getHasWhatsapp() != null ? existing.getHasWhatsapp() : false;
            Boolean currentHasTelegram = existing.getHasTelegram() != null ? existing.getHasTelegram() : false;

            int phoneMilestonePoints = milestoneService.checkPhoneMilestones(
                    existing,
                    previousTotalPhones,
                    currentTotalPhones,
                    previousHasWhatsapp,
                    currentHasWhatsapp,
                    previousHasTelegram,
                    currentHasTelegram);

            if (phoneMilestonePoints > 0) {
                existing.setScoreUpdatedAt(LocalDateTime.now(ZoneOffset.UTC)); // Correction #3: UTC timestamp
                // Correction #6: Remover persist() redundante
                log.infof("🎯 Phone milestones applied: +%d total points", phoneMilestonePoints);
            }
        }

        log.infof("✅ Ranking updated successfully: id=%s, totalScore=%d", id, existing.getTotalScore());

        return existing;
    }

    /**
     * Remove um ranking (soft delete).
     *
     * @param id ID do ranking
     * @throws WebApplicationException (404) se ranking não encontrado
     */
    @Transactional
    public void softDelete(UUID id) {
        log.infof("🗑️ Soft deleting ranking: id=%s", id);

        UserRankingModel ranking = userRankingRepository.findByIdOptional(id)
                .filter(UserRankingModel::isActive)
                .orElseThrow(() -> {
                    log.warnf("⚠️ Ranking not found: id=%s", id);
                    return new WebApplicationException(
                            "Ranking not found",
                            Response.Status.NOT_FOUND);
                });

        ranking.softDelete();
        // Correction #6: Remover persist() redundante - JPA dirty checking automático

        log.infof("✅ Ranking soft deleted successfully: id=%s", id);
    }

    /**
     * Restaura um ranking deletado.
     *
     * @param id ID do ranking
     * @return UserRankingModel restaurado
     * @throws WebApplicationException (404) se ranking não encontrado
     */
    @Transactional
    public UserRankingModel restore(UUID id) {
        log.infof("♻️ Restoring ranking: id=%s", id);

        UserRankingModel ranking = userRankingRepository.findByIdOptional(id)
                .orElseThrow(() -> {
                    log.warnf("⚠️ Ranking not found: id=%s", id);
                    return new WebApplicationException(
                            "Ranking not found",
                            Response.Status.NOT_FOUND);
                });

        ranking.restore();
        metricsService.calculateEngagementLevel(ranking);
        metricsService.calculateConversionPotential(ranking);
        // Correction #6: Remover persist() redundante - JPA dirty checking automático

        log.infof("✅ Ranking restored successfully: id=%s", id);

        return ranking;
    }

    /**
     * Incrementa a pontuação de um usuário com proteção contra race conditions.
     *
     * @param userId ID do usuário
     * @param points Pontos a adicionar (deve ser positivo)
     * @return UserRankingModel atualizado
     * @throws WebApplicationException (404) se ranking não encontrado
     * @throws WebApplicationException (400) se pontos inválidos ou limites
     *                                 excedidos
     */
    @Transactional
    public UserRankingModel addPoints(UUID userId, int points) {
        return addPoints(userId, points, null, null, null, null);
    }

    /**
     * Incrementa a pontuação de um usuário com auditoria completa.
     *
     * REFATORAÇÃO (2026-04-05):
     * - Correction #1: Lock delegado para Repository (findByUserIdWithLock)
     * - Correction #2: Idempotência via requestId check
     * - Correction #3: Timestamps UTC
     * - Correction #4: Chamada interna com score cap (addPointsInternal)
     * - Correction #5: Default pointsReason = "SYSTEM"
     * - Correction #6: Remover persist() redundante
     *
     * @param userId       ID do usuário
     * @param points       Pontos a adicionar (deve ser positivo)
     * @param pointsReason Motivo da adição (daily_login, 7day_bonus, etc.)
     * @param ipAddress    IP do cliente (para auditoria)
     * @param userAgent    User-Agent do cliente (para auditoria)
     * @param requestId    Correlation ID (para rastreamento e idempotência)
     * @return UserRankingModel atualizado
     * @throws WebApplicationException (404) se ranking não encontrado
     * @throws WebApplicationException (400) se pontos inválidos ou limites
     *                                 excedidos
     */
    @Transactional
    public UserRankingModel addPoints(
            UUID userId,
            int points,
            String pointsReason,
            String ipAddress,
            String userAgent,
            String requestId) {

        // Correction #5: Default pointsReason to "SYSTEM" when null
        String effectiveReason = (pointsReason != null && !pointsReason.isBlank()) ? pointsReason : "SYSTEM";

        log.infof("➕ Adding %d points to user: userId=%s, reason=%s", points, userId, effectiveReason);

        // Correction #2: Idempotency check - prevent duplicate processing of same
        // requestId
        if (requestId != null && auditRepository.existsByRequestId(requestId)) {
            log.warnf("⚠️ Duplicate requestId detected, skipping points addition: %s", requestId);
            return userRankingRepository.findByUserId(userId)
                    .orElseThrow(() -> {
                        log.warnf("⚠️ Ranking not found for userId: %s", userId);
                        return new WebApplicationException(
                                "Ranking not found for user",
                                Response.Status.NOT_FOUND);
                    });
        }

        // Public API call - strict validation (no score cap)
        return addPointsInternal(userId, points, effectiveReason, ipAddress, userAgent, requestId, false);
    }

    /**
     * Método interno para adicionar pontos com controle de score cap.
     *
     * Correction #4: Implementação de score cap para chamadas internas
     * (milestones).
     *
     * Quando capAtMax=true (chamadas de milestones), se o novo score exceder
     * MAX_TOTAL_SCORE,
     * o score é limitado ao máximo permitido em vez de lançar exceção.
     *
     * Quando capAtMax=false (API pública), lança exceção se exceder o limite.
     *
     * @param userId       ID do usuário
     * @param points       Pontos a adicionar
     * @param pointsReason Motivo (já tratado como "SYSTEM" se null)
     * @param ipAddress    IP do cliente
     * @param userAgent    User-Agent do cliente
     * @param requestId    Correlation ID
     * @param capAtMax     Se true, limita score ao máximo; se false, lança exceção
     * @return UserRankingModel atualizado
     */
    private UserRankingModel addPointsInternal(
            UUID userId,
            int points,
            String pointsReason,
            String ipAddress,
            String userAgent,
            String requestId,
            boolean capAtMax) {

        // Validação 1: Pontos devem ser positivos
        if (points <= 0) {
            log.warnf("⚠️ Attempt to add non-positive points: %d", points);
            throw new WebApplicationException(
                    "Points must be positive",
                    Response.Status.BAD_REQUEST);
        }

        // Validação 2: Pontos por requisição não podem exceder o limite
        if (points > MAX_POINTS_PER_REQUEST) {
            log.warnf("⚠️ Points exceed maximum allowed per request: %d > %d", points, MAX_POINTS_PER_REQUEST);
            throw new WebApplicationException(
                    String.format("Points per request cannot exceed %d", MAX_POINTS_PER_REQUEST),
                    Response.Status.BAD_REQUEST);
        }

        // Correction #1: Buscar ranking com lock delegado para Repository
        UserRankingModel ranking = userRankingRepository
                .findByUserIdWithLock(userId, LockModeType.PESSIMISTIC_WRITE)
                .orElseThrow(() -> {
                    log.warnf("⚠️ Ranking not found for userId: %s", userId);
                    return new WebApplicationException(
                            "Ranking not found for user",
                            Response.Status.NOT_FOUND);
                });

        // Validação 3: Score total não pode exceder o limite
        int oldScore = ranking.getTotalScore();
        int newScore = oldScore + points;

        // Correction #4: Aplicar score cap se chamada interna
        if (newScore > MAX_TOTAL_SCORE) {
            if (capAtMax) {
                // Chamada interna (milestone) - limitar ao máximo
                newScore = MAX_TOTAL_SCORE;
                int effectivePoints = newScore - oldScore;
                log.warnf("⚠️ Score capped at maximum: requested=%d, applied=%d (max: %,d)",
                        points, effectivePoints, MAX_TOTAL_SCORE);
                points = effectivePoints; // Ajustar para auditoria correta
            } else {
                // Chamada pública - lançar exceção
                log.warnf("⚠️ Total score would exceed maximum: %d + %d = %d > %,d",
                        oldScore, points, newScore, MAX_TOTAL_SCORE);
                throw new WebApplicationException(
                        String.format("Total score cannot exceed %,d", MAX_TOTAL_SCORE),
                        Response.Status.BAD_REQUEST);
            }
        }

        ranking.setTotalScore(newScore);
        metricsService.calculateEngagementLevel(ranking);
        metricsService.calculateConversionPotential(ranking);
        ranking.setScoreUpdatedAt(LocalDateTime.now(ZoneOffset.UTC)); // Correction #3: UTC timestamp

        // Correction #6: Remover persist() redundante - JPA dirty checking automático

        // Registrar auditoria
        UserRankingAuditModel audit = UserRankingAuditModel.forAddPoints(
                ranking.getId(),
                userId,
                points,
                pointsReason,
                ipAddress,
                userAgent,
                requestId);
        auditRepository.persist(audit);

        log.infof("✅ Points added: userId=%s, oldScore=%d, newScore=%d, reason=%s",
                userId, oldScore, newScore, pointsReason);

        return ranking;
    }
}
