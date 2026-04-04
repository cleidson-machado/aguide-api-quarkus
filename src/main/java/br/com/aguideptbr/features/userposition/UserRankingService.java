package br.com.aguideptbr.features.userposition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.userposition.enuns.ConversionPotential;
import br.com.aguideptbr.features.userposition.enuns.EngagementLevel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Service para lógica de negócio relacionada a rankings de usuários.
 * Gerencia criação, atualização, busca e remoção de dados de ranking.
 *
 * Responsabilidades:
 * - Calcular níveis de engajamento e potencial de conversão
 * - Atualizar pontuações e métricas de usuários
 * - Validar regras de negócio específicas de ranking
 *
 * @see UserRankingModel
 * @see UserRankingRepository
 */
@ApplicationScoped
public class UserRankingService {

    private final Logger log;
    private final UserRankingRepository userRankingRepository;
    private final UserRankingAuditRepository auditRepository;

    // Constantes de validação
    private static final int MAX_TOTAL_SCORE = 9_999_999;
    private static final int MAX_STREAK_DAYS = 9999; // ~27 anos
    private static final int MAX_POINTS_PER_REQUEST = 1000;
    private static final long TIMESTAMP_TOLERANCE_MINUTES = 5; // 5 minutos no futuro
    private static final long TIMESTAMP_MAX_PAST_HOURS = 24; // 24 horas no passado
    private static final int MAX_DAILY_USAGE_MINUTES = 1440; // 24 horas * 60 minutos
    private static final int MAX_PROFILE_COMPLETION_PERCENTAGE = 100;
    private static final int MIN_PROFILE_COMPLETION_PERCENTAGE = 0;

    // Enum de tipos de conteúdo permitidos (Phase B)
    private static final Set<String> VALID_CONTENT_TYPES = Set.of(
            "video", "article", "course", "tutorial", "guide");

    public UserRankingService(
            Logger log,
            UserRankingRepository userRankingRepository,
            UserRankingAuditRepository auditRepository) {
        this.log = log;
        this.userRankingRepository = userRankingRepository;
        this.auditRepository = auditRepository;
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

        // Calcular métricas iniciais
        calculateEngagementLevel(userRanking);
        calculateConversionPotential(userRanking);
        userRanking.setScoreUpdatedAt(LocalDateTime.now());

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

        // Atualizar campos
        if (updatedData.getTotalScore() != null) {
            existing.setTotalScore(updatedData.getTotalScore());
        }

        // Capturar valor anterior de totalContentViews para detecção de milestones
        Long previousContentViews = existing.getTotalContentViews();

        if (updatedData.getTotalContentViews() != null) {
            validateNonNegative(updatedData.getTotalContentViews(), "totalContentViews");
            existing.setTotalContentViews(updatedData.getTotalContentViews());
        }
        if (updatedData.getUniqueContentViews() != null) {
            validateNonNegative(updatedData.getUniqueContentViews(), "uniqueContentViews");
            existing.setUniqueContentViews(updatedData.getUniqueContentViews());
        }
        if (updatedData.getAvgDailyUsageMinutes() != null) {
            validateDailyUsageMinutes(updatedData.getAvgDailyUsageMinutes());
            existing.setAvgDailyUsageMinutes(updatedData.getAvgDailyUsageMinutes());
        }
        if (updatedData.getConsecutiveDaysStreak() != null) {
            validateConsecutiveDaysStreak(updatedData.getConsecutiveDaysStreak());
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
            validateTimestamp(updatedData.getLastActivityAt(), "lastActivityAt");
            existing.setLastActivityAt(updatedData.getLastActivityAt());
        }
        if (updatedData.getLastContentViewAt() != null) {
            validateTimestamp(updatedData.getLastContentViewAt(), "lastContentViewAt");
            existing.setLastContentViewAt(updatedData.getLastContentViewAt());
        }
        if (updatedData.getLastMessageSentAt() != null) {
            validateTimestamp(updatedData.getLastMessageSentAt(), "lastMessageSentAt");
            existing.setLastMessageSentAt(updatedData.getLastMessageSentAt());
        }
        if (updatedData.getLastLoginAt() != null) {
            validateTimestamp(updatedData.getLastLoginAt(), "lastLoginAt");
            existing.setLastLoginAt(updatedData.getLastLoginAt());
        }
        if (updatedData.getFavoriteCategory() != null) {
            validateStringLength(updatedData.getFavoriteCategory(), 100, "favoriteCategory");
            existing.setFavoriteCategory(updatedData.getFavoriteCategory());
        }
        if (updatedData.getFavoriteContentType() != null) {
            validateStringLength(updatedData.getFavoriteContentType(), 50, "favoriteContentType");
            validateContentType(updatedData.getFavoriteContentType());
            existing.setFavoriteContentType(updatedData.getFavoriteContentType());
        }
        if (updatedData.getPreferredUsageTime() != null) {
            existing.setPreferredUsageTime(updatedData.getPreferredUsageTime());
        }
        if (updatedData.getProfileCompletionPercentage() != null) {
            validateProfileCompletionPercentage(updatedData.getProfileCompletionPercentage());
            existing.setProfileCompletionPercentage(updatedData.getProfileCompletionPercentage());
        }

        // Recalcular métricas
        calculateEngagementLevel(existing);
        calculateConversionPotential(existing);
        existing.setScoreUpdatedAt(LocalDateTime.now());

        userRankingRepository.persist(existing);

        // Verificar milestones de visualização de conteúdo (após persistência para
        // evitar duplicação)
        if (updatedData.getTotalContentViews() != null
                && !updatedData.getTotalContentViews().equals(previousContentViews)) {
            checkContentViewsMilestones(existing.getUserId(), previousContentViews, existing.getTotalContentViews());
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
        userRankingRepository.persist(ranking);

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
        calculateEngagementLevel(ranking);
        calculateConversionPotential(ranking);
        userRankingRepository.persist(ranking);

        log.infof("✅ Ranking restored successfully: id=%s", id);

        return ranking;
    }

    /**
     * Calcula o nível de engajamento baseado em métricas.
     * Regras de negócio para classificação:
     * - VERY_HIGH: score >= 1000 OU streak >= 30 dias
     * - HIGH: score >= 500 OU streak >= 14 dias
     * - MEDIUM: score >= 100 OU 50+ visualizações
     * - LOW: demais casos
     */
    private void calculateEngagementLevel(UserRankingModel ranking) {
        // Garantir valores não-nulos para evitar NullPointerException
        int totalScore = ranking.getTotalScore() != null ? ranking.getTotalScore() : 0;
        int consecutiveDaysStreak = ranking.getConsecutiveDaysStreak() != null ? ranking.getConsecutiveDaysStreak() : 0;
        long totalContentViews = ranking.getTotalContentViews() != null ? ranking.getTotalContentViews() : 0L;

        if (totalScore >= 1000 || consecutiveDaysStreak >= 30) {
            ranking.setEngagementLevel(EngagementLevel.VERY_HIGH);
        } else if (totalScore >= 500 || consecutiveDaysStreak >= 14) {
            ranking.setEngagementLevel(EngagementLevel.HIGH);
        } else if (totalScore >= 100 || totalContentViews >= 50) {
            ranking.setEngagementLevel(EngagementLevel.MEDIUM);
        } else {
            ranking.setEngagementLevel(EngagementLevel.LOW);
        }

        log.debugf("Calculated engagement level: %s (score=%d, streak=%d, views=%d)",
                ranking.getEngagementLevel(), totalScore, consecutiveDaysStreak, totalContentViews);
    }

    /**
     * Calcula o potencial de conversão baseado em métricas.
     * Regras de negócio para classificação:
     * - VERY_HIGH: tem contatos E engajamento VERY_HIGH/HIGH
     * - HIGH: tem contatos E engajamento MEDIUM
     * - MEDIUM: tem contatos OU engajamento >= MEDIUM
     * - LOW: engajamento LOW sem contatos
     * - VERY_LOW: sem atividade significativa
     */
    private void calculateConversionPotential(UserRankingModel ranking) {
        boolean hasContacts = ranking.getHasWhatsapp() || ranking.getHasTelegram() || ranking.getTotalPhones() > 0;
        boolean hasHighEngagement = ranking.getEngagementLevel() == EngagementLevel.VERY_HIGH
                || ranking.getEngagementLevel() == EngagementLevel.HIGH;
        boolean hasMediumEngagement = ranking.getEngagementLevel() == EngagementLevel.MEDIUM;

        if (hasContacts && hasHighEngagement) {
            ranking.setConversionPotential(ConversionPotential.VERY_HIGH);
        } else if (hasContacts && hasMediumEngagement) {
            ranking.setConversionPotential(ConversionPotential.HIGH);
        } else if (hasContacts || hasMediumEngagement) {
            ranking.setConversionPotential(ConversionPotential.MEDIUM);
        } else if (ranking.getEngagementLevel() == EngagementLevel.LOW && ranking.getTotalScore() > 0) {
            ranking.setConversionPotential(ConversionPotential.LOW);
        } else {
            ranking.setConversionPotential(ConversionPotential.VERY_LOW);
        }

        log.debugf("Calculated conversion potential: %s (hasContacts=%b, engagement=%s)",
                ranking.getConversionPotential(), hasContacts, ranking.getEngagementLevel());
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
     * @param userId       ID do usuário
     * @param points       Pontos a adicionar (deve ser positivo)
     * @param pointsReason Motivo da adição (daily_login, 7day_bonus, etc.)
     * @param ipAddress    IP do cliente (para auditoria)
     * @param userAgent    User-Agent do cliente (para auditoria)
     * @param requestId    Correlation ID (para rastreamento)
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
        log.infof("➕ Adding %d points to user: userId=%s, reason=%s", points, userId, pointsReason);

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

        // Buscar ranking com PESSIMISTIC_WRITE lock (previne race conditions)
        UserRankingModel ranking = userRankingRepository.getEntityManager()
                .createQuery("SELECT r FROM UserRankingModel r WHERE r.userId = :userId AND r.deletedAt IS NULL",
                        UserRankingModel.class)
                .setParameter("userId", userId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> {
                    log.warnf("⚠️ Ranking not found for userId: %s", userId);
                    return new WebApplicationException(
                            "Ranking not found for user",
                            Response.Status.NOT_FOUND);
                });

        // Validação 3: Score total não pode exceder o limite
        int oldScore = ranking.getTotalScore();
        int newScore = oldScore + points;
        if (newScore > MAX_TOTAL_SCORE) {
            log.warnf("⚠️ Total score would exceed maximum: %d + %d = %d > %d",
                    oldScore, points, newScore, MAX_TOTAL_SCORE);
            throw new WebApplicationException(
                    String.format("Total score cannot exceed %,d", MAX_TOTAL_SCORE),
                    Response.Status.BAD_REQUEST);
        }

        ranking.setTotalScore(newScore);
        calculateEngagementLevel(ranking);
        calculateConversionPotential(ranking);
        ranking.setScoreUpdatedAt(LocalDateTime.now());

        userRankingRepository.persist(ranking);

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

    /**
     * Valida timestamps para garantir que estão dentro de limites razoáveis.
     *
     * @param timestamp Timestamp a validar
     * @param fieldName Nome do campo (para mensagem de erro)
     * @throws WebApplicationException (400) se timestamp inválido
     */
    private void validateTimestamp(LocalDateTime timestamp, String fieldName) {
        if (timestamp == null) {
            return; // Null é permitido (campo opcional)
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxFuture = now.plusMinutes(TIMESTAMP_TOLERANCE_MINUTES);
        LocalDateTime minPast = now.minusHours(TIMESTAMP_MAX_PAST_HOURS);

        if (timestamp.isAfter(maxFuture)) {
            log.warnf("⚠️ Timestamp is too far in the future: %s = %s (max: %s)",
                    fieldName, timestamp, maxFuture);
            throw new WebApplicationException(
                    String.format("%s cannot be in the future (received: %s)", fieldName, timestamp),
                    Response.Status.BAD_REQUEST);
        }

        if (timestamp.isBefore(minPast)) {
            log.warnf("⚠️ Timestamp is too far in the past: %s = %s (min: %s)",
                    fieldName, timestamp, minPast);
            throw new WebApplicationException(
                    String.format("%s cannot be more than %d hours in the past",
                            fieldName, TIMESTAMP_MAX_PAST_HOURS),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida consecutiveDaysStreak para garantir que está dentro de limites
     * razoáveis.
     *
     * @param streak Streak a validar
     * @throws WebApplicationException (400) se streak inválido
     */
    private void validateConsecutiveDaysStreak(Integer streak) {
        if (streak == null) {
            return; // Null é tratado como 0
        }

        if (streak < 0) {
            log.warnf("⚠️ Negative streak not allowed: %d", streak);
            throw new WebApplicationException(
                    "Consecutive days streak cannot be negative",
                    Response.Status.BAD_REQUEST);
        }

        if (streak > MAX_STREAK_DAYS) {
            log.warnf("⚠️ Streak exceeds maximum allowed: %d > %d", streak, MAX_STREAK_DAYS);
            throw new WebApplicationException(
                    String.format("Consecutive days streak cannot exceed %d days", MAX_STREAK_DAYS),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida valores não-negativos (totalContentViews, uniqueContentViews, etc.).
     *
     * @param value     Valor a validar
     * @param fieldName Nome do campo (para mensagem de erro)
     * @throws WebApplicationException (400) se valor for negativo
     */
    private void validateNonNegative(Long value, String fieldName) {
        if (value == null) {
            return; // Null é permitido
        }

        if (value < 0) {
            log.warnf("⚠️ Negative value not allowed for %s: %d", fieldName, value);
            throw new WebApplicationException(
                    String.format("%s cannot be negative", fieldName),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida avgDailyUsageMinutes (0 a 1440 minutos).
     *
     * @param minutes Minutos de uso diário
     * @throws WebApplicationException (400) se valor inválido
     */
    private void validateDailyUsageMinutes(Integer minutes) {
        if (minutes == null) {
            return; // Null é permitido
        }

        if (minutes < 0) {
            log.warnf("⚠️ Negative usage minutes not allowed: %d", minutes);
            throw new WebApplicationException(
                    "Average daily usage minutes cannot be negative",
                    Response.Status.BAD_REQUEST);
        }

        if (minutes > MAX_DAILY_USAGE_MINUTES) {
            log.warnf("⚠️ Usage minutes exceed 24 hours: %d > %d", minutes, MAX_DAILY_USAGE_MINUTES);
            throw new WebApplicationException(
                    String.format("Average daily usage minutes cannot exceed %d (24 hours)", MAX_DAILY_USAGE_MINUTES),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida profileCompletionPercentage (0 a 100).
     *
     * @param percentage Percentual de conclusão do perfil
     * @throws WebApplicationException (400) se valor inválido
     */
    private void validateProfileCompletionPercentage(Integer percentage) {
        if (percentage == null) {
            return; // Null é permitido
        }

        if (percentage < MIN_PROFILE_COMPLETION_PERCENTAGE || percentage > MAX_PROFILE_COMPLETION_PERCENTAGE) {
            log.warnf("⚠️ Profile completion percentage out of range: %d (allowed: %d-%d)",
                    percentage, MIN_PROFILE_COMPLETION_PERCENTAGE, MAX_PROFILE_COMPLETION_PERCENTAGE);
            throw new WebApplicationException(
                    String.format("Profile completion percentage must be between %d and %d",
                            MIN_PROFILE_COMPLETION_PERCENTAGE, MAX_PROFILE_COMPLETION_PERCENTAGE),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida comprimento de strings.
     *
     * @param value     String a validar
     * @param maxLength Comprimento máximo permitido
     * @param fieldName Nome do campo (para mensagem de erro)
     * @throws WebApplicationException (400) se string exceder o limite
     */
    private void validateStringLength(String value, int maxLength, String fieldName) {
        if (value == null) {
            return; // Null é permitido
        }

        if (value.length() > maxLength) {
            log.warnf("⚠️ String length exceeds maximum for %s: %d > %d", fieldName, value.length(), maxLength);
            throw new WebApplicationException(
                    String.format("%s must not exceed %d characters", fieldName, maxLength),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida se o tipo de conteúdo favorito é um dos valores permitidos.
     *
     * PHASE B: Frontend solicitou validação enum para favoriteContentType.
     * Valores válidos: video, article, course, tutorial, guide
     *
     * @param contentType Tipo de conteúdo a validar
     * @throws WebApplicationException (400) se tipo inválido
     */
    private void validateContentType(String contentType) {
        if (contentType == null) {
            return; // Null é permitido
        }

        String normalizedType = contentType.toLowerCase().trim();
        if (!VALID_CONTENT_TYPES.contains(normalizedType)) {
            log.warnf("⚠️ Invalid content type: %s (allowed: %s)", contentType, VALID_CONTENT_TYPES);
            throw new WebApplicationException(
                    String.format("favoriteContentType must be one of: %s", String.join(", ", VALID_CONTENT_TYPES)),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Verifica se o usuário atingiu milestones de visualização de conteúdo
     * e adiciona pontos automaticamente quando thresholds são cruzados.
     *
     * Milestones:
     * - 10 visualizações: +2 pontos
     * - 50 visualizações: +10 pontos
     * - 100 visualizações: +25 pontos
     *
     * @param userId        ID do usuário
     * @param previousViews Quantidade anterior de visualizações
     * @param currentViews  Quantidade atual de visualizações
     */
    private void checkContentViewsMilestones(UUID userId, Long previousViews, Long currentViews) {
        if (previousViews == null) {
            previousViews = 0L;
        }
        if (currentViews == null) {
            currentViews = 0L;
        }

        log.debugf("📊 Checking content views milestones: userId=%s, previous=%d, current=%d",
                userId, previousViews, currentViews);

        // Milestone: 10 visualizações
        if (previousViews < 10 && currentViews >= 10) {
            log.infof("🎯 Milestone reached: 10 content views for userId=%s", userId);
            addPoints(userId, 2, "CONTENT_VIEWS_10", null, null, null);
        }

        // Milestone: 50 visualizações
        if (previousViews < 50 && currentViews >= 50) {
            log.infof("🎯 Milestone reached: 50 content views for userId=%s", userId);
            addPoints(userId, 10, "CONTENT_VIEWS_50", null, null, null);
        }

        // Milestone: 100 visualizações
        if (previousViews < 100 && currentViews >= 100) {
            log.infof("🎯 Milestone reached: 100 content views for userId=%s", userId);
            addPoints(userId, 25, "CONTENT_VIEWS_100", null, null, null);
        }
    }
}
