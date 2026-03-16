package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando a frequência de publicação de conteúdo (CREATOR).
 *
 * VALORES:
 * - DAILY: Diariamente
 * - WEEKLY: Semanalmente
 * - BIWEEKLY: Quinzenalmente
 * - MONTHLY: Mensalmente
 * - IRREGULAR: Irregular
 *
 * USO:
 * - Identificar criadores consistentes (DAILY, WEEKLY)
 * - Priorizar criadores com alta produção para parcerias
 * - Analisar correlação entre frequência e engajamento
 */
public enum PublishingFrequency {
    /**
     * Publicação diária.
     * Criador altamente ativo, conteúdo constante.
     */
    DAILY,

    /**
     * Publicação semanal.
     * Criador regular, consistência estabelecida.
     */
    WEEKLY,

    /**
     * Publicação quinzenal.
     * Criador moderado, conteúdo planejado.
     */
    BIWEEKLY,

    /**
     * Publicação mensal.
     * Criador ocasional ou conteúdo de alta produção.
     */
    MONTHLY,

    /**
     * Publicação irregular.
     * Criador sem padrão definido.
     */
    IRREGULAR
}
