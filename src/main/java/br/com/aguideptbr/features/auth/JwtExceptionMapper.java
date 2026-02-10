package br.com.aguideptbr.features.auth;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.auth.dto.ErrorResponse;
import br.com.aguideptbr.features.auth.exceptions.JwtAuthenticationException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Exception mapper específico para exceções de autenticação JWT.
 *
 * Este mapper intercepta exceções do tipo {@link JwtAuthenticationException}
 * e retorna respostas HTTP 401 Unauthorized com detalhes estruturados do erro.
 *
 * Benefícios:
 * - Frontend recebe código de erro específico (token_expired, token_invalid,
 * etc.)
 * - Mensagens amigáveis para o usuário
 * - Logs detalhados para debugging
 * - Não expõe detalhes de implementação/segurança
 *
 * Exemplos de resposta:
 * {
 * "error": "token_expired",
 * "message": "Sua sessão expirou. Faça login novamente",
 * "timestamp": "2026-02-09T14:30:00"
 * }
 *
 * {
 * "error": "token_missing",
 * "message": "Token de autenticação é obrigatório",
 * "timestamp": "2026-02-09T14:30:00"
 * }
 */
@Provider
@SuppressWarnings("java:S6813") // Field injection required for JAX-RS @Provider classes (RESTEasy limitation)
public class JwtExceptionMapper implements ExceptionMapper<JwtAuthenticationException> {

        @Inject
        Logger log;

        @Override
        public Response toResponse(JwtAuthenticationException exception) {
                // Log do erro (sem stacktrace para erros de autenticação esperados)
                log.warnf("🔒 JWT Authentication Error: [%s] %s",
                                exception.getErrorType().getCode(),
                                exception.getMessage());

                // Cria resposta estruturada
                ErrorResponse errorResponse = new ErrorResponse(
                                exception.getErrorType().getCode(),
                                exception.getMessage());

                // Retorna 401 Unauthorized
                return Response
                                .status(Status.UNAUTHORIZED)
                                .entity(errorResponse)
                                .build();
        }
}
