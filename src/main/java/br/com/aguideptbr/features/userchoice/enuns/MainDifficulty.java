package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando a maior dificuldade ao buscar informações sobre imigração
 * (CONSUMER).
 *
 * VALORES:
 * - OUTDATED_INFO: Informação desatualizada
 * - SUPERFICIAL_CONTENT: Conteúdo superficial
 * - HARD_TO_FIND_NICHE: Difícil encontrar nicho específico
 * - CONTRADICTORY_INFO: Informações contraditórias
 * - OTHER: Outras dificuldades
 *
 * USO:
 * - Identificar pain points dos consumidores
 * - Direcionar criação de conteúdo para resolver problemas específicos
 * - Validar proposta de valor da plataforma (resolver essas dificuldades)
 */
public enum MainDifficulty {
    /**
     * Informação desatualizada.
     * Usuário encontra conteúdo obsoleto (leis antigas, requisitos mudados).
     */
    OUTDATED_INFO,

    /**
     * Conteúdo superficial.
     * Usuário não encontra análises profundas, apenas visões gerais.
     */
    SUPERFICIAL_CONTENT,

    /**
     * Difícil encontrar nicho específico.
     * Usuário não encontra informações para seu caso particular (freelancer,
     * investidor, família).
     */
    HARD_TO_FIND_NICHE,

    /**
     * Informações contraditórias.
     * Usuário encontra informações conflitantes em diferentes fontes.
     */
    CONTRADICTORY_INFO,

    /**
     * Outras dificuldades.
     */
    OTHER
}
