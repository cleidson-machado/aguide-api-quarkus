package br.com.aguideptbr.features.engagement;

/**
 * Enum representando o status de um registro de engajamento com conteúdo.
 * Permite rastrear o ciclo de vida das interações dos usuários.
 *
 * Estados do ciclo de vida:
 * - ACTIVE: Engajamento ativo e contabilizado
 * - REMOVED: Engajamento removido pelo usuário
 * - EXPIRED: Engajamento expirado (ex: bookmarks temporários)
 * - FLAGGED: Engajamento marcado como spam ou abuso
 *
 * Fluxo típico de estados:
 * 1. Criação → ACTIVE
 * 2. Usuário remove → REMOVED
 * 3. Sistema expira → EXPIRED
 * 4. Moderação flagga → FLAGGED
 *
 * @author Cleidson Machado
 * @since 1.0
 */
public enum EngagementStatus {
    /**
     * Engajamento ativo e contabilizado.
     * Estado padrão quando o engajamento é criado.
     * Contabilizado em métricas e estatísticas.
     */
    ACTIVE,

    /**
     * Engajamento removido pelo usuário.
     * Exemplo: usuário desfaz like, remove bookmark.
     * Não é contabilizado em métricas mas permanece no histórico.
     */
    REMOVED,

    /**
     * Engajamento expirado pelo sistema.
     * Exemplo: bookmarks temporários com data de expiração.
     * Pode ser usado para limpeza automática de dados antigos.
     */
    EXPIRED,

    /**
     * Engajamento marcado como spam ou abuso.
     * Identificado por moderação ou sistema automático.
     * Removido das métricas públicas mas mantido para auditoria.
     */
    FLAGGED;

    /**
     * Verifica se o status representa um engajamento ativo.
     * Apenas engajamentos ACTIVE são contabilizados em métricas.
     *
     * @return true se o status for ACTIVE, false caso contrário
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Verifica se o status representa um engajamento válido para estatísticas.
     * Engajamentos ACTIVE e REMOVED são considerados válidos.
     * EXPIRED e FLAGGED não são contabilizados.
     *
     * @return true se o status for válido para estatísticas
     */
    public boolean isValidForStatistics() {
        return this == ACTIVE || this == REMOVED;
    }

    /**
     * Verifica se o status representa um engajamento que deve ser ocultado.
     * Engajamentos EXPIRED e FLAGGED devem ser ocultados da visualização pública.
     *
     * @return true se o engajamento deve ser ocultado
     */
    public boolean shouldBeHidden() {
        return this == EXPIRED || this == FLAGGED;
    }

    /**
     * Verifica se o status permite reativação.
     * Engajamentos REMOVED podem ser reativados (ex: curtir novamente).
     * Engajamentos EXPIRED e FLAGGED não podem ser reativados.
     *
     * @return true se o engajamento pode ser reativado
     */
    public boolean canBeReactivated() {
        return this == REMOVED;
    }
}
