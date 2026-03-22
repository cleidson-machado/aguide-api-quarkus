package br.com.aguideptbr.features.engagement.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.aguideptbr.features.engagement.ContentEngagementModel;
import br.com.aguideptbr.features.engagement.EngagementStatus;
import br.com.aguideptbr.features.engagement.EngagementType;

/**
 * DTO for returning engagement data in API responses.
 * Contains all relevant engagement information.
 */
public class EngagementResponseDTO {

    private UUID id;
    private UUID userId;
    private UUID contentId;
    private EngagementType engagementType;
    private EngagementStatus engagementStatus;
    private Integer viewDurationSeconds;
    private Integer completionPercentage;
    private Integer repeatCount;
    private String deviceType;
    private String platform;
    private String source;
    private String metadata;
    private String commentText;
    private Integer rating;
    private LocalDateTime engagedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public EngagementResponseDTO() {
    }

    /**
     * Converts a ContentEngagementModel entity to DTO.
     *
     * @param entity The engagement entity
     */
    public EngagementResponseDTO(ContentEngagementModel entity) {
        this.id = entity.id;
        this.userId = entity.userId;
        this.contentId = entity.contentId;
        this.engagementType = entity.engagementType;
        this.engagementStatus = entity.engagementStatus;
        this.viewDurationSeconds = entity.viewDurationSeconds;
        this.completionPercentage = entity.completionPercentage;
        this.repeatCount = entity.repeatCount;
        this.deviceType = entity.deviceType;
        this.platform = entity.platform;
        this.source = entity.source;
        this.metadata = entity.metadata;
        this.commentText = entity.commentText;
        this.rating = entity.rating;
        this.engagedAt = entity.getEngagedAt();
        this.endedAt = entity.getEndedAt();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public EngagementType getEngagementType() {
        return engagementType;
    }

    public void setEngagementType(EngagementType engagementType) {
        this.engagementType = engagementType;
    }

    public EngagementStatus getEngagementStatus() {
        return engagementStatus;
    }

    public void setEngagementStatus(EngagementStatus engagementStatus) {
        this.engagementStatus = engagementStatus;
    }

    public Integer getViewDurationSeconds() {
        return viewDurationSeconds;
    }

    public void setViewDurationSeconds(Integer viewDurationSeconds) {
        this.viewDurationSeconds = viewDurationSeconds;
    }

    public Integer getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Integer completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDateTime getEngagedAt() {
        return engagedAt;
    }

    public void setEngagedAt(LocalDateTime engagedAt) {
        this.engagedAt = engagedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
