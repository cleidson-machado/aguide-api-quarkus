package br.com.aguideptbr.features.usermessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.usermessage.dto.ConversationDetailResponse;
import br.com.aguideptbr.features.usermessage.dto.ConversationSummaryDTO;
import br.com.aguideptbr.features.usermessage.dto.CreateDirectConversationRequest;
import br.com.aguideptbr.features.usermessage.dto.CreateGroupRequest;
import br.com.aguideptbr.util.SecurityUtils;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Controller REST para gerenciamento de conversas.
 *
 * Endpoints:
 * - POST /api/v1/conversations/direct - Criar conversa direta (1-1)
 * - POST /api/v1/conversations/group - Criar grupo
 * - GET /api/v1/conversations - Listar conversas do usuário (inbox)
 * - GET /api/v1/conversations/{conversationId} - Detalhes da conversa
 * - PUT /api/v1/conversations/{conversationId}/archive - Arquivar/desarquivar
 * - PUT /api/v1/conversations/{conversationId}/pin - Fixar/desfixar
 * - POST /api/v1/conversations/{conversationId}/participants - Adicionar
 * participante
 * - DELETE /api/v1/conversations/{conversationId}/participants/{userId} -
 * Remover participante
 * - GET /api/v1/conversations/unread-count - Total de não lidas
 */
@Path("/api/v1/conversations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConversationController {

    private final ConversationService conversationService;
    private final Logger log;

    public ConversationController(ConversationService conversationService, Logger log) {
        this.conversationService = conversationService;
        this.log = log;
    }

    /**
     * Cria uma conversa direta (1-1).
     *
     * POST /api/v1/conversations/direct
     * Body: { "otherUserId": "uuid" }
     */
    @POST
    @Path("/direct")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response createDirectConversation(
            @Valid CreateDirectConversationRequest request,
            @Context SecurityContext securityContext) {

        log.infof("POST /api/v1/conversations/direct - Creating conversation with user %s", request.getOtherUserId());

        // TODO: Extrair userId do SecurityContext (JWT)
        UUID currentUserId = UUID.randomUUID(); // PLACEHOLDER

        ConversationModel conversation = conversationService.createDirectConversation(
                currentUserId,
                request.getOtherUserId());

        ConversationDetailResponse response = new ConversationDetailResponse(conversation);

        log.infof("Direct conversation created: id=%s", conversation.id);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * Cria um grupo.
     *
     * POST /api/v1/conversations/group
     * Body: { "name": "Group Name", "description": "...", "participantIds":
     * ["uuid1", "uuid2"] }
     */
    @POST
    @Path("/group")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response createGroupConversation(
            @Valid CreateGroupRequest request,
            @Context SecurityContext securityContext) {

        log.infof("POST /api/v1/conversations/group - Creating group '%s' with %d participants",
                request.getName(), request.getParticipantIds() != null ? request.getParticipantIds().size() : 0);

        // TODO: Extrair userId do SecurityContext
        UUID creatorId = UUID.randomUUID(); // PLACEHOLDER

        ConversationModel conversation = conversationService.createGroupConversation(
                request.getName(),
                request.getDescription(),
                creatorId,
                request.getParticipantIds());

        ConversationDetailResponse response = new ConversationDetailResponse(conversation);

        log.infof("Group conversation created: id=%s, name=%s", conversation.id, conversation.name);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * Lista conversas do usuário (inbox).
     *
     * GET /api/v1/conversations?includeArchived=false
     */
    @GET
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response getUserConversations(
            @QueryParam("includeArchived") @DefaultValue("false") boolean includeArchived,
            @HeaderParam("Authorization") String authHeader,
            @Context SecurityContext securityContext) {

        log.infof("GET /api/v1/conversations - includeArchived=%b", includeArchived);

        UUID userId = SecurityUtils.extractUserIdFromToken(authHeader);

        List<ConversationSummaryDTO> response = conversationService.getUserConversationSummaries(userId,
                includeArchived);

        log.infof("Found %d conversations for user %s", response.size(), userId);
        return Response.ok(response).build();
    }

    /**
     * Busca detalhes de uma conversa.
     *
     * GET /api/v1/conversations/{conversationId}
     */
    @GET
    @Path("/{conversationId}")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response getConversationDetails(
            @PathParam("conversationId") UUID conversationId,
            @Context SecurityContext securityContext) {

        log.infof("GET /api/v1/conversations/%s", conversationId);

        // TODO: Extrair userId do SecurityContext
        UUID userId = UUID.randomUUID(); // PLACEHOLDER

        ConversationModel conversation = conversationService.getConversationDetails(conversationId, userId);
        ConversationDetailResponse response = new ConversationDetailResponse(conversation);

        log.infof("Conversation details retrieved: id=%s, name=%s", conversation.id, conversation.name);
        return Response.ok(response).build();
    }

    /**
     * Arquiva ou desarquiva uma conversa (toggle).
     *
     * PUT /api/v1/conversations/{conversationId}/archive
     */
    @PUT
    @Path("/{conversationId}/archive")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response archiveConversation(
            @PathParam("conversationId") UUID conversationId,
            @Context SecurityContext securityContext) {

        log.infof("PUT /api/v1/conversations/%s/archive - Toggling archive", conversationId);

        // TODO: Extrair userId do SecurityContext
        UUID userId = UUID.randomUUID(); // PLACEHOLDER

        // TODO: Implementar toggle real (buscar estado atual e inverter)
        // Por enquanto, sempre arquiva (true)
        conversationService.archiveConversation(conversationId, userId, true);

        log.infof("Conversation %s archived", conversationId);
        return Response.noContent().build();
    }

    /**
     * Fixa ou desfixa uma conversa (toggle).
     *
     * PUT /api/v1/conversations/{conversationId}/pin
     */
    @PUT
    @Path("/{conversationId}/pin")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response pinConversation(
            @PathParam("conversationId") UUID conversationId,
            @Context SecurityContext securityContext) {

        log.infof("PUT /api/v1/conversations/%s/pin - Toggling pin", conversationId);

        // TODO: Extrair userId do SecurityContext
        UUID userId = UUID.randomUUID(); // PLACEHOLDER

        // TODO: Implementar toggle real (buscar estado atual e inverter)
        // Por enquanto, sempre fixa (true)
        conversationService.pinConversation(conversationId, userId, true);

        log.infof("Conversation %s pinned", conversationId);
        return Response.noContent().build();
    }

    /**
     * Adiciona um participante ao grupo.
     *
     * POST /api/v1/conversations/{conversationId}/participants
     * Body: { "userId": "uuid" }
     */
    @POST
    @Path("/{conversationId}/participants")
    @RolesAllowed({ "USER", "ADMIN", "CHANNEL_OWNER", "MANAGER" })
    public Response addParticipant(
            @PathParam("conversationId") UUID conversationId,
            Map<String, String> body,
            @Context SecurityContext securityContext) {

        UUID newUserId = UUID.fromString(body.get("userId"));

        log.infof("POST /api/v1/conversations/%s/participants - Adding user %s", conversationId, newUserId);

        // TODO: Extrair userId do SecurityContext
        UUID adminId = UUID.randomUUID(); // PLACEHOLDER

        conversationService.addParticipant(conversationId, newUserId, adminId);

        log.infof("User %s added to conversation %s", newUserId, conversationId);
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * Remove um participante do grupo.
     *
     * DELETE /api/v1/conversations/{conversationId}/participants/{userId}
     */
    @DELETE
    @Path("/{conversationId}/participants/{userId}")
    @RolesAllowed({ "USER", "ADMIN", "CHANNEL_OWNER", "MANAGER" })
    public Response removeParticipant(
            @PathParam("conversationId") UUID conversationId,
            @PathParam("userId") UUID userIdToRemove,
            @Context SecurityContext securityContext) {

        log.infof("DELETE /api/v1/conversations/%s/participants/%s - Removing participant", conversationId,
                userIdToRemove);

        // TODO: Extrair userId do SecurityContext
        UUID requesterId = UUID.randomUUID(); // PLACEHOLDER

        conversationService.removeParticipant(conversationId, userIdToRemove, requesterId);

        log.infof("User %s removed from conversation %s", userIdToRemove, conversationId);
        return Response.noContent().build();
    }

    /**
     * Retorna o total de conversas não lidas (badge count).
     *
     * GET /api/v1/conversations/unread-count
     */
    @GET
    @Path("/unread-count")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response getTotalUnreadCount(
            @Context SecurityContext securityContext) {

        log.info("GET /api/v1/conversations/unread-count");

        // TODO: Extrair userId do SecurityContext
        UUID userId = UUID.randomUUID(); // PLACEHOLDER

        long unreadCount = conversationService.getTotalUnreadCount(userId);

        log.infof("User %s has %d unread conversations", userId, unreadCount);
        return Response.ok(Map.of("unreadCount", unreadCount)).build();
    }
}
