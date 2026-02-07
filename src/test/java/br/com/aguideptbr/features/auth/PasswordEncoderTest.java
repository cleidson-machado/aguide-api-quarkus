package br.com.aguideptbr.features.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Testes unitários para PasswordEncoder.
 * Valida hash e verificação de senhas usando BCrypt.
 */
@QuarkusTest
class PasswordEncoderTest {

    @Inject
    PasswordEncoder passwordEncoder;

    @Test
    void testHashPassword() {
        String plainPassword = "senhaSecreta123";
        String hash = passwordEncoder.hashPassword(plainPassword);

        assertNotNull(hash, "Hash não deve ser nulo");
        assertTrue(hash.matches("^\\$2[aby]\\$.*"), "Hash deve começar com prefixo BCrypt");
    }

    @Test
    void testHashPasswordGeneratesDifferentHashes() {
        String plainPassword = "mesmasenha";
        String hash1 = passwordEncoder.hashPassword(plainPassword);
        String hash2 = passwordEncoder.hashPassword(plainPassword);

        assertNotNull(hash1);
        assertNotNull(hash2);
        // BCrypt usa salt aleatório, então hashes diferentes para mesma senha
        assertTrue(!hash1.equals(hash2), "Hashes devem ser diferentes (salt diferente)");
    }

    @Test
    void testVerifyPassword_Success() {
        String plainPassword = "minhasenha456";
        String hash = passwordEncoder.hashPassword(plainPassword);

        boolean valid = passwordEncoder.verifyPassword(plainPassword, hash);

        assertTrue(valid, "Senha correta deve ser validada");
    }

    @Test
    void testVerifyPassword_WrongPassword() {
        String correctPassword = "senhaCorreta";
        String wrongPassword = "senhaErrada";
        String hash = passwordEncoder.hashPassword(correctPassword);

        boolean valid = passwordEncoder.verifyPassword(wrongPassword, hash);

        assertFalse(valid, "Senha incorreta deve falhar");
    }

    @Test
    void testVerifyPassword_EmptyPassword() {
        String hash = passwordEncoder.hashPassword("senhaqualquer");

        boolean valid = passwordEncoder.verifyPassword("", hash);

        assertFalse(valid, "Senha vazia deve falhar");
    }
}
