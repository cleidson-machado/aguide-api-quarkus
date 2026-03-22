package br.com.aguideptbr.features.engagement;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.engagement.dto.UserTopContentsDTO;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for ContentEngagementModel with custom queries for analytics and
 * statistics.
 */
@ApplicationScoped
public class ContentEngagementRepository implements PanacheRepositoryBase<ContentEngagementModel, UUID> {

    private final Logger log;

    public ContentEngagementRepository(Logger log) {
        this.log = log;
    }

    /**
     * Finds all engagements for a specific user.
     *
     * @param userId The user ID
     * @return List of engagements
     */
    public List<ContentEngagementModel> findByUserId(UUID userId) {
        log.infof("Finding engagements for user: %s", userId);
        return list("userId = ?1 ORDER BY engagedAt DESC", userId);
    }

    /**
     * Finds all engagements for a specific content.
     *
     * @param contentId The content ID
     * @return List of engagements
     */
    public List<ContentEngagementModel> findByContentId(UUID contentId) {
        log.infof("Finding engagements for content: %s", contentId);
        return list("contentId = ?1 ORDER BY engagedAt DESC", contentId);
    }

    /**
     * Finds engagements by user and content.
     *
     * @param userId    The user ID
     * @param contentId The content ID
     * @return List of engagements
     */
    public List<ContentEngagementModel> findByUserAndContent(UUID userId, UUID contentId) {
        return list("userId = ?1 AND contentId = ?2 ORDER BY engagedAt DESC", userId, contentId);
    }

    /**
     * Finds active engagements by user and content.
     *
     * @param userId    The user ID
     * @param contentId The content ID
     * @return List of active engagements
     */
    public List<ContentEngagementModel> findActiveByUserAndContent(UUID userId, UUID contentId) {
        return list("userId = ?1 AND contentId = ?2 AND engagementStatus = ?3 ORDER BY engagedAt DESC",
                userId, contentId, EngagementStatus.ACTIVE);
    }

    /**
     * Finds recent engagements for a user (last N days).
     *
     * @param userId The user ID
     * @param days   Number of days to look back
     * @return List of recent engagements
     */
    public List<ContentEngagementModel> findRecentByUser(UUID userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return list("userId = ?1 AND engagedAt >= ?2 ORDER BY engagedAt DESC", userId, since);
    }

    /**
     * Finds engagements within a date range.
     *
     * @param userId    The user ID
     * @param startDate Start date
     * @param endDate   End date
     * @return List of engagements
     */
    public List<ContentEngagementModel> findByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        return list("userId = ?1 AND engagedAt >= ?2 AND engagedAt <= ?3 ORDER BY engagedAt DESC",
                userId, startDate, endDate);
    }

    /**
     * Gets top N contents that a user interacted with the most.
     * Groups by contentId and counts total interactions.
     *
     * @param userId The user ID
     * @param limit  Maximum number of contents to return
     * @return List of UserTopContentsDTO with aggregated data
     */
    public List<UserTopContentsDTO> findUserTopContents(UUID userId, int limit) {
        log.infof("Finding top %d contents for user: %s", limit, userId);

        String query = """
                SELECT
                    e.contentId as contentId,
                    COUNT(e.id) as totalInteractions,
                    SUM(CASE WHEN e.engagementType = 'VIEW' OR e.engagementType = 'COMPLETE' THEN 1 ELSE 0 END) as totalViews,
                    SUM(CASE WHEN e.engagementType = 'LIKE' THEN 1 ELSE 0 END) as totalLikes,
                    SUM(CASE WHEN e.engagementType = 'SHARE' THEN 1 ELSE 0 END) as totalShares,
                    AVG(COALESCE(e.completionPercentage, 0)) as avgCompletion,
                    SUM(COALESCE(e.viewDurationSeconds, 0)) as totalViewDuration
                FROM ContentEngagementModel e
                WHERE e.userId = :userId
                  AND e.engagementStatus = :status
                GROUP BY e.contentId
                ORDER BY totalInteractions DESC
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = getEntityManager()
                .createQuery(query)
                .setParameter("userId", userId)
                .setParameter("status", EngagementStatus.ACTIVE)
                .setMaxResults(limit)
                .getResultList();

        return results.stream()
                .map(row -> UserTopContentsDTO.builder()
                        .contentId((UUID) row[0])
                        .totalInteractions(((Number) row[1]).longValue())
                        .totalViews(((Number) row[2]).longValue())
                        .totalLikes(((Number) row[3]).longValue())
                        .totalShares(((Number) row[4]).longValue())
                        .avgCompletionPercentage(((Number) row[5]).intValue())
                        .totalViewDurationSeconds(((Number) row[6]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Counts total engagements for a user.
     *
     * @param userId The user ID
     * @return Total count
     */
    public long countByUser(UUID userId) {
        return count("userId = ?1 AND engagementStatus = ?2", userId, EngagementStatus.ACTIVE);
    }

    /**
     * Counts engagements by type for a specific content.
     *
     * @param contentId The content ID
     * @return Map of engagement type to count
     */
    public Map<EngagementType, Long> countByTypeForContent(UUID contentId) {
        log.infof("Counting engagements by type for content: %s", contentId);

        String query = """
                SELECT e.engagementType, COUNT(e.id)
                FROM ContentEngagementModel e
                WHERE e.contentId = :contentId
                  AND e.engagementStatus = :status
                GROUP BY e.engagementType
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = getEntityManager()
                .createQuery(query)
                .setParameter("contentId", contentId)
                .setParameter("status", EngagementStatus.ACTIVE)
                .getResultList();

        Map<EngagementType, Long> counts = new HashMap<>();
        for (Object[] row : results) {
            counts.put((EngagementType) row[0], ((Number) row[1]).longValue());
        }

        return counts;
    }

    /**
     * Finds specific engagement type between user and content.
     *
     * @param userId    The user ID
     * @param contentId The content ID
     * @param type      The engagement type
     * @return The engagement or null
     */
    public ContentEngagementModel findActiveEngagement(UUID userId, UUID contentId, EngagementType type) {
        return find("userId = ?1 AND contentId = ?2 AND engagementType = ?3 AND engagementStatus = ?4",
                userId, contentId, type, EngagementStatus.ACTIVE)
                .firstResult();
    }

    /**
     * Checks if a specific engagement exists and is active.
     *
     * @param userId    The user ID
     * @param contentId The content ID
     * @param type      The engagement type
     * @return True if exists and active
     */
    public boolean existsActiveEngagement(UUID userId, UUID contentId, EngagementType type) {
        return count("userId = ?1 AND contentId = ?2 AND engagementType = ?3 AND engagementStatus = ?4",
                userId, contentId, type, EngagementStatus.ACTIVE) > 0;
    }

    /**
     * Gets average completion percentage for a user across all content.
     *
     * @param userId The user ID
     * @return Average completion percentage
     */
    public Double getAverageCompletionPercentage(UUID userId) {
        String query = """
                SELECT AVG(e.completionPercentage)
                FROM ContentEngagementModel e
                WHERE e.userId = :userId
                  AND e.completionPercentage IS NOT NULL
                  AND e.engagementStatus = :status
                """;

        Number result = (Number) getEntityManager()
                .createQuery(query)
                .setParameter("userId", userId)
                .setParameter("status", EngagementStatus.ACTIVE)
                .getSingleResult();

        return result != null ? result.doubleValue() : 0.0;
    }

    /**
     * Gets total view duration for a user in seconds.
     *
     * @param userId The user ID
     * @return Total view duration in seconds
     */
    public Long getTotalViewDuration(UUID userId) {
        String query = """
                SELECT SUM(e.viewDurationSeconds)
                FROM ContentEngagementModel e
                WHERE e.userId = :userId
                  AND e.viewDurationSeconds IS NOT NULL
                  AND e.engagementStatus = :status
                """;

        Number result = (Number) getEntityManager()
                .createQuery(query)
                .setParameter("userId", userId)
                .setParameter("status", EngagementStatus.ACTIVE)
                .getSingleResult();

        return result != null ? result.longValue() : 0L;
    }
}
