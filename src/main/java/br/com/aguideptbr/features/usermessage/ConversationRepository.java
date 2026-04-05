package br.com.aguideptbr.features.usermessage;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository para operações de banco de dados com conversas.
 * Especifica UUID como tipo do ID (não o Long padrão).
 */
@ApplicationScoped
public class ConversationRepository implements PanacheRepositoryBase<ConversationModel, UUID> {

    /**
     * Busca conversas de um usuário (via participantes), ordenadas por última
     * mensagem.
     *
     * @param userId ID do usuário
     * @return Lista de conversas do usuário
     */
    public List<ConversationModel> findByUserId(UUID userId) {
        return find("SELECT DISTINCT c FROM ConversationModel c " +
                "JOIN c.participants p " +
                "WHERE p.user.id = ?1 " +
                "AND p.leftAt IS NULL " +
                "AND c.deletedAt IS NULL " +
                "ORDER BY c.lastMessageAt DESC NULLS LAST", userId)
                .list();
    }

    /**
     * Busca conversas ativas de um usuário (não arquivadas).
     *
     * @param userId ID do usuário
     * @return Lista de conversas ativas
     */
    public List<ConversationModel> findActiveByUserId(UUID userId) {
        return find("SELECT DISTINCT c FROM ConversationModel c " +
                "JOIN c.participants p " +
                "WHERE p.user.id = ?1 " +
                "AND p.leftAt IS NULL " +
                "AND p.isArchived = false " +
                "AND c.deletedAt IS NULL " +
                "ORDER BY c.lastMessageAt DESC NULLS LAST", userId)
                .list();
    }

    /**
     * Busca conversa direta entre dois usuários.
     *
     * @param user1Id ID do primeiro usuário
     * @param user2Id ID do segundo usuário
     * @return Conversa existente ou null
     */
    public ConversationModel findDirectConversation(UUID user1Id, UUID user2Id) {
        return find("SELECT c FROM ConversationModel c " +
                "WHERE c.conversationType = ?1 " +
                "AND c.deletedAt IS NULL " +
                "AND EXISTS (SELECT p1 FROM ConversationParticipantModel p1 WHERE p1.conversation.id = c.id AND p1.user.id = ?2) "
                +
                "AND EXISTS (SELECT p2 FROM ConversationParticipantModel p2 WHERE p2.conversation.id = c.id AND p2.user.id = ?3) "
                +
                "AND (SELECT COUNT(p) FROM ConversationParticipantModel p WHERE p.conversation.id = c.id AND p.leftAt IS NULL) = 2",
                ConversationType.DIRECT, user1Id, user2Id)
                .firstResult();
    }

    /**
     * Busca conversas pelo nome (para grupos e canais).
     *
     * @param userId ID do usuário (para verificar participação)
     * @param query  Texto a buscar no nome
     * @return Lista de conversas que correspondem à busca
     */
    public List<ConversationModel> searchByName(UUID userId, String query) {
        return find("SELECT DISTINCT c FROM ConversationModel c " +
                "JOIN c.participants p " +
                "WHERE p.user.id = ?1 " +
                "AND p.leftAt IS NULL " +
                "AND LOWER(c.name) LIKE LOWER(?2) " +
                "AND c.deletedAt IS NULL " +
                "ORDER BY c.lastMessageAt DESC NULLS LAST",
                userId, "%" + query + "%")
                .list();
    }

    /**
     * Busca uma conversa por ID (apenas se não deletada).
     *
     * @param conversationId ID da conversa
     * @return Conversa ou null
     */
    public ConversationModel findByIdActive(UUID conversationId) {
        return find("id = ?1 and deletedAt is null", conversationId).firstResult();
    }

    /**
     * Busca conversa com participantes carregados (eager loading).
     *
     * @param conversationId ID da conversa
     * @return Conversa com participantes ou null
     */
    public ConversationModel findByIdWithParticipants(UUID conversationId) {
        return find("SELECT DISTINCT c FROM ConversationModel c " +
                "LEFT JOIN FETCH c.participants p " +
                "WHERE c.id = ?1 " +
                "AND c.deletedAt IS NULL", conversationId)
                .firstResult();
    }

    /**
     * Conta conversas de um usuário.
     *
     * @param userId ID do usuário
     * @return Número de conversas
     */
    public long countByUserId(UUID userId) {
        return count("SELECT COUNT(DISTINCT c) FROM ConversationModel c " +
                "JOIN c.participants p " +
                "WHERE p.user.id = ?1 " +
                "AND p.leftAt IS NULL " +
                "AND c.deletedAt IS NULL", userId);
    }
}
