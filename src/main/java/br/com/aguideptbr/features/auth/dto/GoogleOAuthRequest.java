package br.com.aguideptbr.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de autenticação via Google OAuth.
 * Recebe os dados do Google após autenticação bem-sucedida no cliente.
 *
 * <p>
 * Campos recebidos do Flutter/Mobile:
 * </p>
 * <ul>
 * <li><b>email:</b> Email do usuário obtido do Google</li>
 * <li><b>name:</b> Primeiro nome do usuário</li>
 * <li><b>surname:</b> Sobrenome do usuário</li>
 * <li><b>oauthProvider:</b> Sempre "GOOGLE" para este endpoint</li>
 * <li><b>oauthId:</b> ID único do usuário no Google</li>
 * <li><b>accessToken:</b> Token de acesso do Google</li>
 * <li><b>idToken:</b> Token JWT do Google com informações do usuário</li>
 * </ul>
 *
 * @author Cleidson Machado
 * @since 1.0
 */
public class GoogleOAuthRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Surname is required")
    private String surname;

    @NotBlank(message = "OAuth provider is required")
    private String oauthProvider;

    @NotBlank(message = "OAuth ID is required")
    private String oauthId;

    @NotBlank(message = "Access token is required")
    private String accessToken;

    @NotBlank(message = "ID token is required")
    private String idToken;

    // ========== Construtores ==========

    public GoogleOAuthRequest() {
    }

    public GoogleOAuthRequest(String email, String name, String surname, String oauthProvider,
            String oauthId, String accessToken, String idToken) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.accessToken = accessToken;
        this.idToken = idToken;
    }

    // ========== Getters e Setters ==========

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
