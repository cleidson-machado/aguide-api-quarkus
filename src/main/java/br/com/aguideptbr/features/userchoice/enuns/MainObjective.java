package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando o objetivo principal de pesquisa do consumidor (CONSUMER).
 *
 * VALORES:
 * - VISA_INFO: Informações sobre vistos
 * - JOB_OPPORTUNITIES: Oportunidades de trabalho
 * - QUALITY_OF_LIFE: Qualidade de vida
 * - EDUCATION: Educação (escolas, universidades)
 * - ENTREPRENEURSHIP: Empreendedorismo
 * - OTHER: Outros objetivos
 *
 * USO:
 * - Segmentar consumidores por interesse principal
 * - Recomendar conteúdo relevante por objetivo
 * - Identificar oportunidades de serviços nichados
 */
public enum MainObjective {
    /**
     * Busca informações sobre vistos.
     * Usuário em fase de planejamento legal.
     */
    VISA_INFO,

    /**
     * Busca oportunidades de trabalho.
     * Usuário focado em carreira/empregabilidade.
     */
    JOB_OPPORTUNITIES,

    /**
     * Busca informações sobre qualidade de vida.
     * Usuário avaliando estilo de vida, segurança, saúde.
     */
    QUALITY_OF_LIFE,

    /**
     * Busca informações sobre educação.
     * Usuário com foco em escolas, universidades para filhos ou si mesmo.
     */
    EDUCATION,

    /**
     * Busca oportunidades de empreendedorismo.
     * Usuário planejando abrir negócio em Portugal.
     */
    ENTREPRENEURSHIP,

    /**
     * Outros objetivos.
     */
    OTHER
}
