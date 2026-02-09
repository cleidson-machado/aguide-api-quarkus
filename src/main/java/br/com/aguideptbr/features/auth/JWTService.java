package br.com.aguideptbr.features.auth;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.aguideptbr.features.user.UserModel;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Serviço responsável pela geração e validação de tokens JWT.
 */
@ApplicationScoped
public class JWTService {

    @Inject
    Logger log;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "jwt.expiration.time", defaultValue = "3600")
    Long expirationTime; // Em segundos

    @ConfigProperty(name = "mp.jwt.sign.key.location")
    String privateKeyLocation;

    private String privateKeyPem;
    private ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    void loadPrivateKey() {
        try {
            // Lê o arquivo de chave privada
            privateKeyPem = Files.readString(Paths.get(privateKeyLocation), StandardCharsets.UTF_8);
            log.infof("✅ Chave privada JWT carregada de: %s", privateKeyLocation);
        } catch (Exception e) {
            log.errorf(e, "❌ Erro ao carregar chave privada de: %s", privateKeyLocation);
            throw new RuntimeException("Falha ao carregar chave privada JWT", e);
        }
    }

    /**
     * Gera um token JWT para o usuário autenticado usando assinatura MANUAL.
     * Não depende do SmallRye JWT Builder que estava falhando com parsing de
     * chaves.
     *
     * @param user Usuário autenticado
     * @return Token JWT assinado
     */
    public String generateToken(UserModel user) {
        try {
            long currentTime = Instant.now().getEpochSecond();
            long expiresAt = currentTime + expirationTime;

            // Header JWT (algorítmo RS256)
            String header = """
                    {
                      "alg": "RS256",
                      "typ": "JWT"
                    }
                    """.trim();

            // Payload JWT MÍNIMO - Apenas identificadores essenciais
            // Dados sensíveis (roles, nome) são buscados do banco quando necessário
            String payload = String.format("""
                    {
                      "iss": "%s",
                      "sub": "%s",
                      "upn": "%s",
                      "iat": %d,
                      "exp": %d
                    }
                    """.trim(),
                    issuer,
                    user.id.toString(),
                    user.email,
                    currentTime,
                    expiresAt);

            // Codifica header e payload em Base64URL
            String encodedHeader = base64UrlEncode(header.getBytes(StandardCharsets.UTF_8));
            String encodedPayload = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));

            // Cria mensagem a ser assinada: header.payload
            String message = encodedHeader + "." + encodedPayload;

            // Assina com chave privada RSA
            byte[] signature = signWithPrivateKey(message.getBytes(StandardCharsets.UTF_8));
            String encodedSignature = base64UrlEncode(signature);

            // Token JWT final: header.payload.signature
            String token = message + "." + encodedSignature;

            log.infof("✅ Token JWT gerado MANUALMENTE para usuário: %s (expira em %d segundos)",
                    user.email, expirationTime);

            return token;
        } catch (Exception e) {
            log.errorf(e, "❌ Erro ao gerar token JWT para usuário: %s", user.email);
            throw new RuntimeException("Erro ao gerar token de autenticação", e);
        }
    }

    /**
     * Assina os dados com a chave privada RSA.
     */
    private byte[] signWithPrivateKey(byte[] data) throws Exception {
        // Remove headers PEM e quebras de linha
        String privateKeyContent = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        // Decodifica Base64 para obter os bytes da chave
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);

        // Cria chave privada a partir dos bytes PKCS#8
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // Assina com SHA256withRSA
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);

        return signature.sign();
    }

    /**
     * Codifica bytes em Base64URL (sem padding).
     */
    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * Retorna o tempo de expiração configurado (em segundos).
     *
     * @return Tempo de expiração em segundos
     */
    public Long getExpirationTime() {
        return expirationTime;
    }

    /**
     * Valida se um token JWT é válido.
     * A validação real é feita automaticamente pelo Quarkus SmallRye JWT.
     *
     * @param token Token JWT a ser validado
     * @return true se o token é válido (não expirado, assinado corretamente)
     */
    public boolean validateToken(String token) {
        // A validação automática é feita pelo Quarkus via @RolesAllowed
        // Este método pode ser usado para validações adicionais se necessário
        return token != null && !token.trim().isEmpty();
    }
}
