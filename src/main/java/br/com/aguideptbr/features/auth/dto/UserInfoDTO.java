package br.com.aguideptbr.features.auth.dto;

import java.util.UUID;

/**
 * DTO contendo informações básicas do usuário autenticado.
 * Usado em respostas de autenticação (login, register) e outras operações que
 * precisam
 * retornar dados resumidos do usuário sem expor informações sensíveis.
 *
 * <p>
 * Este DTO segue os princípios de encapsulamento com campos privados e
 * accessors,
 * permitindo controle sobre acesso aos dados e facilitando debugging.
 * </p>
 *
 * @see LoginResponse
 */
public class UserInfoDTO {

    private UUID id;
    private String name;
    private String surname;
    private String email;
    private String role;

    /**
     * Construtor padrão.
     * Necessário para frameworks de serialização (Jackson, etc.).
     */
    public UserInfoDTO() {
    }

    /**
     * Construtor completo.
     *
     * @param id      ID único do usuário
     * @param name    Nome do usuário
     * @param surname Sobrenome do usuário
     * @param email   Email do usuário
     * @param role    Papel/permissão do usuário (USER, ADMIN)
     */
    public UserInfoDTO(UUID id, String name, String surname, String email, String role) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.role = role;
    }

    // ========== Getters e Setters ==========

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

    @Override
    public String toString() {
        return "UserInfoDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
