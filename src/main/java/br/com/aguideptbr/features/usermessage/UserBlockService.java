package br.com.aguideptbr.features.usermessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.user.UserModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Service para lógica de bloqueio entre usuários.
 *
 * Regras:
 * - Bloqueio é unilateral (A bloqueia B ≠ B bloqueia A)
 * - Usuário não pode bloquear a si mesmo
 * - Bloquear usuário já bloqueado retorna 409
 * - Desbloquear usuário não bloqueado retorna 404
 */
@ApplicationScoped
public class UserBlockService {

    private final UserBlockRepository blockRepository;
    private final Logger log;

    public UserBlockService(UserBlockRepository blockRepository, Logger log) {
        this.blockRepository = blockRepository;
        this.log = log;
    }

    /**
     * Bloqueia um usuário.
     *
     * @param blockerUserId ID do usuário que está bloqueando
     * @param blockedUserId ID do usuário a ser bloqueado
     * @throws BadRequestException     se tentar bloquear a si mesmo
     * @throws WebApplicationException 409 se já bloqueado
     * @throws NotFoundException       se usuário alvo não existe
     */
    @Transactional
    public UserBlockModel blockUser(UUID blockerUserId, UUID blockedUserId) {
        log.infof("Blocking user: blocker=%s, blocked=%s", blockerUserId, blockedUserId);

        if (blockerUserId.equals(blockedUserId)) {
            throw new BadRequestException("Não é possível bloquear a si mesmo");
        }

        // Verificar se usuário alvo existe
        UserModel blockedUser = UserModel.findByIdActive(blockedUserId);
        if (blockedUser == null) {
            throw new NotFoundException("Usuário não encontrado");
        }

        // Verificar se já está bloqueado
        if (blockRepository.isBlocked(blockerUserId, blockedUserId)) {
            throw new WebApplicationException(
                    Response.status(409)
                            .entity(Map.of(
                                    "error", "BUSINESS_RULE",
                                    "message", "Você já bloqueou este usuário"))
                            .build());
        }

        UserModel blocker = new UserModel();
        blocker.id = blockerUserId;

        UserBlockModel block = new UserBlockModel();
        block.blocker = blocker;
        block.blocked = blockedUser;
        blockRepository.persist(block);

        log.infof("User %s blocked by %s", blockedUserId, blockerUserId);
        return block;
    }

    /**
     * Desbloqueia um usuário.
     *
     * @param blockerUserId ID do usuário que está desbloqueando
     * @param blockedUserId ID do usuário a ser desbloqueado
     * @throws NotFoundException se o bloqueio não existe
     */
    @Transactional
    public void unblockUser(UUID blockerUserId, UUID blockedUserId) {
        log.infof("Unblocking user: blocker=%s, blocked=%s", blockerUserId, blockedUserId);

        UserBlockModel block = blockRepository.findByBlockerAndBlocked(blockerUserId, blockedUserId);
        if (block == null) {
            throw new NotFoundException("Bloqueio não encontrado");
        }

        blockRepository.delete(block);
        log.infof("User %s unblocked by %s", blockedUserId, blockerUserId);
    }

    /**
     * Lista todos os usuários bloqueados pelo usuário atual.
     *
     * @param blockerUserId ID do usuário
     * @return Lista de registros de bloqueio
     */
    public List<UserBlockModel> listBlockedUsers(UUID blockerUserId) {
        log.infof("Listing blocked users for: %s", blockerUserId);
        return blockRepository.findAllByBlocker(blockerUserId);
    }
}
