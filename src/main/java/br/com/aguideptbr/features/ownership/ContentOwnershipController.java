package br.com.aguideptbr.features.ownership;

import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.ownership.dto.OwnershipStatusResponse;
import br.com.aguideptbr.features.ownership.dto.UserContentResponse;
import br.com.aguideptbr.features.ownership.dto.ValidateOwnershipRequest;
import br.com.aguideptbr.features.ownership.dto.ValidateOwnershipResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST Controller for content ownership validation.
 *
 * Endpoints:
 * - POST /api/v1/ownership/validate - Validate content ownership
 * - GET /api/v1/ownership/user/{userId}/content - List user's verified content
 * - GET /api/v1/ownership/status - Check ownership status
 * - GET /api/v1/ownership/pending - List pending ownerships (admin)
 * - POST /api/v1/ownership/cancel - Cancel ownership claim by user
 *
 * Features:
 * - Idempotent validation with retry tracking
 * - Audit trail for rejection reasons and attempts
 * - User cancellation support
 *
 * Security:
 * - All endpoints protected with JWT (AuthenticationFilter)
 * - User can only validate ownership for their own userId
 * - HMAC hash always recalculated on backend
 *
 * @author System
 * @since 1.0
 * @see ContentOwnershipService
 */
@Path("/api/v1/ownership")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContentOwnershipController {

    private final Logger log;
    private final ContentOwnershipService ownershipService;

    public ContentOwnershipController(Logger log, ContentOwnershipService ownershipService) {
        this.log = log;
        this.ownershipService = ownershipService;
    }

    /**
     * Validate content ownership.
     *
     * Process:
     * 1. Verify user and content exist
     * 2. Check if user's youtubeChannelId matches content's channelId
     * 3. Calculate HMAC-SHA256 hash
     * 4. Create/update ownership record
     * 5. Update content_record.validation_hash
     *
     * Example request:
     * POST /api/v1/ownership/validate
     * {
     * "userId": "550e8400-e29b-41d4-a716-446655440000",
     * "contentId": "660e8400-e29b-41d4-a716-446655440001"
     * }
     *
     * @param request ValidateOwnershipRequest with userId and contentId
     * @return ValidateOwnershipResponse with validation result
     */
    @POST
    @Path("/validate")
    public Response validateOwnership(@Valid ValidateOwnershipRequest request) {
        log.infof("POST /api/v1/ownership/validate - userId=%s, contentId=%s",
                request.getUserId(), request.getContentId());

        try {
            ValidateOwnershipResponse response = ownershipService.validateOwnership(request);

            if (response.getStatus() == OwnershipStatus.VERIFIED) {
                log.infof("✅ Ownership validated successfully: ownershipId=%s",
                        response.getOwnershipId());
                return Response.ok(response).build();
            } else {
                log.warnf("⚠️ Ownership validation rejected: %s", response.getMessage());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(response)
                        .build();
            }

        } catch (Exception e) {
            log.errorf(e, "❌ Error validating ownership: %s", e.getMessage());
            throw e;
        }
    }

    /**
     * List all verified content for a specific user.
     *
     * Returns content records where ownership has been verified.
     * Includes both content details and ownership information.
     *
     * Example:
     * GET /api/v1/ownership/user/550e8400-e29b-41d4-a716-446655440000/content
     *
     * @param userId User ID
     * @return List of UserContentResponse with verified content
     */
    @GET
    @Path("/user/{userId}/content")
    public Response getUserVerifiedContent(@PathParam("userId") UUID userId) {
        log.infof("GET /api/v1/ownership/user/%s/content - Listing verified content", userId);

        try {
            List<UserContentResponse> content = ownershipService.getUserVerifiedContent(userId);

            log.infof("✅ Found %d verified content items for user %s",
                    content.size(), userId);

            return Response.ok(content).build();

        } catch (Exception e) {
            log.errorf(e, "❌ Error getting user verified content: %s", e.getMessage());
            throw e;
        }
    }

    /**
     * Check ownership status for specific user and content.
     *
     * Returns current ownership status (PENDING, VERIFIED, REJECTED).
     *
     * Example:
     * GET
     * /api/v1/ownership/status?userId=550e8400-e29b-41d4-a716-446655440000&contentId=660e8400-e29b-41d4-a716-446655440001
     *
     * @param userId    User ID (required)
     * @param contentId Content ID (required)
     * @return OwnershipStatusResponse with current status
     */
    @GET
    @Path("/status")
    public Response getOwnershipStatus(
            @QueryParam("userId") UUID userId,
            @QueryParam("contentId") UUID contentId) {

        log.infof("GET /api/v1/ownership/status - userId=%s, contentId=%s",
                userId, contentId);

        if (userId == null || contentId == null) {
            log.warn("⚠️ Missing required parameters: userId or contentId");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("userId and contentId are required")
                    .build();
        }

        try {
            OwnershipStatusResponse response = ownershipService.getOwnershipStatus(userId,
                    contentId);

            log.infof("✅ Ownership status retrieved: status=%s", response.getStatus());

            return Response.ok(response).build();

        } catch (Exception e) {
            log.errorf(e, "❌ Error getting ownership status: %s", e.getMessage());
            throw e;
        }
    }

    /**
     * List all pending ownership validations (for admin use).
     *
     * Returns ownership records with PENDING status.
     *
     * Example:
     * GET /api/v1/ownership/pending
     *
     * @return List of ContentOwnershipModel with PENDING status
     */
    @GET
    @Path("/pending")
    public Response getPendingOwnerships() {
        log.info("GET /api/v1/ownership/pending - Listing pending ownerships");

        try {
            List<ContentOwnershipModel> pending = ContentOwnershipModel.findPending();

            log.infof("✅ Found %d pending ownerships", pending.size());

            return Response.ok(pending).build();

        } catch (Exception e) {
            log.errorf(e, "❌ Error getting pending ownerships: %s", e.getMessage());
            throw e;
        }
    }

    /**
     * Cancel ownership claim by user.
     *
     * Marks ownership as REJECTED with reason USER_CANCELLED.
     * Sets cancelled_by_user = true for audit purposes.
     *
     * Example:
     * POST
     * /api/v1/ownership/cancel?userId=550e8400-e29b-41d4-a716-446655440000&contentId=660e8400-e29b-41d4-a716-446655440001
     *
     * @param userId    User ID (required)
     * @param contentId Content ID (required)
     * @return ValidateOwnershipResponse with cancellation result
     */
    @POST
    @Path("/cancel")
    public Response cancelOwnership(
            @QueryParam("userId") UUID userId,
            @QueryParam("contentId") UUID contentId) {

        log.infof("POST /api/v1/ownership/cancel - userId=%s, contentId=%s",
                userId, contentId);

        if (userId == null || contentId == null) {
            log.warn("⚠️ Missing required parameters: userId or contentId");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("userId and contentId are required")
                    .build();
        }

        try {
            ValidateOwnershipResponse response = ownershipService.cancelOwnershipClaim(userId,
                    contentId);

            log.infof("✅ Ownership cancelled successfully: ownershipId=%s",
                    response.getOwnershipId());

            return Response.ok(response).build();

        } catch (Exception e) {
            log.errorf(e, "❌ Error cancelling ownership: %s", e.getMessage());
            throw e;
        }
    }
}
