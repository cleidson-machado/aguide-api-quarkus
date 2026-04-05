package br.com.aguideptbr.features.usermessage.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.aguideptbr.features.usermessage.ConversationParticipantModel;

/**
 * DTO de participante de conversa.
 * Usado em ConversationDetailResponse para listar participantes.
 */
public class ParticipantDTO {

    private UUID userId;
    private String userName;
    private String userFullName;
    private Boolean isAdmin;
    private Boolean isCreator;
    private LocalDateTime joinedAt;
    private LocalDateTime lastReadAt;

    public ParticipantDTO(ConversationParticipantModel participant) {
        this.userId = participant.user.id;
        this.userName = participant.user.name;
        this.userFullName = participant.user.getFullName();
        this.isAdmin = participant.isAdmin;
        this.isCreator = participant.isCreator;
        this.joinedAt = participant.joinedAt;
        this.lastReadAt = participant.lastReadAt;
    }

    // Construtores

    public ParticipantDTO() {
    }

    // Getters e Setters

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Boolean getIsCreator() {
        return isCreator;
    }

    public void setIsCreator(Boolean isCreator) {
        this.isCreator = isCreator;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(LocalDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}
