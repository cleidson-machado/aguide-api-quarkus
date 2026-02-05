package br.com.aguideptbr.features.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.UUID;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import br.com.aguideptbr.features.user.UserModel;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Testes unitários para JWTService.
 * Valida geração de tokens JWT.
 */
@QuarkusTest
class JWTServiceTest {

    @Inject
    JWTService jwtService;

    private boolean isJwtEnabled() {
        return ConfigProvider.getConfig()
                .getOptionalValue("quarkus.smallrye-jwt.enabled", Boolean.class)
                .orElse(true);
    }

    @Test
    void testGenerateToken() {
        assumeTrue(isJwtEnabled(), "JWT desabilitado no profile de teste");
        UserModel user = new UserModel();
        user.id = UUID.randomUUID();
        user.name = "João";
        user.surname = "Silva";
        user.email = "joao@example.com";
        user.role = "USER";

        String token = jwtService.generateToken(user);

        assertNotNull(token, "Token não deve ser nulo");
        assertTrue(token.length() > 100, "Token deve ter tamanho significativo");
        // JWT padrão tem 3 partes separadas por ponto (header.payload.signature)
        assertEquals(3, token.split("\\.").length, "Token JWT deve ter 3 partes");
    }

    @Test
    void testGenerateToken_AdminRole() {
        assumeTrue(isJwtEnabled(), "JWT desabilitado no profile de teste");
        UserModel admin = new UserModel();
        admin.id = UUID.randomUUID();
        admin.name = "Admin";
        admin.surname = "Master";
        admin.email = "admin@example.com";
        admin.role = "ADMIN";

        String token = jwtService.generateToken(admin);

        assertNotNull(token, "Token de admin não deve ser nulo");
        assertTrue(token.length() > 100, "Token deve ter tamanho significativo");
    }

    @Test
    void testGenerateToken_NullRole() {
        assumeTrue(isJwtEnabled(), "JWT desabilitado no profile de teste");
        UserModel user = new UserModel();
        user.id = UUID.randomUUID();
        user.name = "User";
        user.surname = "NoRole";
        user.email = "norole@example.com";
        user.role = null; // Role nulo

        String token = jwtService.generateToken(user);

        // Deve gerar token com role padrão "USER"
        assertNotNull(token, "Token deve ser gerado mesmo com role nulo");
    }

    @Test
    void testGetExpirationTime() {
        Long expirationTime = jwtService.getExpirationTime();

        assertNotNull(expirationTime, "Expiration time não deve ser nulo");
        assertTrue(expirationTime > 0, "Expiration time deve ser positivo");
    }
}
