package br.com.aguideptbr.features.usermessage;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository para operações de banco de dados com bloqueios entre usuários.
 */
@ApplicationScoped
public class UserBlockRepository implements PanacheRepositoryBase<UserBlockModel, UUID> {

    /**
     * Verifica se blockerUserId bloqueou blockedUserId.
     */
    public boolean isBlocked(UUID blockerUserId, UUID blockedUserId) {
        return count("blocker.id = ?1 and blocked.id = ?2", blockerUserId, blockedUserId) > 0;
    }

    /**
     * Verifica se existe bloqueio em qualquer direção entre dois usuários.
     * Usado para impedir criação/envio em conversas DIRECT.
     */
    public boolean isBlockedInAnyDirection(UUID userOneId, UUID userTwoId) {
        return count(
                "(blocker.id = ?1 and blocked.id = ?2) or (blocker.id = ?2 and blocked.id = ?1)",
                userOneId, userTwoId) > 0;
    }

    /**
     * Busca o registro de bloqueio (para delete).
     */
    public UserBlockModel findByBlockerAndBlocked(UUID blockerUserId, UUID blockedUserId) {
        return find("blocker.id = ?1 and blocked.id = ?2", blockerUserId, blockedUserId).firstResult();
    }

    /**
     * Lista todos os usuários bloqueados por um bloqueador.
     */
    public List<UserBlockModel> findAllByBlocker(UUID blockerUserId) {
        return list("blocker.id = ?1 ORDER BY createdAt DESC", blockerUserId);
    }
}
