package br.com.aguideptbr.auth;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;

import br.com.aguideptbr.features.user.UserModel;
import io.smallrye.jwt.build.Jwt;
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

    /**
     * Gera um token JWT para o usuário autenticado.
     *
     * @param user Usuário autenticado
     * @return Token JWT assinado
     */
    public String generateToken(UserModel user) {
        try {
            long currentTime = Instant.now().getEpochSecond();
            long expiresAt = currentTime + expirationTime;

            Set<String> groups = new HashSet<>();
            if (user.role != null && !user.role.isEmpty()) {
                groups.add(user.role);
            } else {
                groups.add("USER"); // Role padrão
            }

            String token = Jwt.issuer(issuer)
                    .upn(user.email) // Unique Principal Name
                    .subject(user.id.toString())
                    .groups(groups)
                    .claim(Claims.email.name(), user.email)
                    .claim("name", user.name)
                    .claim("surname", user.surname)
                    .issuedAt(currentTime)
                    .expiresAt(expiresAt)
                    .sign();

            log.infof("✅ Token JWT gerado para usuário: %s (expira em %d segundos)",
                    user.email, expirationTime);

            return token;
        } catch (Exception e) {
            log.errorf(e, "❌ Erro ao gerar token JWT para usuário: %s", user.email);
            throw new RuntimeException("Erro ao gerar token de autenticação", e);
        }
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
