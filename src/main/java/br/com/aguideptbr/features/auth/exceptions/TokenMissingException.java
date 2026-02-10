package br.com.aguideptbr.features.auth.exceptions;

/**
 * Exceção lançada quando o token JWT não está presente no header Authorization.
 */
public class TokenMissingException extends JwtAuthenticationException {

    public TokenMissingException() {
        super(JwtErrorType.TOKEN_MISSING,
                "Token de autenticação é obrigatório. Inclua o header 'Authorization: Bearer <token>'");
    }
}
