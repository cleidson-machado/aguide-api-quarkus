package br.com.aguideptbr.features.engagement.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.aguideptbr.features.engagement.EngagementStatus;
import br.com.aguideptbr.features.engagement.EngagementType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating new content engagement records.
 * Used when users interact with content (view, like, share, etc.).
 */
public class CreateEngagementDTO {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Content ID is required")
    private UUID contentId;

    @NotNull(message = "Engagement type is required")
    private EngagementType engagementType;

    private EngagementStatus engagementStatus;

    // Viewing metrics
    @Min(value = 0, message = "View duration cannot be negative")
    private Integer viewDurationSeconds;

    @Min(value = 0, message = "Completion percentage must be between 0 and 100")
    @Max(value = 100, message = "Completion percentage must be between 0 and 100")
    private Integer completionPercentage;

    @Min(value = 1, message = "Repeat count must be at least 1")
    private Integer repeatCount;

    // Technical context
    private String deviceType;
    private String platform;
    private String source;
    private String userIp;
    private String userAgent;

    // Additional data
    private String metadata;
    private String commentText;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    // Timestamps
    private LocalDateTime engagedAt;
    private LocalDateTime endedAt;

    // Constructors
    public CreateEngagementDTO() {
    }

    public CreateEngagementDTO(UUID userId, UUID contentId, EngagementType engagementType) {
        this.userId = userId;
        this.contentId = contentId;
        this.engagementType = engagementType;
        this.engagementStatus = EngagementStatus.ACTIVE;
        this.repeatCount = 1;
    }

    // Getters and Setters
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

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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
}
