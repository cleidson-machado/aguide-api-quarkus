package br.com.aguideptbr.features.usermessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository para operações de banco de dados com mensagens.
 * Especifica UUID como tipo do ID (não o Long padrão).
 */
@ApplicationScoped
public class UserMessageRepository implements PanacheRepositoryBase<UserMessageModel, UUID> {

    /**
     * Busca mensagens de uma conversa (paginadas, ordenadas por data decrescente).
     *
     * @param conversationId ID da conversa
     * @param page           Número da página (0-based)
     * @param size           Tamanho da página
     * @return Lista de mensagens paginadas
     */
    public List<UserMessageModel> findByConversation(UUID conversationId, int page, int size) {
        return find("conversation.id = ?1 and deletedAt is null ORDER BY sentAt DESC", conversationId)
                .page(Page.of(page, size))
                .list();
    }

    /**
     * Busca mensagens de uma conversa respeitando o marco de limpeza do
     * participante.
     * Exclui mensagens com sentAt {@literal <=} clearedAt (ocultas apenas para este
     * usuário).
     *
     * @param conversationId ID da conversa
     * @param clearedAt      Marco de limpeza do participante (null = sem filtro)
     * @param page           Número da página (0-based)
     * @param size           Tamanho da página
     * @return Lista de mensagens paginadas visíveis para o participante
     */
    public List<UserMessageModel> findByConversationAfterClearedAt(
            UUID conversationId, LocalDateTime clearedAt, int page, int size) {
        if (clearedAt == null) {
            return findByConversation(conversationId, page, size);
        }
        return find(
                "conversation.id = ?1 and deletedAt is null and sentAt > ?2 ORDER BY sentAt DESC",
                conversationId, clearedAt)
                .page(Page.of(page, size))
                .list();
    }

    /**
     * Conta mensagens não lidas de um usuário em uma conversa, respeitando
     * cleared_at.
     */
    public long countUnreadByUserAfterClearedAt(UUID userId, UUID conversationId, LocalDateTime clearedAt) {
        if (clearedAt == null) {
            return countUnreadByUser(userId, conversationId);
        }
        return count(
                "conversation.id = ?1 and sender.id != ?2 and isRead = false and deletedAt is null and sentAt > ?3",
                conversationId, userId, clearedAt);
    }

    /**
     * Conta mensagens não lidas de um usuário em uma conversa.
     *
     * @param userId         ID do usuário
     * @param conversationId ID da conversa
     * @return Número de mensagens não lidas
     */
    public long countUnreadByUser(UUID userId, UUID conversationId) {
        return count("conversation.id = ?1 and sender.id != ?2 and isRead = false and deletedAt is null",
                conversationId, userId);
    }

    /**
     * Busca mensagens não lidas de um usuário em uma conversa.
     *
     * @param userId         ID do usuário
     * @param conversationId ID da conversa
     * @return Lista de mensagens não lidas
     */
    public List<UserMessageModel> findUnreadByUser(UUID userId, UUID conversationId) {
        return list(
                "conversation.id = ?1 and sender.id != ?2 and isRead = false and deletedAt is null ORDER BY sentAt ASC",
                conversationId, userId);
    }

    /**
     * Busca mensagens por texto em uma conversa (full-text search simplificado).
     *
     * @param conversationId ID da conversa
     * @param query          Texto a buscar
     * @return Lista de mensagens que contêm o texto
     */
    public List<UserMessageModel> searchInConversation(UUID conversationId, String query) {
        return list(
                "conversation.id = ?1 and LOWER(txtContent) LIKE LOWER(?2) and deletedAt is null ORDER BY sentAt DESC",
                conversationId, "%" + query + "%");
    }

    /**
     * Busca respostas a uma mensagem (thread).
     *
     * @param parentMessageId ID da mensagem pai
     * @return Lista de respostas
     */
    public List<UserMessageModel> findThreadReplies(UUID parentMessageId) {
        return list("parentMessage.id = ?1 and deletedAt is null ORDER BY sentAt ASC", parentMessageId);
    }

    /**
     * Busca a última mensagem de uma conversa.
     *
     * @param conversationId ID da conversa
     * @return Última mensagem ou null
     */
    public UserMessageModel findLastByConversation(UUID conversationId) {
        return find("conversation.id = ?1 and deletedAt is null ORDER BY sentAt DESC", conversationId)
                .firstResult();
    }

    /**
     * Conta total de mensagens em uma conversa.
     *
     * @param conversationId ID da conversa
     * @return Número total de mensagens
     */
    public long countByConversation(UUID conversationId) {
        return count("conversation.id = ?1 and deletedAt is null", conversationId);
    }

    /**
     * Busca uma mensagem por ID (apenas se não deletada).
     *
     * @param messageId ID da mensagem
     * @return Mensagem ou null
     */
    public UserMessageModel findByIdActive(UUID messageId) {
        return find("id = ?1 and deletedAt is null", messageId).firstResult();
    }

    /**
     * Verifica se o usuário é o remetente da mensagem.
     *
     * @param messageId ID da mensagem
     * @param userId    ID do usuário
     * @return true se o usuário é o remetente
     */
    public boolean isUserSender(UUID messageId, UUID userId) {
        return count("id = ?1 and sender.id = ?2 and deletedAt is null", messageId, userId) > 0;
    }
}
