package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando a intenção de contratar serviços profissionais (CONSUMER).
 *
 * VALORES:
 * - YES_CONSULTING: Sim, consultoria
 * - YES_MENTORING: Sim, mentoria
 * - MAYBE: Talvez
 * - NO_FREE_ONLY: Não, só conteúdo grátis
 *
 * USO:
 * - Identificar consumidores com alto potencial de conversão (YES_CONSULTING,
 * YES_MENTORING)
 * - Priorizar leads para time de vendas
 * - Segmentar ofertas pagas vs conteúdo gratuito
 */
public enum ServiceHiringIntent {
    /**
     * Sim, consultoria.
     * Usuário disposto a pagar por consultoria especializada (advogados,
     * despachantes).
     */
    YES_CONSULTING,

    /**
     * Sim, mentoria.
     * Usuário disposto a pagar por mentoria personalizada (planejamento
     * financeiro, roadmap).
     */
    YES_MENTORING,

    /**
     * Talvez.
     * Usuário considerando contratar serviços no futuro.
     */
    MAYBE,

    /**
     * Não, só conteúdo grátis.
     * Usuário focado em conteúdo gratuito, sem intenção de pagar.
     */
    NO_FREE_ONLY
}
