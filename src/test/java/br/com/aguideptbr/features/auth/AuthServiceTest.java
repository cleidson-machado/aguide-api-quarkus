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

    @Test
    @Transactional
    void testUserWithYoutubeData_ShouldPersistCorrectly() {
        // Cria usuário com dados do YouTube
        UserModel user = new UserModel();
        user.name = "YouTuber";
        user.surname = "Test";
        user.email = "youtuber-test-auth@example.com";
        user.passwordHash = passwordEncoder.hashPassword("senha123");
        user.role = UserRole.FREE;
        user.youtubeUserId = "UCUXeX3iLBjsWbc_1bCrEdCQ";
        user.youtubeChannelId = "UCUXeX3iLBjsWbc_1bCrEdCQ";
        user.youtubeChannelTitle = "Test Channel";
        user.persist();

        // Busca usuário e verifica dados do YouTube
        UserModel found = UserModel.findByEmail("youtuber-test-auth@example.com");

        assertNotNull(found, "Usuário deve ser encontrado");
        assertNotNull(found.youtubeUserId, "YouTube User ID não deve ser nulo");
        assertNotNull(found.youtubeChannelId, "YouTube Channel ID não deve ser nulo");
        assertNotNull(found.youtubeChannelTitle, "YouTube Channel Title não deve ser nulo");
        assertTrue(found.youtubeUserId.equals("UCUXeX3iLBjsWbc_1bCrEdCQ"),
                "YouTube User ID deve estar correto");
        assertTrue(found.youtubeChannelId.equals("UCUXeX3iLBjsWbc_1bCrEdCQ"),
                "YouTube Channel ID deve estar correto");
        assertTrue(found.youtubeChannelTitle.equals("Test Channel"),
                "YouTube Channel Title deve estar correto");
    }

    @Test
    @Transactional
    void testUserWithoutYoutubeData_ShouldAcceptNull() {
        // Cria usuário sem dados do YouTube
        UserModel user = new UserModel();
        user.name = "Regular";
        user.surname = "User";
        user.email = "regular-test-auth@example.com";
        user.passwordHash = passwordEncoder.hashPassword("senha123");
        user.role = UserRole.FREE;
        user.youtubeUserId = null;
        user.youtubeChannelId = null;
        user.youtubeChannelTitle = null;
        user.persist();

        // Busca usuário e verifica que campos são null
        UserModel found = UserModel.findByEmail("regular-test-auth@example.com");

        assertNotNull(found, "Usuário deve ser encontrado");
        assertNull(found.youtubeUserId, "YouTube User ID deve ser null");
        assertNull(found.youtubeChannelId, "YouTube Channel ID deve ser null");
        assertNull(found.youtubeChannelTitle, "YouTube Channel Title deve ser null");
    }

    @Test
    @Transactional
    void testOAuthUserWithYoutubeData_ShouldPersistCorrectly() {
        // Cria usuário OAuth com dados do YouTube
        UserModel user = new UserModel();
        user.name = "Google";
        user.surname = "User";
        user.email = "google-test-auth@example.com";
        user.oauthProvider = "GOOGLE";
        user.oauthId = "112072455526965200736";
        user.passwordHash = null; // OAuth users não têm senha local
        user.role = UserRole.FREE;
        user.youtubeUserId = "UCTestChannelId123";
        user.youtubeChannelId = "UCTestChannelId123";
        user.youtubeChannelTitle = "My YouTube Channel";
        user.persist();

        // Busca usuário e verifica dados OAuth + YouTube
        UserModel found = UserModel.findByEmail("google-test-auth@example.com");

        assertNotNull(found, "Usuário deve ser encontrado");
        assertTrue(found.isOAuthUser(), "Usuário deve ser OAuth");
        assertNotNull(found.youtubeUserId, "YouTube User ID não deve ser nulo");
        assertNotNull(found.youtubeChannelId, "YouTube Channel ID não deve ser nulo");
        assertNotNull(found.youtubeChannelTitle, "YouTube Channel Title não deve ser nulo");
        assertTrue(found.youtubeUserId.equals("UCTestChannelId123"),
                "YouTube User ID deve estar correto");
        assertTrue(found.youtubeChannelId.equals("UCTestChannelId123"),
                "YouTube Channel ID deve estar correto");
        assertTrue(found.youtubeChannelTitle.equals("My YouTube Channel"),
                "YouTube Channel Title deve estar correto");
    }
}
