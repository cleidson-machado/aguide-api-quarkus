package br.com.aguideptbr.features.usermessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Service para lógica de negócio relacionada a mensagens.
 *
 * Responsável por:
 * - Envio de mensagens (texto, imagens, links, vídeos, arquivos)
 * - Marcação de mensagens como lidas (read receipts)
 * - Edição e exclusão de mensagens
 * - Busca e paginação de mensagens
 * - Threads (respostas a mensagens)
 * - Validação de permissões
 */
@ApplicationScoped
public class MessageService {

    private final UserMessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final UserBlockRepository blockRepository;
    private final Logger log;

    @Inject
    public MessageService(
            UserMessageRepository messageRepository,
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            UserBlockRepository blockRepository,
            Logger log) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.blockRepository = blockRepository;
        this.log = log;
    }

    /**
     * Envia uma nova mensagem em uma conversa.
     *
     * @param senderId        ID do usuário remetente
     * @param conversationId  ID da conversa
     * @param content         Conteúdo da mensagem
     * @param messageType     Tipo de mensagem (TEXT, IMAGE, LINK, VIDEO, FILE)
     * @param parentMessageId ID da mensagem pai (para threads/respostas), null se
     *                        mensagem original
     * @return Mensagem criada
     * @throws NotFoundException   se conversa não existe
     * @throws ForbiddenException  se usuário não é participante
     * @throws BadRequestException se dados inválidos
     */
    @Transactional
    public UserMessageModel sendMessage(
            UUID senderId,
            UUID conversationId,
            String content,
            MessageType messageType,
            UUID parentMessageId) {

        log.infof("Sending message: sender=%s, conversation=%s, type=%s", senderId, conversationId, messageType);

        // Validar conversa existe
        ConversationModel conversation = conversationRepository.findByIdActive(conversationId);
        if (conversation == null) {
            log.warnf("Send message denied: conversation %s not found", conversationId);
            throw new NotFoundException("Conversa não encontrada");
        }

        // Validar usuário é participante
        if (!participantRepository.isUserParticipant(senderId, conversationId)) {
            log.warnf("Send message denied: user %s is not participant of conversation %s", senderId, conversationId);
            throw new ForbiddenException("Você não é participante desta conversa");
        }

        // Para conversas DIRECT, verificar bloqueio entre os participantes
        if (conversation.conversationType == ConversationType.DIRECT) {
            List<ConversationParticipantModel> participants = participantRepository.findByConversation(conversationId);
            for (ConversationParticipantModel p : participants) {
                if (!p.user.id.equals(senderId) && blockRepository.isBlockedInAnyDirection(senderId, p.user.id)) {
                    log.warnf("Send message denied: block detected between %s and %s", senderId, p.user.id);
                    throw new WebApplicationException(
                            Response.status(409)
                                    .entity(Map.of(
                                            "error", "BUSINESS_RULE",
                                            "message", "Não é possível enviar mensagem para este usuário"))
                                    .build());
                }
            }
        }

        // Validar conteúdo
        if (messageType == MessageType.TEXT && (content == null || content.trim().isEmpty())) {
            log.warnf("Send message denied: empty text content for conversation %s by user %s", conversationId,
                    senderId);
            throw new BadRequestException("Mensagem de texto não pode estar vazia");
        }

        // Validar mensagem pai (se for resposta)
        UserMessageModel parentMessage = null;
        if (parentMessageId != null) {
            parentMessage = messageRepository.findByIdActive(parentMessageId);
            if (parentMessage == null) {
                throw new NotFoundException("Mensagem pai não encontrada");
            }
            if (!parentMessage.conversation.id.equals(conversationId)) {
                throw new BadRequestException("Mensagem pai não pertence a esta conversa");
            }
        }

        // Criar mensagem
        UserMessageModel message = new UserMessageModel();
        message.conversation = conversation;
        message.sender = new br.com.aguideptbr.features.user.UserModel();
        message.sender.id = senderId;
        message.txtContent = content;
        message.messageType = messageType;
        message.parentMessage = parentMessage;
        message.sentAt = LocalDateTime.now();

        messageRepository.persist(message);

        // Atualizar data da última mensagem na conversa
        conversation.lastMessageAt = message.sentAt;
        conversationRepository.persist(conversation);

        log.infof("Message sent successfully: id=%s", message.id);
        return message;
    }

    /**
     * Marca uma mensagem como lida.
     *
     * @param messageId ID da mensagem
     * @param userId    ID do usuário que leu
     * @throws NotFoundException  se mensagem não existe
     * @throws ForbiddenException se usuário não é participante
     */
    @Transactional
    public void markAsRead(UUID messageId, UUID userId) {
        log.infof("Marking message as read: message=%s, user=%s", messageId, userId);

        // Buscar mensagem
        UserMessageModel message = messageRepository.findByIdActive(messageId);
        if (message == null) {
            throw new NotFoundException("Mensagem não encontrada");
        }

        // Validar usuário é participante
        if (!participantRepository.isUserParticipant(userId, message.conversation.id)) {
            throw new ForbiddenException("Você não é participante desta conversa");
        }

        // Não marcar como lida se o usuário é o remetente
        if (message.sender.id.equals(userId)) {
            log.debugf("Skipping mark as read: user is the sender");
            return;
        }

        // Marcar como lida
        message.isRead = true;
        message.readAt = LocalDateTime.now();
        messageRepository.persist(message);

        // Atualizar registro de participante
        ConversationParticipantModel participant = participantRepository.findByUserAndConversation(userId,
                message.conversation.id);
        if (participant != null) {
            participant.markAsRead(message);
            participantRepository.persist(participant);
        }

        log.debugf("Message marked as read successfully");
    }

    /**
     * Edita uma mensagem existente.
     *
     * @param messageId  ID da mensagem
     * @param userId     ID do usuário que está editando
     * @param newContent Novo conteúdo da mensagem
     * @return Mensagem editada
     * @throws NotFoundException  se mensagem não existe
     * @throws ForbiddenException se usuário não é o remetente
     */
    @Transactional
    public UserMessageModel editMessage(UUID messageId, UUID userId, String newContent) {
        log.infof("Editing message: message=%s, user=%s", messageId, userId);

        // Buscar mensagem
        UserMessageModel message = messageRepository.findByIdActive(messageId);
        if (message == null) {
            throw new NotFoundException("Mensagem não encontrada");
        }

        // Validar usuário é o remetente
        if (!message.sender.id.equals(userId)) {
            throw new ForbiddenException("Você só pode editar suas próprias mensagens");
        }

        // Validar novo conteúdo
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new BadRequestException("Conteúdo da mensagem não pode estar vazio");
        }

        // Editar mensagem
        message.txtContent = newContent;
        message.isEdited = true;
        message.editedAt = LocalDateTime.now();
        messageRepository.persist(message);

        log.infof("Message edited successfully");
        return message;
    }

    /**
     * Deleta uma mensagem (soft delete).
     *
     * @param messageId ID da mensagem
     * @param userId    ID do usuário que está deletando
     * @throws NotFoundException  se mensagem não existe
     * @throws ForbiddenException se usuário não é o remetente
     */
    @Transactional
    public void deleteMessage(UUID messageId, UUID userId) {
        log.infof("Deleting message: message=%s, user=%s", messageId, userId);

        // Buscar mensagem
        UserMessageModel message = messageRepository.findByIdActive(messageId);
        if (message == null) {
            throw new NotFoundException("Mensagem não encontrada");
        }

        // Validar usuário é o remetente
        if (!message.sender.id.equals(userId)) {
            throw new ForbiddenException("Você só pode deletar suas próprias mensagens");
        }

        // Soft delete
        message.softDelete();
        messageRepository.persist(message);

        log.infof("Message deleted successfully");
    }

    /**
     * Busca mensagens de uma conversa (paginadas).
     *
     * @param conversationId ID da conversa
     * @param userId         ID do usuário solicitante
     * @param page           Número da página (0-based)
     * @param size           Tamanho da página
     * @return Lista de mensagens
     * @throws NotFoundException  se conversa não existe
     * @throws ForbiddenException se usuário não é participante
     */
    public List<UserMessageModel> getMessagesByConversation(
            UUID conversationId,
            UUID userId,
            int page,
            int size) {

        log.infof("Getting messages: conversation=%s, user=%s, page=%d, size=%d",
                conversationId, userId, page, size);

        // Validar conversa existe
        if (conversationRepository.findByIdActive(conversationId) == null) {
            log.warnf("Get messages denied: conversation %s not found", conversationId);
            throw new NotFoundException("Conversa não encontrada");
        }

        // Validar usuário é participante
        ConversationParticipantModel participant = participantRepository.findByUserAndConversation(userId,
                conversationId);
        if (participant == null || !participant.isActive()) {
            log.warnf("Get messages denied: user %s is not participant of conversation %s", userId, conversationId);
            throw new ForbiddenException("Você não é participante desta conversa");
        }

        // Respeitar marco de limpeza do participante
        return messageRepository.findByConversationAfterClearedAt(conversationId, participant.clearedAt, page, size);
    }

    /**
     * Conta o total de mensagens ativas em uma conversa.
     * Usado para metadata de paginação (totalElements/totalPages).
     */
    public long countTotalMessages(UUID conversationId) {
        return messageRepository.countByConversation(conversationId);
    }

    /**
     * Busca mensagens por texto em uma conversa.
     *
     * @param conversationId ID da conversa
     * @param query          Texto a buscar
     * @param userId         ID do usuário solicitante
     * @return Lista de mensagens que contêm o texto
     * @throws NotFoundException  se conversa não existe
     * @throws ForbiddenException se usuário não é participante
     */
    public List<UserMessageModel> searchMessages(UUID conversationId, String query, UUID userId) {
        log.infof("Searching messages: conversation=%s, query=%s, user=%s", conversationId, query, userId);

        // Validar conversa existe
        if (conversationRepository.findByIdActive(conversationId) == null) {
            throw new NotFoundException("Conversa não encontrada");
        }

        // Validar usuário é participante
        if (!participantRepository.isUserParticipant(userId, conversationId)) {
            throw new ForbiddenException("Você não é participante desta conversa");
        }

        // Validar query
        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException("Texto de busca não pode estar vazio");
        }

        return messageRepository.searchInConversation(conversationId, query);
    }

    /**
     * Busca respostas a uma mensagem (thread).
     *
     * @param parentMessageId ID da mensagem pai
     * @param userId          ID do usuário solicitante
     * @return Lista de respostas
     * @throws NotFoundException  se mensagem não existe
     * @throws ForbiddenException se usuário não é participante
     */
    public List<UserMessageModel> getThreadReplies(UUID parentMessageId, UUID userId) {
        log.infof("Getting thread replies: parentMessage=%s, user=%s", parentMessageId, userId);

        // Buscar mensagem pai
        UserMessageModel parentMessage = messageRepository.findByIdActive(parentMessageId);
        if (parentMessage == null) {
            throw new NotFoundException("Mensagem não encontrada");
        }

        // Validar usuário é participante
        if (!participantRepository.isUserParticipant(userId, parentMessage.conversation.id)) {
            throw new ForbiddenException("Você não é participante desta conversa");
        }

        return messageRepository.findThreadReplies(parentMessageId);
    }

    /**
     * Conta mensagens não lidas de um usuário em uma conversa.
     *
     * @param userId         ID do usuário
     * @param conversationId ID da conversa
     * @return Número de mensagens não lidas
     */
    public long countUnreadMessages(UUID userId, UUID conversationId) {
        return messageRepository.countUnreadByUser(userId, conversationId);
    }
}
