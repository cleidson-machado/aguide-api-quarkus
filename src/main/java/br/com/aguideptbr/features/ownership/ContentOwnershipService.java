package br.com.aguideptbr.features.ownership;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import br.com.aguideptbr.features.content.ContentRecordModel;
import br.com.aguideptbr.features.ownership.dto.OwnershipStatusResponse;
import br.com.aguideptbr.features.ownership.dto.UserContentResponse;
import br.com.aguideptbr.features.ownership.dto.ValidateOwnershipRequest;
import br.com.aguideptbr.features.ownership.dto.ValidateOwnershipResponse;
import br.com.aguideptbr.features.user.UserModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

/**
 * Service responsible for content ownership validation using HMAC-SHA256
 * cryptographic verification.
 *
 * Security model:
 * - User's youtubeChannelId must match content's channelId
 * - HMAC hash is ALWAYS recalculated on backend (never trust client)
 * - Secret key managed via environment variable
 * - User can only validate ownership for their own userId
 */
@ApplicationScoped
public class ContentOwnershipService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final Logger log;
    private final String ownershipSecretKey;

    public ContentOwnershipService(
            Logger log,
            @ConfigProperty(name = "ownership.validation.secret") String ownershipSecretKey) {
        this.log = log;
        this.ownershipSecretKey = ownershipSecretKey;
    }

    /**
     * Validates content ownership by checking channel IDs and generating HMAC
     * hash.
     *
     * IDEMPOTENCY: Multiple calls with same userId+contentId update the same
     * record.
     * Each attempt increments retry_count and updates last_attempt_at.
     *
     * Process:
     * 1. Verify user and content exist
     * 2. Check if user has YouTube channel ID
     * 3. Verify channel IDs match
     * 4. Calculate HMAC-SHA256 hash
     * 5. Create/update ownership record (IDEMPOTENT)
     * 6. Update content_record.validation_hash if verified
     *
     * @param request Validation request with userId and contentId
     * @return ValidateOwnershipResponse with validation result
     * @throws WebApplicationException if validation fails
     */
    @Transactional
    public ValidateOwnershipResponse validateOwnership(ValidateOwnershipRequest request) {
        log.infof("🔐 Validating ownership: userId=%s, contentId=%s",
                request.getUserId(), request.getContentId());

        // 1. Verify user exists
        UserModel user = UserModel.findById(request.getUserId());
        if (user == null || user.deletedAt != null) {
            log.warnf("⚠️ User not found or deleted: %s", request.getUserId());
            throw new WebApplicationException("User not found", Status.NOT_FOUND);
        }

        // 2. Verify content exists
        ContentRecordModel content = ContentRecordModel.findById(request.getContentId());
        if (content == null) {
            log.warnf("⚠️ Content not found: %s", request.getContentId());
            throw new WebApplicationException("Content not found", Status.NOT_FOUND);
        }

        // 3. Check if ownership already exists (IDEMPOTENCY)
        ContentOwnershipModel ownership = ContentOwnershipModel.findByUserAndContent(
                request.getUserId(), request.getContentId());

        // 4. Check if user has YouTube channel ID
        if (user.youtubeChannelId == null || user.youtubeChannelId.isBlank()) {
            log.warnf("⚠️ User has no YouTube channel ID: %s", user.id);
            return buildRejectedResponse(
                    ownership,
                    request.getUserId(),
                    request.getContentId(),
                    user.youtubeChannelId,
                    content.channelId,
                    "NO_CHANNEL",
                    "User has no YouTube channel ID");
        }

        // 5. Verify channel IDs match
        boolean channelsMatch = user.youtubeChannelId.equals(content.channelId);
        if (!channelsMatch) {
            log.warnf("⚠️ Channel IDs mismatch: user=%s, content=%s",
                    user.youtubeChannelId, content.channelId);
            return buildRejectedResponse(
                    ownership,
                    request.getUserId(),
                    request.getContentId(),
                    user.youtubeChannelId,
                    content.channelId,
                    "CHANNEL_MISMATCH",
                    "Channel IDs do not match");
        }

        // 6. Calculate HMAC-SHA256 hash
        String validationHash = calculateHMAC(
                request.getUserId(),
                request.getContentId(),
                user.youtubeChannelId,
                content.channelId);

        // 7. Update or create ownership (IDEMPOTENT)
        if (ownership != null) {
            log.infof("ℹ️ Ownership exists (retry #%d), updating to VERIFIED", ownership.retryCount);
            ownership.ownershipStatus = OwnershipStatus.VERIFIED;
            ownership.validationHash = validationHash;
            ownership.verifiedAt = LocalDateTime.now();
            ownership.rejectionReason = null; // Clear previous rejection
            ownership.cancelledByUser = false; // Reset cancellation flag
            ownership.recordAttempt(); // Increment retry_count and update last_attempt_at
            ownership.persist();
        } else {
            log.infof("✅ Creating new verified ownership");
            ownership = new ContentOwnershipModel();
            ownership.userId = request.getUserId();
            ownership.contentId = request.getContentId();
            ownership.youtubeChannelId = user.youtubeChannelId;
            ownership.contentChannelId = content.channelId;
            ownership.ownershipStatus = OwnershipStatus.VERIFIED;
            ownership.validationHash = validationHash;
            ownership.verifiedAt = LocalDateTime.now();
            ownership.retryCount = 0; // First attempt
            ownership.lastAttemptAt = LocalDateTime.now();
            ownership.persist();
        }

        // 8. Update content_record.validation_hash for fast queries
        content.validationHash = validationHash;
        content.persist();

        log.infof("✅ Ownership validated successfully: ownershipId=%s, retryCount=%d",
                ownership.id, ownership.retryCount);

        return new ValidateOwnershipResponse(
                ownership.id,
                ownership.userId,
                ownership.contentId,
                ownership.youtubeChannelId,
                ownership.contentChannelId,
                ownership.ownershipStatus,
                ownership.validationHash,
                ownership.verifiedAt,
                ownership.getCreatedAt(),
                "Ownership validated successfully");
    }

    /**
     * Get ownership status for specific user and content.
     *
     * @param userId    User ID
     * @param contentId Content ID
     * @return OwnershipStatusResponse with current status
     * @throws WebApplicationException if ownership not found
     */
    public OwnershipStatusResponse getOwnershipStatus(UUID userId, UUID contentId) {
        log.infof("ℹ️ Getting ownership status: userId=%s, contentId=%s", userId, contentId);

        ContentOwnershipModel ownership = ContentOwnershipModel.findByUserAndContent(userId,
                contentId);

        if (ownership == null) {
            log.warnf("⚠️ Ownership not found: userId=%s, contentId=%s", userId, contentId);
            throw new WebApplicationException("Ownership not found", Status.NOT_FOUND);
        }

        boolean channelsMatch = ownership.youtubeChannelId.equals(ownership.contentChannelId);
        boolean isVerified = ownership.ownershipStatus == OwnershipStatus.VERIFIED;

        return new OwnershipStatusResponse(
                ownership.id,
                ownership.userId,
                ownership.contentId,
                ownership.ownershipStatus,
                isVerified,
                channelsMatch,
                ownership.verifiedAt,
                ownership.getCreatedAt());
    }

    /**
     * Get all verified content for a user.
     *
     * @param userId User ID
     * @return List of UserContentResponse with verified content
     * @throws WebApplicationException if user not found or has no verified content
     */
    public List<UserContentResponse> getUserVerifiedContent(UUID userId) {
        log.infof("ℹ️ Getting verified content for user: %s", userId);

        // 1. Verify user exists
        UserModel user = UserModel.findById(userId);
        if (user == null || user.deletedAt != null) {
            log.warnf("⚠️ User not found or deleted: %s", userId);
            throw new WebApplicationException(
                    "User not found",
                    Status.NOT_FOUND);
        }

        // 2. Find verified ownerships for this user
        List<ContentOwnershipModel> verifiedOwnerships = ContentOwnershipModel
                .findVerifiedByUserId(userId);

        // 3. Check if user has any verified content
        if (verifiedOwnerships.isEmpty()) {
            log.warnf("⚠️ User %s has no verified content", userId);
            throw new WebApplicationException(
                    "No verified content found for this user",
                    Status.NOT_FOUND);
        }

        // 4. Build response list
        List<UserContentResponse> contentList = new ArrayList<>();

        for (ContentOwnershipModel ownership : verifiedOwnerships) {
            ContentRecordModel content = ContentRecordModel.findById(ownership.contentId);

            if (content != null) {
                UserContentResponse response = new UserContentResponse(
                        content.id,
                        content.title,
                        content.description,
                        content.videoUrl,
                        content.videoThumbnailUrl,
                        content.channelId,
                        content.channelName,
                        content.getPublishedAt(),
                        ownership.id,
                        ownership.validationHash,
                        ownership.verifiedAt,
                        true);

                contentList.add(response);
            }
        }

        log.infof("✅ Found %d verified content items for user %s",
                contentList.size(), userId);

        return contentList;
    }

    /**
     * Calculate HMAC-SHA256 hash for ownership validation.
     *
     * Formula: HMAC(userId + contentId + youtubeChannelId + contentChannelId,
     * secretKey)
     *
     * @param userId           User ID
     * @param contentId        Content ID
     * @param youtubeChannelId YouTube channel ID from user
     * @param contentChannelId YouTube channel ID from content
     * @return Hexadecimal HMAC-SHA256 hash
     */
    private String calculateHMAC(
            UUID userId,
            UUID contentId,
            String youtubeChannelId,
            String contentChannelId) {

        try {
            // Build data string
            String data = userId.toString() +
                    contentId.toString() +
                    youtubeChannelId +
                    contentChannelId;

            // Create HMAC instance
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    ownershipSecretKey.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM);
            mac.init(secretKeySpec);

            // Calculate hash
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert to hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.errorf(e, "❌ Error calculating HMAC: %s", e.getMessage());
            throw new WebApplicationException(
                    "Error calculating validation hash",
                    Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Build rejected ownership response with retry tracking.
     *
     * IDEMPOTENCY: Updates existing ownership or creates new one.
     * Increments retry_count and records rejection_reason.
     *
     * NOTE: No @Transactional needed - this private method is called from
     * validateOwnership() which already has @Transactional.
     */
    private ValidateOwnershipResponse buildRejectedResponse(
            ContentOwnershipModel existingOwnership,
            UUID userId,
            UUID contentId,
            String youtubeChannelId,
            String contentChannelId,
            String rejectionReason,
            String message) {

        ContentOwnershipModel ownership;

        if (existingOwnership != null) {
            // Update existing ownership (RETRY)
            log.infof("ℹ️ Updating existing ownership to REJECTED (retry #%d)",
                    existingOwnership.retryCount);
            ownership = existingOwnership;
            ownership.ownershipStatus = OwnershipStatus.REJECTED;
            ownership.rejectionReason = rejectionReason;
            ownership.recordAttempt(); // Increment retry_count and update timestamp
            ownership.persist();
        } else {
            // Create new rejected ownership
            log.infof("⚠️ Creating new REJECTED ownership: %s", rejectionReason);
            ownership = new ContentOwnershipModel();
            ownership.userId = userId;
            ownership.contentId = contentId;
            ownership.youtubeChannelId = youtubeChannelId != null ? youtubeChannelId : "";
            ownership.contentChannelId = contentChannelId != null ? contentChannelId : "";
            ownership.ownershipStatus = OwnershipStatus.REJECTED;
            ownership.rejectionReason = rejectionReason;
            ownership.validationHash = ""; // No hash for rejected
            ownership.retryCount = 0; // First attempt
            ownership.lastAttemptAt = LocalDateTime.now();
            ownership.persist();
        }

        return new ValidateOwnershipResponse(
                ownership.id,
                ownership.userId,
                ownership.contentId,
                ownership.youtubeChannelId,
                ownership.contentChannelId,
                ownership.ownershipStatus,
                "",
                null,
                ownership.getCreatedAt(),
                message);
    }

    /**
     * Cancel ownership claim by user.
     * Sets status to REJECTED with reason USER_CANCELLED.
     *
     * @param userId    User ID
     * @param contentId Content ID
     * @return ValidateOwnershipResponse with cancellation result
     * @throws WebApplicationException if ownership not found
     */
    @Transactional
    public ValidateOwnershipResponse cancelOwnershipClaim(UUID userId, UUID contentId) {
        log.infof("🚫 Cancelling ownership claim: userId=%s, contentId=%s", userId, contentId);

        ContentOwnershipModel ownership = ContentOwnershipModel.findByUserAndContent(userId,
                contentId);

        if (ownership == null) {
            log.warnf("⚠️ Ownership not found for cancellation: userId=%s, contentId=%s",
                    userId, contentId);
            throw new WebApplicationException("Ownership not found", Status.NOT_FOUND);
        }

        // Mark as cancelled by user
        ownership.cancelByUser();
        ownership.persist();

        log.infof("✅ Ownership cancelled by user: ownershipId=%s", ownership.id);

        return new ValidateOwnershipResponse(
                ownership.id,
                ownership.userId,
                ownership.contentId,
                ownership.youtubeChannelId,
                ownership.contentChannelId,
                ownership.ownershipStatus,
                "",
                null,
                ownership.getCreatedAt(),
                "Ownership claim cancelled by user");
    }
}
