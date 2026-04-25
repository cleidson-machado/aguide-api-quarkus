package br.com.aguideptbr.features.usermessage.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resposta do endpoint PUT /conversations/{id}/mute.
 * Informa o estado atual de silenciamento da conversa para o usuário.
 */
public class MuteStatusResponse {

    private UUID conversationId;
    private boolean isMuted;
    private LocalDateTime mutedAt;

    public MuteStatusResponse() {
    }

    public MuteStatusResponse(UUID conversationId, boolean isMuted, LocalDateTime mutedAt) {
        this.conversationId = conversationId;
        this.isMuted = isMuted;
        this.mutedAt = mutedAt;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public LocalDateTime getMutedAt() {
        return mutedAt;
    }
}
