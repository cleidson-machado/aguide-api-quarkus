package br.com.aguideptbr.features.auth.exceptions;

/**
 * Exceção lançada quando o token JWT está malformado (não possui 3 partes,
 * Base64 inválido, etc).
 */
public class TokenMalformedException extends JwtAuthenticationException {

    public TokenMalformedException() {
        super(JwtErrorType.TOKEN_MALFORMED,
                "Token de autenticação está malformado. Certifique-se de enviar o token completo");
    }

    public TokenMalformedException(String details) {
        super(JwtErrorType.TOKEN_MALFORMED,
                "Token de autenticação está malformado: " + details);
    }
}
