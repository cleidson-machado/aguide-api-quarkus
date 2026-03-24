package br.com.aguideptbr.features.userposition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.userposition.enuns.ConversionPotential;
import br.com.aguideptbr.features.userposition.enuns.EngagementLevel;
import jakarta.enterprise.context.ApplicationScoped;
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

    public UserRankingService(Logger log, UserRankingRepository userRankingRepository) {
        this.log = log;
        this.userRankingRepository = userRankingRepository;
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
        if (updatedData.getTotalContentViews() != null) {
            existing.setTotalContentViews(updatedData.getTotalContentViews());
        }
        if (updatedData.getUniqueContentViews() != null) {
            existing.setUniqueContentViews(updatedData.getUniqueContentViews());
        }
        if (updatedData.getAvgDailyUsageMinutes() != null) {
            existing.setAvgDailyUsageMinutes(updatedData.getAvgDailyUsageMinutes());
        }
        if (updatedData.getConsecutiveDaysStreak() != null) {
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
            existing.setLastActivityAt(updatedData.getLastActivityAt());
        }
        if (updatedData.getLastContentViewAt() != null) {
            existing.setLastContentViewAt(updatedData.getLastContentViewAt());
        }
        if (updatedData.getLastMessageSentAt() != null) {
            existing.setLastMessageSentAt(updatedData.getLastMessageSentAt());
        }
        if (updatedData.getLastLoginAt() != null) {
            existing.setLastLoginAt(updatedData.getLastLoginAt());
        }
        if (updatedData.getFavoriteCategory() != null) {
            existing.setFavoriteCategory(updatedData.getFavoriteCategory());
        }
        if (updatedData.getFavoriteContentType() != null) {
            existing.setFavoriteContentType(updatedData.getFavoriteContentType());
        }
        if (updatedData.getPreferredUsageTime() != null) {
            existing.setPreferredUsageTime(updatedData.getPreferredUsageTime());
        }

        // Recalcular métricas
        calculateEngagementLevel(existing);
        calculateConversionPotential(existing);
        existing.setScoreUpdatedAt(LocalDateTime.now());

        userRankingRepository.persist(existing);

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
        if (ranking.getTotalScore() >= 1000 || ranking.getConsecutiveDaysStreak() >= 30) {
            ranking.setEngagementLevel(EngagementLevel.VERY_HIGH);
        } else if (ranking.getTotalScore() >= 500 || ranking.getConsecutiveDaysStreak() >= 14) {
            ranking.setEngagementLevel(EngagementLevel.HIGH);
        } else if (ranking.getTotalScore() >= 100 || ranking.getTotalContentViews() >= 50) {
            ranking.setEngagementLevel(EngagementLevel.MEDIUM);
        } else {
            ranking.setEngagementLevel(EngagementLevel.LOW);
        }

        log.debugf("Calculated engagement level: %s (score=%d, streak=%d)",
                ranking.getEngagementLevel(), ranking.getTotalScore(), ranking.getConsecutiveDaysStreak());
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
     * Incrementa a pontuação de um usuário.
     *
     * @param userId ID do usuário
     * @param points Pontos a adicionar
     * @return UserRankingModel atualizado
     * @throws WebApplicationException (404) se ranking não encontrado
     */
    @Transactional
    public UserRankingModel addPoints(UUID userId, int points) {
        log.infof("➕ Adding %d points to user: userId=%s", points, userId);

        UserRankingModel ranking = userRankingRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warnf("⚠️ Ranking not found for userId: %s", userId);
                    return new WebApplicationException(
                            "Ranking not found for user",
                            Response.Status.NOT_FOUND);
                });

        ranking.setTotalScore(ranking.getTotalScore() + points);
        calculateEngagementLevel(ranking);
        calculateConversionPotential(ranking);
        ranking.setScoreUpdatedAt(LocalDateTime.now());

        userRankingRepository.persist(ranking);

        log.infof("✅ Points added: userId=%s, newScore=%d", userId, ranking.getTotalScore());

        return ranking;
    }
}
