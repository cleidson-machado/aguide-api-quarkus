package br.com.aguideptbr.features.usermessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.usermessage.dto.MessageResponse;
import br.com.aguideptbr.features.usermessage.dto.SendMessageRequest;
import br.com.aguideptbr.util.PaginatedResponse;
import br.com.aguideptbr.util.SecurityUtils;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
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
 * Controller REST para gerenciamento de mensagens.
 *
 * Endpoints:
 * - POST /api/v1/messages - Enviar mensagem
 * - GET /api/v1/messages/conversation/{conversationId} - Listar mensagens
 * (paginado)
 * - GET /api/v1/messages/{messageId} - Buscar mensagem por ID
 * - PUT /api/v1/messages/{messageId}/read - Marcar como lida
 * - PUT /api/v1/messages/{messageId} - Editar mensagem
 * - DELETE /api/v1/messages/{messageId} - Deletar mensagem
 * - GET /api/v1/messages/conversation/{conversationId}/search - Buscar
 * mensagens
 * - GET /api/v1/messages/{messageId}/replies - Buscar respostas (thread)
 */
@Path("/api/v1/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessageController {

    private final MessageService messageService;
    private final Logger log;

    public MessageController(MessageService messageService, Logger log) {
        this.messageService = messageService;
        this.log = log;
    }

    /**
     * Envia uma nova mensagem.
     *
     * POST /api/v1/messages
     * Body: { "conversationId": "uuid", "content": "text", "messageType": "TEXT",
     * "parentMessageId": "uuid" }
     */
    @POST
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response sendMessage(
            @Valid SendMessageRequest request,
            @HeaderParam("Authorization") String authHeader) {

        log.infof("POST /api/v1/messages - Sending message to conversation %s", request.getConversationId());

        UUID senderId = SecurityUtils.extractUserIdFromToken(authHeader);

        UserMessageModel message = messageService.sendMessage(
                senderId,
                request.getConversationId(),
                request.getContent(),
                request.getMessageType(),
                request.getParentMessageId());

        MessageResponse response = new MessageResponse(message);

        log.infof("Message sent successfully: id=%s", message.id);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * Lista mensagens de uma conversa (paginado).
     *
     * GET /api/v1/messages/conversation/{conversationId}?page=0&size=20
     */
    @GET
    @Path("/conversation/{conversationId}")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response getMessagesByConversation(
            @PathParam("conversationId") UUID conversationId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @HeaderParam("Authorization") String authHeader) {

        if (page < 0) {
            log.warnf("Invalid pagination request for conversation %s: page=%d", conversationId, page);
            throw new BadRequestException("Page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            log.warnf("Invalid pagination request for conversation %s: size=%d", conversationId, size);
            throw new BadRequestException("Size must be between 1 and 100");
        }

        if (page < 0) {
            log.warnf("Invalid pagination request for conversation %s: page=%d", conversationId, page);
            throw new BadRequestException("Page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            log.warnf("Invalid pagination request for conversation %s: size=%d", conversationId, size);
            throw new BadRequestException("Size must be between 1 and 100");
        }

        log.infof("GET /api/v1/messages/conversation/%s - page=%d, size=%d", conversationId, page, size);

        UUID userId = SecurityUtils.extractUserIdFromToken(authHeader);

        List<UserMessageModel> messages = messageService.getMessagesByConversation(conversationId, userId, page, size);
        List<MessageResponse> messageResponses = messages.stream()
                .map(MessageResponse::new)
                .toList();

        // Calcular total de mensagens para paginação
        long totalMessages = messageService.countTotalMessages(conversationId);
        int totalPages = (int) Math.ceil((double) totalMessages / size);

        PaginatedResponse<MessageResponse> response = new PaginatedResponse<>(
                messageResponses,
                totalMessages,
                totalPages,
                page);

        log.infof("Found %d messages for conversation %s", messageResponses.size(), conversationId);
        return Response.ok(response).build();
    }

    /**
     * Busca uma mensagem por ID.
     *
     * GET /api/v1/messages/{messageId}
     */
    @GET
    @Path("/{messageId}")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response getMessageById(
            @PathParam("messageId") UUID messageId,
            @HeaderParam("Authorization") String authHeader) {

        log.infof("GET /api/v1/messages/%s", messageId);

        // TODO: Implementar validação de permissão e busca real por ID
        // Por enquanto retorna NotImplementedException
        throw new jakarta.ws.rs.NotSupportedException("Endpoint not yet implemented");
    }

    /**
     * Marca uma mensagem como lida.
     *
     * PUT /api/v1/messages/{messageId}/read
     */
    @PUT
    @Path("/{messageId}/read")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response markAsRead(
            @PathParam("messageId") UUID messageId,
            @HeaderParam("Authorization") String authHeader) {

        log.infof("PUT /api/v1/messages/%s/read - Marking as read", messageId);

        UUID userId = SecurityUtils.extractUserIdFromToken(authHeader);

        messageService.markAsRead(messageId, userId);

        log.infof("Message %s marked as read", messageId);
        return Response.noContent().build();
    }

    /**
     * Edita uma mensagem.
     *
     * PUT /api/v1/messages/{messageId}
     * Body: { "content": "new text" }
     */
    @PUT
    @Path("/{messageId}")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response editMessage(
            @PathParam("messageId") UUID messageId,
            Map<String, String> body,
            @HeaderParam("Authorization") String authHeader) {

        log.infof("PUT /api/v1/messages/%s - Editing message", messageId);

        UUID userId = SecurityUtils.extractUserIdFromToken(authHeader);

        String newContent = body.get("content");
        UserMessageModel message = messageService.editMessage(messageId, userId, newContent);
        MessageResponse response = new MessageResponse(message);

        log.infof("Message %s edited successfully", messageId);
        return Response.ok(response).build();
    }

    /**
     * Deleta uma mensagem (soft delete).
     *
     * DELETE /api/v1/messages/{messageId}
     */
    @DELETE
    @Path("/{messageId}")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response deleteMessage(
            @PathParam("messageId") UUID messageId,
            @HeaderParam("Authorization") String authHeader) {

        log.infof("DELETE /api/v1/messages/%s - Deleting message", messageId);

        UUID userId = SecurityUtils.extractUserIdFromToken(authHeader);

        messageService.deleteMessage(messageId, userId);

        log.infof("Message %s deleted successfully", messageId);
        return Response.noContent().build();
    }

    /**
     * Busca mensagens por texto em uma conversa.
     *
     * GET /api/v1/messages/conversation/{conversationId}/search?query=text
     */
    @GET
    @Path("/conversation/{conversationId}/search")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response searchMessages(
            @PathParam("conversationId") UUID conversationId,
            @QueryParam("query") String query,
            @HeaderParam("Authorization") String authHeader) {

        log.infof("GET /api/v1/messages/conversation/%s/search?query=%s", conversationId, query);

        UUID userId = SecurityUtils.extractUserIdFromToken(authHeader);

        List<UserMessageModel> messages = messageService.searchMessages(conversationId, query, userId);
        List<MessageResponse> response = messages.stream()
                .map(MessageResponse::new)
                .toList();

        log.infof("Found %d messages matching query '%s'", response.size(), query);
        return Response.ok(response).build();
    }

    /**
     * Busca respostas a uma mensagem (thread).
     *
     * GET /api/v1/messages/{messageId}/replies
     */
    @GET
    @Path("/{messageId}/replies")
    @RolesAllowed({ "USER", "ADMIN", "FREE", "PREMIUM_USER", "CHANNEL_OWNER", "MANAGER" })
    public Response getThreadReplies(
            @PathParam("messageId") UUID messageId,
            @HeaderParam("Authorization") String authHeader) {

        log.infof("GET /api/v1/messages/%s/replies - Getting thread", messageId);

        UUID userId = SecurityUtils.extractUserIdFromToken(authHeader);

        List<UserMessageModel> replies = messageService.getThreadReplies(messageId, userId);
        List<MessageResponse> response = replies.stream()
                .map(MessageResponse::new)
                .toList();

        log.infof("Found %d replies for message %s", response.size(), messageId);
        return Response.ok(response).build();
    }
}
