package br.com.aguideptbr.features.engagement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.content.ContentRecordModel;
import br.com.aguideptbr.features.engagement.dto.CreateEngagementDTO;
import br.com.aguideptbr.features.engagement.dto.EngagementResponseDTO;
import br.com.aguideptbr.features.engagement.dto.UpdateEngagementDTO;
import br.com.aguideptbr.features.engagement.dto.UserTopContentsDTO;
import br.com.aguideptbr.features.user.UserModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Service for managing content engagement operations.
 * Handles business logic for user interactions with content.
 */
@ApplicationScoped
public class ContentEngagementService {

    private final Logger log;
    private final ContentEngagementRepository engagementRepository;

    public ContentEngagementService(Logger log, ContentEngagementRepository engagementRepository) {
        this.log = log;
        this.engagementRepository = engagementRepository;
    }

    /**
     * Creates a new engagement record.
     *
     * @param dto The engagement data
     * @return The created engagement
     */
    @Transactional
    public EngagementResponseDTO createEngagement(CreateEngagementDTO dto) {
        log.infof("Creating engagement: userId=%s, contentId=%s, type=%s",
                dto.getUserId(), dto.getContentId(), dto.getEngagementType());

        // Validate user exists
        UserModel user = UserModel.findById(dto.getUserId());
        if (user == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of(
                                    "error", "User not found",
                                    "message", "User with ID " + dto.getUserId() + " does not exist",
                                    "timestamp", LocalDateTime.now()))
                            .build());
        }

        // Validate content exists
        ContentRecordModel content = ContentRecordModel.findById(dto.getContentId());
        if (content == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of(
                                    "error", "Content not found",
                                    "message", "Content with ID " + dto.getContentId() + " does not exist",
                                    "timestamp", LocalDateTime.now()))
                            .build());
        }

        // Check if reversible engagement already exists (LIKE, DISLIKE, BOOKMARK)
        if (dto.getEngagementType().isReversible()) {
            ContentEngagementModel existing = engagementRepository.findActiveEngagement(
                    dto.getUserId(),
                    dto.getContentId(),
                    dto.getEngagementType());

            if (existing != null) {
                throw new WebApplicationException(
                        Response.status(Response.Status.CONFLICT)
                                .entity(Map.of(
                                        "error", "Engagement already exists",
                                        "message",
                                        "User already has active " + dto.getEngagementType() + " for this content",
                                        "timestamp", LocalDateTime.now()))
                                .build());
            }
        }

        // Create engagement entity
        ContentEngagementModel engagement = new ContentEngagementModel();
        engagement.userId = dto.getUserId();
        engagement.contentId = dto.getContentId();
        engagement.engagementType = dto.getEngagementType();
        engagement.engagementStatus = dto.getEngagementStatus() != null
                ? dto.getEngagementStatus()
                : EngagementStatus.ACTIVE;
        engagement.viewDurationSeconds = dto.getViewDurationSeconds();
        engagement.completionPercentage = dto.getCompletionPercentage();
        engagement.repeatCount = dto.getRepeatCount() != null ? dto.getRepeatCount() : 1;
        engagement.deviceType = dto.getDeviceType();
        engagement.platform = dto.getPlatform();
        engagement.source = dto.getSource();
        engagement.userIp = dto.getUserIp();
        engagement.userAgent = dto.getUserAgent();
        engagement.metadata = dto.getMetadata();
        engagement.commentText = dto.getCommentText();
        engagement.rating = dto.getRating();

        if (dto.getEngagedAt() != null) {
            engagement.setEngagedAt(dto.getEngagedAt());
        }
        if (dto.getEndedAt() != null) {
            engagement.setEndedAt(dto.getEndedAt());
        }

        engagement.persist();
        log.infof("Engagement created successfully: id=%s", engagement.id);

        return new EngagementResponseDTO(engagement);
    }

    /**
     * Updates an existing engagement.
     *
     * @param engagementId The engagement ID
     * @param dto          The update data
     * @return The updated engagement
     */
    @Transactional
    public EngagementResponseDTO updateEngagement(UUID engagementId, UpdateEngagementDTO dto) {
        log.infof("Updating engagement: %s", engagementId);

        ContentEngagementModel engagement = engagementRepository.findById(engagementId);
        if (engagement == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of(
                                    "error", "Engagement not found",
                                    "message", "Engagement with ID " + engagementId + " does not exist",
                                    "timestamp", LocalDateTime.now()))
                            .build());
        }

        // Update fields if provided
        if (dto.getEngagementStatus() != null) {
            engagement.engagementStatus = dto.getEngagementStatus();
        }
        if (dto.getViewDurationSeconds() != null) {
            engagement.viewDurationSeconds = dto.getViewDurationSeconds();
        }
        if (dto.getCompletionPercentage() != null) {
            engagement.completionPercentage = dto.getCompletionPercentage();
        }
        if (dto.getRepeatCount() != null) {
            engagement.repeatCount = dto.getRepeatCount();
        }
        if (dto.getMetadata() != null) {
            engagement.metadata = dto.getMetadata();
        }
        if (dto.getCommentText() != null) {
            engagement.commentText = dto.getCommentText();
        }
        if (dto.getRating() != null) {
            engagement.rating = dto.getRating();
        }
        if (dto.getEndedAt() != null) {
            engagement.setEndedAt(dto.getEndedAt());
        }

        engagement.persist();
        log.infof("Engagement updated successfully: id=%s", engagementId);

        return new EngagementResponseDTO(engagement);
    }

    /**
     * Deletes (marks as REMOVED) an engagement.
     *
     * @param engagementId The engagement ID
     */
    @Transactional
    public void deleteEngagement(UUID engagementId) {
        log.infof("Deleting engagement: %s", engagementId);

        ContentEngagementModel engagement = engagementRepository.findById(engagementId);
        if (engagement == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of(
                                    "error", "Engagement not found",
                                    "message", "Engagement with ID " + engagementId + " does not exist",
                                    "timestamp", LocalDateTime.now()))
                            .build());
        }

        // Soft delete: mark as REMOVED instead of actual deletion
        engagement.engagementStatus = EngagementStatus.REMOVED;
        engagement.persist();

        log.infof("Engagement marked as REMOVED: id=%s", engagementId);
    }

    /**
     * Gets engagement by ID.
     *
     * @param engagementId The engagement ID
     * @return The engagement
     */
    public EngagementResponseDTO getEngagement(UUID engagementId) {
        log.infof("Getting engagement: %s", engagementId);

        ContentEngagementModel engagement = engagementRepository.findById(engagementId);
        if (engagement == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of(
                                    "error", "Engagement not found",
                                    "message", "Engagement with ID " + engagementId + " does not exist",
                                    "timestamp", LocalDateTime.now()))
                            .build());
        }

        return new EngagementResponseDTO(engagement);
    }

    /**
     * Gets all engagements for a user.
     *
     * @param userId The user ID
     * @return List of engagements
     */
    public List<EngagementResponseDTO> getUserEngagements(UUID userId) {
        log.infof("Getting engagements for user: %s", userId);

        List<ContentEngagementModel> engagements = engagementRepository.findByUserId(userId);
        return engagements.stream()
                .map(EngagementResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Gets all engagements for a content.
     *
     * @param contentId The content ID
     * @return List of engagements
     */
    public List<EngagementResponseDTO> getContentEngagements(UUID contentId) {
        log.infof("Getting engagements for content: %s", contentId);

        List<ContentEngagementModel> engagements = engagementRepository.findByContentId(contentId);
        return engagements.stream()
                .map(EngagementResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Gets top contents that a user has interacted with the most.
     * This is a KEY feature for recommendations and analytics.
     *
     * @param userId The user ID
     * @param limit  Maximum number of contents to return
     * @return List of top contents with enriched data
     */
    public List<UserTopContentsDTO> getUserTopContents(UUID userId, int limit) {
        log.infof("Getting top %d contents for user: %s", limit, userId);

        // Get aggregated data from repository
        List<UserTopContentsDTO> topContents = engagementRepository.findUserTopContents(userId, limit);

        // Enrich with content details
        for (UserTopContentsDTO dto : topContents) {
            ContentRecordModel content = ContentRecordModel.findById(dto.getContentId());
            if (content != null) {
                dto.setContentTitle(content.title);
                dto.setContentUrl(content.videoUrl);
                dto.setThumbnailUrl(content.videoThumbnailUrl);
            }
        }

        log.infof("Found %d top contents for user: %s", topContents.size(), userId);
        return topContents;
    }

    /**
     * Gets engagement statistics for a content (counts by type).
     *
     * @param contentId The content ID
     * @return Map of engagement type to count
     */
    public Map<EngagementType, Long> getContentStats(UUID contentId) {
        log.infof("Getting stats for content: %s", contentId);
        return engagementRepository.countByTypeForContent(contentId);
    }

    /**
     * Gets user engagement statistics.
     *
     * @param userId The user ID
     * @return Map with statistics
     */
    public Map<String, Object> getUserStats(UUID userId) {
        log.infof("Getting stats for user: %s", userId);

        long totalEngagements = engagementRepository.countByUser(userId);
        Double avgCompletion = engagementRepository.getAverageCompletionPercentage(userId);
        Long totalViewDuration = engagementRepository.getTotalViewDuration(userId);

        return Map.of(
                "totalEngagements", totalEngagements,
                "averageCompletionPercentage", avgCompletion,
                "totalViewDurationSeconds", totalViewDuration,
                "totalViewDurationMinutes", totalViewDuration / 60);
    }

    /**
     * Gets recent engagements for a user.
     *
     * @param userId The user ID
     * @param days   Number of days to look back
     * @return List of recent engagements
     */
    public List<EngagementResponseDTO> getRecentEngagements(UUID userId, int days) {
        log.infof("Getting recent %d days engagements for user: %s", days, userId);

        List<ContentEngagementModel> engagements = engagementRepository.findRecentByUser(userId, days);
        return engagements.stream()
                .map(EngagementResponseDTO::new)
                .collect(Collectors.toList());
    }
}
