package br.com.aguideptbr.features.userposition.enuns;

/**
 * Enum que define os tipos de operações auditadas em UserRankingModel.
 *
 * Usado em UserRankingAuditModel para garantir type safety e prevenir
 * erros de digitação ao registrar operações.
 */
public enum AuditOperationType {
    /**
     * Criação de novo registro de ranking.
     */
    CREATE,

    /**
     * Atualização de campo existente.
     */
    UPDATE,

    /**
     * Adição de pontos ao usuário.
     */
    ADD_POINTS,

    /**
     * Exclusão lógica (soft delete) do ranking.
     */
    DELETE,

    /**
     * Restauração de ranking previamente deletado.
     */
    RESTORE
}
