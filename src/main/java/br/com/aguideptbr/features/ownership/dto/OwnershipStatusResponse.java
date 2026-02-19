package br.com.aguideptbr.features.ownership.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.aguideptbr.features.ownership.OwnershipStatus;

/**
 * DTO for ownership status query response.
 *
 * Used to check the current ownership status of a specific content.
 */
public class OwnershipStatusResponse {

    private UUID ownershipId;
    private UUID userId;
    private UUID contentId;
    private OwnershipStatus status;
    private boolean isVerified;
    private boolean channelsMatch;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;

    // Construtores
    public OwnershipStatusResponse() {
    }

    public OwnershipStatusResponse(
            UUID ownershipId,
            UUID userId,
            UUID contentId,
            OwnershipStatus status,
            boolean isVerified,
            boolean channelsMatch,
            LocalDateTime verifiedAt,
            LocalDateTime createdAt) {
        this.ownershipId = ownershipId;
        this.userId = userId;
        this.contentId = contentId;
        this.status = status;
        this.isVerified = isVerified;
        this.channelsMatch = channelsMatch;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
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

    public OwnershipStatus getStatus() {
        return status;
    }

    public void setStatus(OwnershipStatus status) {
        this.status = status;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isChannelsMatch() {
        return channelsMatch;
    }

    public void setChannelsMatch(boolean channelsMatch) {
        this.channelsMatch = channelsMatch;
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
}
