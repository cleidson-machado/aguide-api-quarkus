package br.com.aguideptbr.features.usermessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import br.com.aguideptbr.features.user.UserModel;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

/**
 * Unit tests for MessageService.
 *
 * Tests block enforcement on sendMessage() for DIRECT conversations and
 * basic error-path coverage for missing conversation / non-participant.
 * Uses plain Mockito (no @QuarkusTest) for fast isolated execution.
 */
class MessageServiceTest {

    private UserMessageRepository messageRepository;
    private ConversationRepository conversationRepository;
    private ConversationParticipantRepository participantRepository;
    private UserBlockRepository blockRepository;
    private MessageService service;

    @BeforeEach
    void setUp() {
        messageRepository = Mockito.mock(UserMessageRepository.class);
        conversationRepository = Mockito.mock(ConversationRepository.class);
        participantRepository = Mockito.mock(ConversationParticipantRepository.class);
        blockRepository = Mockito.mock(UserBlockRepository.class);
        Logger log = Mockito.mock(Logger.class);

        service = new MessageService(
                messageRepository,
                conversationRepository,
                participantRepository,
                blockRepository,
                log);
    }

    // ---------------------------------------------------------------------------
    // sendMessage - block check for DIRECT conversations
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("sendMessage() - block checks")
    class SendMessageBlockCheck {

        private UUID senderId;
        private UUID conversationId;
        private ConversationModel conversation;

        @BeforeEach
        void setUp() {
            senderId = UUID.randomUUID();
            conversationId = UUID.randomUUID();

            conversation = new ConversationModel();
            conversation.id = conversationId;
            conversation.conversationType = ConversationType.DIRECT;
            conversation.deletedAt = null;
        }

        @Test
        @DisplayName("should throw 409 when sender is blocked by other DIRECT participant")
        void shouldThrow409WhenBlockedInDirectConversation() {
            UUID otherUserId = UUID.randomUUID();

            UserModel otherUser = new UserModel();
            otherUser.id = otherUserId;

            ConversationParticipantModel otherParticipant = new ConversationParticipantModel();
            otherParticipant.user = otherUser;

            when(conversationRepository.findByIdActive(conversationId)).thenReturn(conversation);
            when(participantRepository.isUserParticipant(senderId, conversationId)).thenReturn(true);
            when(participantRepository.findByConversation(conversationId))
                    .thenReturn(List.of(otherParticipant));
            when(blockRepository.isBlockedInAnyDirection(senderId, otherUserId)).thenReturn(true);

            WebApplicationException exception = assertThrows(
                    WebApplicationException.class,
                    () -> service.sendMessage(senderId, conversationId, "hello", MessageType.TEXT, null));

            assertEquals(409, exception.getResponse().getStatus());
        }

        @Test
        @DisplayName("should not check block when message is in GROUP conversation")
        void shouldNotCheckBlockForGroupConversation() {
            // For GROUP conversation the block check block is skipped.
            // An empty text message will trigger BadRequestException (content validation),
            // which proves the block check was never reached/applied.
            conversation.conversationType = ConversationType.GROUP;

            when(conversationRepository.findByIdActive(conversationId)).thenReturn(conversation);
            when(participantRepository.isUserParticipant(senderId, conversationId)).thenReturn(true);

            // Empty content → BadRequestException (content validation, not block)
            assertThrows(jakarta.ws.rs.BadRequestException.class,
                    () -> service.sendMessage(senderId, conversationId, "", MessageType.TEXT, null));

            verify(blockRepository, never()).isBlockedInAnyDirection(any(), any());
        }
    }

    // ---------------------------------------------------------------------------
    // sendMessage - basic error paths (no block involved)
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("sendMessage() - basic error paths")
    class SendMessageBasicErrors {

        @Test
        @DisplayName("should throw NotFoundException when conversation does not exist")
        void shouldThrowNotFoundWhenConversationMissing() {
            UUID senderId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();

            when(conversationRepository.findByIdActive(conversationId)).thenReturn(null);

            assertThrows(NotFoundException.class,
                    () -> service.sendMessage(senderId, conversationId, "hello", MessageType.TEXT, null));
        }

        @Test
        @DisplayName("should throw ForbiddenException when user is not a participant")
        void shouldThrowForbiddenWhenNotParticipant() {
            UUID senderId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();

            ConversationModel conversation = new ConversationModel();
            conversation.id = conversationId;
            conversation.conversationType = ConversationType.GROUP;
            conversation.deletedAt = null;

            when(conversationRepository.findByIdActive(conversationId)).thenReturn(conversation);
            when(participantRepository.isUserParticipant(senderId, conversationId)).thenReturn(false);

            assertThrows(ForbiddenException.class,
                    () -> service.sendMessage(senderId, conversationId, "hello", MessageType.TEXT, null));
        }

        @Test
        @DisplayName("should throw BadRequestException when text message content is empty")
        void shouldThrowBadRequestWhenTextContentIsEmpty() {
            UUID senderId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();

            ConversationModel conversation = new ConversationModel();
            conversation.id = conversationId;
            conversation.conversationType = ConversationType.GROUP;
            conversation.deletedAt = null;

            when(conversationRepository.findByIdActive(conversationId)).thenReturn(conversation);
            when(participantRepository.isUserParticipant(senderId, conversationId)).thenReturn(true);

            assertThrows(jakarta.ws.rs.BadRequestException.class,
                    () -> service.sendMessage(senderId, conversationId, "  ", MessageType.TEXT, null));
        }
    }

    // ---------------------------------------------------------------------------
    // markAsRead - regression: should not be broken by mute/clear feature
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("markAsRead() - regression")
    class MarkAsReadRegression {

        @Test
        @DisplayName("should throw NotFoundException when message does not exist")
        void shouldThrowNotFoundWhenMessageMissing() {
            UUID messageId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            when(messageRepository.findByIdActive(messageId)).thenReturn(null);

            assertThrows(NotFoundException.class, () -> service.markAsRead(messageId, userId));
        }
    }
}
