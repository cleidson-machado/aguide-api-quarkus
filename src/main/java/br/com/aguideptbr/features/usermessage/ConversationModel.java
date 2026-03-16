package br.com.aguideptbr.features.usermessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entidade representando uma conversa (thread) entre usuários.
 *
 * Suporta:
 * - Conversas diretas 1-1 (DIRECT)
 * - Grupos privados (GROUP)
 * - Canais públicos (CHANNEL)
 *
 * Relacionamento N-N com UserModel via ConversationParticipantModel.
 */
@Entity
@Table(name = "app_conversation")
public class ConversationModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * Nome da conversa.
     * - DIRECT: null (usa nome dos participantes)
     * - GROUP: nome personalizado do grupo
     * - CHANNEL: nome do canal
     */
    @Column(length = 255)
    public String name;

    /**
     * Descrição da conversa (para grupos e canais).
     */
    @Column(columnDefinition = "TEXT")
    public String description;

    /**
     * URL do ícone/foto da conversa (para grupos e canais).
     */
    @Column(name = "icon_url", length = 512)
    public String iconUrl;

    /**
     * Tipo de conversa (DIRECT, GROUP, CHANNEL).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_type", length = 20, nullable = false)
    public ConversationType conversationType = ConversationType.DIRECT;

    /**
     * Data da última mensagem enviada nesta conversa.
     * Atualizado automaticamente ao enviar mensagem.
     */
    @Column(name = "last_message_at")
    public LocalDateTime lastMessageAt;

    /**
     * Se true, conversa está arquivada.
     * Arquivamento é por usuário (via ConversationParticipantModel).
     */
    @Column(name = "is_archived", nullable = false)
    public boolean isArchived = false;

    /**
     * Se true, conversa está fixada (pinned).
     * Fixação é por usuário (via ConversationParticipantModel).
     */
    @Column(name = "is_pinned", nullable = false)
    public boolean isPinned = false;

    /**
     * Participantes da conversa.
     * Relacionamento N-N com UserModel via ConversationParticipantModel.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ConversationParticipantModel> participants = new ArrayList<>();

    /**
     * Mensagens da conversa.
     * Relacionamento 1-N com UserMessageModel.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<UserMessageModel> messages = new ArrayList<>();

    /**
     * Data de criação da conversa.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    /**
     * Data da última atualização da conversa.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    /**
     * Data de exclusão lógica da conversa (soft delete).
     * Null significa que a conversa está ativa.
     */
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    // ========== Métodos de Negócio ==========

    /**
     * Verifica se é uma conversa direta (1-1).
     */
    public boolean isDirect() {
        return conversationType == ConversationType.DIRECT;
    }

    /**
     * Verifica se é um grupo.
     */
    public boolean isGroup() {
        return conversationType == ConversationType.GROUP;
    }

    /**
     * Verifica se é um canal.
     */
    public boolean isChannel() {
        return conversationType == ConversationType.CHANNEL;
    }

    /**
     * Verifica se a conversa está ativa.
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * Marca a conversa como deletada (soft delete).
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restaura uma conversa deletada.
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * Retorna o número de participantes ativos.
     */
    public long getActiveParticipantsCount() {
        return participants.stream()
                .filter(p -> p.leftAt == null)
                .count();
    }

    /**
     * Atualiza a data da última mensagem.
     */
    public void updateLastMessageAt() {
        this.lastMessageAt = LocalDateTime.now();
    }
}
