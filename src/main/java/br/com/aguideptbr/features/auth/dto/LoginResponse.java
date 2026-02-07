package br.com.aguideptbr.features.auth.dto;

/**
 * DTO para resposta de login/registro.
 * Retorna token JWT e informações básicas do usuário autenticado.
 *
 * <p>
 * Segue princípios SOLID:
 * </p>
 * <ul>
 * <li><b>Single Responsibility:</b> Responsável apenas por estruturar a
 * resposta de autenticação</li>
 * <li><b>Open/Closed:</b> Extensível via herança, protegido por
 * encapsulamento</li>
 * <li><b>Dependency Inversion:</b> Depende da abstração UserInfoDTO, não de
 * detalhes de implementação</li>
 * </ul>
 *
 * @see UserInfoDTO
 */
public class LoginResponse {

    /**
     * Constante para tipo de token (Bearer Authentication).
     * Usar constante evita valores mágicos e facilita manutenção.
     */
    private static final String TOKEN_TYPE = "Bearer";

    private String token;
    private String type;
    private Long expiresIn; // Segundos até expiração
    private UserInfoDTO user;

    /**
     * Construtor padrão.
     * Inicializa o tipo de token com o valor padrão "Bearer".
     */
    public LoginResponse() {
        this.type = TOKEN_TYPE;
    }

    /**
     * Construtor completo.
     *
     * @param token     Token JWT gerado
     * @param expiresIn Tempo de expiração em segundos
     * @param user      Informações do usuário autenticado
     */
    public LoginResponse(String token, Long expiresIn, UserInfoDTO user) {
        this.token = token;
        this.type = TOKEN_TYPE;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // ========== Getters e Setters ==========

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    /**
     * Define o tipo de token.
     * Nota: Normalmente é "Bearer" e não deve ser alterado.
     */
    public void setType(String type) {
        this.type = type;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfoDTO getUser() {
        return user;
    }

    public void setUser(UserInfoDTO user) {
        this.user = user;
    }
}
