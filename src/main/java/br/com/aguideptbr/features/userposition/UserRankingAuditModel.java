package br.com.aguideptbr.features.userposition;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import br.com.aguideptbr.features.userposition.enuns.AuditOperationType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade de auditoria para rastreamento de todas as operações em
 * UserRankingModel.
 *
 * Registra CREATE, UPDATE, ADD_POINTS, DELETE e RESTORE com metadados completos
 * incluindo IP, User-Agent e correlation ID.
 */
@Entity
@Table(name = "app_user_ranking_audit")
public class UserRankingAuditModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ranking_id", nullable = false)
    private UUID rankingId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, length = 20)
    private AuditOperationType operation;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "points_added")
    private Integer pointsAdded;

    @Column(name = "points_reason", length = 50)
    private String pointsReason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors

    public UserRankingAuditModel() {
    }

    /**
     * Construtor para operação ADD_POINTS.
     */
    public static UserRankingAuditModel forAddPoints(
            UUID rankingId,
            UUID userId,
            int pointsAdded,
            String pointsReason,
            String ipAddress,
            String userAgent,
            String requestId) {
        UserRankingAuditModel audit = new UserRankingAuditModel();
        audit.rankingId = rankingId;
        audit.userId = userId;
        audit.operation = AuditOperationType.ADD_POINTS;
        audit.pointsAdded = pointsAdded;
        audit.pointsReason = pointsReason;
        audit.ipAddress = ipAddress;
        audit.userAgent = userAgent;
        audit.requestId = requestId;
        return audit;
    }

    /**
     * Construtor para operações UPDATE de campo específico.
     */
    public static UserRankingAuditModel forFieldUpdate(
            UUID rankingId,
            UUID userId,
            String fieldName,
            String oldValue,
            String newValue,
            String ipAddress,
            String userAgent,
            String requestId) {
        UserRankingAuditModel audit = new UserRankingAuditModel();
        audit.rankingId = rankingId;
        audit.userId = userId;
        audit.operation = AuditOperationType.UPDATE;
        audit.fieldName = fieldName;
        audit.oldValue = oldValue;
        audit.newValue = newValue;
        audit.ipAddress = ipAddress;
        audit.userAgent = userAgent;
        audit.requestId = requestId;
        return audit;
    }

    /**
     * Construtor para operações CREATE/DELETE/RESTORE.
     */
    public static UserRankingAuditModel forOperation(
            AuditOperationType operation,
            UUID rankingId,
            UUID userId,
            String ipAddress,
            String userAgent,
            String requestId) {
        UserRankingAuditModel audit = new UserRankingAuditModel();
        audit.rankingId = rankingId;
        audit.userId = userId;
        audit.operation = operation;
        audit.ipAddress = ipAddress;
        audit.userAgent = userAgent;
        audit.requestId = requestId;
        return audit;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRankingId() {
        return rankingId;
    }

    public void setRankingId(UUID rankingId) {
        this.rankingId = rankingId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public AuditOperationType getOperation() {
        return operation;
    }

    public void setOperation(AuditOperationType operation) {
        this.operation = operation;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Integer getPointsAdded() {
        return pointsAdded;
    }

    public void setPointsAdded(Integer pointsAdded) {
        this.pointsAdded = pointsAdded;
    }

    public String getPointsReason() {
        return pointsReason;
    }

    public void setPointsReason(String pointsReason) {
        this.pointsReason = pointsReason;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
