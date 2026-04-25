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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
            @HeaderParam("Authorization") String authHeader) {

        log.infof("POST /api/v1/conversations/direct - Creating conversation with user %s", request.getOtherUserId());

        UUID currentUserId = SecurityUtils.extractUserIdFromToken(authHeader);

        // Verificar se já existe antes de criar (para determinar status HTTP correto)
        boolean alreadyExists = conversationService.directConversationExists(currentUserId, request.getOtherUserId());

        ConversationModel conversation = conversationService.createDirectConversation(
                currentUserId,
                request.getOtherUserId());

        ConversationDetailResponse response = new ConversationDetailResponse(conversation);

        // POST idempotente: 200 se já existia, 201 se foi criada agora
        Response.Status status = alreadyExists ? Response.Status.OK : Response.Status.CREATED;
        log.infof("Direct conversation %s: id=%s", alreadyExists ? "found (existing)" : "created", conversation.id);
        return Response.status(status).entity(response).build();
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
            @HeaderParam("Authorization") String authHeader) {

        log.infof("POST /api/v1/conversations/group - Creating group '%s' with %d participants",
                request.getName(), request.getParticipantIds() != null ? request.getParticipantIds().size() : 0);

        UUID creatorId = SecurityUtils.extractUserIdFromToken(authHeader);

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
            @HeaderParam("Authorization") String authHeader) {

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
            @HeaderParam("Authorization") String authHeader) {

        log.infof("GET /api/v1/conversations/%s", conversationId);

        UUID userId = SecurityUtils.extractUserIdFromToken(authHeader);

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
            @HeaderParam("Authorization") String authHeader) {

        log.infof("PUT /api/v1/conversations/%s/archive - Toggling archive", conversationId);

        UUID userId = SecurityUtils.extractUserIdFromToken(authHeader);

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
            @HeaderParam("Authorization") String authHeader) {

        log.infof("PUT /api/v1/conversations/%s/pin - Toggling pin", conversationId);

        UUID userId = SecurityUtils.extractUserIdFromToken(authHeader);

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
            @HeaderParam("Authorization") String authHeader) {

        UUID newUserId = UUID.fromString(body.get("userId"));

        log.infof("POST /api/v1/conversations/%s/participants - Adding user %s", conversationId, newUserId);

        UUID adminId = SecurityUtils.extractUserIdFromToken(authHeader);

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
            @HeaderParam("Authorization") String authHeader) {

        log.infof("DELETE /api/v1/conversations/%s/participants/%s - Removing participant", conversationId,
                userIdToRemove);

        UUID requesterId = SecurityUtils.extractUserIdFromToken(authHeader);

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
            @HeaderParam("Authorization") String authHeader) {

        log.info("GET /api/v1/conversations/unread-count");

        UUID userId = SecurityUtils.extractUserIdFromToken(authHeader);

        long unreadCount = conversationService.getTotalUnreadCount(userId);

        log.infof("User %s has %d unread conversations", userId, unreadCount);
        return Response.ok(Map.of("unreadCount", unreadCount)).build();
    }
}
