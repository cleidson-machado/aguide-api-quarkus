package br.com.aguideptbr.features.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.aguideptbr.features.user.UserModel;
import br.com.aguideptbr.features.user.UserRole;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Testes de lógica de negócio para Auth (sem geração de JWT).
 *
 * NOTA: Este teste foca apenas em operações de banco de dados (find, persist).
 * Testes de PasswordEncoder estão em PasswordEncoderTest.
 * Testes de JWTService estão em JWTServiceTest.
 */
@QuarkusTest
class AuthServiceTest {

    @Inject
    PasswordEncoder passwordEncoder;

    /**
     * Limpa dados de teste antes de cada teste.
     */
    @BeforeEach
    @Transactional
    void cleanupTestData() {
        // Remove usuários de teste criados durante os testes
        UserModel.delete("email like ?1", "%test-auth%");
    }

    @Test
    @Transactional
    void testEmailAlreadyExists_ShouldFindExistingUser() {
        // Cria usuário diretamente no banco
        UserModel user = new UserModel();
        user.name = "Maria";
        user.surname = "Silva";
        user.email = "maria-test-auth@example.com";
        user.passwordHash = passwordEncoder.hashPassword("senha123");
        user.role = UserRole.FREE;
        user.persist();

        // Busca usuário pelo email
        UserModel found = UserModel.findByEmail("maria-test-auth@example.com");

        assertNotNull(found, "Usuário deve ser encontrado");
        assertTrue(found.email.equals("maria-test-auth@example.com"), "Email deve estar correto");
    }

    @Test
    @Transactional
    void testUserNotFound_ShouldReturnNull() {
        // Tenta buscar usuário inexistente
        UserModel found = UserModel.findByEmail("naoexiste-test-auth@example.com");

        assertNull(found, "Usuário não encontrado deve retornar null");
    }

    @Test
    @Transactional
    void testEmailNormalization() {
        // Cria usuário com email em lowercase
        UserModel user = new UserModel();
        user.name = "Pedro";
        user.surname = "Oliveira";
        user.email = "PEDRO-TEST-AUTH@EXAMPLE.COM".toLowerCase().trim(); // Simula normalização
        user.passwordHash = passwordEncoder.hashPassword("senha789");
        user.role = UserRole.FREE;
        user.persist();

        // Busca com email em lowercase
        UserModel found = UserModel.findByEmail("pedro-test-auth@example.com");

        assertNotNull(found, "Email normalizado deve ser encontrado");
        assertTrue(found.email.equals("pedro-test-auth@example.com"), "Email deve estar em lowercase");
    }

    @Test
    @Transactional
    void testUserPersistenceWithHashedPassword() {
        String plainPassword = "senhaSegura123";
        String hash = passwordEncoder.hashPassword(plainPassword);

        // Cria usuário com senha hasheada
        UserModel user = new UserModel();
        user.name = "João";
        user.surname = "Teste";
        user.email = "joao-test-auth@example.com";
        user.passwordHash = hash;
        user.role = UserRole.FREE;
        user.persist();

        // Busca usuário e verifica que hash foi persistido
        UserModel found = UserModel.findByEmail("joao-test-auth@example.com");

        assertNotNull(found, "Usuário deve ser encontrado");
        assertNotNull(found.passwordHash, "Password hash não deve ser nulo");
        assertTrue(found.passwordHash.startsWith("$2a$"), "Hash deve ser BCrypt");
    }
}
