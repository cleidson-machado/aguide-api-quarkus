package br.com.aguideptbr.features.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.aguideptbr.features.auth.dto.GoogleOAuthRequest;
import br.com.aguideptbr.features.auth.dto.LoginResponse;
import br.com.aguideptbr.features.user.UserModel;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Testes de integração para autenticação OAuth com Google.
 * Testa a lógica de negócio do AuthService com os novos campos do YouTube.
 *
 * Nota: Testes de endpoint REST foram simplificados para testar diretamente o
 * serviço,
 * pois o AuthenticationFilter complica testes REST sem agregar valor.
 * A cobertura de testes REST já é feita em outros testes (login, register).
 *
 * @author Cleidson Machado
 * @since 1.0
 */
@QuarkusTest
class AuthResourceOAuthTest {

    @Inject
    AuthService authService;

    /**
     * Limpa dados de teste antes de cada teste.
     */
    @BeforeEach
    @Transactional
    void cleanupTestData() {
        // Remove usuários OAuth de teste criados durante os testes
        UserModel.delete("email like ?1", "%oauth-test%");
    }

    @Test
    @Transactional
    void testOAuthLogin_WithYoutubeData_ShouldAuthenticateSuccessfully() {
        // Arrange
        GoogleOAuthRequest request = new GoogleOAuthRequest();
        request.setEmail("youtuber-oauth-test@gmail.com");
        request.setName("YouTuber");
        request.setSurname("Test");
        request.setOauthProvider("GOOGLE");
        request.setOauthId("123456789012345678901");
        request.setAccessToken("ya29.mock_access_token");
        request.setIdToken("mock_id_token");
        request.setYoutubeUserId("UCUXeX3iLBjsWbc_1bCrEdCQ");
        request.setYoutubeChannelTitle("Test Channel");

        // Act
        LoginResponse response = authService.loginWithGoogle(request);

        // Assert
        assertNotNull(response, "LoginResponse deve estar presente");
        assertNotNull(response.getToken(), "Token deve estar presente");
        assertNotNull(response.getUser(), "User info deve estar presente");
        assertEquals("youtuber-oauth-test@gmail.com", response.getUser().getEmail());
    }

    @Test
    @Transactional
    void testOAuthLogin_WithoutYoutubeData_ShouldAuthenticateSuccessfully() {
        // Arrange
        GoogleOAuthRequest request = new GoogleOAuthRequest();
        request.setEmail("regular-oauth-test@gmail.com");
        request.setName("Regular");
        request.setSurname("User");
        request.setOauthProvider("GOOGLE");
        request.setOauthId("987654321098765432109");
        request.setAccessToken("ya29.mock_access_token");
        request.setIdToken("mock_id_token");
        // YouTube fields = null (não enviados)

        // Act
        LoginResponse response = authService.loginWithGoogle(request);

        // Assert
        assertNotNull(response, "LoginResponse deve estar presente");
        assertNotNull(response.getToken(), "Token deve estar presente");
    }

    @Test
    @Transactional
    void testOAuthLogin_WithYoutubeData_ShouldPersistInDatabase() {
        // Arrange
        GoogleOAuthRequest request = new GoogleOAuthRequest();
        request.setEmail("persist-oauth-test@gmail.com");
        request.setName("Persist");
        request.setSurname("Test");
        request.setOauthProvider("GOOGLE");
        request.setOauthId("111222333444555666777");
        request.setAccessToken("ya29.mock_access_token");
        request.setIdToken("mock_id_token");
        request.setYoutubeUserId("UCTestPersistChannel");
        request.setYoutubeChannelTitle("Persist Channel");

        // Act - Faz login OAuth
        authService.loginWithGoogle(request);

        // Assert - Verifica se os dados foram persistidos no banco
        UserModel user = UserModel.findByEmail("persist-oauth-test@gmail.com");

        assertNotNull(user, "Usuário deve estar no banco");
        assertEquals("UCTestPersistChannel", user.youtubeUserId,
                "YouTube User ID deve estar persistido");
        assertEquals("Persist Channel", user.youtubeChannelTitle,
                "YouTube Channel Title deve estar persistido");
    }

    @Test
    @Transactional
    void testOAuthLogin_WithoutYoutubeData_ShouldAcceptNull() {
        // Arrange
        GoogleOAuthRequest request = new GoogleOAuthRequest();
        request.setEmail("nullyoutube-oauth-test@gmail.com");
        request.setName("Null");
        request.setSurname("YouTube");
        request.setOauthProvider("GOOGLE");
        request.setOauthId("999888777666555444333");
        request.setAccessToken("ya29.mock_access_token");
        request.setIdToken("mock_id_token");
        // YouTube fields não são setados (null)

        // Act - Faz login OAuth
        authService.loginWithGoogle(request);

        // Assert - Verifica que usuário foi criado sem campos do YouTube
        UserModel user = UserModel.findByEmail("nullyoutube-oauth-test@gmail.com");

        assertNotNull(user, "Usuário deve estar no banco");
        assertNull(user.youtubeUserId, "YouTube User ID deve ser null");
        assertNull(user.youtubeChannelTitle, "YouTube Channel Title deve ser null");
    }

    @Test
    @Transactional
    void testOAuthLogin_UpdateYoutubeData_ShouldPreserveIfNull() {
        // Arrange - Cria usuário com dados do YouTube
        GoogleOAuthRequest firstLogin = new GoogleOAuthRequest();
        firstLogin.setEmail("update-oauth-test@gmail.com");
        firstLogin.setName("Update");
        firstLogin.setSurname("Test");
        firstLogin.setOauthProvider("GOOGLE");
        firstLogin.setOauthId("555666777888999000111");
        firstLogin.setAccessToken("ya29.mock_access_token");
        firstLogin.setIdToken("mock_id_token");
        firstLogin.setYoutubeUserId("UCOriginalChannel");
        firstLogin.setYoutubeChannelTitle("Original Channel");

        // First login
        authService.loginWithGoogle(firstLogin);

        // Act - Segundo login sem dados do YouTube (simula falha na captura)
        GoogleOAuthRequest secondLogin = new GoogleOAuthRequest();
        secondLogin.setEmail("update-oauth-test@gmail.com");
        secondLogin.setName("Update");
        secondLogin.setSurname("Test");
        secondLogin.setOauthProvider("GOOGLE");
        secondLogin.setOauthId("555666777888999000111");
        secondLogin.setAccessToken("ya29.mock_access_token_new");
        secondLogin.setIdToken("mock_id_token_new");
        // YouTube fields não são setados (null) - simula falha na captura

        authService.loginWithGoogle(secondLogin);

        // Assert - Verifica que dados antigos do YouTube foram preservados
        UserModel user = UserModel.findByEmail("update-oauth-test@gmail.com");

        assertNotNull(user, "Usuário deve estar no banco");
        assertEquals("UCOriginalChannel", user.youtubeUserId,
                "YouTube User ID deve ser preservado do primeiro login");
        assertEquals("Original Channel", user.youtubeChannelTitle,
                "YouTube Channel Title deve ser preservado do primeiro login");
    }
}
