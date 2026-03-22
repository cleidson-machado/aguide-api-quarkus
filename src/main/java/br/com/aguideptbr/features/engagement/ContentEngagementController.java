package br.com.aguideptbr.features.engagement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.engagement.dto.CreateEngagementDTO;
import br.com.aguideptbr.features.engagement.dto.EngagementResponseDTO;
import br.com.aguideptbr.features.engagement.dto.UpdateEngagementDTO;
import br.com.aguideptbr.features.engagement.dto.UserTopContentsDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST Controller for managing content engagement operations.
 * Handles user interactions with content such as views, likes, shares, etc.
 *
 * @author Cleidson Machado
 * @version 1.0
 */
@Path("/api/v1/engagements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContentEngagementController {

    private final Logger log;
    private final ContentEngagementService engagementService;

    public ContentEngagementController(Logger log, ContentEngagementService engagementService) {
        this.log = log;
        this.engagementService = engagementService;
    }

    /**
     * Creates a new engagement record.
     * POST /api/v1/engagements
     *
     * @param dto The engagement data
     * @return 201 Created with the engagement data
     */
    @POST
    public Response createEngagement(@Valid CreateEngagementDTO dto) {
        log.infof("POST /api/v1/engagements - Creating engagement: userId=%s, contentId=%s, type=%s",
                dto.getUserId(), dto.getContentId(), dto.getEngagementType());

        EngagementResponseDTO response = engagementService.createEngagement(dto);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    /**
     * Gets engagement by ID.
     * GET /api/v1/engagements/{id}
     *
     * @param id The engagement ID
     * @return 200 OK with engagement data
     */
    @GET
    @Path("/{id}")
    public Response getEngagement(@PathParam("id") UUID id) {
        log.infof("GET /api/v1/engagements/%s - Getting engagement", id);

        EngagementResponseDTO response = engagementService.getEngagement(id);

        return Response.ok(response).build();
    }

    /**
     * Updates an existing engagement.
     * PUT /api/v1/engagements/{id}
     *
     * @param id  The engagement ID
     * @param dto The update data
     * @return 200 OK with updated engagement data
     */
    @PUT
    @Path("/{id}")
    public Response updateEngagement(@PathParam("id") UUID id, @Valid UpdateEngagementDTO dto) {
        log.infof("PUT /api/v1/engagements/%s - Updating engagement", id);

        EngagementResponseDTO response = engagementService.updateEngagement(id, dto);

        return Response.ok(response).build();
    }

    /**
     * Deletes (soft delete) an engagement.
     * DELETE /api/v1/engagements/{id}
     *
     * @param id The engagement ID
     * @return 204 No Content
     */
    @DELETE
    @Path("/{id}")
    public Response deleteEngagement(@PathParam("id") UUID id) {
        log.infof("DELETE /api/v1/engagements/%s - Deleting engagement", id);

        engagementService.deleteEngagement(id);

        return Response.noContent().build();
    }

    /**
     * Gets all engagements for a specific user.
     * GET /api/v1/engagements/user/{userId}
     *
     * @param userId The user ID
     * @return 200 OK with list of engagements
     */
    @GET
    @Path("/user/{userId}")
    public Response getUserEngagements(@PathParam("userId") UUID userId) {
        log.infof("GET /api/v1/engagements/user/%s - Getting user engagements", userId);

        List<EngagementResponseDTO> engagements = engagementService.getUserEngagements(userId);

        return Response.ok(Map.of(
                "userId", userId,
                "total", engagements.size(),
                "engagements", engagements)).build();
    }

    /**
     * Gets all engagements for a specific content.
     * GET /api/v1/engagements/content/{contentId}
     *
     * @param contentId The content ID
     * @return 200 OK with list of engagements
     */
    @GET
    @Path("/content/{contentId}")
    public Response getContentEngagements(@PathParam("contentId") UUID contentId) {
        log.infof("GET /api/v1/engagements/content/%s - Getting content engagements", contentId);

        List<EngagementResponseDTO> engagements = engagementService.getContentEngagements(contentId);

        return Response.ok(Map.of(
                "contentId", contentId,
                "total", engagements.size(),
                "engagements", engagements)).build();
    }

    /**
     * Gets top contents that a user has interacted with the most.
     * This is a KEY endpoint for recommendations and user analytics.
     * GET /api/v1/engagements/user/{userId}/top-contents
     *
     * @param userId The user ID
     * @param limit  Maximum number of contents to return (default: 10)
     * @return 200 OK with list of top contents
     */
    @GET
    @Path("/user/{userId}/top-contents")
    public Response getUserTopContents(
            @PathParam("userId") UUID userId,
            @QueryParam("limit") @DefaultValue("10") int limit) {

        log.infof("GET /api/v1/engagements/user/%s/top-contents?limit=%d - Getting top contents", userId, limit);

        List<UserTopContentsDTO> topContents = engagementService.getUserTopContents(userId, limit);

        return Response.ok(Map.of(
                "userId", userId,
                "limit", limit,
                "total", topContents.size(),
                "topContents", topContents)).build();
    }

    /**
     * Gets engagement statistics for a content.
     * GET /api/v1/engagements/content/{contentId}/stats
     *
     * @param contentId The content ID
     * @return 200 OK with statistics map
     */
    @GET
    @Path("/content/{contentId}/stats")
    public Response getContentStats(@PathParam("contentId") UUID contentId) {
        log.infof("GET /api/v1/engagements/content/%s/stats - Getting content stats", contentId);

        Map<EngagementType, Long> stats = engagementService.getContentStats(contentId);

        return Response.ok(Map.of(
                "contentId", contentId,
                "timestamp", LocalDateTime.now(),
                "statistics", stats)).build();
    }

    /**
     * Gets user engagement statistics.
     * GET /api/v1/engagements/user/{userId}/stats
     *
     * @param userId The user ID
     * @return 200 OK with user statistics
     */
    @GET
    @Path("/user/{userId}/stats")
    public Response getUserStats(@PathParam("userId") UUID userId) {
        log.infof("GET /api/v1/engagements/user/%s/stats - Getting user stats", userId);

        Map<String, Object> stats = engagementService.getUserStats(userId);

        return Response.ok(Map.of(
                "userId", userId,
                "timestamp", LocalDateTime.now(),
                "statistics", stats)).build();
    }

    /**
     * Gets recent engagements for a user.
     * GET /api/v1/engagements/user/{userId}/recent
     *
     * @param userId The user ID
     * @param days   Number of days to look back (default: 7)
     * @return 200 OK with list of recent engagements
     */
    @GET
    @Path("/user/{userId}/recent")
    public Response getRecentEngagements(
            @PathParam("userId") UUID userId,
            @QueryParam("days") @DefaultValue("7") int days) {

        log.infof("GET /api/v1/engagements/user/%s/recent?days=%d - Getting recent engagements", userId, days);

        List<EngagementResponseDTO> engagements = engagementService.getRecentEngagements(userId, days);

        return Response.ok(Map.of(
                "userId", userId,
                "days", days,
                "total", engagements.size(),
                "engagements", engagements)).build();
    }
}
