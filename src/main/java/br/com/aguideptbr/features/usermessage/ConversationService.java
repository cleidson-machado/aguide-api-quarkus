package br.com.aguideptbr.features.usermessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.user.UserModel;
import br.com.aguideptbr.features.usermessage.dto.ConversationSummaryDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

/**
 * Service para lógica de negócio relacionada a conversas.
 *
 * Responsável por:
 * - Criação de conversas diretas (1-1) e grupos
 * - Gerenciamento de participantes (adicionar/remover)
 * - Arquivamento e fixação de conversas (por usuário)
 * - Listagem de conversas (inbox)
 * - Contadores de mensagens não lidas
 * - Validação de permissões
 */
@ApplicationScoped
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final UserMessageRepository messageRepository;
    private final UserBlockRepository blockRepository;
    private final Logger log;

    @Inject
    public ConversationService(
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            UserMessageRepository messageRepository,
            UserBlockRepository blockRepository,
            Logger log) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.blockRepository = blockRepository;
        this.log = log;
    }

    /**
     * Verifica se já existe uma conversa direta entre dois usuários.
     * Usado pelo controller para determinar o status HTTP correto (200 vs 201).
     *
     * @param user1Id ID do primeiro usuário
     * @param user2Id ID do segundo usuário
     * @return true se já existe conversa direta entre eles
     */
    public boolean directConversationExists(UUID user1Id, UUID user2Id) {
        return conversationRepository.findDirectConversation(user1Id, user2Id) != null;
    }

    /**
     * Cria uma conversa direta entre dois usuários.
     * Se já existe uma conversa entre eles, retorna a existente.
     *
     * @param user1Id ID do primeiro usuário
     * @param user2Id ID do segundo usuário
     * @return Conversa criada ou existente
     * @throws BadRequestException se user1 == user2
     * @throws NotFoundException   se algum usuário não existe
     */
    @Transactional
    public ConversationModel createDirectConversation(UUID user1Id, UUID user2Id) {
        log.infof("Creating direct conversation: user1=%s, user2=%s", user1Id, user2Id);

        // Validar usuários diferentes
        if (user1Id.equals(user2Id)) {
            throw new BadRequestException("Não é possível criar uma conversa consigo mesmo");
        }

        // Verificar bloqueio em qualquer direção (fail-fast, antes de carregar
        // entidades)
        if (blockRepository.isBlockedInAnyDirection(user1Id, user2Id)) {
            throw new jakarta.ws.rs.WebApplicationException(
                    jakarta.ws.rs.core.Response.status(409)
                            .entity(Map.of(
                                    "error", "BUSINESS_RULE",
                                    "message", "Não é possível iniciar conversa com este usuário"))
                            .build());
        }

        // Verificar se usuários existem
        UserModel user1 = UserModel.findByIdActive(user1Id);
        UserModel user2 = UserModel.findByIdActive(user2Id);
        if (user1 == null || user2 == null) {
            throw new NotFoundException("Um ou mais usuários não encontrados");
        }

        // Verificar bloqueio em qualquer direção
        if (blockRepository.isBlockedInAnyDirection(user1Id, user2Id)) {
            throw new jakarta.ws.rs.WebApplicationException(
                    jakarta.ws.rs.core.Response.status(409)
                            .entity(Map.of(
                                    "error", "BUSINESS_RULE",
                                    "message", "Não é possível iniciar conversa com este usuário"))
                            .build());
        }

        // Verificar se já existe conversa direta entre eles
        ConversationModel existingConversation = conversationRepository.findDirectConversation(user1Id, user2Id);
        if (existingConversation != null) {
            log.infof("Direct conversation already exists: id=%s", existingConversation.id);
            // Recarregar com participantes e usuários usando JOIN FETCH para evitar
            // LazyInitializationException ao construir ConversationDetailResponse fora
            // desta transação (participants é LAZY no ConversationModel).
            return conversationRepository.findByIdWithParticipants(existingConversation.id);
        }

        // Criar nova conversa
        ConversationModel conversation = new ConversationModel();
        conversation.conversationType = ConversationType.DIRECT;
        conversation.name = null; // Conversas diretas não têm nome
        conversationRepository.persist(conversation);

        // Adicionar participantes
        createParticipant(conversation, user1, false, false);
        createParticipant(conversation, user2, false, false);

        log.infof("Direct conversation created: id=%s", conversation.id);
        return conversation;
    }

    /**
     * Cria um grupo de conversa.
     *
     * @param name           Nome do grupo
     * @param description    Descrição do grupo (opcional)
     * @param creatorId      ID do criador do grupo
     * @param participantIds IDs dos participantes iniciais (além do criador)
     * @return Grupo criado
     * @throws BadRequestException se nome vazio ou sem participantes
     * @throws NotFoundException   se algum usuário não existe
     */
    @Transactional
    public ConversationModel createGroupConversation(
            String name,
            String description,
            UUID creatorId,
            List<UUID> participantIds) {

        log.infof("Creating group conversation: name=%s, creator=%s, participants=%d",
                name, creatorId, participantIds != null ? participantIds.size() : 0);

        // Validar nome
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Nome do grupo é obrigatório");
        }

        // Verificar se criador existe
        UserModel creator = UserModel.findByIdActive(creatorId);
        if (creator == null) {
            throw new NotFoundException("Criador não encontrado");
        }

        // Criar grupo
        ConversationModel conversation = new ConversationModel();
        conversation.conversationType = ConversationType.GROUP;
        conversation.name = name.trim();
        conversation.description = description;
        conversationRepository.persist(conversation);

        // Adicionar criador como admin
        createParticipant(conversation, creator, true, true);

        // Adicionar outros participantes (se houver)
        if (participantIds != null && !participantIds.isEmpty()) {
            for (UUID participantId : participantIds) {
                if (!participantId.equals(creatorId)) {
                    UserModel participant = UserModel.findByIdActive(participantId);
                    if (participant != null) {
                        createParticipant(conversation, participant, false, false);
                    } else {
                        log.warnf("Participant not found, skipping: %s", participantId);
                    }
                }
            }
        }

        log.infof("Group conversation created: id=%s, name=%s", conversation.id, conversation.name);
        return conversation;
    }

    /**
     * Adiciona um participante a uma conversa.
     *
     * @param conversationId ID da conversa
     * @param userId         ID do usuário a adicionar
     * @param requesterId    ID do usuário solicitante
     * @throws NotFoundException   se conversa ou usuário não existe
     * @throws ForbiddenException  se solicitante não tem permissão
     * @throws BadRequestException se já é participante
     */
    @Transactional
    public void addParticipant(UUID conversationId, UUID userId, UUID requesterId) {
        log.infof("Adding participant: conversation=%s, user=%s, requester=%s",
                conversationId, userId, requesterId);

        // Buscar conversa
        ConversationModel conversation = conversationRepository.findByIdActive(conversationId);
        if (conversation == null) {
            throw new NotFoundException("Conversa não encontrada");
        }

        // Validar tipo de conversa (não permitir adicionar em DIRECT)
        if (conversation.conversationType == ConversationType.DIRECT) {
            throw new BadRequestException("Não é possível adicionar participantes em conversas diretas");
        }

        // Validar solicitante é admin ou creator
        if (!participantRepository.isUserAdmin(requesterId, conversationId) &&
                !participantRepository.isUserCreator(requesterId, conversationId)) {
            throw new ForbiddenException("Apenas administradores podem adicionar participantes");
        }

        // Verificar se usuário existe
        UserModel user = UserModel.findByIdActive(userId);
        if (user == null) {
            throw new NotFoundException("Usuário não encontrado");
        }

        // Verificar se já é participante
        if (participantRepository.isUserParticipant(userId, conversationId)) {
            throw new BadRequestException("Usuário já é participante desta conversa");
        }

        // Adicionar participante
        createParticipant(conversation, user, false, false);

        log.infof("Participant added successfully");
    }

    /**
     * Remove um participante de uma conversa.
     *
     * @param conversationId ID da conversa
     * @param userId         ID do usuário a remover
     * @param requesterId    ID do usuário solicitante
     * @throws NotFoundException   se conversa não existe
     * @throws ForbiddenException  se solicitante não tem permissão
     * @throws BadRequestException se é conversa direta
     */
    @Transactional
    public void removeParticipant(UUID conversationId, UUID userId, UUID requesterId) {
        log.infof("Removing participant: conversation=%s, user=%s, requester=%s",
                conversationId, userId, requesterId);

        // Buscar conversa
        ConversationModel conversation = conversationRepository.findByIdActive(conversationId);
        if (conversation == null) {
            throw new NotFoundException("Conversa não encontrada");
        }

        // Validar tipo de conversa
        if (conversation.conversationType == ConversationType.DIRECT) {
            throw new BadRequestException("Não é possível remover participantes de conversas diretas");
        }

        // Validar permissão: admin/creator pode remover qualquer um, usuário pode sair
        boolean isSelfLeaving = userId.equals(requesterId);
        boolean isAdminOrCreator = participantRepository.isUserAdmin(requesterId, conversationId) ||
                participantRepository.isUserCreator(requesterId, conversationId);

        if (!isSelfLeaving && !isAdminOrCreator) {
            throw new ForbiddenException("Você não tem permissão para remover este participante");
        }

        // Remover participante (marca como saído)
        participantRepository.markAsLeft(userId, conversationId);

        log.infof("Participant removed successfully");
    }

    /**
     * Arquiva ou desarquiva uma conversa para um usuário (toggle).
     *
     * @param conversationId ID da conversa
     * @param userId         ID do usuário
     * @throws NotFoundException  se conversa não existe
     * @throws ForbiddenException se usuário não é participante
     * @return true se ficou arquivada, false se foi desarquivada
     */
    @Transactional
    public boolean archiveConversation(UUID conversationId, UUID userId) {
        log.infof("Toggling archive: conversation=%s, user=%s", conversationId, userId);

        if (conversationRepository.findByIdActive(conversationId) == null) {
            throw new NotFoundException("Conversa não encontrada");
        }

        ConversationParticipantModel participant = participantRepository.findByUserAndConversation(userId,
                conversationId);

        if (participant == null || !participant.isActive()) {
            throw new ForbiddenException("Você não é participante desta conversa");
        }

        participant.isArchived = !participant.isArchived;
        participantRepository.persist(participant);

        log.infof("Conversation %s archive toggled to: %b", conversationId, participant.isArchived);
        return participant.isArchived;
    }

    /**
     * Fixa ou desfixa uma conversa para um usuário (toggle).
     *
     * @param conversationId ID da conversa
     * @param userId         ID do usuário
     * @throws NotFoundException  se conversa não existe
     * @throws ForbiddenException se usuário não é participante
     * @return true se ficou fixada, false se foi desfixada
     */
    @Transactional
    public boolean pinConversation(UUID conversationId, UUID userId) {
        log.infof("Toggling pin: conversation=%s, user=%s", conversationId, userId);

        if (conversationRepository.findByIdActive(conversationId) == null) {
            throw new NotFoundException("Conversa não encontrada");
        }

        ConversationParticipantModel participant = participantRepository.findByUserAndConversation(userId,
                conversationId);

        if (participant == null || !participant.isActive()) {
            throw new ForbiddenException("Você não é participante desta conversa");
        }

        participant.isPinned = !participant.isPinned;
        participantRepository.persist(participant);

        log.infof("Conversation %s pin toggled to: %b", conversationId, participant.isPinned);
        return participant.isPinned;
    }

    /**
     * Silencia ou dessilencia uma conversa para um usuário (toggle).
     *
     * @param conversationId ID da conversa
     * @param userId         ID do usuário
     * @throws NotFoundException  se conversa não existe
     * @throws ForbiddenException se usuário não é participante
     * @return participante atualizado (isMuted, mutedAt)
     */
    @Transactional
    public ConversationParticipantModel muteConversation(UUID conversationId, UUID userId) {
        log.infof("Toggling mute: conversation=%s, user=%s", conversationId, userId);

        if (conversationRepository.findByIdActive(conversationId) == null) {
            throw new NotFoundException("Conversa não encontrada");
        }

        ConversationParticipantModel participant = participantRepository.findByUserAndConversation(userId,
                conversationId);

        if (participant == null || !participant.isActive()) {
            throw new ForbiddenException("Você não é participante desta conversa");
        }

        participant.isMuted = !participant.isMuted;
        participant.mutedAt = participant.isMuted ? LocalDateTime.now() : null;
        participantRepository.persist(participant);

        log.infof("Conversation %s mute toggled to: %b", conversationId, participant.isMuted);
        return participant;
    }

    /**
     * Limpa o histórico de uma conversa para um participante (clear por
     * participante).
     * Define um marco {@code clearedAt}: mensagens com sentAt &lt;= clearedAt ficam
     * ocultas apenas para este usuário. Outros participantes não são afetados.
     *
     * @param conversationId ID da conversa
     * @param userId         ID do usuário
     * @throws NotFoundException  se conversa não existe
     * @throws ForbiddenException se usuário não é participante
     * @return participante atualizado (clearedAt)
     */
    @Transactional
    public ConversationParticipantModel clearConversation(UUID conversationId, UUID userId) {
        log.infof("Clearing conversation: conversation=%s, user=%s", conversationId, userId);

        if (conversationRepository.findByIdActive(conversationId) == null) {
            throw new NotFoundException("Conversa não encontrada");
        }

        ConversationParticipantModel participant = participantRepository.findByUserAndConversation(userId,
                conversationId);

        if (participant == null || !participant.isActive()) {
            throw new ForbiddenException("Você não é participante desta conversa");
        }

        participant.clearedAt = LocalDateTime.now();
        participantRepository.persist(participant);

        log.infof("Conversation %s cleared at: %s for user %s", conversationId, participant.clearedAt, userId);
        return participant;
    }

    /**
     * Busca conversas de um usuário (inbox).
     *
     * @param userId          ID do usuário
     * @param includeArchived true para incluir conversas arquivadas
     * @return Lista de conversas
     */
    public List<ConversationModel> getUserConversations(UUID userId, boolean includeArchived) {
        log.infof("Getting user conversations: user=%s, includeArchived=%b", userId, includeArchived);

        if (includeArchived) {
            return conversationRepository.findByUserId(userId);
        } else {
            return conversationRepository.findActiveByUserId(userId);
        }
    }

    /**
     * Retorna summaries enriquecidos para inbox com campos usados no frontend.
     *
     * @Transactional garante que todas as operações Hibernate (incluindo o JOIN
     *                FETCH
     *                em findOtherParticipantWithUser) compartilhem a mesma sessão,
     *                evitando
     *                LazyInitializationException e garantindo que displayName seja
     *                populado.
     */
    @Transactional
    public List<ConversationSummaryDTO> getUserConversationSummaries(UUID userId, boolean includeArchived) {
        List<ConversationModel> conversations = getUserConversations(userId, includeArchived);

        return conversations.stream()
                .map(conversation -> {
                    ConversationParticipantModel participant = participantRepository.findByUserAndConversation(userId,
                            conversation.id);
                    UserMessageModel lastMessage = messageRepository.findLastByConversation(conversation.id);
                    long unreadCount = messageRepository.countUnreadByUserAfterClearedAt(
                            userId, conversation.id,
                            participant != null ? participant.clearedAt : null);

                    String preview = lastMessage != null ? lastMessage.txtContent : null;
                    if (preview != null && preview.length() > 100) {
                        preview = preview.substring(0, 100);
                    }

                    boolean isPinned = participant != null && participant.isPinned;
                    boolean isArchived = participant != null && participant.isArchived;

                    String displayName = null;
                    if (conversation.conversationType == ConversationType.DIRECT) {
                        ConversationParticipantModel other = participantRepository
                                .findOtherParticipantWithUser(userId, conversation.id);
                        if (other != null && other.user != null) {
                            displayName = other.user.getFullName();
                            log.debugf("displayName resolved for DIRECT conversation %s: '%s'",
                                    conversation.id, displayName);
                        } else {
                            log.warnf("Could not resolve displayName for DIRECT conversation %s: "
                                    + "otherParticipant=%s, user=%s",
                                    conversation.id,
                                    other != null ? other.id : "null",
                                    other != null ? other.user : "null");
                        }
                    }

                    return new ConversationSummaryDTO(
                            conversation,
                            unreadCount,
                            preview,
                            isPinned,
                            isArchived,
                            displayName);
                })
                .toList();
    }

    /**
     * Busca detalhes de uma conversa com participantes.
     *
     * @param conversationId ID da conversa
     * @param userId         ID do usuário solicitante
     * @return Conversa com participantes
     * @throws NotFoundException  se conversa não existe
     * @throws ForbiddenException se usuário não é participante
     */
    public ConversationModel getConversationDetails(UUID conversationId, UUID userId) {
        log.infof("Getting conversation details: conversation=%s, user=%s", conversationId, userId);

        // Buscar conversa
        ConversationModel conversation = conversationRepository.findByIdWithParticipants(conversationId);
        if (conversation == null) {
            throw new NotFoundException("Conversa não encontrada");
        }

        // Validar usuário é participante
        if (!participantRepository.isUserParticipant(userId, conversationId)) {
            throw new ForbiddenException("Você não é participante desta conversa");
        }

        return conversation;
    }

    /**
     * Conta total de mensagens não lidas de um usuário em todas as conversas.
     *
     * @param userId ID do usuário
     * @return Número total de mensagens não lidas
     */
    public long getTotalUnreadCount(UUID userId) {
        log.infof("Getting total unread count: user=%s", userId);

        // Buscar todas as conversas do usuário
        List<ConversationModel> conversations = conversationRepository.findByUserId(userId);

        long totalUnread = 0;
        for (ConversationModel conversation : conversations) {
            totalUnread += messageRepository.countUnreadByUser(userId, conversation.id);
        }

        return totalUnread;
    }

    // ========== Métodos Privados ==========

    /**
     * Cria um registro de participante.
     *
     * @param conversation Conversa
     * @param user         Usuário
     * @param isAdmin      Se é administrador
     * @param isCreator    Se é criador
     */
    private void createParticipant(
            ConversationModel conversation,
            UserModel user,
            boolean isAdmin,
            boolean isCreator) {

        ConversationParticipantModel participant = new ConversationParticipantModel();
        participant.conversation = conversation;
        participant.user = user;
        participant.isAdmin = isAdmin;
        participant.isCreator = isCreator;
        participant.joinedAt = LocalDateTime.now();
        participantRepository.persist(participant);
        // Keep in-memory collection in sync so the response DTO reflects the
        // newly added participants without requiring a DB re-fetch.
        conversation.participants.add(participant);
    }
}
