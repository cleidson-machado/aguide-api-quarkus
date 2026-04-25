package br.com.aguideptbr.features.usermessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

/**
 * Unit tests for ConversationService.
 *
 * Tests mute/clear business rules and block check on conversation creation.
 * Uses plain Mockito (no @QuarkusTest) for fast isolated execution.
 */
class ConversationServiceTest {

    private ConversationRepository conversationRepository;
    private ConversationParticipantRepository participantRepository;
    private UserMessageRepository messageRepository;
    private UserBlockRepository blockRepository;
    private ConversationService service;

    @BeforeEach
    void setUp() {
        conversationRepository = Mockito.mock(ConversationRepository.class);
        participantRepository = Mockito.mock(ConversationParticipantRepository.class);
        messageRepository = Mockito.mock(UserMessageRepository.class);
        blockRepository = Mockito.mock(UserBlockRepository.class);
        Logger log = Mockito.mock(Logger.class);

        service = new ConversationService(
                conversationRepository,
                participantRepository,
                messageRepository,
                blockRepository,
                log);
    }

    // ---------------------------------------------------------------------------
    // muteConversation
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("muteConversation()")
    class MuteConversation {

        private UUID conversationId;
        private UUID userId;
        private ConversationModel conversation;
        private ConversationParticipantModel participant;

        @BeforeEach
        void setUp() {
            conversationId = UUID.randomUUID();
            userId = UUID.randomUUID();

            conversation = new ConversationModel();
            conversation.id = conversationId;
            conversation.deletedAt = null;

            participant = new ConversationParticipantModel();
            participant.isMuted = false;
            participant.mutedAt = null;
            participant.leftAt = null;
        }

        @Test
        @DisplayName("should mute when conversation is not muted")
        void shouldMuteWhenNotMuted() {
            when(conversationRepository.findByIdActive(conversationId)).thenReturn(conversation);
            when(participantRepository.findByUserAndConversation(userId, conversationId)).thenReturn(participant);

            ConversationParticipantModel result = service.muteConversation(conversationId, userId);

            assertTrue(result.isMuted);
            assertNotNull(result.mutedAt);
        }

        @Test
        @DisplayName("should unmute when conversation is already muted (toggle off)")
        void shouldUnmuteWhenAlreadyMuted() {
            participant.isMuted = true;
            participant.mutedAt = java.time.LocalDateTime.now().minusDays(1);

            when(conversationRepository.findByIdActive(conversationId)).thenReturn(conversation);
            when(participantRepository.findByUserAndConversation(userId, conversationId)).thenReturn(participant);

            ConversationParticipantModel result = service.muteConversation(conversationId, userId);

            assertFalse(result.isMuted);
            assertNull(result.mutedAt);
        }

        @Test
        @DisplayName("should throw NotFoundException when conversation does not exist")
        void shouldThrowNotFoundWhenConversationMissing() {
            when(conversationRepository.findByIdActive(conversationId)).thenReturn(null);

            assertThrows(NotFoundException.class, () -> service.muteConversation(conversationId, userId));
        }

        @Test
        @DisplayName("should throw ForbiddenException when user is not a participant")
        void shouldThrowForbiddenWhenNotParticipant() {
            when(conversationRepository.findByIdActive(conversationId)).thenReturn(conversation);
            when(participantRepository.findByUserAndConversation(userId, conversationId)).thenReturn(null);

            assertThrows(ForbiddenException.class, () -> service.muteConversation(conversationId, userId));
        }

        @Test
        @DisplayName("should throw ForbiddenException when participant has left")
        void shouldThrowForbiddenWhenParticipantLeft() {
            participant.leftAt = java.time.LocalDateTime.now().minusDays(1);

            when(conversationRepository.findByIdActive(conversationId)).thenReturn(conversation);
            when(participantRepository.findByUserAndConversation(userId, conversationId)).thenReturn(participant);

            assertThrows(ForbiddenException.class, () -> service.muteConversation(conversationId, userId));
        }
    }

    // ---------------------------------------------------------------------------
    // clearConversation
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("clearConversation()")
    class ClearConversation {

        private UUID conversationId;
        private UUID userId;
        private ConversationModel conversation;
        private ConversationParticipantModel participant;

        @BeforeEach
        void setUp() {
            conversationId = UUID.randomUUID();
            userId = UUID.randomUUID();

            conversation = new ConversationModel();
            conversation.id = conversationId;
            conversation.deletedAt = null;

            participant = new ConversationParticipantModel();
            participant.clearedAt = null;
            participant.leftAt = null;
        }

        @Test
        @DisplayName("should set clearedAt to current time")
        void shouldSetClearedAt() {
            when(conversationRepository.findByIdActive(conversationId)).thenReturn(conversation);
            when(participantRepository.findByUserAndConversation(userId, conversationId)).thenReturn(participant);

            ConversationParticipantModel result = service.clearConversation(conversationId, userId);

            assertNotNull(result.clearedAt);
            assertTrue(result.clearedAt.isBefore(java.time.LocalDateTime.now().plusSeconds(1)));
        }

        @Test
        @DisplayName("should update clearedAt when clearing again")
        void shouldUpdateClearedAtOnSecondClear() throws InterruptedException {
            java.time.LocalDateTime firstClear = java.time.LocalDateTime.now().minusHours(2);
            participant.clearedAt = firstClear;

            when(conversationRepository.findByIdActive(conversationId)).thenReturn(conversation);
            when(participantRepository.findByUserAndConversation(userId, conversationId)).thenReturn(participant);

            ConversationParticipantModel result = service.clearConversation(conversationId, userId);

            assertNotNull(result.clearedAt);
            assertTrue(result.clearedAt.isAfter(firstClear));
        }

        @Test
        @DisplayName("should throw NotFoundException when conversation does not exist")
        void shouldThrowNotFoundWhenConversationMissing() {
            when(conversationRepository.findByIdActive(conversationId)).thenReturn(null);

            assertThrows(NotFoundException.class, () -> service.clearConversation(conversationId, userId));
        }

        @Test
        @DisplayName("should throw ForbiddenException when user is not a participant")
        void shouldThrowForbiddenWhenNotParticipant() {
            when(conversationRepository.findByIdActive(conversationId)).thenReturn(conversation);
            when(participantRepository.findByUserAndConversation(userId, conversationId)).thenReturn(null);

            assertThrows(ForbiddenException.class, () -> service.clearConversation(conversationId, userId));
        }
    }

    // ---------------------------------------------------------------------------
    // createDirectConversation - block check
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("createDirectConversation() - block check")
    class CreateDirectConversationBlockCheck {

        @Test
        @DisplayName("should throw 409 WebApplicationException when users are blocked in any direction")
        void shouldThrow409WhenBlockedInAnyDirection() {
            UUID user1Id = UUID.randomUUID();
            UUID user2Id = UUID.randomUUID();

            when(blockRepository.isBlockedInAnyDirection(user1Id, user2Id)).thenReturn(true);

            WebApplicationException exception = assertThrows(
                    WebApplicationException.class,
                    () -> service.createDirectConversation(user1Id, user2Id));

            assertEquals(409, exception.getResponse().getStatus());
        }
    }
}
