package br.com.aguideptbr.features.usermessage.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resposta dos endpoints de bloqueio de usuário.
 */
public class BlockStatusResponse {

    private UUID blockedUserId;
    private boolean isBlocked;
    private LocalDateTime blockedAt;

    public BlockStatusResponse() {
    }

    public BlockStatusResponse(UUID blockedUserId, boolean isBlocked, LocalDateTime blockedAt) {
        this.blockedUserId = blockedUserId;
        this.isBlocked = isBlocked;
        this.blockedAt = blockedAt;
    }

    public UUID getBlockedUserId() {
        return blockedUserId;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }
}
