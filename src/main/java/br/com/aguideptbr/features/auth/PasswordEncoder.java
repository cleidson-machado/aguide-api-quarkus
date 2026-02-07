package br.com.aguideptbr.features.auth;

import org.jboss.logging.Logger;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Serviço para hash e verificação de senhas usando BCrypt.
 * Utiliza a implementação do WildFly Elytron (incluída no Quarkus).
 */
@ApplicationScoped
public class PasswordEncoder {

    @Inject
    Logger log;

    private static final int BCRYPT_ITERATION_COUNT = 10; // Custo do BCrypt (2^10 = 1024 rounds)

    /**
     * Gera o hash BCrypt de uma senha.
     *
     * @param plainPassword Senha em texto plano
     * @return Hash BCrypt da senha
     * @throws RuntimeException se ocorrer erro ao gerar o hash
     */
    public String hashPassword(String plainPassword) {
        String hash = BcryptUtil.bcryptHash(plainPassword, BCRYPT_ITERATION_COUNT);
        log.debugf("✅ Hash BCrypt gerado com sucesso (cost: %d)", BCRYPT_ITERATION_COUNT);
        return hash;
    }

    /**
     * Verifica se uma senha em texto plano corresponde ao hash BCrypt.
     *
     * @param plainPassword  Senha em texto plano
     * @param hashedPassword Hash BCrypt armazenado
     * @return true se a senha corresponde ao hash, false caso contrário
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        log.debugf("🔐 Verifying password...");
        log.debugf("   Plain password length: %d", plainPassword != null ? plainPassword.length() : 0);
        log.debugf("   Hash from DB: %s",
                hashedPassword != null ? hashedPassword.substring(0, Math.min(20, hashedPassword.length())) + "..."
                        : "NULL");

        if (plainPassword == null || hashedPassword == null) {
            log.warn("⚠️ Password or hash is null!");
            return false;
        }

        boolean valid = BcryptUtil.matches(plainPassword, hashedPassword);

        if (valid) {
            log.info("✅ Password verified successfully");
        } else {
            log.warn("⚠️ Password verification failed - credentials do not match");
        }

        return valid;
    }
}
