package br.com.aguideptbr.features.usermessage;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import br.com.aguideptbr.features.user.UserModel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidade intermediária para relacionamento N-N entre Conversation e User.
 *
 * Armazena metadados específicos de cada participante em cada conversa:
 * - Última mensagem lida
 * - Status de administrador (para grupos)
 * - Data de entrada/saída
 * - Arquivamento/fixação por usuário
 *
 * Tabela: app_conversation_participant
 */
@Entity
@Table(name = "app_conversation_participant")
public class ConversationParticipantModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * Conversa da qual o usuário participa.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    public ConversationModel conversation;

    /**
     * Usuário participante da conversa.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public UserModel user;

    /**
     * Última mensagem lida por este usuário nesta conversa.
     * Usado para calcular contador de mensagens não lidas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    public UserMessageModel lastReadMessage;

    /**
     * Data e hora da última leitura.
     */
    @Column(name = "last_read_at")
    public LocalDateTime lastReadAt;

    /**
     * Se true, este usuário é administrador da conversa.
     * Aplicável apenas para grupos e canais.
     */
    @Column(name = "is_admin", nullable = false)
    public boolean isAdmin = false;

    /**
     * Se true, este usuário é criador da conversa.
     * Criador tem permissões especiais (deletar grupo, promover admins).
     */
    @Column(name = "is_creator", nullable = false)
    public boolean isCreator = false;

    /**
     * Se true, usuário arquivou esta conversa.
     * Arquivamento é por usuário (não afeta outros participantes).
     */
    @Column(name = "is_archived", nullable = false)
    public boolean isArchived = false;

    /**
     * Se true, usuário fixou esta conversa.
     * Fixação é por usuário (não afeta outros participantes).
     */
    @Column(name = "is_pinned", nullable = false)
    public boolean isPinned = false;

    /**
     * Se true, usuário silenciou notificações desta conversa.
     */
    @Column(name = "is_muted", nullable = false)
    public boolean isMuted = false;

    /**
     * Data/hora em que o usuário silenciou esta conversa.
     * Null quando não silenciada. Atualizado junto com isMuted.
     */
    @Column(name = "muted_at")
    public LocalDateTime mutedAt;

    /**
     * Marco de limpeza por participante.
     * Mensagens com sentAt {@literal <=} clearedAt ficam ocultas apenas para este
     * usuário.
     * Outros participantes continuam vendo o histórico completo.
     */
    @Column(name = "cleared_at")
    public LocalDateTime clearedAt;

    /**
     * Data de entrada do usuário na conversa.
     */
    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    public LocalDateTime joinedAt;

    /**
     * Data de saída do usuário da conversa.
     * Null significa que o usuário ainda participa.
     * Soft delete do participante (não deleta o usuário, apenas remove da
     * conversa).
     */
    @Column(name = "left_at")
    public LocalDateTime leftAt;

    // ========== Métodos de Negócio ==========

    /**
     * Verifica se o usuário ainda participa da conversa.
     */
    public boolean isActive() {
        return leftAt == null;
    }

    /**
     * Marca que o usuário saiu da conversa.
     */
    public void leave() {
        this.leftAt = LocalDateTime.now();
    }

    /**
     * Permite que o usuário retorne à conversa.
     */
    public void rejoin() {
        this.leftAt = null;
    }

    /**
     * Atualiza a última mensagem lida.
     */
    public void markAsRead(UserMessageModel message) {
        this.lastReadMessage = message;
        this.lastReadAt = LocalDateTime.now();
    }

    /**
     * Arquiva a conversa para este usuário.
     */
    public void archive() {
        this.isArchived = true;
    }

    /**
     * Desarquiva a conversa para este usuário.
     */
    public void unarchive() {
        this.isArchived = false;
    }

    /**
     * Fixa a conversa para este usuário.
     */
    public void pin() {
        this.isPinned = true;
    }

    /**
     * Desafixa a conversa para este usuário.
     */
    public void unpin() {
        this.isPinned = false;
    }

    /**
     * Silencia notificações da conversa.
     */
    public void mute() {
        this.isMuted = true;
    }

    /**
     * Ativa notificações da conversa.
     */
    public void unmute() {
        this.isMuted = false;
    }

    /**
     * Promove o usuário a administrador.
     */
    public void promoteToAdmin() {
        this.isAdmin = true;
    }

    /**
     * Remove privilégios de administrador.
     */
    public void demoteFromAdmin() {
        this.isAdmin = false;
    }
}
