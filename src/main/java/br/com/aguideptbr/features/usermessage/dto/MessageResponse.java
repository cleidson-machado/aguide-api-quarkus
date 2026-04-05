package br.com.aguideptbr.features.usermessage.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.aguideptbr.features.usermessage.MessageType;
import br.com.aguideptbr.features.usermessage.UserMessageModel;

/**
 * DTO de resposta de mensagem.
 * Contém todos os detalhes da mensagem incluindo remetente.
 */
public class MessageResponse {

    private UUID id;
    private UUID conversationId;
    private UserSummaryDTO sender;
    private String content;
    private MessageType messageType;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime sentAt;
    private UUID parentMessageId;
    private Boolean isEdited;
    private LocalDateTime editedAt;
    private LocalDateTime createdAt;

    public MessageResponse(UserMessageModel message) {
        this.id = message.id;
        this.conversationId = message.conversation.id;
        this.sender = new UserSummaryDTO(message.sender);
        this.content = message.txtContent;
        this.messageType = message.messageType;
        this.isRead = message.isRead;
        this.readAt = message.readAt;
        this.sentAt = message.sentAt;
        this.parentMessageId = message.parentMessage != null ? message.parentMessage.id : null;
        this.isEdited = message.isEdited;
        this.editedAt = message.editedAt;
        this.createdAt = message.createdAt;
    }

    // Construtores

    public MessageResponse() {
    }

    // Getters e Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public UserSummaryDTO getSender() {
        return sender;
    }

    public void setSender(UserSummaryDTO sender) {
        this.sender = sender;
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

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public UUID getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(UUID parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
