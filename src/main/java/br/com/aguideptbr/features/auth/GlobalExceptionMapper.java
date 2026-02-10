package br.com.aguideptbr.features.auth;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Exception mapper global para WebApplicationException.
 * Garante que todas as exceções retornem JSON estruturado.
 */
@Provider
@SuppressWarnings("java:S6813") // Field injection required for JAX-RS @Provider classes (RESTEasy limitation)
public class GlobalExceptionMapper implements ExceptionMapper<WebApplicationException> {

    // NOTE: Field injection is intentionally used here instead of constructor
    // injection.
    // RESTEasy requires @Provider classes to have no-arg constructor or field
    // injection.
    @Inject
    Logger log;

    @Override
    public Response toResponse(WebApplicationException exception) {
        Response.Status status = Response.Status.fromStatusCode(exception.getResponse().getStatus());

        // Se status for null, usa INTERNAL_SERVER_ERROR
        if (status == null) {
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        String errorCode = status.name();
        String message = exception.getMessage();

        // Se mensagem for nula, usa o motivo padrão do status
        if (message == null || message.isEmpty()) {
            message = status.getReasonPhrase();
        }

        // Log de erro apenas para erros 5xx
        if (status.getStatusCode() >= 500) {
            log.errorf("❌ Internal Server Error: %s", message, exception);
        } else {
            log.warnf("⚠️ Client Error (%s): %s", status, message);
        }

        // Monta resposta JSON estruturada
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now().toString());

        return Response
                .status(status)
                .entity(errorResponse)
                .build();
    }
}
