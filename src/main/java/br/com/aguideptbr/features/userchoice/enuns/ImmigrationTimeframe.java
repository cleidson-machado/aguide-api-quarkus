package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando o prazo planejado para imigrar (CONSUMER).
 *
 * VALORES:
 * - LESS_THAN_6_MONTHS: Menos de 6 meses
 * - SIX_MONTHS_TO_1_YEAR: 6 meses a 1 ano
 * - ONE_TO_2_YEARS: 1 a 2 anos
 * - NO_DEFINED_TIMEFRAME: Sem prazo definido
 * - NOT_PLANNING: Não planeja imigrar
 *
 * USO:
 * - Priorizar consumidores com prazos curtos (alto potencial de conversão)
 * - Segmentar campanhas por urgência
 * - Identificar usuários em fase exploratória (NOT_PLANNING,
 * NO_DEFINED_TIMEFRAME)
 */
public enum ImmigrationTimeframe {
    /**
     * Menos de 6 meses.
     * Usuário com plano iminente, alta urgência.
     * ALTÍSSIMO potencial de conversão.
     */
    LESS_THAN_6_MONTHS,

    /**
     * 6 meses a 1 ano.
     * Usuário com plano de curto prazo.
     * ALTO potencial de conversão.
     */
    SIX_MONTHS_TO_1_YEAR,

    /**
     * 1 a 2 anos.
     * Usuário com plano de médio prazo.
     * MÉDIO potencial de conversão.
     */
    ONE_TO_2_YEARS,

    /**
     * Sem prazo definido.
     * Usuário planejando, mas sem timeline clara.
     * BAIXO potencial de conversão (ainda explorando).
     */
    NO_DEFINED_TIMEFRAME,

    /**
     * Não planeja imigrar.
     * Usuário apenas pesquisando, sem intenção concreta.
     * MUITO BAIXO potencial de conversão.
     */
    NOT_PLANNING
}
