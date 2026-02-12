package br.com.aguideptbr.features.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Testes simples para validar conectividade e operações básicas no banco de
 * dados PostgreSQL (quarkus_test).
 *
 * OBJETIVO: Garantir que o banco de dados de testes está configurado
 * corretamente.
 */
@QuarkusTest
class ContentRecordResourceTest {

        @Inject
        EntityManager entityManager;

        @Test
        void testDatabaseConnection() {
                // Verifica que conseguimos fazer uma query simples no banco de dados
                Long count = entityManager.createQuery("SELECT COUNT(c) FROM ContentRecordModel c", Long.class)
                                .getSingleResult();

                assertNotNull(count, "Database connection should work");
                assertTrue(count >= 0, "Count should be non-negative");
        }

        @Test
        void testPanacheRepositoryWorks() {
                // Verifica que o Panache (Active Record) está funcionando
                long count = ContentRecordModel.count();
                assertTrue(count >= 0, "Panache count should work");
        }

        @Test
        @Transactional
        void testDatabaseWrite() {
                // Verifica que conseguimos escrever no banco de dados
                ContentRecordModel testContent = new ContentRecordModel();
                testContent.title = "Test Content " + System.currentTimeMillis();
                testContent.description = "Test Description";
                testContent.videoUrl = "https://test.example.com/" + System.currentTimeMillis();
                testContent.channelId = "UC-test-channel-123";
                testContent.channelOwnerLinkId = "owner-link-456";
                testContent.channelName = "Test Channel";
                testContent.type = ContentType.VIDEO;

                testContent.persist();

                assertNotNull(testContent.id, "ContentRecord should have ID after persist");

                // Verifica que conseguimos ler de volta
                ContentRecordModel found = ContentRecordModel.findById(testContent.id);
                assertNotNull(found, "Should find persisted content");
                assertEquals(testContent.title, found.title);
                assertEquals(testContent.description, found.description);
        }

        @Test
        @Transactional
        void testFindByChannelId() {
                // Cria conteúdo de teste com channelId
                ContentRecordModel testContent = new ContentRecordModel();
                testContent.title = "Channel Test " + System.currentTimeMillis();
                testContent.videoUrl = "https://test.example.com/channel/" + System.currentTimeMillis();
                testContent.channelId = "UC-test-unique-123";
                testContent.channelName = "Test Channel Name";
                testContent.type = ContentType.VIDEO;
                testContent.persist();

                // Busca por channelId
                var results = ContentRecordModel.findByChannelId("UC-test-unique-123");
                assertNotNull(results, "Results should not be null");
                assertTrue(results.size() > 0, "Should find at least one content by channelId");
                assertEquals("UC-test-unique-123", results.get(0).channelId);
        }

        @Test
        @Transactional
        void testFindByChannelOwner() {
                // Cria conteúdo de teste com channelOwnerLinkId
                ContentRecordModel testContent = new ContentRecordModel();
                testContent.title = "Owner Test " + System.currentTimeMillis();
                testContent.videoUrl = "https://test.example.com/owner/" + System.currentTimeMillis();
                testContent.channelOwnerLinkId = "owner-link-unique-789";
                testContent.channelName = "Owner Test Channel";
                testContent.type = ContentType.VIDEO;
                testContent.persist();

                // Busca por channelOwnerLinkId
                var results = ContentRecordModel.findByChannelOwner("owner-link-unique-789");
                assertNotNull(results, "Results should not be null");
                assertTrue(results.size() > 0, "Should find at least one content by channelOwnerLinkId");
                assertEquals("owner-link-unique-789", results.get(0).channelOwnerLinkId);
        }
}
