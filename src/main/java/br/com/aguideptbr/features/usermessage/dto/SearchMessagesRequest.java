package br.com.aguideptbr.features.usermessage.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de requisição para buscar mensagens por texto.
 */
public class SearchMessagesRequest {

    @NotNull(message = "ID da conversa é obrigatório")
    private UUID conversationId;

    @NotBlank(message = "Texto de busca é obrigatório")
    private String query;

    // Construtores

    public SearchMessagesRequest() {
    }

    public SearchMessagesRequest(UUID conversationId, String query) {
        this.conversationId = conversationId;
        this.query = query;
    }

    // Getters e Setters

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
