package br.com.aguideptbr.features.usermessage.dto;

import java.util.UUID;

import br.com.aguideptbr.features.usermessage.MessageType;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de requisição para enviar mensagem.
 */
public class SendMessageRequest {

    @NotNull(message = "ID da conversa é obrigatório")
    private UUID conversationId;

    private String content;

    @NotNull(message = "Tipo de mensagem é obrigatório")
    private MessageType messageType;

    private UUID parentMessageId; // Opcional - para threads/respostas

    // Construtores

    public SendMessageRequest() {
    }

    public SendMessageRequest(UUID conversationId, String content, MessageType messageType, UUID parentMessageId) {
        this.conversationId = conversationId;
        this.content = content;
        this.messageType = messageType;
        this.parentMessageId = parentMessageId;
    }

    // Getters e Setters

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public UUID getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(UUID parentMessageId) {
        this.parentMessageId = parentMessageId;
    }
}
