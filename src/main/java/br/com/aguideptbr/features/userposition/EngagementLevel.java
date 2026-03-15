package br.com.aguideptbr.features.userposition;

/**
 * Enum representando o nível de engajamento de um usuário.
 *
 * Derivado do totalScore em UserRankingModel:
 * - LOW: 0-25 pontos
 * - MEDIUM: 26-50 pontos
 * - HIGH: 51-75 pontos
 * - VERY_HIGH: 76-100 pontos
 *
 * USO:
 * - Segmentação de campanhas (enviar promoções para HIGH e VERY_HIGH)
 * - Dashboard admin (filtrar usuários por nível)
 * - Análise de churn (usuários LOW podem estar em risco)
 */
public enum EngagementLevel {
    /**
     * Engajamento muito baixo (0-25 pontos).
     * Usuário pouco ativo, em risco de churn.
     */
    LOW,

    /**
     * Engajamento médio (26-50 pontos).
     * Usuário ocasional, pode ser estimulado.
     */
    MEDIUM,

    /**
     * Engajamento alto (51-75 pontos).
     * Usuário frequente, bom candidato para conversão.
     */
    HIGH,

    /**
     * Engajamento muito alto (76-100 pontos).
     * Usuário super ativo, prioridade para campanhas premium.
     */
    VERY_HIGH
}
