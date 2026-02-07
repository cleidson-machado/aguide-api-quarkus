package br.com.aguideptbr.features.auth.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO para padronizar mensagens de erro na API.
 * Retorna mensagens em inglês para o cliente.
 */
public class ErrorResponse {

    private String error;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Métodos de conveniência para erros comuns
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
