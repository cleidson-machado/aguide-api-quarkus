package br.com.aguideptbr.features.usermessage.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de requisição para criar conversa direta (1-1).
 */
public class CreateDirectConversationRequest {

    @NotNull(message = "ID do outro usuário é obrigatório")
    private UUID otherUserId;

    // Construtores

    public CreateDirectConversationRequest() {
    }

    public CreateDirectConversationRequest(UUID otherUserId) {
        this.otherUserId = otherUserId;
    }

    // Getters e Setters

    public UUID getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(UUID otherUserId) {
        this.otherUserId = otherUserId;
    }
}
