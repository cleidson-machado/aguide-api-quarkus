package br.com.aguideptbr.features.engagement.dto;

import java.util.UUID;

/**
 * DTO representing content that a user has interacted with the most.
 * Used for analytics and recommendations.
 */
public class UserTopContentsDTO {

    private UUID contentId;
    private String contentTitle;
    private String contentUrl;
    private String thumbnailUrl;
    private Long totalInteractions;
    private Long totalViews;
    private Long totalLikes;
    private Long totalShares;
    private Integer avgCompletionPercentage;
    private Integer totalViewDurationSeconds;

    // Constructors
    public UserTopContentsDTO() {
    }

    public UserTopContentsDTO(UUID contentId, Long totalInteractions) {
        this.contentId = contentId;
        this.totalInteractions = totalInteractions;
    }

    // Builder pattern for easy construction
    public static class Builder {
        private UUID contentId;
        private String contentTitle;
        private String contentUrl;
        private String thumbnailUrl;
        private Long totalInteractions;
        private Long totalViews;
        private Long totalLikes;
        private Long totalShares;
        private Integer avgCompletionPercentage;
        private Integer totalViewDurationSeconds;

        public Builder contentId(UUID contentId) {
            this.contentId = contentId;
            return this;
        }

        public Builder contentTitle(String contentTitle) {
            this.contentTitle = contentTitle;
            return this;
        }

        public Builder contentUrl(String contentUrl) {
            this.contentUrl = contentUrl;
            return this;
        }

        public Builder thumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
            return this;
        }

        public Builder totalInteractions(Long totalInteractions) {
            this.totalInteractions = totalInteractions;
            return this;
        }

        public Builder totalViews(Long totalViews) {
            this.totalViews = totalViews;
            return this;
        }

        public Builder totalLikes(Long totalLikes) {
            this.totalLikes = totalLikes;
            return this;
        }

        public Builder totalShares(Long totalShares) {
            this.totalShares = totalShares;
            return this;
        }

        public Builder avgCompletionPercentage(Integer avgCompletionPercentage) {
            this.avgCompletionPercentage = avgCompletionPercentage;
            return this;
        }

        public Builder totalViewDurationSeconds(Integer totalViewDurationSeconds) {
            this.totalViewDurationSeconds = totalViewDurationSeconds;
            return this;
        }

        public UserTopContentsDTO build() {
            UserTopContentsDTO dto = new UserTopContentsDTO();
            dto.contentId = this.contentId;
            dto.contentTitle = this.contentTitle;
            dto.contentUrl = this.contentUrl;
            dto.thumbnailUrl = this.thumbnailUrl;
            dto.totalInteractions = this.totalInteractions;
            dto.totalViews = this.totalViews;
            dto.totalLikes = this.totalLikes;
            dto.totalShares = this.totalShares;
            dto.avgCompletionPercentage = this.avgCompletionPercentage;
            dto.totalViewDurationSeconds = this.totalViewDurationSeconds;
            return dto;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public UUID getContentId() {
        return contentId;
    }

    public void setContentId(UUID contentId) {
        this.contentId = contentId;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Long getTotalInteractions() {
        return totalInteractions;
    }

    public void setTotalInteractions(Long totalInteractions) {
        this.totalInteractions = totalInteractions;
    }

    public Long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Long totalViews) {
        this.totalViews = totalViews;
    }

    public Long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(Long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public Long getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(Long totalShares) {
        this.totalShares = totalShares;
    }

    public Integer getAvgCompletionPercentage() {
        return avgCompletionPercentage;
    }

    public void setAvgCompletionPercentage(Integer avgCompletionPercentage) {
        this.avgCompletionPercentage = avgCompletionPercentage;
    }

    public Integer getTotalViewDurationSeconds() {
        return totalViewDurationSeconds;
    }

    public void setTotalViewDurationSeconds(Integer totalViewDurationSeconds) {
        this.totalViewDurationSeconds = totalViewDurationSeconds;
    }
}
