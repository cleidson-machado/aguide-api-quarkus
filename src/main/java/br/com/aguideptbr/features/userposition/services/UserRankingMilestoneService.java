package br.com.aguideptbr.features.userposition.services;

import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.userposition.UserRankingAuditModel;
import br.com.aguideptbr.features.userposition.UserRankingAuditRepository;
import br.com.aguideptbr.features.userposition.UserRankingModel;
import br.com.aguideptbr.features.userposition.UserRankingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Service responsável por detectar e premiar milestones de usuários.
 *
 * Extração de SRP (Single Responsibility Principle) e OCP (Open/Closed
 * Principle)
 * do UserRankingService. Centraliza lógica de thresholds para facilitar adição
 * de novos milestones sem modificar o core service.
 *
 * Milestones implementados:
 * - Visualizações de conteúdo: 10, 50, 100
 * - Completude de perfil: 50%, 100%
 * - Telefones: primeiro cadastrado, WhatsApp, Telegram, 3+ telefones
 *
 * IMPORTANTE: Este service tem dependência circular com UserRankingService
 * (necessário para chamar addPoints()). Field injection é usada aqui como
 * exceção ao padrão de constructor injection devido à limitação do CDI.
 *
 * @see UserRankingService
 * @see UserRankingAuditRepository
 * @author Refactored following SOLID principles
 */
@ApplicationScoped
public class UserRankingMilestoneService {

    private final Logger log;
    private final UserRankingAuditRepository auditRepository;

    // NOTA: Field injection é usada APENAS para UserRankingService devido a
    // dependência circular.
    // RESTEasy/CDI requer field injection neste caso específico.
    @Inject
    @SuppressWarnings("java:S6813") // Field injection required for circular dependency with UserRankingService
    UserRankingService userRankingService;

    public UserRankingMilestoneService(Logger log, UserRankingAuditRepository auditRepository) {
        this.log = log;
        this.auditRepository = auditRepository;
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
    public void checkContentViewsMilestones(UUID userId, Long previousViews, Long currentViews) {
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
            userRankingService.addPoints(userId, 2, "CONTENT_VIEWS_10", null, null, null);
        }

        // Milestone: 50 visualizações
        if (previousViews < 50 && currentViews >= 50) {
            log.infof("🎯 Milestone reached: 50 content views for userId=%s", userId);
            userRankingService.addPoints(userId, 10, "CONTENT_VIEWS_50", null, null, null);
        }

        // Milestone: 100 visualizações
        if (previousViews < 100 && currentViews >= 100) {
            log.infof("🎯 Milestone reached: 100 content views for userId=%s", userId);
            userRankingService.addPoints(userId, 25, "CONTENT_VIEWS_100", null, null, null);
        }
    }

    /**
     * Verifica se o usuário atingiu milestones de completude de perfil
     * e adiciona pontos diretamente ao ranking (sem chamar addPoints para evitar
     * recursão).
     *
     * Milestones (Phase B):
     * - 50% de completude: +3 pontos
     * - 100% de completude: +10 pontos
     *
     * IMPORTANTE: Pontos são adicionados apenas em transições ASCENDENTES
     * (old < threshold AND new >= threshold). Se o percentual diminuir,
     * nenhum ponto é adicionado.
     *
     * DESIGN: Este método adiciona pontos diretamente ao ranking e cria auditoria
     * manual
     * para evitar chamadas a addPoints() dentro de update() (performance e
     * simplicidade).
     *
     * @param ranking            Modelo de ranking a ser atualizado
     * @param previousPercentage Percentual anterior de completude (0-100)
     * @param currentPercentage  Percentual atual de completude (0-100)
     * @return Total de pontos adicionados (0, 3, 10 ou 13)
     */
    public int checkProfileCompletionMilestones(
            UserRankingModel ranking,
            Integer previousPercentage,
            Integer currentPercentage) {
        if (previousPercentage == null) {
            previousPercentage = 0;
        }
        if (currentPercentage == null) {
            currentPercentage = 0;
        }

        log.debugf("📊 Checking profile completion milestones: userId=%s, previous=%d%%, current=%d%%",
                ranking.getUserId(), previousPercentage, currentPercentage);

        int totalPointsAdded = 0;

        // Milestone: 50% de completude → +3 pontos
        if (previousPercentage < 50 && currentPercentage >= 50) {
            ranking.setTotalScore(ranking.getTotalScore() + 3);
            totalPointsAdded += 3;

            // Criar auditoria específica para milestone 50%
            UserRankingAuditModel audit50 = UserRankingAuditModel.forAddPoints(
                    ranking.getId(),
                    ranking.getUserId(),
                    3,
                    "PROFILE_50_PERCENT",
                    null, null, null);
            auditRepository.persist(audit50);

            log.infof("🎯 Milestone reached: 50%% profile completion (+3 points, totalScore: %d)",
                    ranking.getTotalScore());
        }

        // Milestone: 100% de completude → +10 pontos
        if (previousPercentage < 100 && currentPercentage >= 100) {
            ranking.setTotalScore(ranking.getTotalScore() + 10);
            totalPointsAdded += 10;

            // Criar auditoria específica para milestone 100%
            UserRankingAuditModel audit100 = UserRankingAuditModel.forAddPoints(
                    ranking.getId(),
                    ranking.getUserId(),
                    10,
                    "PROFILE_100_PERCENT",
                    null, null, null);
            auditRepository.persist(audit100);

            log.infof("🎯 Milestone reached: 100%% profile completion (+10 points, totalScore: %d)",
                    ranking.getTotalScore());
        }

        // Detectar inconsistências (opcional, apenas para debug)
        if (currentPercentage < previousPercentage) {
            log.warnf("⚠️ Profile completion percentage decreased: %d%% → %d%% (no points removed)",
                    previousPercentage, currentPercentage);
        }

        return totalPointsAdded;
    }

    /**
     * Verifica se o usuário atingiu milestones relacionados a telefones/contatos
     * e adiciona pontos diretamente ao ranking (sem chamar addPoints para evitar
     * recursão).
     *
     * NOVA IMPLEMENTAÇÃO (Correção #7 do plano de refatoração):
     *
     * Milestones:
     * - Primeiro telefone cadastrado (previous=0 → current>0): +5 pontos
     * - WhatsApp verificado (previous=false → current=true): +3 pontos
     * - Telegram verificado (previous=false → current=true): +3 pontos
     * - 3 ou mais telefones (previous<3 → current>=3): +10 pontos
     *
     * IMPORTANTE: Pontos são adicionados apenas em transições ASCENDENTES.
     * Se valores diminuírem (ex: telefone removido), nenhum ponto é adicionado ou
     * removido.
     *
     * DESIGN: Similar a checkProfileCompletionMilestones(), adiciona pontos
     * diretamente
     * ao ranking para evitar chamadas recursivas a addPoints() dentro de update().
     *
     * @param ranking             Modelo de ranking a ser atualizado
     * @param previousTotalPhones Quantidade anterior de telefones cadastrados
     * @param currentTotalPhones  Quantidade atual de telefones cadastrados
     * @param previousHasWhatsapp Estado anterior de WhatsApp verificado
     * @param currentHasWhatsapp  Estado atual de WhatsApp verificado
     * @param previousHasTelegram Estado anterior de Telegram verificado
     * @param currentHasTelegram  Estado atual de Telegram verificado
     * @return Total de pontos adicionados (0 a 21)
     */
    public int checkPhoneMilestones(
            UserRankingModel ranking,
            Integer previousTotalPhones,
            Integer currentTotalPhones,
            Boolean previousHasWhatsapp,
            Boolean currentHasWhatsapp,
            Boolean previousHasTelegram,
            Boolean currentHasTelegram) {

        // Garantir valores não-nulos
        if (previousTotalPhones == null) {
            previousTotalPhones = 0;
        }
        if (currentTotalPhones == null) {
            currentTotalPhones = 0;
        }
        if (previousHasWhatsapp == null) {
            previousHasWhatsapp = false;
        }
        if (currentHasWhatsapp == null) {
            currentHasWhatsapp = false;
        }
        if (previousHasTelegram == null) {
            previousHasTelegram = false;
        }
        if (currentHasTelegram == null) {
            currentHasTelegram = false;
        }

        log.debugf("📊 Checking phone milestones: userId=%s, phones=%d→%d, whatsapp=%b→%b, telegram=%b→%b",
                ranking.getUserId(), previousTotalPhones, currentTotalPhones,
                previousHasWhatsapp, currentHasWhatsapp,
                previousHasTelegram, currentHasTelegram);

        int totalPointsAdded = 0;

        // Milestone: Primeiro telefone cadastrado → +5 pontos
        if (previousTotalPhones == 0 && currentTotalPhones > 0) {
            ranking.setTotalScore(ranking.getTotalScore() + 5);
            totalPointsAdded += 5;

            UserRankingAuditModel audit = UserRankingAuditModel.forAddPoints(
                    ranking.getId(),
                    ranking.getUserId(),
                    5,
                    "FIRST_PHONE_ADDED",
                    null, null, null);
            auditRepository.persist(audit);

            log.infof("🎯 Milestone reached: First phone added (+5 points, totalScore: %d)",
                    ranking.getTotalScore());
        }

        // Milestone: WhatsApp verificado → +3 pontos
        if (!previousHasWhatsapp && currentHasWhatsapp) {
            ranking.setTotalScore(ranking.getTotalScore() + 3);
            totalPointsAdded += 3;

            UserRankingAuditModel audit = UserRankingAuditModel.forAddPoints(
                    ranking.getId(),
                    ranking.getUserId(),
                    3,
                    "WHATSAPP_VERIFIED",
                    null, null, null);
            auditRepository.persist(audit);

            log.infof("🎯 Milestone reached: WhatsApp verified (+3 points, totalScore: %d)",
                    ranking.getTotalScore());
        }

        // Milestone: Telegram verificado → +3 pontos
        if (!previousHasTelegram && currentHasTelegram) {
            ranking.setTotalScore(ranking.getTotalScore() + 3);
            totalPointsAdded += 3;

            UserRankingAuditModel audit = UserRankingAuditModel.forAddPoints(
                    ranking.getId(),
                    ranking.getUserId(),
                    3,
                    "TELEGRAM_VERIFIED",
                    null, null, null);
            auditRepository.persist(audit);

            log.infof("🎯 Milestone reached: Telegram verified (+3 points, totalScore: %d)",
                    ranking.getTotalScore());
        }

        // Milestone: 3 ou mais telefones → +10 pontos
        if (previousTotalPhones < 3 && currentTotalPhones >= 3) {
            ranking.setTotalScore(ranking.getTotalScore() + 10);
            totalPointsAdded += 10;

            UserRankingAuditModel audit = UserRankingAuditModel.forAddPoints(
                    ranking.getId(),
                    ranking.getUserId(),
                    10,
                    "MULTIPLE_PHONES_BONUS",
                    null, null, null);
            auditRepository.persist(audit);

            log.infof("🎯 Milestone reached: 3+ phones registered (+10 points, totalScore: %d)",
                    ranking.getTotalScore());
        }

        // Detectar remoções (opcional, apenas para debug)
        if (currentTotalPhones < previousTotalPhones) {
            log.warnf("⚠️ Phone count decreased: %d → %d (no points removed)",
                    previousTotalPhones, currentTotalPhones);
        }

        return totalPointsAdded;
    }
}
