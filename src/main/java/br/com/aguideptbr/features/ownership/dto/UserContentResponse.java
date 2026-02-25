package br.com.aguideptbr.features.ownership.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for user's verified content response.
 *
 * Includes both content details and ownership information.
 */
public class UserContentResponse {

    // Content information
    private UUID contentId;
    private String title;
    private String description;
    private String videoUrl;
    private String videoThumbnailUrl;
    private String channelId;
    private String channelName;
    private LocalDateTime publishedAt;

    // Ownership information
    private UUID ownershipId;
    private String validationHash;
    private LocalDateTime verifiedAt;
    private boolean isVerified;

    // Construtores
    public UserContentResponse() {
    }

    public UserContentResponse(
            UUID contentId,
            String title,
            String description,
            String videoUrl,
            String videoThumbnailUrl,
            String channelId,
            String channelName,
            LocalDateTime publishedAt,
            UUID ownershipId,
            String validationHash,
            LocalDateTime verifiedAt,
            boolean isVerified) {
        this.contentId = contentId;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.videoThumbnailUrl = videoThumbnailUrl;
        this.channelId = channelId;
        this.channelName = channelName;
        this.publishedAt = publishedAt;
        this.ownershipId = ownershipId;
        this.validationHash = validationHash;
        this.verifiedAt = verifiedAt;
        this.isVerified = isVerified;
    }

    // Getters e Setters
    public UUID getContentId() {
        return contentId;
    }

    public void setContentId(UUID contentId) {
        this.contentId = contentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoThumbnailUrl() {
        return videoThumbnailUrl;
    }

    public void setVideoThumbnailUrl(String videoThumbnailUrl) {
        this.videoThumbnailUrl = videoThumbnailUrl;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public UUID getOwnershipId() {
        return ownershipId;
    }

    public void setOwnershipId(UUID ownershipId) {
        this.ownershipId = ownershipId;
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

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
}
