package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando o tempo de atividade do canal do YouTube (CREATOR).
 *
 * VALORES:
 * - LESS_THAN_6_MONTHS: Menos de 6 meses
 * - SIX_MONTHS_TO_1_YEAR: 6 meses a 1 ano
 * - ONE_TO_3_YEARS: 1 a 3 anos
 * - MORE_THAN_3_YEARS: Mais de 3 anos
 *
 * USO:
 * - Classificar maturidade do canal
 * - Segmentar criadores por experiência
 * - Priorizar criadores estabelecidos (> 1 ano) para parcerias
 */
public enum ChannelAgeRange {
    /**
     * Canal novo (menos de 6 meses).
     * Criador iniciante, ainda estabelecendo presença.
     */
    LESS_THAN_6_MONTHS,

    /**
     * Canal recente (6 meses a 1 ano).
     * Criador em fase de crescimento inicial.
     */
    SIX_MONTHS_TO_1_YEAR,

    /**
     * Canal estabelecido (1 a 3 anos).
     * Criador com presença consistente.
     */
    ONE_TO_3_YEARS,

    /**
     * Canal veterano (mais de 3 anos).
     * Criador experiente e consolidado.
     */
    MORE_THAN_3_YEARS
}
