package br.com.aguideptbr.features.user;

/**
 * Enum representando as roles (papéis) disponíveis no sistema.
 *
 * Hierarquia de permissões (do maior para o menor):
 * - ADMIN: Administrador do sistema (controle total)
 * - MANAGER: Gerente/Moderador (gerencia conteúdo e usuários)
 * - CHANNEL_OWNER: Usuário pagante proprietário de canal
 * - PREMIUM_USER: Usuário pagante normal (sem canal próprio)
 * - FREE: Usuário gratuito (funcionalidades limitadas)
 *
 * @author Cleidson Machado
 * @since 1.0
 */
public enum UserRole {
    /**
     * Administrador do sistema com acesso total.
     * Único role que retorna "admin": true no JWT.
     */
    ADMIN,

    /**
     * Gerente/Moderador com permissões de gerenciamento.
     */
    MANAGER,

    /**
     * Usuário pagante proprietário de canal.
     * Pode criar e gerenciar seu próprio canal de conteúdo.
     */
    CHANNEL_OWNER,

    /**
     * Usuário pagante normal sem canal próprio.
     * Acesso a funcionalidades premium.
     */
    PREMIUM_USER,

    /**
     * Usuário gratuito com funcionalidades limitadas.
     */
    FREE;

    /**
     * Verifica se a role é administrativa (ADMIN).
     * Usado para determinar o valor de "admin" no JWT.
     *
     * @return true se for ADMIN, false caso contrário
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Verifica se a role é de usuário pagante (CHANNEL_OWNER ou PREMIUM_USER).
     *
     * @return true se for usuário pagante
     */
    public boolean isPaidUser() {
        return this == CHANNEL_OWNER || this == PREMIUM_USER;
    }

    /**
     * Verifica se a role pode criar canais.
     *
     * @return true se puder criar canais (ADMIN, MANAGER, CHANNEL_OWNER)
     */
    public boolean canCreateChannels() {
        return this == ADMIN || this == MANAGER || this == CHANNEL_OWNER;
    }
}
