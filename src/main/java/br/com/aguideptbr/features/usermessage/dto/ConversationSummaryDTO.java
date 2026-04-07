package br.com.aguideptbr.features.usermessage.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.aguideptbr.features.usermessage.ConversationModel;
import br.com.aguideptbr.features.usermessage.ConversationType;

/**
 * DTO resumido de conversa para listagem (inbox).
 * Contém apenas as informações essenciais para exibir na lista de conversas.
 */
public class ConversationSummaryDTO {

    private UUID id;
    private String name;
    private ConversationType type;
    private String iconUrl;
    private LocalDateTime lastMessageAt;
    private String lastMessagePreview; // Preview da última mensagem (primeiras palavras)
    private Long unreadCount;
    private Boolean isPinned;
    private Boolean isArchived;
    private LocalDateTime createdAt;

    // Construtores

    public ConversationSummaryDTO() {
    }

    public ConversationSummaryDTO(ConversationModel conversation) {
        this.id = conversation.id;
        this.name = conversation.name;
        this.type = conversation.conversationType;
        this.iconUrl = conversation.iconUrl;
        this.lastMessageAt = conversation.lastMessageAt;
        this.lastMessagePreview = null;
        this.unreadCount = 0L;
        this.isPinned = Boolean.FALSE;
        this.isArchived = Boolean.FALSE;
        this.createdAt = conversation.createdAt;
    }

    public ConversationSummaryDTO(
            ConversationModel conversation,
            Long unreadCount,
            String lastMessagePreview,
            Boolean isPinned,
            Boolean isArchived) {
        this(conversation);
        this.unreadCount = unreadCount != null ? unreadCount : 0L;
        this.lastMessagePreview = lastMessagePreview;
        this.isPinned = isPinned != null ? isPinned : Boolean.FALSE;
        this.isArchived = isArchived != null ? isArchived : Boolean.FALSE;
    }

    // Getters e Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConversationType getType() {
        return type;
    }

    public void setType(ConversationType type) {
        this.type = type;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
