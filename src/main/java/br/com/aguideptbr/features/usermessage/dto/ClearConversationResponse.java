package br.com.aguideptbr.features.usermessage.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resposta do endpoint PUT /conversations/{id}/clear.
 * Informa quando a conversa foi limpa para o usuário.
 */
public class ClearConversationResponse {

    private UUID conversationId;
    private LocalDateTime clearedAt;

    public ClearConversationResponse() {
    }

    public ClearConversationResponse(UUID conversationId, LocalDateTime clearedAt) {
        this.conversationId = conversationId;
        this.clearedAt = clearedAt;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public LocalDateTime getClearedAt() {
        return clearedAt;
    }
}
