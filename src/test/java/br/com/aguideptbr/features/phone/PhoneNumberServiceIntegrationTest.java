package br.com.aguideptbr.features.phone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.aguideptbr.features.user.UserModel;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

/**
 * Testes de integração para PhoneNumberService.
 *
 * Usa banco de dados real (quarkus_test) para validar fluxo completo:
 * Service → Repository → Database
 */
@QuarkusTest
class PhoneNumberServiceIntegrationTest {

    @Inject
    PhoneNumberService phoneService;

    @Inject
    PhoneNumberRepository phoneRepository;

    private UserModel testUser;

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpar dados de testes anteriores
        phoneRepository.deleteAll();
        UserModel.deleteAll();

        // Criar usuário de teste
        testUser = new UserModel();
        testUser.email = "test@phonetest.com";
        testUser.name = "Phone";
        testUser.surname = "Test";
        testUser.passwordHash = "hash123";
        testUser.persist();
    }

    // ========== Testes de Validação de Telefone Brasileiro ==========

    @Test
    @DisplayName("Deve aceitar celular brasileiro válido")
    @Transactional
    void testValidBrazilianMobile() {
        // Arrange
        PhoneNumberModel phone = createPhone("+55", "67", "984073221", "MOBILE");

        // Act
        PhoneNumberModel result = phoneService.create(testUser.id, phone);

        // Assert
        assertNotNull(result);
        assertNotNull(result.id);
        // Número brasileiro celular: +55 + 67 + 984073221 = +5567984073221 (14 dígitos)
        assertEquals("+5567984073221", result.fullNumber);
        assertEquals(testUser.id, result.user.id);
        assertTrue(result.isPrimary); // Primeiro telefone é automático principal
    }

    @Test
    @DisplayName("Deve aceitar fixo brasileiro válido")
    @Transactional
    void testValidBrazilianLandline() {
        // Arrange
        PhoneNumberModel phone = createPhone("+55", "67", "33334444", "LANDLINE");

        // Act
        PhoneNumberModel result = phoneService.create(testUser.id, phone);

        // Assert
        assertNotNull(result);
        assertEquals("+556733334444", result.fullNumber);
    }

    @Test
    @DisplayName("Deve rejeitar celular brasileiro sem DDD")
    @Transactional
    void testInvalidBrazilianMobileWithoutAreaCode() {
        // Arrange
        PhoneNumberModel phone = createPhone("+55", null, "984073221", "MOBILE");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            phoneService.create(testUser.id, phone);
        });
    }

    @Test
    @DisplayName("Deve rejeitar celular brasileiro sem o 9 no início")
    @Transactional
    void testInvalidBrazilianMobileWithoutNine() {
        // Arrange
        PhoneNumberModel phone = createPhone("+55", "67", "84073221", "MOBILE");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            phoneService.create(testUser.id, phone);
        });
    }

    // ========== Testes de Validação de Telefone Português ==========

    @Test
    @DisplayName("Deve aceitar celular português válido")
    @Transactional
    void testValidPortugueseMobile() {
        // Arrange
        PhoneNumberModel phone = createPhone("+351", null, "912345678", "MOBILE");

        // Act
        PhoneNumberModel result = phoneService.create(testUser.id, phone);

        // Assert
        assertNotNull(result);
        assertEquals("+351912345678", result.fullNumber);
    }

    @Test
    @DisplayName("Deve rejeitar celular português com prefixo inválido")
    @Transactional
    void testInvalidPortugueseMobilePrefix() {
        // Arrange
        PhoneNumberModel phone = createPhone("+351", null, "812345678", "MOBILE");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            phoneService.create(testUser.id, phone);
        });
    }

    // ========== Testes de Validação Genérica (Outros Países) ==========

    @Test
    @DisplayName("Deve aceitar número E164 genérico (outros países)")
    @Transactional
    void testValidE164Generic() {
        // Arrange
        PhoneNumberModel phone = createPhone("+1", null, "4155552671", "MOBILE"); // EUA

        // Act
        PhoneNumberModel result = phoneService.create(testUser.id, phone);

        // Assert
        assertNotNull(result);
        assertEquals("+14155552671", result.fullNumber);
    }

    // ========== Testes de Lógica de Negócio ==========

    @Test
    @DisplayName("Deve marcar primeiro telefone como principal automaticamente")
    @Transactional
    void testFirstPhoneAutomaticallyPrimary() {
        // Arrange
        PhoneNumberModel phone = createPhone("+55", "67", "984073221", "MOBILE");
        phone.isPrimary = false; // Explicitamente não-principal

        // Act
        PhoneNumberModel result = phoneService.create(testUser.id, phone);

        // Assert
        assertTrue(result.isPrimary); // Deve ser marcado como principal
    }

    @Test
    @DisplayName("Deve rejeitar número duplicado")
    @Transactional
    void testRejectDuplicateNumber() {
        // Arrange
        PhoneNumberModel phone1 = createPhone("+55", "67", "984073221", "MOBILE");
        phoneService.create(testUser.id, phone1);

        PhoneNumberModel phone2 = createPhone("+55", "67", "984073221", "MOBILE");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            phoneService.create(testUser.id, phone2);
        });
    }

    @Test
    @DisplayName("Deve remover flag principal de outros telefones ao definir novo principal")
    @Transactional
    void testRemovePrimaryFlagWhenSettingNewPrimary() {
        // Arrange
        PhoneNumberModel phone1 = createPhone("+55", "67", "984073221", "MOBILE");
        PhoneNumberModel createdPhone1 = phoneService.create(testUser.id, phone1);
        assertTrue(createdPhone1.isPrimary); // Primeiro é principal

        PhoneNumberModel phone2 = createPhone("+55", "67", "987654321", "MOBILE");
        PhoneNumberModel createdPhone2 = phoneService.create(testUser.id, phone2);
        assertFalse(createdPhone2.isPrimary); // Segundo não é principal

        // Act
        phoneService.setPrimary(testUser.id, createdPhone2.id);

        // Forçar flush do Hibernate para persistir mudanças no banco
        phoneRepository.flush();

        // Limpar cache do EntityManager para buscar dados frescos do banco
        PhoneNumberModel.getEntityManager().clear();

        // Assert
        PhoneNumberModel updatedPhone1 = phoneRepository.findById(createdPhone1.id);
        PhoneNumberModel updatedPhone2 = phoneRepository.findById(createdPhone2.id);

        assertFalse(updatedPhone1.isPrimary); // Não é mais principal
        assertTrue(updatedPhone2.isPrimary); // Agora é principal
    }

    @Test
    @DisplayName("Deve rejeitar tentativa de definir telefone de outro usuário como principal")
    @Transactional
    void testCannotSetPrimaryForOtherUser() {
        // Arrange
        UserModel otherUser = new UserModel();
        otherUser.email = "other@test.com";
        otherUser.name = "Other";
        otherUser.surname = "User";
        otherUser.passwordHash = "hash456";
        otherUser.persist();

        PhoneNumberModel phone = createPhone("+55", "67", "984073221", "MOBILE");
        PhoneNumberModel createdPhone = phoneService.create(otherUser.id, phone);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            phoneService.setPrimary(testUser.id, createdPhone.id); // Tentando com userId diferente
        });
    }

    @Test
    @DisplayName("Deve buscar telefones por usuário")
    @Transactional
    void testFindByUser() {
        // Arrange
        PhoneNumberModel phone1 = createPhone("+55", "67", "984073221", "MOBILE");
        PhoneNumberModel phone2 = createPhone("+55", "67", "987654321", "MOBILE");
        phoneService.create(testUser.id, phone1);
        phoneService.create(testUser.id, phone2);

        // Act
        List<PhoneNumberModel> phones = phoneService.findByUser(testUser.id);

        // Assert
        assertEquals(2, phones.size());
    }

    @Test
    @DisplayName("Deve soft delete telefone")
    @Transactional
    void testSoftDelete() {
        // Arrange
        PhoneNumberModel phone = createPhone("+55", "67", "984073221", "MOBILE");
        PhoneNumberModel created = phoneService.create(testUser.id, phone);

        // Act
        phoneService.delete(created.id);

        // Assert
        PhoneNumberModel deleted = phoneRepository.findById(created.id);
        assertNotNull(deleted);
        assertNotNull(deleted.deletedAt);
        assertTrue(deleted.isDeleted());
        assertFalse(deleted.isActive());
    }

    @Test
    @DisplayName("Deve restaurar telefone deletado")
    @Transactional
    void testRestore() {
        // Arrange
        PhoneNumberModel phone = createPhone("+55", "67", "984073221", "MOBILE");
        PhoneNumberModel created = phoneService.create(testUser.id, phone);
        phoneService.delete(created.id);

        // Act
        phoneService.restore(created.id);

        // Assert
        PhoneNumberModel restored = phoneRepository.findById(created.id);
        assertNotNull(restored);
        assertTrue(restored.isActive());
        assertFalse(restored.isDeleted());
    }

    @Test
    @DisplayName("Deve rejeitar restauração de telefone inexistente")
    @Transactional
    void testRestoreNonExistent() {
        // Arrange
        java.util.UUID fakeId = java.util.UUID.randomUUID();

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            phoneService.restore(fakeId);
        });
    }

    // ========== Métodos Auxiliares ==========

    private PhoneNumberModel createPhone(String countryCode, String areaCode, String number, String type) {
        PhoneNumberModel phone = new PhoneNumberModel();
        phone.countryCode = countryCode;
        phone.areaCode = areaCode;
        phone.number = number;
        phone.type = type;
        phone.isPrimary = false;
        phone.hasWhatsApp = false;
        phone.hasTelegram = false;
        phone.hasSignal = false;
        return phone;
    }
}
