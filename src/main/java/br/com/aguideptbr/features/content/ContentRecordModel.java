package br.com.aguideptbr.features.content;

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
 * Represents a content record (e.g., video, article, podcast) in the database.
 * This class utilizes the Active Record pattern provided by Panache.
 */
@Entity
@Table(name = "content_record")
public class ContentRecordModel extends PanacheEntityBase {

    // ========== IDENTIFICAÇÃO ============
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    // ========== INFORMAÇÕES BÁSICAS ==========
    @Column(name = "title", nullable = false, length = 1000)
    public String title;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "video_url", length = 2048, unique = true)
    public String videoUrl;

    @Column(name = "video_thumbnail_url")
    public String videoThumbnailUrl;

    @Column(name = "published_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime publishedAt;

    // ========== TIPO DE CONTEÚDO ==========
    @Column(name = "content_type")
    @Enumerated(EnumType.STRING)
    public ContentType type;

    // ========== CATEGORIZAÇÃO ==========
    @Column(name = "category_id", length = 50)
    public String categoryId;

    @Column(name = "category_name")
    public String categoryName;

    @Column(name = "tags", columnDefinition = "TEXT")
    public String tags;

    // ========== CARACTERÍSTICAS TÉCNICAS ==========
    @Column(name = "duration_seconds")
    public Integer durationSeconds;

    @Column(name = "duration_iso", length = 50)
    public String durationIso;

    @Column(name = "definition", length = 20)
    public String definition;

    @Column(name = "caption")
    public Boolean caption;

    // ========== MÉTRICAS DE ENGAJAMENTO ==========
    @Column(name = "view_count", columnDefinition = "BIGINT DEFAULT 0")
    public Long viewCount = 0L;

    @Column(name = "like_count", columnDefinition = "BIGINT DEFAULT 0")
    public Long likeCount = 0L;

    @Column(name = "comment_count", columnDefinition = "BIGINT DEFAULT 0")
    public Long commentCount = 0L;

    // ========== IDIOMAS ==========
    @Column(name = "default_language", length = 10)
    public String defaultLanguage;

    @Column(name = "default_audio_language", length = 10)
    public String defaultAudioLanguage;

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
    }

    /**
     * Automatically updates the modification timestamp before updating the entity.
     * This method is called by JPA before the entity is updated in the database.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== GETTERS E SETTERS para interagir com a data de publicação do vídeo
    // no youtube ==========

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    // ========== GETTERS E SETTERS PARA AUDITORIA E PUBLICAÇÃO ==========

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ========== MÉTODOS DE BUSCA ==========

    /**
     * Searches for content records whose title starts with the given search term.
     * This search is case-insensitive and returns a list of results.
     * It is ideal for implementing autocomplete or dynamic search features.
     *
     * @param searchTerm The partial term to search for at the beginning of the
     *                   title.
     * @return A list of ContentRecordModel matching the criteria. The list will be
     *         empty if no matches are found.
     */
    public static List<ContentRecordModel> searchByTitle(String searchTerm) {
        return list("lower(title) like ?1", searchTerm.toLowerCase() + "%");
    }

    /**
     * Finds a single content record by an exact title match.
     * This method is case-sensitive by default and expects the title to be unique.
     *
     * @param titleText The exact title to search for.
     * @return The found ContentRecordModel object, or {@code null} if no content
     *         matches the exact title.
     * @see br.com.aguideptbr.features.content.ContentRecordResource#findByTitle(String)
     */
    public static ContentRecordModel findByTitle(String titleText) {
        return find("title", titleText).firstResult();
    }

    /**
     * Finds content records by category ID.
     *
     * @param categoryId The YouTube category ID to search for.
     * @return A list of ContentRecordModel matching the category.
     */
    public static List<ContentRecordModel> findByCategory(String categoryId) {
        return list("categoryId", categoryId);
    }

    /**
     * Finds content records by tag.
     *
     * @param tag The tag to search for.
     * @return A list of ContentRecordModel containing the specified tag.
     */
    public static List<ContentRecordModel> findByTag(String tag) {
        return list("lower(tags) like ?1", "%" + tag.toLowerCase() + "%");
    }

    /**
     * Finds a content record by URL.
     *
     * @param videoUrl The video URL to search for.
     * @return The found ContentRecordModel, or null if not found.
     */
    public static ContentRecordModel findByUrl(String videoUrl) {
        return find("videoUrl", videoUrl).firstResult();
    }

    /**
     * Finds content records by channel ID.
     *
     * @param channelId The channel ID to search for (e.g., YouTube channel ID).
     * @return A list of ContentRecordModel matching the channel.
     */
    public static List<ContentRecordModel> findByChannelId(String channelId) {
        return list("channelId", channelId);
    }

    /**
     * Finds content records by channel owner link ID.
     *
     * @param ownerId The channel owner link ID to search for.
     * @return A list of ContentRecordModel matching the owner.
     */
    public static List<ContentRecordModel> findByChannelOwner(String ownerId) {
        return list("channelOwnerLinkId", ownerId);
    }
}
