package br.com.aguideptbr.features.ownership.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.aguideptbr.features.ownership.OwnershipStatus;

/**
 * DTO for content ownership validation response.
 *
 * Returned after validating ownership claim.
 */
public class ValidateOwnershipResponse {

    private UUID ownershipId;
    private UUID userId;
    private UUID contentId;
    private String youtubeChannelId;
    private String contentChannelId;
    private OwnershipStatus status;
    private String validationHash;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private String message;

    // Construtores
    public ValidateOwnershipResponse() {
    }

    public ValidateOwnershipResponse(
            UUID ownershipId,
            UUID userId,
            UUID contentId,
            String youtubeChannelId,
            String contentChannelId,
            OwnershipStatus status,
            String validationHash,
            LocalDateTime verifiedAt,
            LocalDateTime createdAt,
            String message) {
        this.ownershipId = ownershipId;
        this.userId = userId;
        this.contentId = contentId;
        this.youtubeChannelId = youtubeChannelId;
        this.contentChannelId = contentChannelId;
        this.status = status;
        this.validationHash = validationHash;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
        this.message = message;
    }

    // Getters e Setters
    public UUID getOwnershipId() {
        return ownershipId;
    }

    public void setOwnershipId(UUID ownershipId) {
        this.ownershipId = ownershipId;
    }

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

    public String getYoutubeChannelId() {
        return youtubeChannelId;
    }

    public void setYoutubeChannelId(String youtubeChannelId) {
        this.youtubeChannelId = youtubeChannelId;
    }

    public String getContentChannelId() {
        return contentChannelId;
    }

    public void setContentChannelId(String contentChannelId) {
        this.contentChannelId = contentChannelId;
    }

    public OwnershipStatus getStatus() {
        return status;
    }

    public void setStatus(OwnershipStatus status) {
        this.status = status;
    }

    public String getValidationHash() {
        return validationHash;
    }

    public void setValidationHash(String validationHash) {
        this.validationHash = validationHash;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
