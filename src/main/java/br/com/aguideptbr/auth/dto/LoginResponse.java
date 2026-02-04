package br.com.aguideptbr.auth.dto;

import java.util.UUID;

/**
 * DTO para resposta de login.
 */
public class LoginResponse {

    public String token;
    public String type = "Bearer";
    public Long expiresIn; // Segundos até expiração
    public UserInfo user;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long expiresIn, UserInfo user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    /**
     * Informações básicas do usuário autenticado.
     */
    public static class UserInfo {
        public UUID id;
        public String name;
        public String surname;
        public String email;
        public String role;

        public UserInfo() {
        }

        public UserInfo(UUID id, String name, String surname, String email, String role) {
            this.id = id;
            this.name = name;
            this.surname = surname;
            this.email = email;
            this.role = role;
        }
    }
}
