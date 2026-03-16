package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando o tipo de conteúdo preferido pelo consumidor (CONSUMER).
 *
 * VALORES:
 * - PERSONAL_STORIES: Histórias pessoais
 * - STEP_BY_STEP_TUTORIALS: Tutoriais passo a passo
 * - LEGAL_ANALYSIS: Análises jurídicas
 * - COMPARISONS: Comparações (custos, vistos, cidades)
 * - NEWS_AND_UPDATES: Notícias e atualizações
 *
 * USO:
 * - Personalizar feed de conteúdo por preferência
 * - Recomendar criadores que produzem formato preferido
 * - Validar tipos de conteúdo mais demandados
 */
public enum PreferredContentType {
    /**
     * Histórias pessoais.
     * Usuário prefere relatos de experiências reais, jornadas de imigrantes.
     */
    PERSONAL_STORIES,

    /**
     * Tutoriais passo a passo.
     * Usuário prefere guias práticos, instruções detalhadas.
     */
    STEP_BY_STEP_TUTORIALS,

    /**
     * Análises jurídicas.
     * Usuário prefere conteúdo técnico, interpretação de leis, requisitos legais.
     */
    LEGAL_ANALYSIS,

    /**
     * Comparações.
     * Usuário prefere análises comparativas (custos, vistos, cidades, países).
     */
    COMPARISONS,

    /**
     * Notícias e atualizações.
     * Usuário prefere conteúdo atual, mudanças legislativas, tendências.
     */
    NEWS_AND_UPDATES
}
