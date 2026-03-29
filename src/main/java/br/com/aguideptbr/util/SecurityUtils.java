package br.com.aguideptbr.util;

import java.util.Base64;
import java.util.UUID;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Utilit\u00e1rios de seguran\u00e7a para valida\u00e7\u00e3o de JWT e
 * autoriza\u00e7\u00e3o de usu\u00e1rios.
 *
 * Provides methods to extract and validate user IDs from JWT tokens.
 */
public final class SecurityUtils {

    private static final Logger LOG = Logger.getLogger(SecurityUtils.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String BEARER_PREFIX = "Bearer ";

    private SecurityUtils() {
        // Utility class - private constructor
    }

    /**
     * Extrai o userId (sub) do token JWT presente no header Authorization.
     *
     * @param authHeader Header Authorization do request
     * @return UUID do usu\u00e1rio autenticado
     * @throws WebApplicationException (401) se token inv\u00e1lido ou userId
     *                                 n\u00e3o encontrado
     */
    public static UUID extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            LOG.warn("\u26a0\ufe0f Authorization header missing or malformed");
            throw new WebApplicationException(
                    "Invalid authorization header",
                    Response.Status.UNAUTHORIZED);
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        String[] parts = token.split("\\.");

        if (parts.length != 3) {
            LOG.warn("\u26a0\ufe0f JWT token malformed");
            throw new WebApplicationException(
                    "Malformed JWT token",
                    Response.Status.UNAUTHORIZED);
        }

        try {
            // Decode payload (parte 2 do JWT)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode payloadJson = OBJECT_MAPPER.readTree(payload);

            if (!payloadJson.has("sub")) {
                LOG.warn("\u26a0\ufe0f JWT token missing 'sub' claim");
                throw new WebApplicationException(
                        "Token missing user ID claim",
                        Response.Status.UNAUTHORIZED);
            }

            String userIdString = payloadJson.get("sub").asText();
            return UUID.fromString(userIdString);

        } catch (IllegalArgumentException e) {
            LOG.warnf("\u26a0\ufe0f Invalid UUID in JWT 'sub' claim: %s", e.getMessage());
            throw new WebApplicationException(
                    "Invalid user ID in token",
                    Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            LOG.errorf(e, "\u274c Error extracting user ID from token");
            throw new WebApplicationException(
                    "Failed to process authentication token",
                    Response.Status.UNAUTHORIZED);
        }
    }

    /**
     * Valida que o userId fornecido no path/body corresponde ao usu\u00e1rio
     * autenticado no JWT.
     *
     * Prevents users from modifying other users' data by validating that the
     * userId in the request path or body matches the authenticated user's ID
     * from the JWT token.
     *
     * @param requestedUserId UUID do usu\u00e1rio no path/body do request
     * @param authHeader      Header Authorization do request
     * @throws WebApplicationException (403) se userId n\u00e3o corresponde ao token
     */
    public static void validateUserIdMatchesToken(UUID requestedUserId, String authHeader) {
        UUID authenticatedUserId = extractUserIdFromToken(authHeader);

        if (!requestedUserId.equals(authenticatedUserId)) {
            LOG.warnf("\u26a0\ufe0f User ID mismatch: requested=%s, authenticated=%s",
                    requestedUserId, authenticatedUserId);
            throw new WebApplicationException(
                    "You can only access your own data",
                    Response.Status.FORBIDDEN);
        }

        LOG.debugf("\u2705 User ID validated: requested=%s matches authenticated=%s",
                requestedUserId, authenticatedUserId);
    }

    /**
     * Extrai userId do SecurityContext (alternativa para endpoints
     * com @RolesAllowed).
     *
     * @param securityContext Security context do request
     * @return UUID do usu\u00e1rio autenticado
     * @throws WebApplicationException (401) se userId n\u00e3o encontrado
     */
    public static UUID extractUserIdFromSecurityContext(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            LOG.warn("\u26a0\ufe0f SecurityContext or UserPrincipal is null");
            throw new WebApplicationException(
                    "User not authenticated",
                    Response.Status.UNAUTHORIZED);
        }

        try {
            String userIdString = securityContext.getUserPrincipal().getName();
            return UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            LOG.warnf("\u26a0\ufe0f Invalid UUID in SecurityContext: %s", e.getMessage());
            throw new WebApplicationException(
                    "Invalid user ID in authentication context",
                    Response.Status.UNAUTHORIZED);
        }
    }
}
