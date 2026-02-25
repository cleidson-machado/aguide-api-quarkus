package br.com.aguideptbr.features.ownership;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

/**
 * Entity representing content ownership validation.
 *
 * This table acts as a soft reference between users (app_user) and content
 * (content_record), validating ownership through HMAC-SHA256 cryptographic
 * verification.
 *
 * Soft references are used instead of rigid foreign keys because:
 * - Data comes from external sources (YouTube API)
 * - Lifecycles are independent
 * - Data may be changed/deleted externally
 */
@Entity
@Table(name = "content_ownership", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ownership_user_content", columnNames = { "user_id", "content_id" })
})
public class ContentOwnershipModel extends PanacheEntityBase {

    // ========== IDENTIFICAÇÃO ==========
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    // ========== SOFT REFERENCES (não usa FK rígidas) ==========

    /**
     * Soft reference to app_user.id.
     * No rigid FK because user data may come from external OAuth providers.
     */
    @Column(name = "user_id", nullable = false)
    public UUID userId;

    /**
     * Soft reference to content_record.id.
     * No rigid FK because content data comes from YouTube API.
     */
    @Column(name = "content_id", nullable = false)
    public UUID contentId;

    // ========== CAMPOS DE VALIDAÇÃO ==========

    /**
     * YouTube Channel ID from user (app_user.youtube_channel_id).
     * Captured at validation time (snapshot for audit).
     */
    @Column(name = "youtube_channel_id", nullable = false, length = 255)
    public String youtubeChannelId;

    /**
     * YouTube Channel ID from content (content_record.channel_id).
     * Captured at validation time (snapshot for audit).
     */
    @Column(name = "content_channel_id", nullable = false, length = 255)
    public String contentChannelId;

    /**
     * Ownership validation status.
     * - PENDING: Awaiting validation
     * - VERIFIED: HMAC validated, channels match
     * - REJECTED: HMAC invalid or channels mismatch
     *
     * NOTE: Database column is VARCHAR(20) with CHECK constraint (changed in
     * V1.0.17).
     * JPA @Enumerated(EnumType.STRING) maps naturally to VARCHAR.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_status", nullable = false, length = 20)
    public OwnershipStatus ownershipStatus = OwnershipStatus.PENDING;

    /**
     * HMAC-SHA256 validation hash.
     * Calculated: HMAC(userId + contentId + channelIds, secret)
     * Always recalculated on backend (never trust client).
     */
    @Column(name = "validation_hash", nullable = false, length = 512)
    public String validationHash;

    // ========== AUDITORIA DE VERIFICAÇÃO ==========

    /**
     * Timestamp when ownership was verified.
     * Null if still PENDING or REJECTED.
     */
    @Column(name = "verified_at")
    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime verifiedAt;

    /**
     * Admin user ID who manually verified (optional).
     * Null for automatic verification.
     */
    @Column(name = "verified_by")
    public UUID verifiedBy;

    // ========== CAMPOS DE AUDITORIA E RETRY ==========

    /**
     * Detailed rejection reason.
     * Values: CHANNEL_MISMATCH, NO_CHANNEL, USER_CANCELLED, etc.
     * Null when status = VERIFIED.
     */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    public String rejectionReason;

    /**
     * Number of validation attempts (retry counter).
     * Starts at 0 on first attempt, incremented on each retry.
     * Useful for analytics and rate limiting.
     */
    @Column(name = "retry_count", nullable = false)
    public Integer retryCount = 0;

    /**
     * Timestamp of last validation attempt (success or failure).
     * Updated on every validation request.
     */
    @Column(name = "last_attempt_at")
    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime lastAttemptAt;

    /**
     * Whether the ownership claim was cancelled by the user.
     * true = user explicitly cancelled the claim
     * false = system automatically rejected (default)
     */
    @Column(name = "cancelled_by_user", nullable = false)
    public Boolean cancelledByUser = false;

    // ========== TIMESTAMPS DE AUDITORIA ==========

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    /**
     * Automatically sets the creation timestamp before persisting the entity.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Set last attempt timestamp on creation
        if (lastAttemptAt == null) {
            lastAttemptAt = LocalDateTime.now();
        }
    }

    /**
     * Automatically updates the modification timestamp before updating the entity.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== GETTERS E SETTERS ==========

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ========== MÉTODOS DE BUSCA ==========

    /**
     * Find all ownerships for a specific user.
     *
     * @param userId User ID
     * @return List of ContentOwnershipModel
     */
    public static List<ContentOwnershipModel> findByUserId(UUID userId) {
        return list("userId", userId);
    }

    /**
     * Find all verified ownerships for a specific user.
     *
     * @param userId User ID
     * @return List of verified ContentOwnershipModel
     */
    public static List<ContentOwnershipModel> findVerifiedByUserId(UUID userId) {
        return list("userId = ?1 and ownershipStatus = ?2", userId, OwnershipStatus.VERIFIED);
    }

    /**
     * Find ownership by user and content.
     *
     * @param userId    User ID
     * @param contentId Content ID
     * @return ContentOwnershipModel or null
     */
    public static ContentOwnershipModel findByUserAndContent(UUID userId, UUID contentId) {
        return find("userId = ?1 and contentId = ?2", userId, contentId).firstResult();
    }

    /**
     * Find all ownerships for a specific content.
     *
     * @param contentId Content ID
     * @return List of ContentOwnershipModel
     */
    public static List<ContentOwnershipModel> findByContentId(UUID contentId) {
        return list("contentId", contentId);
    }

    /**
     * Find all ownerships by status.
     *
     * @param status Ownership status
     * @return List of ContentOwnershipModel
     */
    public static List<ContentOwnershipModel> findByStatus(OwnershipStatus status) {
        return list("ownershipStatus", status);
    }

    /**
     * Find all pending ownerships (awaiting validation).
     *
     * @return List of pending ContentOwnershipModel
     */
    public static List<ContentOwnershipModel> findPending() {
        return list("ownershipStatus", OwnershipStatus.PENDING);
    }

    /**
     * Check if ownership exists (regardless of status).
     *
     * @param userId    User ID
     * @param contentId Content ID
     * @return true if ownership exists
     */
    public static boolean ownershipExists(UUID userId, UUID contentId) {
        return count("userId = ?1 and contentId = ?2", userId, contentId) > 0;
    }

    /**
     * Check if verified ownership exists.
     *
     * @param userId    User ID
     * @param contentId Content ID
     * @return true if verified ownership exists
     */

    /**
     * Find all ownerships with retry count greater than threshold.
     * Useful for identifying users with multiple failed attempts.
     *
     * @param threshold Minimum retry count
     * @return List of ContentOwnershipModel
     */
    public static List<ContentOwnershipModel> findWithHighRetryCount(int threshold) {
        return list("retryCount >= ?1", threshold);
    }

    /**
     * Find all user-cancelled ownerships.
     *
     * @return List of ContentOwnershipModel
     */
    public static List<ContentOwnershipModel> findCancelledByUser() {
        return list("cancelledByUser = true");
    }

    /**
     * Increment retry counter and update last attempt timestamp.
     * Call this method on every validation attempt.
     */
    public void recordAttempt() {
        this.retryCount++;
        this.lastAttemptAt = LocalDateTime.now();
    }

    /**
     * Mark ownership as cancelled by user.
     */
    public void cancelByUser() {
        this.cancelledByUser = true;
        this.ownershipStatus = OwnershipStatus.REJECTED;
        this.rejectionReason = "USER_CANCELLED";
        this.lastAttemptAt = LocalDateTime.now();
    }

    /**
     * Reset retry counter (for admin use).
     * Useful when helping users troubleshoot issues.
     */
    public void resetRetryCount() {
        this.retryCount = 0;
    }

    public static boolean isVerified(UUID userId, UUID contentId) {
        return count("userId = ?1 and contentId = ?2 and ownershipStatus = ?3",
                userId, contentId, OwnershipStatus.VERIFIED) > 0;
    }
}
