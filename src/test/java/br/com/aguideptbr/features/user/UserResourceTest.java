package br.com.aguideptbr.features.user;

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
class UserResourceTest {

    @Inject
    EntityManager entityManager;

    @SuppressWarnings("null")
    @Test
    void testDatabaseConnection() {
        // Verifica que conseguimos fazer uma query simples no banco de dados
        Long count = entityManager.createQuery("SELECT COUNT(u) FROM UserModel u", Long.class)
                .getSingleResult();

        assertNotNull(count, "Database connection should work");
        assertTrue(count >= 0, "Count should be non-negative");
    }

    @Test
    void testPanacheRepositoryWorks() {
        // Verifica que o Panache (Active Record) está funcionando
        long count = UserModel.count();
        assertTrue(count >= 0, "Panache count should work");
    }

    @Test
    @Transactional
    void testDatabaseWrite() {
        // Verifica que conseguimos escrever no banco de dados
        UserModel testUser = new UserModel();
        testUser.name = "Test User " + System.currentTimeMillis();
        testUser.surname = "Test Surname"; // Campo obrigatório
        testUser.email = "test" + System.currentTimeMillis() + "@example.com";
        testUser.passwordHash = "$2a$10$testhashedpassword";
        testUser.role = "USER";

        testUser.persist();

        assertNotNull(testUser.id, "User should have ID after persist");

        // Verifica que conseguimos ler de volta
        UserModel found = UserModel.findById(testUser.id);
        assertNotNull(found, "Should find persisted user");
        assertEquals(testUser.name, found.name);
        assertEquals(testUser.email, found.email);
    }
}
