package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando o tipo de perfil do usuário.
 *
 * VALORES:
 * - CREATOR: Criador de conteúdo (YouTubers, produtores, influencers)
 * - CONSUMER: Consumidor de conteúdo (Pesquisadores, planejadores, audiência)
 *
 * USO:
 * - Determina quais campos da entidade UserChoiceModel devem ser preenchidos
 * - Permite segmentação de usuários para features específicas
 * - Habilita análise de distribuição de perfis na plataforma
 */
public enum UserProfileType {
    /**
     * Criador de conteúdo.
     * Produz conteúdo sobre imigração/nichos específicos no YouTube.
     * Pode oferecer serviços ou produtos.
     */
    CREATOR,

    /**
     * Consumidor de conteúdo.
     * Pesquisa informações sobre imigração/nichos específicos.
     * Potencial cliente para serviços de criadores.
     */
    CONSUMER
}
