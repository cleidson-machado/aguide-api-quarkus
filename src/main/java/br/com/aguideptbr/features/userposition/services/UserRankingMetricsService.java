package br.com.aguideptbr.features.userposition.services;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.userposition.UserRankingModel;
import br.com.aguideptbr.features.userposition.UserRankingService;
import br.com.aguideptbr.features.userposition.enuns.ConversionPotential;
import br.com.aguideptbr.features.userposition.enuns.EngagementLevel;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service responsável por cálculos de métricas de engajamento e conversão.
 *
 * Extração de SRP (Single Responsibility Principle) do UserRankingService.
 * Centraliza regras de negócio para classificação de usuários por engagement e
 * conversion potential.
 *
 * Regras de Engajamento:
 * - VERY_HIGH: score >= 1000 OU streak >= 30 dias
 * - HIGH: score >= 500 OU streak >= 14 dias
 * - MEDIUM: score >= 100 OU 50+ visualizações
 * - LOW: demais casos
 *
 * Regras de Conversão:
 * - VERY_HIGH: tem contatos E engajamento VERY_HIGH/HIGH
 * - HIGH: tem contatos E engajamento MEDIUM
 * - MEDIUM: tem contatos OU engajamento >= MEDIUM
 * - LOW: engajamento LOW sem contatos
 * - VERY_LOW: sem atividade significativa
 *
 * @see UserRankingService
 * @see EngagementLevel
 * @see ConversionPotential
 * @author Refactored following SOLID principles
 */
@ApplicationScoped
public class UserRankingMetricsService {

    private final Logger log;

    public UserRankingMetricsService(Logger log) {
        this.log = log;
    }

    /**
     * Calcula o nível de engajamento baseado em métricas.
     *
     * Regras de negócio para classificação:
     * - VERY_HIGH: score >= 1000 OU streak >= 30 dias
     * - HIGH: score >= 500 OU streak >= 14 dias
     * - MEDIUM: score >= 100 OU 50+ visualizações
     * - LOW: demais casos
     *
     * @param ranking Modelo de ranking a ser atualizado (mutável)
     */
    public void calculateEngagementLevel(UserRankingModel ranking) {
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
     *
     * Regras de negócio para classificação:
     * - VERY_HIGH: tem contatos E engajamento VERY_HIGH/HIGH
     * - HIGH: tem contatos E engajamento MEDIUM
     * - MEDIUM: tem contatos OU engajamento >= MEDIUM
     * - LOW: engajamento LOW sem contatos
     * - VERY_LOW: sem atividade significativa
     *
     * IMPORTANTE: Deve ser chamado APÓS calculateEngagementLevel() pois depende do
     * engagementLevel já calculado.
     *
     * @param ranking Modelo de ranking a ser atualizado (mutável)
     */
    public void calculateConversionPotential(UserRankingModel ranking) {
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
}
