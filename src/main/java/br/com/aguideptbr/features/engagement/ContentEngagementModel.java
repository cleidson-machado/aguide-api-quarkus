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
 * Entidade para armazenar HISTÓRICO DE NAVEGAÇÃO e INTERAÇÕES do usuário com
 * conteúdos.
 *
 * 🎯 OBJETIVO PRINCIPAL:
 * - Rastrear TODOS os vídeos que o usuário clicou e navegou no app Flutter
 * - Medir tempo de visualização e percentual de conclusão
 * - Identificar padrões de consumo de conteúdo
 * - Suportar algoritmos de recomendação baseados no histórico
 *
 * 📱 CASOS DE USO NO APP FLUTTER:
 * 1. Histórico de "Vídeos Assistidos Recentemente"
 * 2. Analytics de comportamento do usuário (tempo médio de visualização)
 * 3. Controle de "quantas vezes o usuário assistiu este vídeo" (repeatCount)
 * 4. Likes/Dislikes/Bookmarks/Compartilhamentos
 * 5. Sistema de comentários com engagementType=COMMENT
 *
 * 🔗 RELACIONAMENTOS:
 * - userId → FK para tabela users
 * - contentId → FK para tabela content_records
 *
 * @author Cleidson Machado
 * @since 1.0
 */
@Entity
@Table(name = "content_engagement_log")
public class ContentEngagementModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id; // PK do registro de engajamento

    // ══════════════════════════════════════════════════════════════
    // 🔗 RELACIONAMENTOS (Foreign Keys)
    // ══════════════════════════════════════════════════════════════
    @Column(name = "user_id", nullable = true)
    public UUID userId; // FK → users (qual usuário interagiu) - Aceita nulo

    @Column(name = "content_id", nullable = true)
    public UUID contentId; // FK → content_records (qual vídeo/conteúdo foi acessado) - Aceita nulo

    // ══════════════════════════════════════════════════════════════
    // 📊 TIPO E STATUS DA INTERAÇÃO
    // ══════════════════════════════════════════════════════════════
    @Column(name = "engagement_type", nullable = false)
    @Enumerated(EnumType.STRING)
    public EngagementType engagementType; // VIEW, LIKE, DISLIKE, SHARE, BOOKMARK, COMMENT, COMPLETE, PARTIAL_VIEW,
                                          // CLICK_TO_VIEW

    @Column(name = "engagement_status", nullable = false)
    @Enumerated(EnumType.STRING)
    public EngagementStatus engagementStatus; // ACTIVE, REMOVED, EXPIRED, FLAGGED

    // ══════════════════════════════════════════════════════════════
    // 🎥 DADOS DE VISUALIZAÇÃO DE VÍDEO (Histórico de Navegação)
    // ══════════════════════════════════════════════════════════════
    @Column(name = "view_duration_seconds")
    public Integer viewDurationSeconds; // Tempo que assistiu em segundos (ex: 300s = 5 minutos)

    @Column(name = "completion_percentage")
    public Integer completionPercentage; // Percentual de conclusão (0-100%, ex: 75% = assistiu até 75%)

    @Column(name = "repeat_count", columnDefinition = "INT DEFAULT 1")
    public Integer repeatCount = 1; // Quantas vezes clicou/assistiu esse vídeo

    // ══════════════════════════════════════════════════════════════
    // 📱 CONTEXTO TÉCNICO DO ACESSO (Device/Platform)
    // ══════════════════════════════════════════════════════════════
    @Column(name = "device_type", length = 20)
    public String deviceType; // Tipo de dispositivo (mobile, tablet, web)

    @Column(name = "platform", length = 20)
    public String platform; // Sistema operacional (Android, iOS, web)

    @Column(name = "source", length = 50)
    public String source; // De onde veio (home, search, recommendations, profile)

    @Column(name = "user_ip", length = 45)
    public String userIp; // IP do usuário (IPv4 ou IPv6)

    @Column(name = "user_agent", columnDefinition = "TEXT")
    public String userAgent; // User agent do navegador/app Flutter

    // ══════════════════════════════════════════════════════════════
    // 💬 DADOS ADICIONAIS DE ENGAJAMENTO
    // ══════════════════════════════════════════════════════════════
    @Column(name = "metadata", columnDefinition = "TEXT")
    public String metadata; // Dados extras em JSON (informações customizadas)

    @Column(name = "comment_text", columnDefinition = "TEXT")
    public String commentText; // Texto do comentário (usado quando engagementType = COMMENT)

    @Column(name = "rating")
    public Integer rating; // Avaliação numérica (ex: 1-5 estrelas)

    // ══════════════════════════════════════════════════════════════
    // ⏱️ TIMESTAMPS DE ENGAJAMENTO
    // ══════════════════════════════════════════════════════════════
    @Column(name = "engaged_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime engagedAt; // Quando começou a interação (clicou no vídeo)

    @Column(name = "ended_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime endedAt; // Quando terminou/saiu do vídeo

    // ══════════════════════════════════════════════════════════════
    // 📝 AUDITORIA (timestamps automáticos)
    // ══════════════════════════════════════════════════════════════
    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt; // Data de criação do registro

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt; // Última atualização do registro

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (engagedAt == null) {
            engagedAt = LocalDateTime.now();
        }

        if (engagementStatus == null) {
            engagementStatus = EngagementStatus.ACTIVE;
        }

        if (repeatCount == null) {
            repeatCount = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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

    public static List<ContentEngagementModel> findByUserId(UUID userId) {
        return list("userId", userId);
    }

    public static List<ContentEngagementModel> findByContentId(UUID contentId) {
        return list("contentId", contentId);
    }

    public static List<ContentEngagementModel> findByUserAndContent(UUID userId, UUID contentId) {
        return list("userId = ?1 and contentId = ?2", userId, contentId);
    }

    public static List<ContentEngagementModel> findActiveByUserAndContent(UUID userId, UUID contentId) {
        return list("userId = ?1 and contentId = ?2 and engagementStatus = ?3",
                userId, contentId, EngagementStatus.ACTIVE);
    }

    public static List<ContentEngagementModel> findByType(EngagementType engagementType) {
        return list("engagementType", engagementType);
    }

    public static List<ContentEngagementModel> findByUserAndType(UUID userId, EngagementType engagementType) {
        return list("userId = ?1 and engagementType = ?2", userId, engagementType);
    }

    public static List<ContentEngagementModel> findByContentAndType(UUID contentId, EngagementType engagementType) {
        return list("contentId = ?1 and engagementType = ?2", contentId, engagementType);
    }

    public static ContentEngagementModel findActiveEngagement(UUID userId, UUID contentId,
            EngagementType engagementType) {
        return find("userId = ?1 and contentId = ?2 and engagementType = ?3 and engagementStatus = ?4",
                userId, contentId, engagementType, EngagementStatus.ACTIVE)
                .firstResult();
    }

    public static long countByContent(UUID contentId) {
        return count("contentId = ?1 and engagementStatus = ?2", contentId, EngagementStatus.ACTIVE);
    }

    public static long countByContentAndType(UUID contentId, EngagementType engagementType) {
        return count("contentId = ?1 and engagementType = ?2 and engagementStatus = ?3",
                contentId, engagementType, EngagementStatus.ACTIVE);
    }

    public static List<ContentEngagementModel> findByStatus(EngagementStatus status) {
        return list("engagementStatus", status);
    }

    public static List<ContentEngagementModel> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return list("createdAt >= ?1 and createdAt <= ?2", startDate, endDate);
    }
}
