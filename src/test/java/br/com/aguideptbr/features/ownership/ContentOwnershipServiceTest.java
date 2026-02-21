package br.com.aguideptbr.features.ownership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.aguideptbr.features.content.ContentRecordModel;
import br.com.aguideptbr.features.content.ContentType;
import br.com.aguideptbr.features.ownership.dto.OwnershipStatusResponse;
import br.com.aguideptbr.features.ownership.dto.UserContentResponse;
import br.com.aguideptbr.features.ownership.dto.ValidateOwnershipRequest;
import br.com.aguideptbr.features.ownership.dto.ValidateOwnershipResponse;
import br.com.aguideptbr.features.user.UserModel;
import br.com.aguideptbr.features.user.UserRole;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Integration tests for ContentOwnershipService.
 *
 * Tests the complete ownership validation flow:
 * - HMAC-SHA256 hash calculation
 * - Channel ID matching validation
 * - Ownership record creation
 * - Content hash update
 */
@QuarkusTest
class ContentOwnershipServiceTest {

        @Inject
        ContentOwnershipService ownershipService;

        private UserModel testUser;
        private ContentRecordModel testContent;
        private static final String TEST_CHANNEL_ID = "UCTestChannel123456789";

        @BeforeEach
        @Transactional
        void setup() {
                // Clean up previous test data
                ContentOwnershipModel.deleteAll();

                // Create test user with YouTube channel
                testUser = new UserModel();
                testUser.name = "Test";
                testUser.surname = "Owner";
                testUser.email = "test.owner." + System.currentTimeMillis() + "@test.com";
                testUser.passwordHash = "test-hash";
                testUser.role = UserRole.FREE;
                testUser.youtubeChannelId = TEST_CHANNEL_ID;
                testUser.persist();

                // Create test content with matching channel ID
                testContent = new ContentRecordModel();
                testContent.title = "Test Video " + System.currentTimeMillis();
                testContent.videoUrl = "https://test.com/video/" + System.currentTimeMillis();
                testContent.channelId = TEST_CHANNEL_ID; // Matches user's channel
                testContent.channelName = "Test Channel";
                testContent.type = ContentType.VIDEO;
                testContent.persist();
        }

        @Test
        @Transactional
        void testValidateOwnership_Success() {
                // Arrange
                ValidateOwnershipRequest request = new ValidateOwnershipRequest(
                                testUser.id,
                                testContent.id);

                // Act
                ValidateOwnershipResponse response = ownershipService.validateOwnership(request);

                // Assert
                assertNotNull(response);
                assertEquals(OwnershipStatus.VERIFIED, response.getStatus());
                assertNotNull(response.getOwnershipId());
                assertEquals(testUser.id, response.getUserId());
                assertEquals(testContent.id, response.getContentId());
                assertEquals(TEST_CHANNEL_ID, response.getYoutubeChannelId());
                assertEquals(TEST_CHANNEL_ID, response.getContentChannelId());
                assertNotNull(response.getValidationHash());
                assertNotNull(response.getVerifiedAt());

                // Verify content hash was updated
                ContentRecordModel updatedContent = ContentRecordModel.findById(testContent.id);
                assertNotNull(updatedContent.validationHash);
                assertEquals(response.getValidationHash(), updatedContent.validationHash);
        }

        @Test
        @Transactional
        void testValidateOwnership_ChannelMismatch() {
                // Arrange - Content with different channel ID
                ContentRecordModel differentContent = new ContentRecordModel();
                differentContent.title = "Different Channel Video";
                differentContent.videoUrl = "https://test.com/different/" + System.currentTimeMillis();
                differentContent.channelId = "UCDifferentChannel987654321";
                differentContent.channelName = "Different Channel";
                differentContent.type = ContentType.VIDEO;
                differentContent.persist();

                ValidateOwnershipRequest request = new ValidateOwnershipRequest(
                                testUser.id,
                                differentContent.id);

                // Act
                ValidateOwnershipResponse response = ownershipService.validateOwnership(request);

                // Assert
                assertNotNull(response);
                assertEquals(OwnershipStatus.REJECTED, response.getStatus());
                assertTrue(response.getMessage().contains("Channel IDs do not match"));

                // Verify content hash was NOT updated
                ContentRecordModel updatedContent = ContentRecordModel.findById(differentContent.id);
                assertNull(updatedContent.validationHash);
        }

        @Test
        @Transactional
        void testValidateOwnership_UserWithoutChannel() {
                // Arrange - User without YouTube channel ID
                UserModel userWithoutChannel = new UserModel();
                userWithoutChannel.name = "No";
                userWithoutChannel.surname = "Channel";
                userWithoutChannel.email = "no.channel." + System.currentTimeMillis() + "@test.com";
                userWithoutChannel.passwordHash = "test-hash";
                userWithoutChannel.role = UserRole.FREE;
                userWithoutChannel.youtubeChannelId = null; // No channel
                userWithoutChannel.persist();

                ValidateOwnershipRequest request = new ValidateOwnershipRequest(
                                userWithoutChannel.id,
                                testContent.id);

                // Act
                ValidateOwnershipResponse response = ownershipService.validateOwnership(request);

                // Assert
                assertNotNull(response);
                assertEquals(OwnershipStatus.REJECTED, response.getStatus());
                assertTrue(response.getMessage().contains("has no YouTube channel ID"));
        }

        @Test
        @Transactional
        void testGetOwnershipStatus() {
                // Arrange - Create verified ownership
                ValidateOwnershipRequest request = new ValidateOwnershipRequest(
                                testUser.id,
                                testContent.id);
                ownershipService.validateOwnership(request);

                // Act
                OwnershipStatusResponse status = ownershipService.getOwnershipStatus(
                                testUser.id,
                                testContent.id);

                // Assert
                assertNotNull(status);
                assertEquals(OwnershipStatus.VERIFIED, status.getStatus());
                assertTrue(status.isVerified());
                assertTrue(status.isChannelsMatch());
                assertNotNull(status.getVerifiedAt());
        }

        @Test
        @Transactional
        void testGetUserVerifiedContent() {
                // Arrange - Create multiple verified contents
                ValidateOwnershipRequest request1 = new ValidateOwnershipRequest(
                                testUser.id,
                                testContent.id);
                ownershipService.validateOwnership(request1);

                ContentRecordModel content2 = new ContentRecordModel();
                content2.title = "Second Video";
                content2.videoUrl = "https://test.com/video2/" + System.currentTimeMillis();
                content2.channelId = TEST_CHANNEL_ID;
                content2.channelName = "Test Channel";
                content2.type = ContentType.VIDEO;
                content2.persist();

                ValidateOwnershipRequest request2 = new ValidateOwnershipRequest(
                                testUser.id,
                                content2.id);
                ownershipService.validateOwnership(request2);

                // Act
                List<UserContentResponse> verifiedContent = ownershipService.getUserVerifiedContent(
                                testUser.id);

                // Assert
                assertNotNull(verifiedContent);
                assertEquals(2, verifiedContent.size());

                UserContentResponse first = verifiedContent.get(0);
                assertTrue(first.isVerified());
                assertNotNull(first.getValidationHash());
                assertNotNull(first.getVerifiedAt());
        }

        @Test
        @Transactional
        void testGetUserVerifiedContent_UserNotFound() {
                // Arrange - Non-existent user ID
                UUID nonExistentUserId = UUID.randomUUID();

                // Act & Assert - Should throw WebApplicationException with 404 NOT FOUND
                jakarta.ws.rs.WebApplicationException exception = assertThrows(
                                jakarta.ws.rs.WebApplicationException.class,
                                () -> ownershipService.getUserVerifiedContent(nonExistentUserId));

                assertEquals(404, exception.getResponse().getStatus());
                assertTrue(exception.getMessage().contains("User not found"));
        }

        @Test
        @Transactional
        void testGetUserVerifiedContent_DeletedUser() {
                // Arrange - Create user and then soft delete
                UserModel deletedUser = new UserModel();
                deletedUser.name = "Deleted";
                deletedUser.surname = "User";
                deletedUser.email = "deleted.user." + System.currentTimeMillis() + "@test.com";
                deletedUser.passwordHash = "test-hash";
                deletedUser.role = UserRole.FREE;
                deletedUser.youtubeChannelId = "UCDeletedChannel123";
                deletedUser.persist();

                // Soft delete the user
                UserModel user = UserModel.findById(deletedUser.id);
                user.deletedAt = LocalDateTime.now();
                // No need to call persist() - Hibernate tracks changes automatically

                // Act & Assert - Should throw WebApplicationException with 404 NOT FOUND
                jakarta.ws.rs.WebApplicationException exception = assertThrows(
                                jakarta.ws.rs.WebApplicationException.class,
                                () -> ownershipService.getUserVerifiedContent(deletedUser.id));

                assertEquals(404, exception.getResponse().getStatus());
                assertTrue(exception.getMessage().contains("User not found"));
        }

        @Test
        @Transactional
        void testGetUserVerifiedContent_NoVerifiedContent() {
                // Arrange - Create user without any verified content
                UserModel userWithoutContent = new UserModel();
                userWithoutContent.name = "No";
                userWithoutContent.surname = "Content";
                userWithoutContent.email = "no.content." + System.currentTimeMillis() + "@test.com";
                userWithoutContent.passwordHash = "test-hash";
                userWithoutContent.role = UserRole.FREE;
                userWithoutContent.youtubeChannelId = "UCNoContentChannel123";
                userWithoutContent.persist();

                // Act & Assert - Should throw WebApplicationException with 404 NOT FOUND
                jakarta.ws.rs.WebApplicationException exception = assertThrows(
                                jakarta.ws.rs.WebApplicationException.class,
                                () -> ownershipService.getUserVerifiedContent(userWithoutContent.id));

                assertEquals(404, exception.getResponse().getStatus());
                assertTrue(exception.getMessage().contains("No verified content found for this user"));
        }

        @Test
        @Transactional
        void testGetUserVerifiedContent_RejectedContentNotIncluded() {
                // Arrange - Create user with one VERIFIED and one REJECTED ownership

                // Create first content (will be VERIFIED)
                ValidateOwnershipRequest request1 = new ValidateOwnershipRequest(
                                testUser.id,
                                testContent.id);
                ownershipService.validateOwnership(request1);

                // Create second content with different channel (will be REJECTED)
                ContentRecordModel differentContent = new ContentRecordModel();
                differentContent.title = "Different Channel Video";
                differentContent.videoUrl = "https://test.com/different/" + System.currentTimeMillis();
                differentContent.channelId = "UCDifferentChannel999";
                differentContent.channelName = "Different Channel";
                differentContent.type = ContentType.VIDEO;
                differentContent.persist();

                ValidateOwnershipRequest request2 = new ValidateOwnershipRequest(
                                testUser.id,
                                differentContent.id);
                ownershipService.validateOwnership(request2); // This will be REJECTED

                // Act
                List<UserContentResponse> verifiedContent = ownershipService.getUserVerifiedContent(
                                testUser.id);

                // Assert - Should return only VERIFIED content (1 item), not REJECTED
                assertNotNull(verifiedContent);
                assertEquals(1, verifiedContent.size());
                assertEquals(testContent.id, verifiedContent.get(0).getContentId());
                assertTrue(verifiedContent.get(0).isVerified());
        }

        @Test
        @Transactional
        void testOwnershipModel_FindMethods() {
                // Arrange
                ValidateOwnershipRequest request = new ValidateOwnershipRequest(
                                testUser.id,
                                testContent.id);
                ownershipService.validateOwnership(request);

                // Act & Assert - Test static finder methods
                List<ContentOwnershipModel> userOwnerships = ContentOwnershipModel
                                .findByUserId(testUser.id);
                assertEquals(1, userOwnerships.size());

                List<ContentOwnershipModel> verifiedOwnerships = ContentOwnershipModel
                                .findVerifiedByUserId(testUser.id);
                assertEquals(1, verifiedOwnerships.size());

                ContentOwnershipModel ownership = ContentOwnershipModel
                                .findByUserAndContent(testUser.id, testContent.id);
                assertNotNull(ownership);
                assertEquals(OwnershipStatus.VERIFIED, ownership.ownershipStatus);

                assertTrue(ContentOwnershipModel.ownershipExists(testUser.id, testContent.id));
                assertTrue(ContentOwnershipModel.isVerified(testUser.id, testContent.id));
        }

        @Test
        @Transactional
        void testIdempotency_RetryIncrements() {
                // Arrange - Content with different channel (will be REJECTED)
                ContentRecordModel differentContent = new ContentRecordModel();
                differentContent.title = "Wrong Channel Video";
                differentContent.videoUrl = "https://test.com/wrong/" + System.currentTimeMillis();
                differentContent.channelId = "UCWrongChannel999";
                differentContent.channelName = "Wrong Channel";
                differentContent.type = ContentType.VIDEO;
                differentContent.persist();

                ValidateOwnershipRequest request = new ValidateOwnershipRequest(
                                testUser.id,
                                differentContent.id);

                // Act - First attempt (REJECTED)
                ValidateOwnershipResponse response1 = ownershipService.validateOwnership(request);
                assertEquals(OwnershipStatus.REJECTED, response1.getStatus());

                ContentOwnershipModel ownership1 = ContentOwnershipModel.findByUserAndContent(
                                testUser.id,
                                differentContent.id);
                assertEquals(0, ownership1.retryCount); // First attempt
                assertEquals("CHANNEL_MISMATCH", ownership1.rejectionReason);
                assertNotNull(ownership1.lastAttemptAt);

                // Act - Second attempt (still REJECTED)
                ValidateOwnershipResponse response2 = ownershipService.validateOwnership(request);
                assertEquals(OwnershipStatus.REJECTED, response2.getStatus());

                ContentOwnershipModel ownership2 = ContentOwnershipModel.findByUserAndContent(
                                testUser.id,
                                differentContent.id);
                assertEquals(1, ownership2.retryCount); // Incremented
                assertEquals("CHANNEL_MISMATCH", ownership2.rejectionReason);

                // Act - Third attempt (still REJECTED)
                ownershipService.validateOwnership(request);

                ContentOwnershipModel ownership3 = ContentOwnershipModel.findByUserAndContent(
                                testUser.id,
                                differentContent.id);
                assertEquals(2, ownership3.retryCount); // Incremented again
        }

        @Test
        @Transactional
        void testIdempotency_SuccessAfterRejection() {
                // Arrange - Create content initially with wrong channel
                ValidateOwnershipRequest request = new ValidateOwnershipRequest(
                                testUser.id,
                                testContent.id);

                // Act - First: Reject by unsetting user's channel
                UserModel user = UserModel.findById(testUser.id);
                user.youtubeChannelId = null;
                // No need to call persist() - Hibernate tracks changes automatically

                ValidateOwnershipResponse rejection = ownershipService.validateOwnership(request);
                assertEquals(OwnershipStatus.REJECTED, rejection.getStatus());

                ContentOwnershipModel ownershipRejected = ContentOwnershipModel.findByUserAndContent(
                                testUser.id,
                                testContent.id);
                assertEquals("NO_CHANNEL", ownershipRejected.rejectionReason);
                assertEquals(0, ownershipRejected.retryCount);

                // Act - Second: Fix user's channel and retry
                user = UserModel.findById(testUser.id);
                user.youtubeChannelId = TEST_CHANNEL_ID;
                // No need to call persist() - Hibernate tracks changes automatically

                ValidateOwnershipResponse success = ownershipService.validateOwnership(request);
                assertEquals(OwnershipStatus.VERIFIED, success.getStatus());

                ContentOwnershipModel ownershipVerified = ContentOwnershipModel.findByUserAndContent(
                                testUser.id,
                                testContent.id);
                assertNull(ownershipVerified.rejectionReason); // Cleared on success
                assertEquals(1, ownershipVerified.retryCount); // Incremented
                assertNotNull(ownershipVerified.verifiedAt);
        }

        @Test
        @Transactional
        void testCancelOwnership() {
                // Arrange - Create ownership first
                ValidateOwnershipRequest request = new ValidateOwnershipRequest(
                                testUser.id,
                                testContent.id);
                ownershipService.validateOwnership(request);

                ContentOwnershipModel ownershipBefore = ContentOwnershipModel.findByUserAndContent(
                                testUser.id,
                                testContent.id);
                assertEquals(OwnershipStatus.VERIFIED, ownershipBefore.ownershipStatus);

                // Act - Cancel ownership
                ValidateOwnershipResponse cancelResponse = ownershipService.cancelOwnershipClaim(
                                testUser.id,
                                testContent.id);

                // Assert
                assertEquals(OwnershipStatus.REJECTED, cancelResponse.getStatus());
                assertTrue(cancelResponse.getMessage().contains("cancelled by user"));

                ContentOwnershipModel ownershipAfter = ContentOwnershipModel.findByUserAndContent(
                                testUser.id,
                                testContent.id);
                assertEquals(OwnershipStatus.REJECTED, ownershipAfter.ownershipStatus);
                assertEquals("USER_CANCELLED", ownershipAfter.rejectionReason);
                assertTrue(ownershipAfter.cancelledByUser);
                assertNotNull(ownershipAfter.lastAttemptAt);
        }

        @Test
        @Transactional
        void testRejectionReason_NoChannel() {
                // Arrange - Re-fetch user and remove channel
                UserModel user = UserModel.findById(testUser.id);
                user.youtubeChannelId = null;
                // No need to call persist() - Hibernate tracks changes automatically

                ValidateOwnershipRequest request = new ValidateOwnershipRequest(
                                testUser.id,
                                testContent.id);

                // Act
                ownershipService.validateOwnership(request);

                // Assert
                ContentOwnershipModel ownership = ContentOwnershipModel.findByUserAndContent(
                                testUser.id,
                                testContent.id);
                assertEquals(OwnershipStatus.REJECTED, ownership.ownershipStatus);
                assertEquals("NO_CHANNEL", ownership.rejectionReason);
        }

        @Test
        @Transactional
        void testRejectionReason_ChannelMismatch() {
                // Arrange - Re-fetch content and update channel
                ContentRecordModel content = ContentRecordModel.findById(testContent.id);
                content.channelId = "UCDifferentChannel123";
                // No need to call persist() - Hibernate tracks changes automatically

                ValidateOwnershipRequest request = new ValidateOwnershipRequest(
                                testUser.id,
                                testContent.id);

                // Act
                ownershipService.validateOwnership(request);

                // Assert
                ContentOwnershipModel ownership = ContentOwnershipModel.findByUserAndContent(
                                testUser.id,
                                testContent.id);
                assertEquals(OwnershipStatus.REJECTED, ownership.ownershipStatus);
                assertEquals("CHANNEL_MISMATCH", ownership.rejectionReason);
        }
}
