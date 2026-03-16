package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando o nível de conhecimento sobre imigração/Portugal
 * (CONSUMER).
 *
 * VALORES:
 * - BEGINNER: Iniciante (pouco ou nenhum conhecimento)
 * - INTERMEDIATE: Intermediário (pesquisa inicial feita)
 * - ADVANCED: Avançado (conhecimento profundo, experiência prévia)
 *
 * USO:
 * - Personalizar conteúdo por nível de expertise
 * - Recomendar conteúdo básico vs avançado
 * - Identificar usuários avançados para mentoria/consultoria reversa
 */
public enum KnowledgeLevel {
    /**
     * Iniciante.
     * Usuário com pouco ou nenhum conhecimento sobre imigração.
     * Necessita conteúdo introdutório, guias básicos.
     */
    BEGINNER,

    /**
     * Intermediário.
     * Usuário com pesquisa inicial feita, entende conceitos básicos.
     * Necessita conteúdo aprofundado, cases específicos.
     */
    INTERMEDIATE,

    /**
     * Avançado.
     * Usuário com conhecimento profundo, experiência prévia ou já imigrante.
     * Necessita conteúdo especializado, updates legislativos.
     */
    ADVANCED
}
