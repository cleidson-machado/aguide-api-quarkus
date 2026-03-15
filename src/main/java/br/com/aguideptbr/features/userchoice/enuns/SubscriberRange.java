package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando a faixa de inscritos do canal do YouTube (CREATOR).
 *
 * VALORES:
 * - LESS_THAN_1K: Menos de 1.000 inscritos
 * - ONE_K_TO_10K: 1.000 a 10.000 inscritos
 * - TEN_K_TO_100K: 10.000 a 100.000 inscritos
 * - MORE_THAN_100K: Mais de 100.000 inscritos
 *
 * USO:
 * - Classificar influência e alcance do criador
 * - Segmentar criadores por tamanho de audiência
 * - Priorizar micro-influencers (1K-10K) para parcerias nichadas
 * - Identificar criadores de alto impacto (> 100K)
 */
public enum SubscriberRange {
    /**
     * Canal micro (menos de 1.000 inscritos).
     * Criador iniciante, audiência em construção.
     */
    LESS_THAN_1K,

    /**
     * Canal pequeno (1.000 a 10.000 inscritos).
     * Criador com audiência inicial estabelecida, alto engajamento.
     */
    ONE_K_TO_10K,

    /**
     * Canal médio (10.000 a 100.000 inscritos).
     * Criador com audiência consolidada e influência regional.
     */
    TEN_K_TO_100K,

    /**
     * Canal grande (mais de 100.000 inscritos).
     * Criador influente, audiência massiva, alto alcance.
     */
    MORE_THAN_100K
}
