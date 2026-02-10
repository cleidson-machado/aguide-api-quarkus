package br.com.aguideptbr.features.auth.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO para respostas de erro estruturadas.
 *
 * Fornece informações detalhadas sobre erros sem expor detalhes de segurança
 * sensíveis.
 *
 * Estrutura JSON retornada:
 * {
 * "error": "token_expired",
 * "message": "Sua sessão expirou. Faça login novamente",
 * "timestamp": "2026-02-09T14:30:00",
 * "path": "/api/v1/users"
 * }
 */
public class ErrorResponse {

    private String error; // Código do erro (ex: token_expired, token_invalid)
    private String message; // Mensagem amigável para o usuário
    private String path; // Path onde ocorreu o erro (opcional)

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Construtor padrão.
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Construtor com error e message.
     */
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Construtor completo com path.
     */
    public ErrorResponse(String error, String message, String path) {
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    // ========== Getters e Setters ==========

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    // ========== Métodos de Conveniência ==========
    public static ErrorResponse unauthorized(String message) {
        return new ErrorResponse("UNAUTHORIZED", message);
    }

    public static ErrorResponse badRequest(String message) {
        return new ErrorResponse("BAD_REQUEST", message);
    }

    public static ErrorResponse conflict(String message) {
        return new ErrorResponse("CONFLICT", message);
    }

    public static ErrorResponse internalServerError(String message) {
        return new ErrorResponse("INTERNAL_SERVER_ERROR", message);
    }
}
