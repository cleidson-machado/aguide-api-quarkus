package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando o status de monetização no YouTube Partner Program (YPP)
 * (CREATOR).
 *
 * VALORES:
 * - MONETIZED: Canal já monetizado
 * - NOT_MONETIZED: Canal não monetizado
 * - IN_PROGRESS: Processo de aprovação em andamento
 *
 * USO:
 * - Identificar criadores profissionalizados (MONETIZED)
 * - Segmentar ofertas (criadores monetizados = maior seriedade)
 * - Priorizar criadores com monetização para parcerias comerciais
 */
public enum MonetizationStatus {
    /**
     * Canal já monetizado pelo YouTube.
     * Criador profissional, gera receita do conteúdo.
     */
    MONETIZED,

    /**
     * Canal não monetizado.
     * Criador hobby ou ainda não atingiu requisitos do YPP.
     */
    NOT_MONETIZED,

    /**
     * Aprovação de monetização em andamento.
     * Criador em transição para profissionalização.
     */
    IN_PROGRESS
}
