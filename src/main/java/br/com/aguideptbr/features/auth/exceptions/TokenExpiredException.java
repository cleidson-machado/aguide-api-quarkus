package br.com.aguideptbr.features.auth.exceptions;

/**
 * Exceção lançada quando o token JWT está expirado.
 */
public class TokenExpiredException extends JwtAuthenticationException {

    public TokenExpiredException() {
        super(JwtErrorType.TOKEN_EXPIRED,
                "Sua sessão expirou. Faça login novamente para obter um novo token");
    }

    public TokenExpiredException(String customMessage) {
        super(JwtErrorType.TOKEN_EXPIRED, customMessage);
    }
}
