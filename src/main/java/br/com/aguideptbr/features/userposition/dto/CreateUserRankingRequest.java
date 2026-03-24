package br.com.aguideptbr.features.userposition.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para criação de ranking de usuário.
 * Campos obrigatórios: userId
 * Demais campos são opcionais e serão inicializados com valores padrão.
 */
public class CreateUserRankingRequest {

    @NotNull(message = "userId is required")
    private UUID userId;

    private Integer totalScore;
    private Long totalContentViews;
    private Long uniqueContentViews;
    private Integer avgDailyUsageMinutes;
    private Integer consecutiveDaysStreak;
    private Long totalActiveDays;
    private Long totalMessagesSent;
    private Long totalConversationsStarted;
    private Long uniqueContactsMessaged;
    private Integer activeConversations;
    private Boolean hasPhones;
    private Integer totalPhones;
    private Boolean hasWhatsapp;
    private Boolean hasTelegram;
    private LocalDateTime lastActivityAt;
    private LocalDateTime lastContentViewAt;
    private LocalDateTime lastMessageSentAt;
    private LocalDateTime lastLoginAt;
    private String favoriteCategory;
    private String favoriteContentType;
    private String preferredUsageTime;

    // Getters and Setters

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
}
