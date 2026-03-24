package br.com.aguideptbr.features.userposition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.aguideptbr.features.userposition.enuns.ConversionPotential;
import br.com.aguideptbr.features.userposition.enuns.EngagementLevel;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository para operações de banco de dados com rankings de usuários.
 * Gerencia dados de engajamento, conversão e atividade dos usuários.
 *
 * @see UserRankingModel
 * @see EngagementLevel
 * @see ConversionPotential
 */
@ApplicationScoped
public class UserRankingRepository implements PanacheRepositoryBase<UserRankingModel, UUID> {

    /**
     * Busca o ranking de um usuário específico.
     *
     * @param userId ID do usuário
     * @return Optional contendo o ranking do usuário ou vazio se não encontrado
     */
    public Optional<UserRankingModel> findByUserId(UUID userId) {
        return find("userId = ?1 and deletedAt is null", userId).firstResultOptional();
    }

    /**
     * Busca todos os rankings ativos (não deletados).
     *
     * @return Lista de rankings ativos
     */
    public List<UserRankingModel> findAllActive() {
        return list("deletedAt is null");
    }

    /**
     * Busca rankings por nível de engajamento.
     *
     * @param engagementLevel Nível de engajamento
     * @return Lista de rankings com o nível especificado
     */
    public List<UserRankingModel> findByEngagementLevel(EngagementLevel engagementLevel) {
        return list("engagementLevel = ?1 and deletedAt is null", engagementLevel);
    }

    /**
     * Busca rankings por potencial de conversão.
     *
     * @param conversionPotential Potencial de conversão
     * @return Lista de rankings com o potencial especificado
     */
    public List<UserRankingModel> findByConversionPotential(ConversionPotential conversionPotential) {
        return list("conversionPotential = ?1 and deletedAt is null", conversionPotential);
    }

    /**
     * Busca top N usuários por score total (ordenado decrescente).
     *
     * @param limit Número máximo de resultados
     * @return Lista dos usuários com maior pontuação
     */
    public List<UserRankingModel> findTopByScore(int limit) {
        return find("deletedAt is null ORDER BY totalScore DESC")
                .page(0, limit)
                .list();
    }

    /**
     * Busca usuários com score acima de um valor mínimo.
     *
     * @param minScore Pontuação mínima
     * @return Lista de usuários com score maior ou igual ao especificado
     */
    public List<UserRankingModel> findByScoreGreaterThanEqual(int minScore) {
        return list("totalScore >= ?1 and deletedAt is null ORDER BY totalScore DESC", minScore);
    }

    /**
     * Busca usuários com streak de dias consecutivos acima de um valor mínimo.
     *
     * @param minStreak Número mínimo de dias consecutivos
     * @return Lista de usuários com streak maior ou igual ao especificado
     */
    public List<UserRankingModel> findByConsecutiveDaysStreakGreaterThanEqual(int minStreak) {
        return list("consecutiveDaysStreak >= ?1 and deletedAt is null ORDER BY consecutiveDaysStreak DESC",
                minStreak);
    }

    /**
     * Busca usuários que possuem WhatsApp cadastrado.
     *
     * @return Lista de usuários com WhatsApp
     */
    public List<UserRankingModel> findUsersWithWhatsApp() {
        return list("hasWhatsapp = true and deletedAt is null");
    }

    /**
     * Busca usuários que possuem Telegram cadastrado.
     *
     * @return Lista de usuários com Telegram
     */
    public List<UserRankingModel> findUsersWithTelegram() {
        return list("hasTelegram = true and deletedAt is null");
    }

    /**
     * Busca usuários por categoria favorita.
     *
     * @param category Categoria favorita
     * @return Lista de usuários com a categoria especificada
     */
    public List<UserRankingModel> findByFavoriteCategory(String category) {
        return list("favoriteCategory = ?1 and deletedAt is null", category);
    }

    /**
     * Verifica se já existe um ranking para um usuário.
     *
     * @param userId ID do usuário
     * @return true se existir um ranking ativo para o usuário
     */
    public boolean existsByUserId(UUID userId) {
        return count("userId = ?1 and deletedAt is null", userId) > 0;
    }

    /**
     * Conta o total de rankings ativos com alto potencial de conversão.
     *
     * @return Quantidade de usuários com HIGH ou VERY_HIGH conversion potential
     */
    public long countHighConversionPotential() {
        return count(
                "(conversionPotential = ?1 OR conversionPotential = ?2) and deletedAt is null",
                ConversionPotential.HIGH,
                ConversionPotential.VERY_HIGH);
    }

    /**
     * Conta o total de rankings ativos com alto engajamento.
     *
     * @return Quantidade de usuários com HIGH ou VERY_HIGH engagement level
     */
    public long countHighEngagement() {
        return count(
                "(engagementLevel = ?1 OR engagementLevel = ?2) and deletedAt is null",
                EngagementLevel.HIGH,
                EngagementLevel.VERY_HIGH);
    }
}
