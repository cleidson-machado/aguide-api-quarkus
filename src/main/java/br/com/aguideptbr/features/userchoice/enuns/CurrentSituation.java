package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando a situação atual do consumidor em relação à imigração
 * (CONSUMER).
 *
 * VALORES:
 * - PLANNING_TO_IMMIGRATE: Planejando imigrar
 * - VISA_IN_PROGRESS: Visto em andamento
 * - ALREADY_IN_PORTUGAL: Já em Portugal
 * - JUST_RESEARCHING: Apenas pesquisando (sem planos concretos)
 *
 * USO:
 * - Segmentar consumidores por estágio da jornada de imigração
 * - Priorizar usuários em estágios avançados (VISA_IN_PROGRESS,
 * ALREADY_IN_PORTUGAL)
 * - Direcionar conteúdo específico por estágio
 */
public enum CurrentSituation {
    /**
     * Usuário planejando imigrar.
     * Fase inicial, pesquisando opções e viabilidade.
     */
    PLANNING_TO_IMMIGRATE,

    /**
     * Usuário com processo de visto em andamento.
     * Fase avançada, alto potencial de conversão para serviços.
     */
    VISA_IN_PROGRESS,

    /**
     * Usuário já residindo em Portugal.
     * Busca informações de adaptação, custo de vida, networking.
     */
    ALREADY_IN_PORTUGAL,

    /**
     * Usuário apenas pesquisando.
     * Fase exploratória, sem planos concretos.
     */
    JUST_RESEARCHING
}
