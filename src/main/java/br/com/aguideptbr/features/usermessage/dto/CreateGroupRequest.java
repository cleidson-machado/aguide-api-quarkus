package br.com.aguideptbr.features.usermessage.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requisição para criar grupo de conversa.
 */
public class CreateGroupRequest {

    @NotBlank(message = "Nome do grupo é obrigatório")
    private String name;

    private String description;

    private List<UUID> participantIds; // IDs dos participantes iniciais (além do criador)

    // Construtores

    public CreateGroupRequest() {
    }

    public CreateGroupRequest(String name, String description, List<UUID> participantIds) {
        this.name = name;
        this.description = description;
        this.participantIds = participantIds;
    }

    // Getters e Setters

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

    public List<UUID> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<UUID> participantIds) {
        this.participantIds = participantIds;
    }
}
