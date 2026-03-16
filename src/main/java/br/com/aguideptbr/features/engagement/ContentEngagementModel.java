package br.com.aguideptbr.features.engagement;

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

/**
 * Represents a user engagement record with content in the database.
 * Tracks interactions such as views, likes, shares, bookmarks, and comments.
 * This class utilizes the Active Record pattern provided by Panache.
 */
@Entity
@Table(name = "content_engagement_log")
public class ContentEngagementModel extends PanacheEntityBase {

    // ========== IDENTIFICAÇÃO ============
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    // ========== REFERÊNCIAS (sem relacionamento JPA por enquanto) ==========
    /**
     * ID do usuário que realizou o engajamento
     */
    @Column(name = "user_id", nullable = false)
    public UUID userId;

    /**
     * ID do conteúdo com o qual houve engajamento
     */
    @Column(name = "content_id", nullable = false)
    public UUID contentId;

    // ========== TIPO E STATUS DO ENGAJAMENTO ==========
    /**
     * Tipo de engajamento (VIEW, LIKE, SHARE, etc.)
     */
    @Column(name = "engagement_type", nullable = false)
    @Enumerated(EnumType.STRING)
    public EngagementType engagementType;

    /**
     * Status do engajamento (ACTIVE, REMOVED, EXPIRED, FLAGGED)
     */
    @Column(name = "engagement_status", nullable = false)
    @Enumerated(EnumType.STRING)
    public EngagementStatus engagementStatus;

    // ========== MÉTRICAS DE ENGAJAMENTO ==========
    /**
     * Duração de visualização em segundos (para VIEW e PARTIAL_VIEW)
     */
    @Column(name = "view_duration_seconds")
    public Integer viewDurationSeconds;

    /**
     * Percentual de conclusão do conteúdo (0-100)
     */
    @Column(name = "completion_percentage")
    public Integer completionPercentage;

    /**
     * Quantidade de vezes que este engajamento foi repetido
     * Exemplo: usuário assiste o vídeo 3 vezes
     */
    @Column(name = "repeat_count", columnDefinition = "INT DEFAULT 1")
    public Integer repeatCount = 1;

    // ========== INFORMAÇÕES CONTEXTUAIS ==========
    /**
     * Dispositivo usado (MOBILE, WEB, TABLET, TV)
     */
    @Column(name = "device_type", length = 20)
    public String deviceType;

    /**
     * Plataforma do dispositivo (ANDROID, IOS, WEB)
     */
    @Column(name = "platform", length = 20)
    public String platform;

    /**
     * Origem do engajamento (FEED, SEARCH, RECOMMENDATION, DIRECT, SHARE)
     */
    @Column(name = "source", length = 50)
    public String source;

    /**
     * IP do usuário (para análise de fraude)
     */
    @Column(name = "user_ip", length = 45)
    public String userIp;

    /**
     * User agent do navegador/app
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    public String userAgent;

    // ========== DADOS ADICIONAIS ==========
    /**
     * Campo livre para metadados em JSON
     * Exemplo: localização geográfica, configurações de qualidade, etc.
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    public String metadata;

    /**
     * Texto do comentário (se engagementType = COMMENT)
     */
    @Column(name = "comment_text", columnDefinition = "TEXT")
    public String commentText;

    /**
     * Rating/nota dada ao conteúdo (1-5 estrelas, se aplicável)
     */
    @Column(name = "rating")
    public Integer rating;

    // ========== TIMESTAMPS DE EVENTOS ==========
    /**
     * Data/hora em que o engajamento começou
     */
    @Column(name = "engaged_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime engagedAt;

    /**
     * Data/hora em que o engajamento foi finalizado/removido
     */
    @Column(name = "ended_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime endedAt;

    // ========== AUDITORIA - DATA E HORA DE CRIAÇÃO E ATUALIZAÇÃO ============
    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    /**
     * Automatically sets the creation timestamp before persisting the entity.
     * This method is called by JPA before the entity is inserted into the database.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Se engaged_at não foi definido, usar o momento da criação
        if (engagedAt == null) {
            engagedAt = LocalDateTime.now();
        }

        // Status padrão como ACTIVE se não definido
        if (engagementStatus == null) {
            engagementStatus = EngagementStatus.ACTIVE;
        }

        // Repeat count padrão
        if (repeatCount == null) {
            repeatCount = 1;
        }
    }

    /**
     * Automatically updates the modification timestamp before updating the entity.
     * This method is called by JPA before the entity is updated in the database.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== GETTERS E SETTERS PARA TIMESTAMPS ==========

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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ========== MÉTODOS DE BUSCA ==========

    /**
     * Finds all engagements by user ID.
     *
     * @param userId The user ID to search for.
     * @return A list of ContentEngagementModel matching the user.
     */
    public static List<ContentEngagementModel> findByUserId(UUID userId) {
        return list("userId", userId);
    }

    /**
     * Finds all engagements by content ID.
     *
     * @param contentId The content ID to search for.
     * @return A list of ContentEngagementModel matching the content.
     */
    public static List<ContentEngagementModel> findByContentId(UUID contentId) {
        return list("contentId", contentId);
    }

    /**
     * Finds all engagements by user and content.
     *
     * @param userId    The user ID.
     * @param contentId The content ID.
     * @return A list of ContentEngagementModel matching both user and content.
     */
    public static List<ContentEngagementModel> findByUserAndContent(UUID userId, UUID contentId) {
        return list("userId = ?1 and contentId = ?2", userId, contentId);
    }

    /**
     * Finds all active engagements by user and content.
     *
     * @param userId    The user ID.
     * @param contentId The content ID.
     * @return A list of active ContentEngagementModel.
     */
    public static List<ContentEngagementModel> findActiveByUserAndContent(UUID userId, UUID contentId) {
        return list("userId = ?1 and contentId = ?2 and engagementStatus = ?3",
                userId, contentId, EngagementStatus.ACTIVE);
    }

    /**
     * Finds engagements by type.
     *
     * @param engagementType The engagement type to search for.
     * @return A list of ContentEngagementModel matching the type.
     */
    public static List<ContentEngagementModel> findByType(EngagementType engagementType) {
        return list("engagementType", engagementType);
    }

    /**
     * Finds engagements by user and type.
     *
     * @param userId         The user ID.
     * @param engagementType The engagement type.
     * @return A list of ContentEngagementModel matching user and type.
     */
    public static List<ContentEngagementModel> findByUserAndType(UUID userId, EngagementType engagementType) {
        return list("userId = ?1 and engagementType = ?2", userId, engagementType);
    }

    /**
     * Finds engagements by content and type.
     *
     * @param contentId      The content ID.
     * @param engagementType The engagement type.
     * @return A list of ContentEngagementModel matching content and type.
     */
    public static List<ContentEngagementModel> findByContentAndType(UUID contentId, EngagementType engagementType) {
        return list("contentId = ?1 and engagementType = ?2", contentId, engagementType);
    }

    /**
     * Finds active engagements by user, content, and type.
     * Useful to check if a specific engagement already exists.
     *
     * @param userId         The user ID.
     * @param contentId      The content ID.
     * @param engagementType The engagement type.
     * @return The found ContentEngagementModel, or null if not found.
     */
    public static ContentEngagementModel findActiveEngagement(UUID userId, UUID contentId,
            EngagementType engagementType) {
        return find("userId = ?1 and contentId = ?2 and engagementType = ?3 and engagementStatus = ?4",
                userId, contentId, engagementType, EngagementStatus.ACTIVE)
                .firstResult();
    }

    /**
     * Counts total engagements for a specific content.
     *
     * @param contentId The content ID.
     * @return The count of engagements.
     */
    public static long countByContent(UUID contentId) {
        return count("contentId = ?1 and engagementStatus = ?2", contentId, EngagementStatus.ACTIVE);
    }

    /**
     * Counts engagements of a specific type for a content.
     *
     * @param contentId      The content ID.
     * @param engagementType The engagement type.
     * @return The count of engagements.
     */
    public static long countByContentAndType(UUID contentId, EngagementType engagementType) {
        return count("contentId = ?1 and engagementType = ?2 and engagementStatus = ?3",
                contentId, engagementType, EngagementStatus.ACTIVE);
    }

    /**
     * Finds all engagements by status.
     *
     * @param status The engagement status to search for.
     * @return A list of ContentEngagementModel matching the status.
     */
    public static List<ContentEngagementModel> findByStatus(EngagementStatus status) {
        return list("engagementStatus", status);
    }

    /**
     * Finds engagements created within a date range.
     *
     * @param startDate The start date.
     * @param endDate   The end date.
     * @return A list of ContentEngagementModel within the date range.
     */
    public static List<ContentEngagementModel> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return list("createdAt >= ?1 and createdAt <= ?2", startDate, endDate);
    }
}
