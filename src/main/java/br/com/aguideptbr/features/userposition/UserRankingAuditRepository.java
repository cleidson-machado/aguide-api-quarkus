package br.com.aguideptbr.features.userposition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.aguideptbr.features.userposition.enuns.AuditOperationType;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository para operações de auditoria de rankings de usuários.
 * Fornece métodos para consultar histórico de mudanças, pontos adicionados,
 * etc.
 */
@ApplicationScoped
public class UserRankingAuditRepository implements PanacheRepositoryBase<UserRankingAuditModel, UUID> {

    /**
     * Busca histórico completo de um ranking específico.
     *
     * @param rankingId ID do ranking
     * @return Lista de registros de auditoria ordenados por data (mais recente
     *         primeiro)
     */
    public List<UserRankingAuditModel> findByRankingId(UUID rankingId) {
        return find("rankingId = ?1 ORDER BY createdAt DESC", rankingId).list();
    }

    /**
     * Busca histórico completo de um usuário (todos os seus rankings).
     *
     * @param userId ID do usuário
     * @return Lista de registros de auditoria ordenados por data (mais recente
     *         primeiro)
     */
    public List<UserRankingAuditModel> findByUserId(UUID userId) {
        return find("userId = ?1 ORDER BY createdAt DESC", userId).list();
    }

    /**
     * Busca histórico de adição de pontos de um usuário.
     *
     * @param userId ID do usuário
     * @param limit  Quantidade máxima de registros
     * @return Lista de registros de ADD_POINTS ordenados por data (mais recente
     *         primeiro)
     */
    public List<UserRankingAuditModel> findPointsHistoryByUserId(UUID userId, int limit) {
        return find("userId = ?1 AND operation = ?2 ORDER BY createdAt DESC", userId, AuditOperationType.ADD_POINTS)
                .page(0, limit)
                .list();
    }

    /**
     * Busca registros de auditoria por tipo de operação.
     *
     * @param operation Tipo de operação (CREATE, UPDATE, ADD_POINTS, DELETE,
     *                  RESTORE)
     * @return Lista de registros ordenados por data (mais recente primeiro)
     */
    public List<UserRankingAuditModel> findByOperation(AuditOperationType operation) {
        return find("operation = ?1 ORDER BY createdAt DESC", operation).list();
    }

    /**
     * Busca registros de auditoria dentro de um período.
     *
     * @param startDate Data inicial
     * @param endDate   Data final
     * @return Lista de registros no período especificado
     */
    public List<UserRankingAuditModel> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return find("createdAt >= ?1 AND createdAt <= ?2 ORDER BY createdAt DESC", startDate, endDate).list();
    }

    /**
     * Busca registros de auditoria de um usuário em um período.
     *
     * @param userId    ID do usuário
     * @param startDate Data inicial
     * @param endDate   Data final
     * @return Lista de registros do usuário no período especificado
     */
    public List<UserRankingAuditModel> findByUserIdAndDateRange(
            UUID userId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return find(
                "userId = ?1 AND createdAt >= ?2 AND createdAt <= ?3 ORDER BY createdAt DESC",
                userId, startDate, endDate)
                .list();
    }

    /**
     * Calcula total de pontos ganhos por um usuário em um período.
     *
     * @param userId    ID do usuário
     * @param startDate Data inicial
     * @param endDate   Data final
     * @return Soma total de pontos adicionados no período
     */
    public Long sumPointsByUserIdAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        return find(
                "SELECT COALESCE(SUM(a.pointsAdded), 0) FROM UserRankingAuditModel a " +
                        "WHERE a.userId = ?1 AND a.operation = ?2 " +
                        "AND a.createdAt >= ?3 AND a.createdAt <= ?4",
                userId, AuditOperationType.ADD_POINTS, startDate, endDate)
                .project(Long.class)
                .firstResult();
    }

    /**
     * Busca últimas operações de um usuário (limit).
     *
     * @param userId ID do usuário
     * @param limit  Quantidade máxima de registros
     * @return Lista dos últimos registros do usuário
     */
    public List<UserRankingAuditModel> findRecentByUserId(UUID userId, int limit) {
        return find("userId = ?1 ORDER BY createdAt DESC", userId)
                .page(0, limit)
                .list();
    }

    /**
     * Verifica se já existe um registro de auditoria com o requestId especificado.
     *
     * Usado para implementar idempotência em addPoints(): previne que o mesmo
     * requestId seja processado duas vezes (ex: retries de cliente, duplicação de
     * requisições).
     *
     * @param requestId Correlation ID da requisição original
     * @return true se já existir auditoria com este requestId
     */
    public boolean existsByRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return false;
        }
        return count("requestId = ?1", requestId) > 0;
    }
}
