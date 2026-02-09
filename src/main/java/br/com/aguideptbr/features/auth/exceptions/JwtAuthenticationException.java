package br.com.aguideptbr.features.auth.exceptions;

/**
 * Exceção base para erros de autenticação JWT.
 * Todas as exceções JWT específicas devem estender esta classe.
 */
public class JwtAuthenticationException extends RuntimeException {

    private final JwtErrorType errorType;

    public JwtAuthenticationException(JwtErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public JwtAuthenticationException(JwtErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public JwtErrorType getErrorType() {
        return errorType;
    }

    /**
     * Tipos de erros JWT para categorização.
     */
    public enum JwtErrorType {
        TOKEN_MISSING("token_missing", "Token de autenticação não fornecido"),
        TOKEN_INVALID("token_invalid", "Token de autenticação inválido"),
        TOKEN_EXPIRED("token_expired", "Token de autenticação expirado"),
        TOKEN_MALFORMED("token_malformed", "Token de autenticação malformado"),
        TOKEN_SIGNATURE_INVALID("token_signature_invalid", "Assinatura do token inválida"),
        TOKEN_CLAIMS_INVALID("token_claims_invalid", "Claims do token inválidos"),
        INSUFFICIENT_PERMISSIONS("insufficient_permissions", "Permissões insuficientes");

        private final String code;
        private final String defaultMessage;

        JwtErrorType(String code, String defaultMessage) {
            this.code = code;
            this.defaultMessage = defaultMessage;
        }

        public String getCode() {
            return code;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }
}
