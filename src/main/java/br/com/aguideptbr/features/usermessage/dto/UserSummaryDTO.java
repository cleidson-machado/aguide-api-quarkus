package br.com.aguideptbr.features.usermessage.dto;

import java.util.UUID;

import br.com.aguideptbr.features.user.UserModel;

/**
 * DTO simplificado de usuário para incluir em respostas de mensagens.
 * Contém apenas as informações essenciais (sem dados sensíveis).
 */
public class UserSummaryDTO {

    private UUID id;
    private String name;
    private String surname;
    private String fullName;

    public UserSummaryDTO(UserModel user) {
        this.id = user.id;
        this.name = user.name;
        this.surname = user.surname;
        this.fullName = user.getFullName();
    }

    // Construtores

    public UserSummaryDTO() {
    }

    public UserSummaryDTO(UUID id, String name, String surname, String fullName) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.fullName = fullName;
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

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
