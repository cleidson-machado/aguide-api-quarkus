package br.com.aguideptbr.features.usermessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.aguideptbr.features.user.UserModel;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

/**
 * Testes unitários com Mockito para
 * ConversationService.createDirectConversation()
 *
 * Este arquivo contém testes unitários que SIMULAM (mock) as dependências
 * do ConversationService para permitir instrumentação do JaCoCo e atingir
 * 100% de cobertura de branches e linhas.
 *
 * Diferença entre este arquivo e ConversationServiceIntegrationTest:
 * - ConversationServiceUnitTest (este arquivo): Usa Mockito, sem @QuarkusTest,
 * foca em cobertura JaCoCo, simula dependências
 * - ConversationServiceIntegrationTest: Usa @QuarkusTest, banco real,
 * validação completa do fluxo end-to-end
 *
 * Ambos são complementares e necessários para garantir qualidade.
 *
 * BRANCH COVERAGE MATRIZ:
 * ============================================================================
 * Branch 1: user1Id.equals(user2Id) → BadRequestException
 * Branch 2: blockRepository.isBlockedInAnyDirection() → WebApplicationException
 * 409
 * Branch 3: user1 == null → NotFoundException
 * Branch 4: user2 == null → NotFoundException
 * Branch 5: Both users exist (proceed with creation/existence check)
 * Branch 6: existingConversation != null → return existing
 * Branch 7: existingConversation == null → create new
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)
class ConversationServiceUnitTest {

    @Mock
    private Logger log;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationParticipantRepository participantRepository;

    @Mock
    private UserBlockRepository blockRepository;

    @InjectMocks
    private ConversationService conversationService;

    private UUID user1Id;
    private UUID user2Id;
    private UserModel user1;
    private UserModel user2;

    @BeforeEach
    void setUp() {
        user1Id = UUID.randomUUID();
        user2Id = UUID.randomUUID();

        user1 = new UserModel();
        user1.id = user1Id;
        user1.email = "user1@test.com";
        user1.name = "User";
        user1.surname = "One";

        user2 = new UserModel();
        user2.id = user2Id;
        user2.email = "user2@test.com";
        user2.name = "User";
        user2.surname = "Two";
    }

    /**
     * BRANCH 1: user1Id.equals(user2Id) → BadRequestException
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("createDirectConversation_sameUser_throwsBadRequest")
    void createDirectConversation_sameUser_throwsBadRequest() {
        // Arrange
        UUID sameUserId = user1Id;

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> conversationService.createDirectConversation(sameUserId, sameUserId));

        assertEquals("Não é possível criar uma conversa consigo mesmo", exception.getMessage());

        // Verify no further interactions (early return after validation)
        verify(blockRepository, never()).isBlockedInAnyDirection(any(), any());
        verify(conversationRepository, never()).findDirectConversation(any(), any());
    }

    /**
     * BRANCH 2: blockRepository.isBlockedInAnyDirection() → WebApplicationException
     * 409
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("createDirectConversation_usersBlocked_throwsConflict409")
    void createDirectConversation_usersBlocked_throwsConflict409() {
        // Arrange
        when(blockRepository.isBlockedInAnyDirection(user1Id, user2Id)).thenReturn(true);

        // Act & Assert
        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> conversationService.createDirectConversation(user1Id, user2Id));

        assertEquals(409, exception.getResponse().getStatus());

        // Verify block check was called
        verify(blockRepository).isBlockedInAnyDirection(user1Id, user2Id);

        // Verify no database queries after block check (early return)
        verifyNoInteractions(conversationRepository);
        verifyNoInteractions(participantRepository);
    }

    /**
     * BRANCH 3: user1 == null → NotFoundException
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("createDirectConversation_user1NotFound_throwsNotFound")
    void createDirectConversation_user1NotFound_throwsNotFound() {
        // Arrange
        when(blockRepository.isBlockedInAnyDirection(user1Id, user2Id)).thenReturn(false);

        try (MockedStatic<UserModel> userModelMock = mockStatic(UserModel.class)) {
            userModelMock.when(() -> UserModel.findByIdActive(user1Id)).thenReturn(null);
            userModelMock.when(() -> UserModel.findByIdActive(user2Id)).thenReturn(user2);

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> conversationService.createDirectConversation(user1Id, user2Id));

            assertEquals("Um ou mais usuários não encontrados", exception.getMessage());

            // Verify both user lookups were attempted
            userModelMock.verify(() -> UserModel.findByIdActive(user1Id));
            userModelMock.verify(() -> UserModel.findByIdActive(user2Id));

            // Verify no conversation lookup (early return)
            verify(conversationRepository, never()).findDirectConversation(any(), any());
        }
    }

    /**
     * BRANCH 4: user2 == null → NotFoundException
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("createDirectConversation_user2NotFound_throwsNotFound")
    void createDirectConversation_user2NotFound_throwsNotFound() {
        // Arrange
        when(blockRepository.isBlockedInAnyDirection(user1Id, user2Id)).thenReturn(false);

        try (MockedStatic<UserModel> userModelMock = mockStatic(UserModel.class)) {
            userModelMock.when(() -> UserModel.findByIdActive(user1Id)).thenReturn(user1);
            userModelMock.when(() -> UserModel.findByIdActive(user2Id)).thenReturn(null);

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> conversationService.createDirectConversation(user1Id, user2Id));

            assertEquals("Um ou mais usuários não encontrados", exception.getMessage());

            // Verify both user lookups were attempted
            userModelMock.verify(() -> UserModel.findByIdActive(user1Id));
            userModelMock.verify(() -> UserModel.findByIdActive(user2Id));

            // Verify no conversation lookup (early return)
            verify(conversationRepository, never()).findDirectConversation(any(), any());
        }
    }

    /**
     * BRANCH 5 & 6: Both users exist, existingConversation != null → return
     * existing
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("createDirectConversation_conversationExists_returnsExisting")
    void createDirectConversation_conversationExists_returnsExisting() {
        // Arrange
        when(blockRepository.isBlockedInAnyDirection(user1Id, user2Id)).thenReturn(false);

        ConversationModel existingConversation = new ConversationModel();
        existingConversation.id = UUID.randomUUID();
        existingConversation.conversationType = ConversationType.DIRECT;

        ConversationModel reloadedConversation = new ConversationModel();
        reloadedConversation.id = existingConversation.id;
        reloadedConversation.conversationType = ConversationType.DIRECT;

        try (MockedStatic<UserModel> userModelMock = mockStatic(UserModel.class)) {
            userModelMock.when(() -> UserModel.findByIdActive(user1Id)).thenReturn(user1);
            userModelMock.when(() -> UserModel.findByIdActive(user2Id)).thenReturn(user2);

            when(conversationRepository.findDirectConversation(user1Id, user2Id))
                    .thenReturn(existingConversation);
            when(conversationRepository.findByIdWithParticipants(existingConversation.id))
                    .thenReturn(reloadedConversation);

            // Act
            ConversationModel result = conversationService.createDirectConversation(user1Id, user2Id);

            // Assert
            assertNotNull(result);
            assertEquals(existingConversation.id, result.id);
            assertEquals(ConversationType.DIRECT, result.conversationType);

            // Verify existing conversation was returned (not created)
            verify(conversationRepository).findDirectConversation(user1Id, user2Id);
            verify(conversationRepository).findByIdWithParticipants(existingConversation.id);
            verify(conversationRepository, never()).persist(any(ConversationModel.class));
            verify(participantRepository, never()).persist(any(ConversationParticipantModel.class));
        }
    }

    /**
     * BRANCH 5 & 7: Both users exist, existingConversation == null → create new
     */
    @Test
    @DisplayName("createDirectConversation_newConversation_createsAndReturnsNew")
    void createDirectConversation_newConversation_createsAndReturnsNew() {
        // Arrange
        when(blockRepository.isBlockedInAnyDirection(user1Id, user2Id)).thenReturn(false);

        try (MockedStatic<UserModel> userModelMock = mockStatic(UserModel.class)) {
            userModelMock.when(() -> UserModel.findByIdActive(user1Id)).thenReturn(user1);
            userModelMock.when(() -> UserModel.findByIdActive(user2Id)).thenReturn(user2);

            when(conversationRepository.findDirectConversation(user1Id, user2Id)).thenReturn(null);

            // Captura a conversa criada para verificar atributos
            doAnswer(invocation -> {
                ConversationModel conversation = invocation.getArgument(0);
                conversation.id = UUID.randomUUID(); // Simula ID gerado pelo DB
                return null;
            }).when(conversationRepository).persist(any(ConversationModel.class));

            // Act
            ConversationModel result = conversationService.createDirectConversation(user1Id, user2Id);

            // Assert
            assertNotNull(result);
            assertNotNull(result.id);
            assertEquals(ConversationType.DIRECT, result.conversationType);
            assertNull(result.name, "Direct conversations should not have a name");

            // Verify new conversation was created
            verify(conversationRepository).findDirectConversation(user1Id, user2Id);
            verify(conversationRepository).persist(any(ConversationModel.class));
            verify(participantRepository, times(2)).persist(any(ConversationParticipantModel.class));

            // Verify both users were looked up
            userModelMock.verify(() -> UserModel.findByIdActive(user1Id));
            userModelMock.verify(() -> UserModel.findByIdActive(user2Id));
        }
    }

    /**
     * BRANCH 3 & 4: Both users not found → NotFoundException
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("createDirectConversation_bothUsersNotFound_throwsNotFound")
    void createDirectConversation_bothUsersNotFound_throwsNotFound() {
        // Arrange
        when(blockRepository.isBlockedInAnyDirection(user1Id, user2Id)).thenReturn(false);

        try (MockedStatic<UserModel> userModelMock = mockStatic(UserModel.class)) {
            userModelMock.when(() -> UserModel.findByIdActive(user1Id)).thenReturn(null);
            userModelMock.when(() -> UserModel.findByIdActive(user2Id)).thenReturn(null);

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> conversationService.createDirectConversation(user1Id, user2Id));

            assertEquals("Um ou mais usuários não encontrados", exception.getMessage());

            // Verify both lookups were attempted
            userModelMock.verify(() -> UserModel.findByIdActive(user1Id));
            userModelMock.verify(() -> UserModel.findByIdActive(user2Id));

            // Verify no conversation creation
            verify(conversationRepository, never()).persist(any(ConversationModel.class));
        }
    }
}
