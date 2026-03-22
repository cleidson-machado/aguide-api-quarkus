package br.com.aguideptbr.features.engagement.dto;

import java.time.LocalDateTime;

import br.com.aguideptbr.features.engagement.EngagementStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DTO for updating existing engagement records.
 * Only fields that can be updated are included.
 */
public class UpdateEngagementDTO {

    private EngagementStatus engagementStatus;

    @Min(value = 0, message = "View duration cannot be negative")
    private Integer viewDurationSeconds;

    @Min(value = 0, message = "Completion percentage must be between 0 and 100")
    @Max(value = 100, message = "Completion percentage must be between 0 and 100")
    private Integer completionPercentage;

    @Min(value = 1, message = "Repeat count must be at least 1")
    private Integer repeatCount;

    private String metadata;
    private String commentText;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private LocalDateTime endedAt;

    // Constructors
    public UpdateEngagementDTO() {
    }

    // Getters and Setters
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

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }
}
