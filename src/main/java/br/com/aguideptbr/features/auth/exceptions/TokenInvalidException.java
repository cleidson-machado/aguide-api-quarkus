package br.com.aguideptbr.features.auth.exceptions;

/**
 * Exceção lançada quando o token JWT é inválido (assinatura incorreta, claims
 * ausentes, etc).
 */
public class TokenInvalidException extends JwtAuthenticationException {

    public TokenInvalidException() {
        super(JwtErrorType.TOKEN_INVALID,
                "Token de autenticação inválido. Faça login novamente");
    }

    public TokenInvalidException(String customMessage) {
        super(JwtErrorType.TOKEN_INVALID, customMessage);
    }

    public TokenInvalidException(Throwable cause) {
        super(JwtErrorType.TOKEN_INVALID,
                "Token de autenticação inválido. Faça login novamente", cause);
    }
}
