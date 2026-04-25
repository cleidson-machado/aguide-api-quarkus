package br.com.aguideptbr.features.usermessage;

import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.usermessage.dto.BlockStatusResponse;
import br.com.aguideptbr.util.SecurityUtils;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Controller REST para bloqueio de usuários.
 *
 * Endpoints:
 * - PUT /api/v1/users/{userId}/block - Bloquear usuário
 * - DELETE /api/v1/users/{userId}/block - Desbloquear usuário
 * - GET /api/v1/users/blocks - Listar usuários bloqueados
 */
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserBlockController {

    private final UserBlockService userBlockService;
    private final Logger log;

    public UserBlockController(UserBlockService userBlockService, Logger log) {
        this.userBlockService = userBlockService;
        this.log = log;
    }

    /**
     * Bloqueia um usuário.
     *
     * PUT /api/v1/users/{userId}/block
     * Resposta: { "blockedUserId": "uuid", "isBlocked": true, "blockedAt": "..." }
     * Erros: 400 se tentar bloquear a si mesmo, 404 se usuário não existe, 409 se
     * já bloqueado
     */
    @PUT
    @Path("/{userId}/block")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response blockUser(
            @PathParam("userId") UUID targetUserId,
            @HeaderParam("Authorization") String authHeader) {

        log.infof("PUT /api/v1/users/%s/block", targetUserId);

        UUID currentUserId = SecurityUtils.extractUserIdFromToken(authHeader);

        UserBlockModel block = userBlockService.blockUser(currentUserId, targetUserId);

        BlockStatusResponse responseBody = new BlockStatusResponse(
                targetUserId,
                true,
                block.createdAt);

        log.infof("User %s blocked successfully by %s", targetUserId, currentUserId);
        return Response.status(Response.Status.CREATED).entity(responseBody).build();
    }

    /**
     * Desbloqueia um usuário.
     *
     * DELETE /api/v1/users/{userId}/block
     * Resposta: 204 No Content
     * Erros: 404 se bloqueio não existe
     */
    @DELETE
    @Path("/{userId}/block")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response unblockUser(
            @PathParam("userId") UUID targetUserId,
            @HeaderParam("Authorization") String authHeader) {

        log.infof("DELETE /api/v1/users/%s/block", targetUserId);

        UUID currentUserId = SecurityUtils.extractUserIdFromToken(authHeader);

        userBlockService.unblockUser(currentUserId, targetUserId);

        log.infof("User %s unblocked successfully by %s", targetUserId, currentUserId);
        return Response.noContent().build();
    }

    /**
     * Lista usuários bloqueados pelo usuário atual.
     *
     * GET /api/v1/users/blocks
     * Resposta: array de { blockedUserId, isBlocked, blockedAt }
     */
    @GET
    @Path("/blocks")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response listBlockedUsers(
            @HeaderParam("Authorization") String authHeader) {

        log.info("GET /api/v1/users/blocks");

        UUID currentUserId = SecurityUtils.extractUserIdFromToken(authHeader);

        List<UserBlockModel> blocks = userBlockService.listBlockedUsers(currentUserId);

        List<BlockStatusResponse> responseBody = blocks.stream()
                .map(b -> new BlockStatusResponse(b.blocked.id, true, b.createdAt))
                .toList();

        log.infof("Found %d blocked users for %s", responseBody.size(), currentUserId);
        return Response.ok(responseBody).build();
    }
}
