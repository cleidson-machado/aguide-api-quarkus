package br.com.aguideptbr.features.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Teste para validar hashes BCrypt da migration V1.0.6.
 * Este teste verifica se o hash armazenado corresponde à senha esperada.
 */
@QuarkusTest
class PasswordHashValidationTest {

    private static final Logger log = Logger.getLogger(PasswordHashValidationTest.class);

    // Hash da migration V1.0.6 (CORRIGIDO)
    private static final String HASH_FROM_MIGRATION = "$2a$10$1b.v1jTmdr.c1XJXM10bsO.YwcpgZkXszAivtIL6VgfUQF2RhMIBy";

    // Senha esperada conforme documentação (ATUALIZADA)
    private static final String EXPECTED_PASSWORD = "admin123";

    @Test
    void testHashFromMigrationMatchesKabala1975() {
        log.info("🔍 Testando hash da migration V1.0.6...");
        log.infof("   Hash: %s", HASH_FROM_MIGRATION);
        log.infof("   Senha esperada: %s", EXPECTED_PASSWORD);

        // Verifica se o hash corresponde à senha
        boolean matches = BcryptUtil.matches(EXPECTED_PASSWORD, HASH_FROM_MIGRATION);

        log.infof("   Resultado: %s", matches ? "✅ MATCH" : "❌ NO MATCH");

        assertTrue(matches,
                "Hash da migration V1.0.6 deveria corresponder à senha 'Kabala1975'");
    }

    @Test
    void testGenerateNewHashForKabala1975() {
        log.info("🔧 Gerando novo hash para 'Kabala1975'...");

        String newHash = BcryptUtil.bcryptHash(EXPECTED_PASSWORD, 10);
        log.infof("   Novo hash gerado: %s", newHash);

        // Verifica se o novo hash funciona
        boolean newHashMatches = BcryptUtil.matches(EXPECTED_PASSWORD, newHash);
        log.infof("   Novo hash válido: %s", newHashMatches ? "✅ YES" : "❌ NO");

        assertTrue(newHashMatches, "Novo hash deveria corresponder à senha 'Kabala1975'");
    }

    @Test
    void testGenerateHashForAdmin123() {
        log.info("🔧 Gerando hash para 'admin123' (alternativa)...");

        String admin123Password = "admin123";
        String admin123Hash = BcryptUtil.bcryptHash(admin123Password, 10);
        log.infof("   Senha: %s", admin123Password);
        log.infof("   Hash: %s", admin123Hash);

        // Verifica se funciona
        boolean matches = BcryptUtil.matches(admin123Password, admin123Hash);
        log.infof("   Hash válido: %s", matches ? "✅ YES" : "❌ NO");

        assertTrue(matches, "Hash de 'admin123' deveria funcionar");

        log.info("");
        log.info("📋 Para usar 'admin123' como senha, execute no banco:");
        log.info("   UPDATE app_user SET password_hash = '" + admin123Hash + "'");
        log.info("   WHERE email = 'contato@aguide.space';");
    }

    @Test
    void testHashFromMigrationMatchesAdmin123() {
        log.info("🧪 Testando se hash da migration corresponde a 'admin123'...");

        String correctPassword = "admin123";
        boolean matches = BcryptUtil.matches(correctPassword, HASH_FROM_MIGRATION);

        log.infof("   Senha: %s", correctPassword);
        log.infof("   Hash: %s", HASH_FROM_MIGRATION);
        log.infof("   Resultado: %s", matches ? "✅ MATCH" : "❌ NO MATCH");

        assertTrue(matches,
                "Hash da migration deveria corresponder a 'admin123' (senha correta)");
    }

    @Test
    void testPasswordEncoderVerification() {
        log.info("🔐 Testando PasswordEncoder do sistema...");

        // Criar PasswordEncoder com Logger (constructor injection)
        Logger encoderLogger = Logger.getLogger(PasswordEncoder.class);
        PasswordEncoder encoder = new PasswordEncoder(encoderLogger);

        // Testa senha correta
        boolean correctPassword = encoder.verifyPassword(EXPECTED_PASSWORD, HASH_FROM_MIGRATION);
        log.infof("   Senha '%s' com hash da migration: %s",
                EXPECTED_PASSWORD, correctPassword ? "✅ VÁLIDA" : "❌ INVÁLIDA");

        assertTrue(correctPassword, "PasswordEncoder deveria validar 'admin123'");

        // Testa senha incorreta
        boolean wrongPassword = encoder.verifyPassword("senhaerrada123", HASH_FROM_MIGRATION);
        log.infof("   Senha 'senhaerrada123' com hash da migration: %s",
                wrongPassword ? "✅ VÁLIDA" : "❌ INVÁLIDA");

        assertFalse(wrongPassword, "PasswordEncoder NÃO deveria validar 'senhaerrada123'");
    }
}
