package br.com.aguideptbr.features.engagement;

/**
 * Enum representando os diferentes tipos de engajamento com conteúdo.
 * Define como os usuários interagem com o conteúdo na plataforma.
 *
 * Tipos de engajamento disponíveis:
 * - VIEW: Visualização do conteúdo
 * - LIKE: Curtida/gostei no conteúdo
 * - DISLIKE: Descurtida/não gostei no conteúdo
 * - SHARE: Compartilhamento do conteúdo
 * - BOOKMARK: Marcação/salvamento para visualização posterior
 * - COMMENT: Comentário no conteúdo
 * - COMPLETE: Conclusão total do conteúdo (assistiu/leu até o fim)
 * - PARTIAL_VIEW: Visualização parcial (abandono antes do término)
 *
 * @author Cleidson Machado
 * @since 1.0
 */
public enum EngagementType {
    /**
     * Visualização do conteúdo.
     * Registra quando o usuário inicia a visualização.
     */
    VIEW,

    /**
     * Curtida/gostei no conteúdo.
     * Indica aprovação positiva do usuário.
     */
    LIKE,

    /**
     * Descurtida/não gostei no conteúdo.
     * Indica desaprovação do usuário.
     */
    DISLIKE,

    /**
     * Compartilhamento do conteúdo.
     * Usuário compartilha o conteúdo com outras pessoas.
     */
    SHARE,

    /**
     * Marcação/salvamento para visualização posterior.
     * Usuário salva o conteúdo para assistir/ler mais tarde.
     */
    BOOKMARK,

    /**
     * Comentário no conteúdo.
     * Usuário deixa um comentário/opinião sobre o conteúdo.
     */
    COMMENT,

    /**
     * Conclusão total do conteúdo.
     * Usuário assistiu/leu o conteúdo até o final.
     */
    COMPLETE,

    /**
     * Visualização parcial do conteúdo.
     * Usuário iniciou mas abandonou antes do término.
     */
    PARTIAL_VIEW;

    /**
     * Verifica se o tipo de engajamento representa uma interação positiva.
     * Engajamentos positivos: LIKE, SHARE, BOOKMARK, COMMENT, COMPLETE.
     *
     * @return true se for engajamento positivo, false caso contrário
     */
    public boolean isPositiveEngagement() {
        return this == LIKE || this == SHARE || this == BOOKMARK ||
                this == COMMENT || this == COMPLETE;
    }

    /**
     * Verifica se o tipo de engajamento representa uma interação negativa.
     * Engajamentos negativos: DISLIKE.
     *
     * @return true se for engajamento negativo, false caso contrário
     */
    public boolean isNegativeEngagement() {
        return this == DISLIKE;
    }

    /**
     * Verifica se o tipo de engajamento está relacionado a visualização.
     * Tipos de visualização: VIEW, COMPLETE, PARTIAL_VIEW.
     *
     * @return true se for relacionado a visualização, false caso contrário
     */
    public boolean isViewRelated() {
        return this == VIEW || this == COMPLETE || this == PARTIAL_VIEW;
    }

    /**
     * Verifica se o tipo de engajamento pode ser revertido.
     * Engajamentos reversíveis: LIKE, DISLIKE, BOOKMARK.
     * Engajamentos não reversíveis: VIEW, SHARE, COMMENT, COMPLETE, PARTIAL_VIEW.
     *
     * @return true se o engajamento pode ser desfeito pelo usuário
     */
    public boolean isReversible() {
        return this == LIKE || this == DISLIKE || this == BOOKMARK;
    }
}
