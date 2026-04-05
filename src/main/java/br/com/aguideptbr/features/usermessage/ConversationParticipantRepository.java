package br.com.aguideptbr.features.usermessage;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository para operações de banco de dados com participantes de conversas.
 * Especifica UUID como tipo do ID (não o Long padrão).
 */
@ApplicationScoped
public class ConversationParticipantRepository implements PanacheRepositoryBase<ConversationParticipantModel, UUID> {

    /**
     * Busca participante por usuário e conversa.
     *
     * @param userId         ID do usuário
     * @param conversationId ID da conversa
     * @return Registro de participante ou null
     */
    public ConversationParticipantModel findByUserAndConversation(UUID userId, UUID conversationId) {
        return find("user.id = ?1 and conversation.id = ?2", userId, conversationId).firstResult();
    }

    /**
     * Busca todos os participantes ativos de uma conversa.
     *
     * @param conversationId ID da conversa
     * @return Lista de participantes
     */
    public List<ConversationParticipantModel> findByConversation(UUID conversationId) {
        return list("conversation.id = ?1 and leftAt is null", conversationId);
    }

    /**
     * Verifica se um usuário é participante de uma conversa.
     *
     * @param userId         ID do usuário
     * @param conversationId ID da conversa
     * @return true se o usuário é participante ativo
     */
    public boolean isUserParticipant(UUID userId, UUID conversationId) {
        return count("user.id = ?1 and conversation.id = ?2 and leftAt is null", userId, conversationId) > 0;
    }

    /**
     * Verifica se um usuário é administrador de uma conversa.
     *
     * @param userId         ID do usuário
     * @param conversationId ID da conversa
     * @return true se o usuário é administrador
     */
    public boolean isUserAdmin(UUID userId, UUID conversationId) {
        return count("user.id = ?1 and conversation.id = ?2 and isAdmin = true and leftAt is null",
                userId, conversationId) > 0;
    }

    /**
     * Verifica se um usuário é criador de uma conversa.
     *
     * @param userId         ID do usuário
     * @param conversationId ID da conversa
     * @return true se o usuário é criador
     */
    public boolean isUserCreator(UUID userId, UUID conversationId) {
        return count("user.id = ?1 and conversation.id = ?2 and isCreator = true and leftAt is null",
                userId, conversationId) > 0;
    }

    /**
     * Conta conversas não arquivadas de um usuário.
     *
     * @param userId ID do usuário
     * @return Número de conversas não arquivadas
     */
    public long countActiveByUser(UUID userId) {
        return count("user.id = ?1 and isArchived = false and leftAt is null", userId);
    }

    /**
     * Conta conversas com mensagens não lidas de um usuário.
     *
     * @param userId ID do usuário
     * @return Número de conversas com mensagens não lidas
     */
    public long countUnreadConversations(UUID userId) {
        // Conta conversas onde há mensagens mais recentes que a última lida
        return count("SELECT COUNT(DISTINCT p) FROM ConversationParticipantModel p " +
                "WHERE p.user.id = ?1 " +
                "AND p.leftAt IS NULL " +
                "AND EXISTS (" +
                "  SELECT m FROM UserMessageModel m " +
                "  WHERE m.conversation.id = p.conversation.id " +
                "  AND m.sender.id != ?1 " +
                "  AND m.deletedAt IS NULL " +
                "  AND (p.lastReadMessage IS NULL OR m.sentAt > p.lastReadAt)" +
                ")", userId);
    }

    /**
     * Busca participantes administradores de uma conversa.
     *
     * @param conversationId ID da conversa
     * @return Lista de administradores
     */
    public List<ConversationParticipantModel> findAdminsByConversation(UUID conversationId) {
        return list("conversation.id = ?1 and isAdmin = true and leftAt is null", conversationId);
    }

    /**
     * Conta participantes ativos de uma conversa.
     *
     * @param conversationId ID da conversa
     * @return Número de participantes
     */
    public long countByConversation(UUID conversationId) {
        return count("conversation.id = ?1 and leftAt is null", conversationId);
    }

    /**
     * Remove um participante de uma conversa (marca como saído).
     *
     * @param userId         ID do usuário
     * @param conversationId ID da conversa
     */
    public void markAsLeft(UUID userId, UUID conversationId) {
        update("leftAt = CURRENT_TIMESTAMP WHERE user.id = ?1 and conversation.id = ?2 and leftAt is null",
                userId, conversationId);
    }
}
