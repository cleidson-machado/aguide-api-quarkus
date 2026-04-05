package br.com.aguideptbr.features.usermessage.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.aguideptbr.features.usermessage.ConversationModel;
import br.com.aguideptbr.features.usermessage.ConversationType;

/**
 * DTO de resposta detalhada de conversa.
 * Contém todos os detalhes da conversa incluindo lista de participantes.
 */
public class ConversationDetailResponse {

    private UUID id;
    private String name;
    private String description;
    private String iconUrl;
    private ConversationType type;
    private List<ParticipantDTO> participants;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ConversationDetailResponse(ConversationModel conversation) {
        this.id = conversation.id;
        this.name = conversation.name;
        this.description = conversation.description;
        this.iconUrl = conversation.iconUrl;
        this.type = conversation.conversationType;
        this.lastMessageAt = conversation.lastMessageAt;
        this.createdAt = conversation.createdAt;
        this.updatedAt = conversation.updatedAt;

        // Mapear participantes (apenas ativos)
        this.participants = conversation.participants.stream()
                .filter(p -> p.isActive())
                .map(ParticipantDTO::new)
                .toList();
    }

    // Construtores

    public ConversationDetailResponse() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public ConversationType getType() {
        return type;
    }

    public void setType(ConversationType type) {
        this.type = type;
    }

    public List<ParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantDTO> participants) {
        this.participants = participants;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
