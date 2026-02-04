package br.com.aguideptbr.auth;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.InvalidKeySpecException;

import org.jboss.logging.Logger;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec;
import org.wildfly.security.password.util.ModularCrypt;

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

    private static final Provider ELYTRON_PROVIDER = WildFlyElytronPasswordProvider.getInstance();
    private static final String BCRYPT_ALGORITHM = BCryptPassword.ALGORITHM_BCRYPT;
    private static final int BCRYPT_ITERATION_COUNT = 10; // Custo do BCrypt (2^10 = 1024 rounds)

    /**
     * Gera o hash BCrypt de uma senha.
     *
     * @param plainPassword Senha em texto plano
     * @return Hash BCrypt da senha
     * @throws RuntimeException se ocorrer erro ao gerar o hash
     */
    public String hashPassword(String plainPassword) {
        try {
            PasswordFactory passwordFactory = PasswordFactory.getInstance(BCRYPT_ALGORITHM, ELYTRON_PROVIDER);

            IteratedSaltedPasswordAlgorithmSpec iteratedAlgorithmSpec = new IteratedSaltedPasswordAlgorithmSpec(
                    BCRYPT_ITERATION_COUNT, generateSalt());

            EncryptablePasswordSpec encryptableSpec = new EncryptablePasswordSpec(plainPassword.toCharArray(),
                    iteratedAlgorithmSpec);

            BCryptPassword bcryptPassword = (BCryptPassword) passwordFactory.generatePassword(encryptableSpec);

            String hash = ModularCrypt.encodeAsString(bcryptPassword);

            log.debugf("✅ Hash BCrypt gerado com sucesso (cost: %d)", BCRYPT_ITERATION_COUNT);

            return hash;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("❌ Erro ao gerar hash BCrypt", e);
            throw new RuntimeException("Erro ao processar senha", e);
        }
    }

    /**
     * Verifica se uma senha em texto plano corresponde ao hash BCrypt.
     *
     * @param plainPassword  Senha em texto plano
     * @param hashedPassword Hash BCrypt armazenado
     * @return true se a senha corresponde ao hash, false caso contrário
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            PasswordFactory passwordFactory = PasswordFactory.getInstance(BCRYPT_ALGORITHM, ELYTRON_PROVIDER);

            Password userPasswordDecoded = ModularCrypt.decode(hashedPassword);

            boolean valid = passwordFactory.verify(userPasswordDecoded, plainPassword.toCharArray());

            if (valid) {
                log.debug("✅ Senha verificada com sucesso");
            } else {
                log.debug("⚠️ Senha incorreta");
            }

            return valid;
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
            log.error("❌ Erro ao verificar senha", e);
            return false;
        }
    }

    /**
     * Gera um salt aleatório para o BCrypt.
     *
     * @return Array de bytes com o salt (16 bytes)
     */
    private byte[] generateSalt() {
        byte[] salt = new byte[BCryptPassword.BCRYPT_SALT_SIZE];
        new java.security.SecureRandom().nextBytes(salt);
        return salt;
    }
}
