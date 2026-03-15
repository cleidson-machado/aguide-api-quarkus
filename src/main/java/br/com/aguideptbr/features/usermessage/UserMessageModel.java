package br.com.aguideptbr.features.usermessage;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.aguideptbr.features.user.UserModel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidade representando uma mensagem em uma conversa.
 *
 * Suporta:
 * - Mensagens diretas 1-1 (via ConversationModel)
 * - Mensagens em grupos (via ConversationModel)
 * - Mensagens em canais (via ConversationModel)
 * - Respostas a mensagens (reply/thread)
 * - Múltiplos tipos de conteúdo (texto, imagem, link, vídeo, arquivo)
 *
 * Padrão: Facebook Messenger, Slack, Discord
 */
@Entity
@Table(name = "app_user_message")
public class UserMessageModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * Conversa à qual esta mensagem pertence.
     * Relacionamento N-1: Uma conversa pode ter muitas mensagens.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    public ConversationModel conversation;

    /**
     * Usuário que enviou a mensagem.
     * Relacionamento N-1: Um usuário pode enviar muitas mensagens.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    public UserModel sender;

    /**
     * Conteúdo da mensagem (texto).
     * Para mensagens de imagem/vídeo/arquivo, pode conter URL ou legenda.
     */
    @Column(name = "txt_content", columnDefinition = "TEXT")
    public String txtContent;

    /**
     * Tipo de mensagem (TEXT, IMAGE, LINK, VIDEO, FILE).
     * Armazenado como String no banco, manipulado como Enum no código.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20, nullable = false)
    public MessageType messageType = MessageType.TEXT;

    /**
     * Indica se a mensagem foi lida pelo destinatário.
     */
    @Column(name = "is_read", nullable = false)
    public boolean isRead = false;

    /**
     * Data e hora em que a mensagem foi lida.
     * Null se ainda não foi lida.
     */
    @Column(name = "read_at")
    public LocalDateTime readAt;

    /**
     * Data e hora em que a mensagem foi enviada.
     * Pode ser diferente de createdAt em casos de mensagens agendadas (futuro).
     */
    @Column(name = "sent_at", nullable = false)
    public LocalDateTime sentAt;

    /**
     * Mensagem pai (para respostas/threads).
     * Null se for uma mensagem original (não é resposta).
     * Permite criar threads de conversação.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    public UserMessageModel parentMessage;

    /**
     * Se true, mensagem foi editada após o envio.
     */
    @Column(name = "is_edited", nullable = false)
    public boolean isEdited = false;

    /**
     * Data da última edição da mensagem.
     */
    @Column(name = "edited_at")
    public LocalDateTime editedAt;

    /**
     * Data de criação do registro.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    /**
     * Data da última atualização do registro.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    /**
     * Data de exclusão lógica da mensagem (soft delete).
     * Null significa que a mensagem está ativa.
     */
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    // ========== Métodos de Negócio ==========

    /**
     * Verifica se a mensagem está ativa (não deletada).
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * Marca a mensagem como deletada (soft delete).
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restaura uma mensagem deletada.
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * Verifica se é uma resposta a outra mensagem.
     */
    public boolean isReply() {
        return parentMessage != null;
    }

    /**
     * Marca a mensagem como editada.
     */
    public void markAsEdited() {
        this.isEdited = true;
        this.editedAt = LocalDateTime.now();
    }

    /**
     * Marca a mensagem como lida.
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }
}
