package br.com.aguideptbr.features.userposition.services;

import java.time.LocalDateTime;
import java.util.Set;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.userposition.UserRankingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Service responsável por todas as validações de entrada para UserRanking.
 *
 * Extração de SRP (Single Responsibility Principle) do UserRankingService.
 * Centraliza lógica de validação para facilitar manutenção, testes e
 * reutilização.
 *
 * Validações implementadas:
 * - Timestamps (limites de passado/futuro)
 * - Consecutivos dias de streak (0 a MAX_STREAK_DAYS)
 * - Valores não-negativos (visualizações, contatos, etc.)
 * - Minutos de uso diário (0 a 1440)
 * - Percentual de completude de perfil (0 a 100)
 * - Comprimento de strings (categorias, etc.)
 * - Tipos de conteúdo válidos (enum)
 *
 * @see UserRankingService
 * @author Refactored following SOLID principles
 */
@ApplicationScoped
public class UserRankingValidationService {

    private final Logger log;

    // Constantes de validação (compartilhadas com UserRankingService)
    private static final int MAX_STREAK_DAYS = 9999; // ~27 anos
    private static final long TIMESTAMP_TOLERANCE_MINUTES = 5; // 5 minutos no futuro
    private static final long TIMESTAMP_MAX_PAST_HOURS = 24; // 24 horas no passado
    private static final int MAX_DAILY_USAGE_MINUTES = 1440; // 24 horas * 60 minutos
    private static final int MAX_PROFILE_COMPLETION_PERCENTAGE = 100;
    private static final int MIN_PROFILE_COMPLETION_PERCENTAGE = 0;

    // Enum de tipos de conteúdo permitidos (Phase B)
    private static final Set<String> VALID_CONTENT_TYPES = Set.of(
            "video", "article", "course", "tutorial", "guide");

    public UserRankingValidationService(Logger log) {
        this.log = log;
    }

    /**
     * Valida timestamps para garantir que estão dentro de limites razoáveis.
     *
     * @param timestamp Timestamp a validar
     * @param fieldName Nome do campo (para mensagem de erro)
     * @throws WebApplicationException (400) se timestamp inválido
     */
    public void validateTimestamp(LocalDateTime timestamp, String fieldName) {
        if (timestamp == null) {
            return; // Null é permitido (campo opcional)
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxFuture = now.plusMinutes(TIMESTAMP_TOLERANCE_MINUTES);
        LocalDateTime minPast = now.minusHours(TIMESTAMP_MAX_PAST_HOURS);

        if (timestamp.isAfter(maxFuture)) {
            log.warnf("⚠️ Timestamp is too far in the future: %s = %s (max: %s)",
                    fieldName, timestamp, maxFuture);
            throw new WebApplicationException(
                    String.format("%s cannot be in the future (received: %s)", fieldName, timestamp),
                    Response.Status.BAD_REQUEST);
        }

        if (timestamp.isBefore(minPast)) {
            log.warnf("⚠️ Timestamp is too far in the past: %s = %s (min: %s)",
                    fieldName, timestamp, minPast);
            throw new WebApplicationException(
                    String.format("%s cannot be more than %d hours in the past",
                            fieldName, TIMESTAMP_MAX_PAST_HOURS),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida que um timestamp não está no futuro (sem restrição de passado).
     * Uso: campos históricos como {@code lastContentViewAt} onde um valor antigo
     * é completamente legítimo (ex.: usuário inativo há semanas).
     *
     * @param timestamp Timestamp a validar
     * @param fieldName Nome do campo (para mensagem de erro)
     * @throws WebApplicationException (400) se timestamp estiver no futuro
     */
    public void validateTimestampNotFuture(LocalDateTime timestamp, String fieldName) {
        if (timestamp == null) {
            return;
        }

        LocalDateTime maxFuture = LocalDateTime.now().plusMinutes(TIMESTAMP_TOLERANCE_MINUTES);

        if (timestamp.isAfter(maxFuture)) {
            log.warnf("⚠️ Timestamp is in the future: %s = %s (max: %s)",
                    fieldName, timestamp, maxFuture);
            throw new WebApplicationException(
                    String.format("%s cannot be in the future (received: %s)", fieldName, timestamp),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida consecutiveDaysStreak para garantir que está dentro de limites
     * razoáveis.
     *
     * @param streak Streak a validar
     * @throws WebApplicationException (400) se streak inválido
     */
    public void validateConsecutiveDaysStreak(Integer streak) {
        if (streak == null) {
            return; // Null é tratado como 0
        }

        if (streak < 0) {
            log.warnf("⚠️ Negative streak not allowed: %d", streak);
            throw new WebApplicationException(
                    "Consecutive days streak cannot be negative",
                    Response.Status.BAD_REQUEST);
        }

        if (streak > MAX_STREAK_DAYS) {
            log.warnf("⚠️ Streak exceeds maximum allowed: %d > %d", streak, MAX_STREAK_DAYS);
            throw new WebApplicationException(
                    String.format("Consecutive days streak cannot exceed %d days", MAX_STREAK_DAYS),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida valores não-negativos (totalContentViews, uniqueContentViews, etc.).
     *
     * @param value     Valor a validar
     * @param fieldName Nome do campo (para mensagem de erro)
     * @throws WebApplicationException (400) se valor for negativo
     */
    public void validateNonNegative(Long value, String fieldName) {
        if (value == null) {
            return; // Null é permitido
        }

        if (value < 0) {
            log.warnf("⚠️ Negative value not allowed for %s: %d", fieldName, value);
            throw new WebApplicationException(
                    String.format("%s cannot be negative", fieldName),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida avgDailyUsageMinutes (0 a 1440 minutos).
     *
     * @param minutes Minutos de uso diário
     * @throws WebApplicationException (400) se valor inválido
     */
    public void validateDailyUsageMinutes(Integer minutes) {
        if (minutes == null) {
            return; // Null é permitido
        }

        if (minutes < 0) {
            log.warnf("⚠️ Negative usage minutes not allowed: %d", minutes);
            throw new WebApplicationException(
                    "Average daily usage minutes cannot be negative",
                    Response.Status.BAD_REQUEST);
        }

        if (minutes > MAX_DAILY_USAGE_MINUTES) {
            log.warnf("⚠️ Usage minutes exceed 24 hours: %d > %d", minutes, MAX_DAILY_USAGE_MINUTES);
            throw new WebApplicationException(
                    String.format("Average daily usage minutes cannot exceed %d (24 hours)", MAX_DAILY_USAGE_MINUTES),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida profileCompletionPercentage (0 a 100).
     *
     * @param percentage Percentual de conclusão do perfil
     * @throws WebApplicationException (400) se valor inválido
     */
    public void validateProfileCompletionPercentage(Integer percentage) {
        if (percentage == null) {
            return; // Null é permitido
        }

        if (percentage < MIN_PROFILE_COMPLETION_PERCENTAGE || percentage > MAX_PROFILE_COMPLETION_PERCENTAGE) {
            log.warnf("⚠️ Profile completion percentage out of range: %d (allowed: %d-%d)",
                    percentage, MIN_PROFILE_COMPLETION_PERCENTAGE, MAX_PROFILE_COMPLETION_PERCENTAGE);
            throw new WebApplicationException(
                    String.format("Profile completion percentage must be between %d and %d",
                            MIN_PROFILE_COMPLETION_PERCENTAGE, MAX_PROFILE_COMPLETION_PERCENTAGE),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida comprimento de strings.
     *
     * @param value     String a validar
     * @param maxLength Comprimento máximo permitido
     * @param fieldName Nome do campo (para mensagem de erro)
     * @throws WebApplicationException (400) se string exceder o limite
     */
    public void validateStringLength(String value, int maxLength, String fieldName) {
        if (value == null) {
            return; // Null é permitido
        }

        if (value.length() > maxLength) {
            log.warnf("⚠️ String length exceeds maximum for %s: %d > %d", fieldName, value.length(), maxLength);
            throw new WebApplicationException(
                    String.format("%s must not exceed %d characters", fieldName, maxLength),
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Valida se o tipo de conteúdo favorito é um dos valores permitidos.
     *
     * PHASE B: Frontend solicitou validação enum para favoriteContentType.
     * Valores válidos: video, article, course, tutorial, guide
     *
     * @param contentType Tipo de conteúdo a validar
     * @throws WebApplicationException (400) se tipo inválido
     */
    public void validateContentType(String contentType) {
        if (contentType == null) {
            return; // Null é permitido
        }

        String normalizedType = contentType.toLowerCase().trim();
        if (!VALID_CONTENT_TYPES.contains(normalizedType)) {
            log.warnf("⚠️ Invalid content type: %s (allowed: %s)", contentType, VALID_CONTENT_TYPES);
            throw new WebApplicationException(
                    String.format("favoriteContentType must be one of: %s", String.join(", ", VALID_CONTENT_TYPES)),
                    Response.Status.BAD_REQUEST);
        }
    }
}
