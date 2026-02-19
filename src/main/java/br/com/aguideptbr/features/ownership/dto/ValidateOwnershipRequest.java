package br.com.aguideptbr.features.ownership.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for content ownership validation request.
 *
 * This request is sent by the Flutter app to validate ownership of content.
 * The backend will:
 * 1. Verify user's youtubeChannelId matches content's channelId
 * 2. Calculate HMAC-SHA256 hash
 * 3. Create/update ownership record
 * 4. Update content_record.validation_hash if verified
 */
public class ValidateOwnershipRequest {

    @NotNull(message = "User ID é obrigatório")
    private UUID userId;

    @NotNull(message = "Content ID é obrigatório")
    private UUID contentId;

    // Construtores
    public ValidateOwnershipRequest() {
    }

    public ValidateOwnershipRequest(UUID userId, UUID contentId) {
        this.userId = userId;
        this.contentId = contentId;
    }

    // Getters e Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getContentId() {
        return contentId;
    }

    public void setContentId(UUID contentId) {
        this.contentId = contentId;
    }
}
