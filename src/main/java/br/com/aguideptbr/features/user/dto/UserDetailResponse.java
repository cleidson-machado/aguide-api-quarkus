package br.com.aguideptbr.features.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.aguideptbr.features.user.UserModel;

/**
 * DTO de resposta completa do usuário incluindo telefones.
 *
 * Use este DTO quando precisar retornar usuário + telefones em uma única
 * chamada.
 * Evita múltiplas requisições do frontend.
 */
public class UserDetailResponse {

    private UUID id;
    private String name;
    private String surname;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String oauthProvider;
    private String oauthId;
    private Boolean active;
    private String fullName;
    private List<PhoneSummaryDTO> phones;

    public UserDetailResponse(UserModel user) {
        this.id = user.id;
        this.name = user.name;
        this.surname = user.surname;
        this.email = user.email;
        this.role = user.role;
        this.createdAt = user.createdAt;
        this.updatedAt = user.updatedAt;
        this.deletedAt = user.deletedAt;
        this.oauthProvider = user.oauthProvider;
        this.oauthId = user.oauthId;
        this.active = user.isActive();
        this.fullName = user.getFullName();
        this.phones = user.phoneNumbers.stream()
                .map(PhoneSummaryDTO::new)
                .toList();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getOauthProvider() {
        return oauthProvider;
    }

    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public String getOauthId() {
        return oauthId;
    }

    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<PhoneSummaryDTO> getPhones() {
        return phones;
    }

    public void setPhones(List<PhoneSummaryDTO> phones) {
        this.phones = phones;
    }
}
