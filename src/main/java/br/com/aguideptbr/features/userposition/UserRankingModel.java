package br.com.aguideptbr.features.userposition;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.aguideptbr.features.userposition.enuns.ConversionPotential;
import br.com.aguideptbr.features.userposition.enuns.EngagementLevel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user_ranking")
public class UserRankingModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "engagement_level", length = 20, nullable = false)
    private EngagementLevel engagementLevel = EngagementLevel.LOW;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversion_potential", length = 20, nullable = false)
    private ConversionPotential conversionPotential = ConversionPotential.VERY_LOW;

    @Column(name = "total_content_views", nullable = false)
    private Long totalContentViews = 0L;

    @Column(name = "unique_content_views", nullable = false)
    private Long uniqueContentViews = 0L;

    @Column(name = "avg_daily_usage_minutes", nullable = false)
    private Integer avgDailyUsageMinutes = 0;

    @Column(name = "consecutive_days_streak", nullable = false)
    private Integer consecutiveDaysStreak = 0;

    @Column(name = "total_active_days", nullable = false)
    private Long totalActiveDays = 0L;

    @Column(name = "total_messages_sent", nullable = false)
    private Long totalMessagesSent = 0L;

    @Column(name = "total_conversations_started", nullable = false)
    private Long totalConversationsStarted = 0L;

    @Column(name = "unique_contacts_messaged", nullable = false)
    private Long uniqueContactsMessaged = 0L;

    @Column(name = "active_conversations", nullable = false)
    private Integer activeConversations = 0;

    @Column(name = "has_phones", nullable = false)
    private Boolean hasPhones = false;

    @Column(name = "total_phones", nullable = false)
    private Integer totalPhones = 0;

    @Column(name = "has_whatsapp", nullable = false)
    private Boolean hasWhatsapp = false;

    @Column(name = "has_telegram", nullable = false)
    private Boolean hasTelegram = false;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "last_content_view_at")
    private LocalDateTime lastContentViewAt;

    @Column(name = "last_message_sent_at")
    private LocalDateTime lastMessageSentAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "favorite_category", length = 100)
    private String favoriteCategory;

    @Column(name = "favorite_content_type", length = 50)
    private String favoriteContentType;

    @Column(name = "preferred_usage_time", length = 20)
    private String preferredUsageTime;

    @Column(name = "profile_completion_percentage", nullable = false)
    private Integer profileCompletionPercentage = 0;

    @Column(name = "score_updated_at")
    private LocalDateTime scoreUpdatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public EngagementLevel getEngagementLevel() {
        return engagementLevel;
    }

    public void setEngagementLevel(EngagementLevel engagementLevel) {
        this.engagementLevel = engagementLevel;
    }

    public ConversionPotential getConversionPotential() {
        return conversionPotential;
    }

    public void setConversionPotential(ConversionPotential conversionPotential) {
        this.conversionPotential = conversionPotential;
    }

    public Long getTotalContentViews() {
        return totalContentViews;
    }

    public void setTotalContentViews(Long totalContentViews) {
        this.totalContentViews = totalContentViews;
    }

    public Long getUniqueContentViews() {
        return uniqueContentViews;
    }

    public void setUniqueContentViews(Long uniqueContentViews) {
        this.uniqueContentViews = uniqueContentViews;
    }

    public Integer getAvgDailyUsageMinutes() {
        return avgDailyUsageMinutes;
    }

    public void setAvgDailyUsageMinutes(Integer avgDailyUsageMinutes) {
        this.avgDailyUsageMinutes = avgDailyUsageMinutes;
    }

    public Integer getConsecutiveDaysStreak() {
        return consecutiveDaysStreak;
    }

    public void setConsecutiveDaysStreak(Integer consecutiveDaysStreak) {
        this.consecutiveDaysStreak = consecutiveDaysStreak;
    }

    public Long getTotalActiveDays() {
        return totalActiveDays;
    }

    public void setTotalActiveDays(Long totalActiveDays) {
        this.totalActiveDays = totalActiveDays;
    }

    public Long getTotalMessagesSent() {
        return totalMessagesSent;
    }

    public void setTotalMessagesSent(Long totalMessagesSent) {
        this.totalMessagesSent = totalMessagesSent;
    }

    public Long getTotalConversationsStarted() {
        return totalConversationsStarted;
    }

    public void setTotalConversationsStarted(Long totalConversationsStarted) {
        this.totalConversationsStarted = totalConversationsStarted;
    }

    public Long getUniqueContactsMessaged() {
        return uniqueContactsMessaged;
    }

    public void setUniqueContactsMessaged(Long uniqueContactsMessaged) {
        this.uniqueContactsMessaged = uniqueContactsMessaged;
    }

    public Integer getActiveConversations() {
        return activeConversations;
    }

    public void setActiveConversations(Integer activeConversations) {
        this.activeConversations = activeConversations;
    }

    public Boolean getHasPhones() {
        return hasPhones;
    }

    public void setHasPhones(Boolean hasPhones) {
        this.hasPhones = hasPhones;
    }

    public Integer getTotalPhones() {
        return totalPhones;
    }

    public void setTotalPhones(Integer totalPhones) {
        this.totalPhones = totalPhones;
    }

    public Boolean getHasWhatsapp() {
        return hasWhatsapp;
    }

    public void setHasWhatsapp(Boolean hasWhatsapp) {
        this.hasWhatsapp = hasWhatsapp;
    }

    public Boolean getHasTelegram() {
        return hasTelegram;
    }

    public void setHasTelegram(Boolean hasTelegram) {
        this.hasTelegram = hasTelegram;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public LocalDateTime getLastContentViewAt() {
        return lastContentViewAt;
    }

    public void setLastContentViewAt(LocalDateTime lastContentViewAt) {
        this.lastContentViewAt = lastContentViewAt;
    }

    public LocalDateTime getLastMessageSentAt() {
        return lastMessageSentAt;
    }

    public void setLastMessageSentAt(LocalDateTime lastMessageSentAt) {
        this.lastMessageSentAt = lastMessageSentAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getFavoriteCategory() {
        return favoriteCategory;
    }

    public void setFavoriteCategory(String favoriteCategory) {
        this.favoriteCategory = favoriteCategory;
    }

    public String getFavoriteContentType() {
        return favoriteContentType;
    }

    public void setFavoriteContentType(String favoriteContentType) {
        this.favoriteContentType = favoriteContentType;
    }

    public String getPreferredUsageTime() {
        return preferredUsageTime;
    }

    public void setPreferredUsageTime(String preferredUsageTime) {
        this.preferredUsageTime = preferredUsageTime;
    }

    public Integer getProfileCompletionPercentage() {
        return profileCompletionPercentage;
    }

    public void setProfileCompletionPercentage(Integer profileCompletionPercentage) {
        this.profileCompletionPercentage = profileCompletionPercentage;
    }

    public LocalDateTime getScoreUpdatedAt() {
        return scoreUpdatedAt;
    }

    public void setScoreUpdatedAt(LocalDateTime scoreUpdatedAt) {
        this.scoreUpdatedAt = scoreUpdatedAt;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // Business methods

    public boolean isActive() {
        return deletedAt == null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }
}
