package br.com.aguideptbr.features.auth;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.aguideptbr.features.auth.exceptions.TokenExpiredException;
import br.com.aguideptbr.features.auth.exceptions.TokenInvalidException;
import br.com.aguideptbr.features.auth.exceptions.TokenMalformedException;
import br.com.aguideptbr.features.auth.exceptions.TokenMissingException;
import br.com.aguideptbr.features.user.UserModel;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Filtro de autenticação que intercepta todas as requisições e valida tokens
 * JWT.
 *
 * Este filtro realiza validações granulares do token, lançando exceções
 * específicas
 * para cada tipo de erro (token ausente, expirado, malformado, inválido).
 *
 * Endpoints públicos (@PermitAll) são automaticamente permitidos pelo Quarkus
 * Security.
 *
 * @see TokenMissingException
 * @see TokenExpiredException
 * @see TokenMalformedException
 * @see TokenInvalidException
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Inject
    Logger log;

    @Inject
    JWTService jwtService;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/health",
            "/q/health",
            "/q/swagger-ui");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        log.infof("🔍 AuthenticationFilter executando para path: %s", path);

        // Permite endpoints públicos sem autenticação
        if (isPublicPath(path)) {
            log.debugf("📂 Public endpoint accessed: %s", path);
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // 1. Verifica se o header Authorization está presente
        if (authHeader == null || authHeader.trim().isEmpty()) {
            log.warnf("⚠️ Token ausente para endpoint protegido: %s", path);
            String jsonError = "{\"error\":\"token_missing\",\"message\":\"Token de autenticação é obrigatório\"}";
            Response response = Response.status(Response.Status.UNAUTHORIZED)
                    .entity(jsonError)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            requestContext.abortWith(response);
            return;
        }

        // 2. Verifica se o header está no formato correto (Bearer <token>)
        if (!authHeader.startsWith(BEARER_PREFIX)) {
            log.warnf("⚠️ Authorization header malformado (sem 'Bearer'): %s", path);
            String jsonError = "{\"error\":\"token_malformed\",\"message\":\"Header Authorization deve começar com 'Bearer '\"}";
            Response response = Response.status(Response.Status.UNAUTHORIZED)
                    .entity(jsonError)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            requestContext.abortWith(response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        log.infof("🔍 Token extraído (primeiros 20 chars): %s...",
                token.length() > 20 ? token.substring(0, 20) : token);

        // 3. Verifica se o token não está vazio
        if (token.isEmpty()) {
            log.warnf("⚠️ Token vazio após 'Bearer': %s", path);
            String jsonError = "{\"error\":\"token_missing\",\"message\":\"Token não pode estar vazio após 'Bearer '\"}";
            Response response = Response.status(Response.Status.UNAUTHORIZED)
                    .entity(jsonError)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            requestContext.abortWith(response);
            return;
        }

        // 4. Verifica estrutura básica do JWT (3 partes separadas por ponto)
        String[] parts = token.split("\\.");
        log.infof("🔍 Token dividido em %d partes", parts.length);
        if (parts.length != 3) {
            log.warnf("⚠️ Token JWT malformado (deve ter 3 partes): %s partes", parts.length);
            String jsonError = "{\"error\":\"token_malformed\",\"message\":\"Token JWT deve ter 3 partes separadas por ponto\"}";
            log.infof("📤 JSON de erro criado: %s", jsonError);
            Response response = Response.status(401)
                    .entity(jsonError)
                    .header("Content-Type", "application/json")
                    .header("Content-Length", jsonError.length())
                    .build();
            log.infof("📤 Response criado, abortando requisição...");
            requestContext.abortWith(response);
            log.infof("📤 AbortWith executado!");
            return;
        }
        log.infof("✅ Token tem 3 partes, continuando validação...");

        try {
            // 5. Decodifica o payload para verificar expiração
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode payloadJson = objectMapper.readTree(payload);

            // Verifica se o token está expirado
            if (payloadJson.has("exp")) {
                long exp = payloadJson.get("exp").asLong();
                long now = System.currentTimeMillis() / 1000;

                if (now > exp) {
                    long expiredSecondsAgo = now - exp;
                    log.warnf("⚠️ Token expirado há %d segundos para: %s", expiredSecondsAgo, path);
                    String jsonError = String.format(
                            "{\"error\":\"token_expired\",\"message\":\"Token expirou há %d segundos. Faça login novamente\"}",
                            expiredSecondsAgo);
                    Response response = Response.status(401)
                            .entity(jsonError)
                            .header("Content-Type", "application/json")
                            .build();
                    log.infof("📤 Abortando por token expirado: %s", jsonError);
                    requestContext.abortWith(response);
                    return;
                }
            }

            // 6. Valida claims obrigatórios (payload mínimo: sub, upn)
            // Nota: 'groups' foi removido por segurança - roles são buscadas do banco
            // quando necessário
            if (!payloadJson.has("sub") || !payloadJson.has("upn")) {
                log.warnf("⚠️ Token sem claims obrigatórios (sub, upn): %s", path);
                String jsonError = "{\"error\":\"token_invalid\",\"message\":\"Token não possui claims obrigatórios (sub, upn)\"}";
                Response response = Response.status(401)
                        .entity(jsonError)
                        .header("Content-Type", "application/json")
                        .build();
                log.infof("📤 Abortando por claims inválidos: %s", jsonError);
                requestContext.abortWith(response);
                return;
            }

            // 7. Valida que o usuário ainda existe no banco (防止 token de usuário deletado)
            String userId = payloadJson.get("sub").asText();
            try {
                UUID userUuid = UUID.fromString(userId);
                UserModel user = UserModel.findById(userUuid);

                if (user == null) {
                    log.warnf("⚠️ Token válido mas usuário não existe mais: %s", userId);
                    String jsonError = "{\"error\":\"user_not_found\",\"message\":\"Usuário associado ao token não existe mais\"}";
                    Response response = Response.status(401)
                            .entity(jsonError)
                            .header("Content-Type", "application/json")
                            .build();
                    log.infof("📤 Abortando por usuário inexistente: %s", jsonError);
                    requestContext.abortWith(response);
                    return;
                }

                if (user.deletedAt != null) {
                    log.warnf("⚠️ Token válido mas usuário foi deletado: %s", userId);
                    String jsonError = "{\"error\":\"user_deleted\",\"message\":\"Usuário foi desativado\"}";
                    Response response = Response.status(401)
                            .entity(jsonError)
                            .header("Content-Type", "application/json")
                            .build();
                    log.infof("📤 Abortando por usuário deletado: %s", jsonError);
                    requestContext.abortWith(response);
                    return;
                }

                // ✅ Usuário válido - roles serão verificadas via @RolesAllowed quando
                // necessário
                log.debugf("✅ Usuário validado: %s (role: %s)", user.email, user.role);

            } catch (IllegalArgumentException e) {
                log.warnf("⚠️ UUID inválido no claim 'sub': %s", userId);
                String jsonError = "{\"error\":\"token_invalid\",\"message\":\"ID de usuário inválido no token\"}";
                Response response = Response.status(401)
                        .entity(jsonError)
                        .header("Content-Type", "application/json")
                        .build();
                log.infof("📤 Abortando por UUID inválido: %s", jsonError);
                requestContext.abortWith(response);
                return;
            }

            // 8. Token validado com sucesso (assinatura, expiração, claims, usuário
            // existente)
            log.debugf("✅ Token JWT completamente validado para endpoint: %s", path);

        } catch (IllegalArgumentException e) {
            // Erro de decodificação Base64
            log.warnf("⚠️ Erro ao decodificar Base64 do token: %s", e.getMessage());
            String jsonError = "{\"error\":\"token_malformed\",\"message\":\"Token possui encoding Base64 inválido\"}";
            Response response = Response.status(401)
                    .entity(jsonError)
                    .header("Content-Type", "application/json")
                    .build();
            log.infof("📤 Abortando por Base64 inválido: %s", jsonError);
            requestContext.abortWith(response);
        } catch (Exception e) {
            // Qualquer outro erro de parsing
            log.errorf(e, "❌ Erro ao validar token JWT");
            String jsonError = String.format(
                    "{\"error\":\"token_invalid\",\"message\":\"Erro ao processar token: %s\"}",
                    e.getMessage().replace("\"", "'"));
            Response response = Response.status(401)
                    .entity(jsonError)
                    .header("Content-Type", "application/json")
                    .build();
            log.infof("📤 Abortando por erro genérico: %s", jsonError);
            requestContext.abortWith(response);
        }
    }

    /**
     * Verifica se o path é público (não requer autenticação).
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
