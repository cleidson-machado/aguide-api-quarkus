package br.com.aguideptbr.features.userposition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.aguideptbr.features.user.UserModel;
import br.com.aguideptbr.features.user.UserRole;
import br.com.aguideptbr.features.userposition.enuns.AuditOperationType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Testes unitários e de integração para UserRankingService.
 *
 * Foco especial em:
 * - Detecção de milestones de completude de perfil (50% e 100%)
 * - Incremento automático de pontos quando thresholds são cruzados
 * - Idempotência (não adicionar pontos duplicados)
 */
@QuarkusTest
class UserRankingServiceTest {

    @Inject
    UserRankingService userRankingService;

    @Inject
    UserRankingRepository userRankingRepository;

    @Inject
    UserRankingAuditRepository auditRepository;

    private UserModel testUser;

    /**
     * Cria usuário de teste antes de cada teste.
     */
    @BeforeEach
    @Transactional
    void setup() {
        // Limpar dados de teste anteriores
        UserRankingModel.deleteAll();
        UserRankingAuditModel.deleteAll();
        UserModel.delete("email like ?1", "%ranking-test%");

        // Criar usuário de teste
        testUser = new UserModel();
        testUser.name = "Test";
        testUser.surname = "User";
        testUser.email = "ranking-test@example.com";
        testUser.passwordHash = "$2a$10$validhash";
        testUser.role = UserRole.FREE;
        testUser.persist();
    }

    @Test
    @Transactional
    void testProfileCompletionMilestone_50Percent_ShouldAdd3Points() {
        // Arrange: Criar ranking inicial com 0% de completude
        UserRankingModel ranking = new UserRankingModel();
        ranking.setUserId(testUser.id);
        ranking.setTotalScore(10); // Pontos iniciais
        ranking.setProfileCompletionPercentage(0);
        ranking.setTotalActiveDays(1L);
        ranking.setLastLoginAt(LocalDateTime.now());
        userRankingService.create(ranking);

        // Act: Atualizar para 50% de completude
        UserRankingModel updateData = new UserRankingModel();
        updateData.setProfileCompletionPercentage(50);
        updateData.setHasPhones(true);
        updateData.setTotalPhones(3);

        UserRankingModel updated = userRankingService.update(ranking.getId(), updateData);

        // Assert: Deve ter 10 (inicial) + 3 (milestone 50%) = 13 pontos
        assertEquals(13, updated.getTotalScore(),
                "Score should be 10 (initial) + 3 (50% milestone) = 13");
        assertEquals(50, updated.getProfileCompletionPercentage(),
                "Profile completion should be 50%");

        // Verificar auditoria
        List<UserRankingAuditModel> audits = auditRepository.findByUserId(testUser.id);
        assertTrue(audits.stream().anyMatch(a -> AuditOperationType.ADD_POINTS.equals(a.getOperation()) &&
                "PROFILE_50_PERCENT".equals(a.getPointsReason()) &&
                a.getPointsAdded() == 3),
                "Audit should contain PROFILE_50_PERCENT entry with +3 points");
    }

    @Test
    @Transactional
    void testProfileCompletionMilestone_100Percent_ShouldAdd10Points() {
        // Arrange: Criar ranking com 80% de completude
        UserRankingModel ranking = new UserRankingModel();
        ranking.setUserId(testUser.id);
        ranking.setTotalScore(20);
        ranking.setProfileCompletionPercentage(80);
        ranking.setTotalActiveDays(1L);
        ranking.setLastLoginAt(LocalDateTime.now());
        userRankingService.create(ranking);

        // Act: Atualizar para 100% de completude
        UserRankingModel updateData = new UserRankingModel();
        updateData.setProfileCompletionPercentage(100);
        updateData.setHasPhones(true);
        updateData.setTotalPhones(5);
        updateData.setHasWhatsapp(true);
        updateData.setHasTelegram(true);

        UserRankingModel updated = userRankingService.update(ranking.getId(), updateData);

        // Assert: Deve ter 20 (inicial) + 10 (milestone 100%) = 30 pontos
        assertEquals(30, updated.getTotalScore(),
                "Score should be 20 (initial) + 10 (100% milestone) = 30");
        assertEquals(100, updated.getProfileCompletionPercentage(),
                "Profile completion should be 100%");

        // Verificar auditoria
        List<UserRankingAuditModel> audits = auditRepository.findByUserId(testUser.id);
        assertTrue(audits.stream().anyMatch(a -> AuditOperationType.ADD_POINTS.equals(a.getOperation()) &&
                "PROFILE_100_PERCENT".equals(a.getPointsReason()) &&
                a.getPointsAdded() == 10),
                "Audit should contain PROFILE_100_PERCENT entry with +10 points");
    }

    @Test
    @Transactional
    void testProfileCompletionMilestone_BothMilestones_ShouldAdd13Points() {
        // Arrange: Criar ranking com 30% de completude (abaixo de ambos thresholds)
        UserRankingModel ranking = new UserRankingModel();
        ranking.setUserId(testUser.id);
        ranking.setTotalScore(5);
        ranking.setProfileCompletionPercentage(30);
        ranking.setTotalActiveDays(1L);
        ranking.setLastLoginAt(LocalDateTime.now());
        userRankingService.create(ranking);

        // Act: Atualizar diretamente para 100% (cruza AMBOS thresholds)
        UserRankingModel updateData = new UserRankingModel();
        updateData.setProfileCompletionPercentage(100);

        UserRankingModel updated = userRankingService.update(ranking.getId(), updateData);

        // Assert: Deve ter 5 + 3 (50%) + 10 (100%) = 18 pontos
        assertEquals(18, updated.getTotalScore(),
                "Score should be 5 + 3 (50% milestone) + 10 (100% milestone) = 18");

        // Verificar auditoria - deve ter 2 entradas
        List<UserRankingAuditModel> audits = auditRepository.findByUserId(testUser.id);
        long milestone50Count = audits.stream()
                .filter(a -> "PROFILE_50_PERCENT".equals(a.getPointsReason()))
                .count();
        long milestone100Count = audits.stream()
                .filter(a -> "PROFILE_100_PERCENT".equals(a.getPointsReason()))
                .count();

        assertEquals(1, milestone50Count, "Should have exactly 1 PROFILE_50_PERCENT audit entry");
        assertEquals(1, milestone100Count, "Should have exactly 1 PROFILE_100_PERCENT audit entry");
    }

    @Test
    @Transactional
    void testProfileCompletionMilestone_NoMilestone_ShouldNotAddPoints() {
        // Arrange: Criar ranking com 60% de completude
        UserRankingModel ranking = new UserRankingModel();
        ranking.setUserId(testUser.id);
        ranking.setTotalScore(15);
        ranking.setProfileCompletionPercentage(60);
        ranking.setTotalActiveDays(1L);
        ranking.setLastLoginAt(LocalDateTime.now());
        userRankingService.create(ranking);

        // Act: Atualizar para 70% (não cruza nenhum threshold)
        UserRankingModel updateData = new UserRankingModel();
        updateData.setProfileCompletionPercentage(70);

        UserRankingModel updated = userRankingService.update(ranking.getId(), updateData);

        // Assert: Score deve permanecer 15 (sem milestones cruzados)
        assertEquals(15, updated.getTotalScore(),
                "Score should remain 15 (no milestone crossed)");
        assertEquals(70, updated.getProfileCompletionPercentage(),
                "Profile completion should be updated to 70%");

        // Verificar que NÃO há entradas de milestone na auditoria
        List<UserRankingAuditModel> audits = auditRepository.findByUserId(testUser.id);
        long milestoneCount = audits.stream()
                .filter(a -> a.getPointsReason() != null &&
                        (a.getPointsReason().equals("PROFILE_50_PERCENT") ||
                                a.getPointsReason().equals("PROFILE_100_PERCENT")))
                .count();

        assertEquals(0, milestoneCount, "Should have 0 profile milestone audit entries");
    }

    @Test
    @Transactional
    void testProfileCompletionMilestone_DecreasePercentage_ShouldNotRemovePoints() {
        // Arrange: Criar ranking com 100% de completude e 30 pontos
        UserRankingModel ranking = new UserRankingModel();
        ranking.setUserId(testUser.id);
        ranking.setTotalScore(30);
        ranking.setProfileCompletionPercentage(100);
        ranking.setTotalActiveDays(1L);
        ranking.setLastLoginAt(LocalDateTime.now());
        userRankingService.create(ranking);

        // Act: Diminuir para 60% (usuário removeu dados do perfil)
        UserRankingModel updateData = new UserRankingModel();
        updateData.setProfileCompletionPercentage(60);

        UserRankingModel updated = userRankingService.update(ranking.getId(), updateData);

        // Assert: Score deve permanecer 30 (não remove pontos quando diminui
        // percentual)
        assertEquals(30, updated.getTotalScore(),
                "Score should remain 30 (points are not removed when percentage decreases)");
        assertEquals(60, updated.getProfileCompletionPercentage(),
                "Profile completion should be updated to 60%");
    }

    @Test
    @Transactional
    void testProfileCompletionMilestone_IdempotencyAt50Percent_ShouldNotAddDuplicatePoints() {
        // Arrange: Criar ranking com 50% de completude (já no threshold)
        UserRankingModel ranking = new UserRankingModel();
        ranking.setUserId(testUser.id);
        ranking.setTotalScore(13);
        ranking.setProfileCompletionPercentage(50);
        ranking.setTotalActiveDays(1L);
        ranking.setLastLoginAt(LocalDateTime.now());
        userRankingService.create(ranking);

        // Act: Atualizar novamente para 50% (ou 51%, 55%, etc.)
        UserRankingModel updateData = new UserRankingModel();
        updateData.setProfileCompletionPercentage(55);

        UserRankingModel updated = userRankingService.update(ranking.getId(), updateData);

        // Assert: Score deve permanecer 13 (não adiciona pontos duplicados)
        assertEquals(13, updated.getTotalScore(),
                "Score should remain 13 (no duplicate points for staying above threshold)");

        // Verificar que NÃO há novas entradas de milestone
        List<UserRankingAuditModel> audits = auditRepository.findByUserId(testUser.id);
        long milestoneCount = audits.stream()
                .filter(a -> "PROFILE_50_PERCENT".equals(a.getPointsReason()))
                .count();

        assertEquals(0, milestoneCount,
                "Should have 0 new PROFILE_50_PERCENT entries (idempotency)");
    }

    @Test
    @Transactional
    void testProfileCompletionMilestone_MultipleUpdatesWithPhoneFields_ShouldPersistAllFields() {
        // Arrange: Criar ranking inicial
        UserRankingModel ranking = new UserRankingModel();
        ranking.setUserId(testUser.id);
        ranking.setTotalScore(5);
        ranking.setProfileCompletionPercentage(0);
        ranking.setHasPhones(false);
        ranking.setTotalPhones(0);
        ranking.setHasWhatsapp(false);
        ranking.setHasTelegram(false);
        ranking.setTotalActiveDays(1L);
        ranking.setLastLoginAt(LocalDateTime.now());
        userRankingService.create(ranking);

        // Act: Atualizar com dados de telefone e 50% de completude
        UserRankingModel updateData = new UserRankingModel();
        updateData.setProfileCompletionPercentage(50);
        updateData.setHasPhones(true);
        updateData.setTotalPhones(5);
        updateData.setHasWhatsapp(true);
        updateData.setHasTelegram(true);

        UserRankingModel updated = userRankingService.update(ranking.getId(), updateData);

        // Assert: Verificar que TODOS os campos foram salvos corretamente
        assertEquals(8, updated.getTotalScore(), "Score should be 5 + 3 = 8");
        assertEquals(50, updated.getProfileCompletionPercentage());
        assertEquals(true, updated.getHasPhones(), "hasPhones should be true");
        assertEquals(5, updated.getTotalPhones(), "totalPhones should be 5");
        assertEquals(true, updated.getHasWhatsapp(), "hasWhatsapp should be true");
        assertEquals(true, updated.getHasTelegram(), "hasTelegram should be true");

        // Este é o bug original - campos de telefone eram enviados mas não salvos!
        assertNotNull(updated.getHasPhones(), "hasPhones must not be null");
        assertTrue(updated.getHasPhones(), "hasPhones must be true (bug fix verification)");
    }

    @Test
    @Transactional
    void testProfileCompletionMilestone_EdgeCaseExactly50Percent_ShouldTriggerMilestone() {
        // Arrange: Criar ranking com 49% de completude
        UserRankingModel ranking = new UserRankingModel();
        ranking.setUserId(testUser.id);
        ranking.setTotalScore(10);
        ranking.setProfileCompletionPercentage(49);
        ranking.setTotalActiveDays(1L);
        ranking.setLastLoginAt(LocalDateTime.now());
        userRankingService.create(ranking);

        // Act: Atualizar para exatamente 50%
        UserRankingModel updateData = new UserRankingModel();
        updateData.setProfileCompletionPercentage(50);

        UserRankingModel updated = userRankingService.update(ranking.getId(), updateData);

        // Assert: Deve adicionar +3 pontos (threshold inclusive)
        assertEquals(13, updated.getTotalScore(),
                "Score should be 10 + 3 = 13 (threshold is inclusive)");
    }

    @Test
    @Transactional
    void testProfileCompletionMilestone_EdgeCaseExactly100Percent_ShouldTriggerMilestone() {
        // Arrange: Criar ranking com 99% de completude
        UserRankingModel ranking = new UserRankingModel();
        ranking.setUserId(testUser.id);
        ranking.setTotalScore(20);
        ranking.setProfileCompletionPercentage(99);
        ranking.setTotalActiveDays(1L);
        ranking.setLastLoginAt(LocalDateTime.now());
        userRankingService.create(ranking);

        // Act: Atualizar para exatamente 100%
        UserRankingModel updateData = new UserRankingModel();
        updateData.setProfileCompletionPercentage(100);

        UserRankingModel updated = userRankingService.update(ranking.getId(), updateData);

        // Assert: Deve adicionar +10 pontos (threshold inclusive)
        assertEquals(30, updated.getTotalScore(),
                "Score should be 20 + 10 = 30 (threshold is inclusive)");
    }
}
