package br.com.aguideptbr.features.phone;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.user.UserModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

/**
 * Service para lógica de negócio relacionada a telefones.
 *
 * Responsável por:
 * - Validação de formatos de telefone por país (Brasil, Portugal, outros)
 * - Construção de número completo no formato E.164
 * - Gerenciamento de telefone principal
 * - CRUD de telefones
 */
@ApplicationScoped
public class PhoneNumberService {

    @Inject
    PhoneNumberRepository phoneRepository;

    @Inject
    Logger log;

    // ========== Regex de Validação ==========

    /**
     * Validação de celular brasileiro.
     * Formato: +55 (DD) 9 XXXX-XXXX
     * - DDI: +55
     * - DDD: 11-99 (2 dígitos)
     * - Número: 9 + 8 dígitos
     */
    private static final Pattern BRAZIL_MOBILE = Pattern.compile("^\\+55([1-9]{2})(9[0-9]{8})$");

    /**
     * Validação de fixo brasileiro.
     * Formato: +55 (DD) XXXX-XXXX
     * - DDI: +55
     * - DDD: 11-99 (2 dígitos)
     * - Número: 8 dígitos
     */
    private static final Pattern BRAZIL_LANDLINE = Pattern.compile("^\\+55([1-9]{2})([2-5][0-9]{7})$");

    /**
     * Validação de celular português.
     * Formato: +351 9[1236] XXXXXXX
     * - DDI: +351
     * - Número: 9 dígitos começando com 91, 92, 93, 96
     */
    private static final Pattern PORTUGAL_MOBILE = Pattern.compile("^\\+351(9[1236][0-9]{7})$");

    /**
     * Validação de fixo português.
     * Formato: +351 2[0-9]{8}
     * - DDI: +351
     * - Número: 9 dígitos começando com 2
     */
    private static final Pattern PORTUGAL_LANDLINE = Pattern.compile("^\\+351(2[0-9]{8})$");

    /**
     * Validação genérica formato E.164.
     * - DDI: + seguido de 1-3 dígitos
     * - Número: 7-15 dígitos
     */
    private static final Pattern E164_GENERIC = Pattern.compile("^\\+[1-9][0-9]{6,14}$");

    // ========== Métodos Públicos ==========

    /**
     * Cria um novo telefone para um usuário.
     *
     * @param userId ID do usuário
     * @param phone  Dados do telefone
     * @return Telefone criado
     * @throws BadRequestException se dados inválidos
     * @throws NotFoundException   se usuário não existe
     */
    @Transactional
    public PhoneNumberModel create(UUID userId, PhoneNumberModel phone) {
        log.infof("Creating phone for user %s: country=%s, area=%s, number=%s",
                userId, phone.countryCode, phone.areaCode, phone.number);

        // Validar usuário existe
        UserModel user = UserModel.findById(userId);
        if (user == null) {
            throw new NotFoundException("Usuário não encontrado");
        }

        // Construir número completo (E.164)
        phone.fullNumber = buildFullNumber(phone.countryCode, phone.areaCode, phone.number);

        // Validar formato do número
        validatePhoneNumber(phone.fullNumber, phone.countryCode);

        // Verificar se número já existe
        if (phoneRepository.existsByFullNumber(phone.fullNumber)) {
            throw new BadRequestException("Este número de telefone já está cadastrado");
        }

        // Associar usuário
        phone.user = user;

        // Se for o primeiro telefone, tornar principal automaticamente
        if (phoneRepository.countByUser(userId) == 0) {
            phone.isPrimary = true;
            log.infof("First phone for user %s, setting as primary", userId);
        }

        // Se marcado como principal, remover flag dos outros telefones
        if (Boolean.TRUE.equals(phone.isPrimary)) {
            phoneRepository.removePrimaryFlagFromUser(userId);
        }

        phoneRepository.persist(phone);
        log.infof("Phone created successfully: id=%s, fullNumber=%s", phone.id, phone.fullNumber);

        return phone;
    }

    /**
     * Busca um telefone por ID.
     *
     * @param id ID do telefone
     * @return Telefone encontrado
     * @throws NotFoundException se não encontrado
     */
    public PhoneNumberModel findById(UUID id) {
        PhoneNumberModel phone = phoneRepository.findById(id);
        if (phone == null) {
            throw new NotFoundException("Telefone não encontrado");
        }
        return phone;
    }

    /**
     * Busca todos os telefones de um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de telefones
     */
    public List<PhoneNumberModel> findByUser(UUID userId) {
        return phoneRepository.findByUser(userId);
    }

    /**
     * Busca o telefone principal de um usuário.
     *
     * @param userId ID do usuário
     * @return Telefone principal ou null
     */
    public PhoneNumberModel findPrimaryByUser(UUID userId) {
        return phoneRepository.findPrimaryByUser(userId);
    }

    /**
     * Atualiza um telefone existente.
     *
     * @param id           ID do telefone
     * @param updatedPhone Dados atualizados
     * @return Telefone atualizado
     * @throws NotFoundException   se não encontrado
     * @throws BadRequestException se dados inválidos
     */
    @Transactional
    public PhoneNumberModel update(UUID id, PhoneNumberModel updatedPhone) {
        log.infof("Updating phone %s", id);

        PhoneNumberModel phone = findById(id);

        // Se mudou o número, validar e verificar duplicação
        boolean numberChanged = !phone.countryCode.equals(updatedPhone.countryCode)
                || !phone.number.equals(updatedPhone.number)
                || (phone.areaCode != null && !phone.areaCode.equals(updatedPhone.areaCode));

        if (numberChanged) {
            String newFullNumber = buildFullNumber(
                    updatedPhone.countryCode,
                    updatedPhone.areaCode,
                    updatedPhone.number);

            validatePhoneNumber(newFullNumber, updatedPhone.countryCode);

            if (!phone.fullNumber.equals(newFullNumber) && phoneRepository.existsByFullNumber(newFullNumber)) {
                throw new BadRequestException("Este número de telefone já está cadastrado");
            }

            phone.countryCode = updatedPhone.countryCode;
            phone.areaCode = updatedPhone.areaCode;
            phone.number = updatedPhone.number;
            phone.fullNumber = newFullNumber;
        }

        // Atualizar outros campos
        phone.type = updatedPhone.type;
        phone.hasWhatsApp = updatedPhone.hasWhatsApp;
        phone.hasTelegram = updatedPhone.hasTelegram;
        phone.hasSignal = updatedPhone.hasSignal;

        // Se marcado como principal, remover flag dos outros telefones
        if (Boolean.TRUE.equals(updatedPhone.isPrimary) && !Boolean.TRUE.equals(phone.isPrimary)) {
            phoneRepository.removePrimaryFlagFromUser(phone.user.id);
            phone.isPrimary = true;
        }

        phoneRepository.persist(phone);
        log.infof("Phone updated successfully: id=%s", id);

        return phone;
    }

    /**
     * Define um telefone como principal.
     *
     * @param userId  ID do usuário
     * @param phoneId ID do telefone
     * @throws NotFoundException se usuário ou telefone não encontrado
     */
    @Transactional
    public void setPrimary(UUID userId, UUID phoneId) {
        log.infof("Setting phone %s as primary for user %s", phoneId, userId);

        PhoneNumberModel phone = findById(phoneId);

        // Verificar se o telefone pertence ao usuário
        if (!phone.user.id.equals(userId)) {
            throw new BadRequestException("Este telefone não pertence ao usuário");
        }

        // Remover flag de todos os telefones do usuário
        phoneRepository.removePrimaryFlagFromUser(userId);

        // Marcar este como principal
        phone.isPrimary = true;
        phoneRepository.persist(phone);

        log.infof("Phone %s set as primary", phoneId);
    }

    /**
     * Marca um telefone como verificado via SMS.
     *
     * @param phoneId ID do telefone
     */
    @Transactional
    public void markAsVerified(UUID phoneId) {
        log.infof("Marking phone %s as verified", phoneId);

        PhoneNumberModel phone = findById(phoneId);
        phone.isVerified = true;
        phoneRepository.persist(phone);

        log.infof("Phone %s marked as verified", phoneId);
    }

    /**
     * Deleta um telefone.
     *
     * @param id ID do telefone
     * @throws NotFoundException   se não encontrado
     * @throws BadRequestException se for o único telefone principal
     */
    @Transactional
    public void delete(UUID id) {
        log.infof("Soft deleting phone %s", id);

        PhoneNumberModel phone = findById(id);

        // Se for o telefone principal e houver outros telefones, promover outro
        if (Boolean.TRUE.equals(phone.isPrimary)) {
            List<PhoneNumberModel> otherPhones = phoneRepository.findByUser(phone.user.id)
                    .stream()
                    .filter(p -> !p.id.equals(id))
                    .toList();

            if (!otherPhones.isEmpty()) {
                PhoneNumberModel newPrimary = otherPhones.get(0);
                newPrimary.isPrimary = true;
                phoneRepository.persist(newPrimary);
                log.infof("Promoted phone %s to primary", newPrimary.id);
            }
        }

        phone.softDelete();
        phoneRepository.persist(phone);
        log.infof("Phone soft deleted successfully: id=%s", id);
    }

    /**
     * Restaura um telefone deletado (soft delete).
     *
     * @param id ID do telefone
     * @throws NotFoundException se não encontrado
     */
    @Transactional
    public void restore(UUID id) {
        log.infof("Restoring phone %s", id);

        PhoneNumberModel phone = phoneRepository.findById(id);
        if (phone == null) {
            throw new NotFoundException("Telefone não encontrado");
        }

        phone.restore();
        phoneRepository.persist(phone);
        log.infof("Phone restored successfully: id=%s", id);
    }

    // ========== Métodos Privados de Validação ==========

    /**
     * Constrói o número completo no formato E.164.
     *
     * @param countryCode Código do país (ex: "+55")
     * @param areaCode    Código de área (ex: "67") - pode ser null
     * @param number      Número (ex: "984073221")
     * @return Número completo (ex: "+556798407322")
     */
    private String buildFullNumber(String countryCode, String areaCode, String number) {
        // Remover espaços e caracteres especiais
        String cleanCountry = countryCode.replaceAll("[^+0-9]", "");
        String cleanArea = areaCode != null ? areaCode.replaceAll("[^0-9]", "") : "";
        String cleanNumber = number.replaceAll("[^0-9]", "");

        // Garantir que countryCode começa com +
        if (!cleanCountry.startsWith("+")) {
            cleanCountry = "+" + cleanCountry;
        }

        return cleanCountry + cleanArea + cleanNumber;
    }

    /**
     * Valida o formato de um número de telefone baseado no país.
     *
     * @param fullNumber  Número completo (E.164)
     * @param countryCode Código do país
     * @throws BadRequestException se formato inválido
     */
    private void validatePhoneNumber(String fullNumber, String countryCode) {
        log.debugf("Validating phone number: %s (country: %s)", fullNumber, countryCode);

        boolean valid = false;
        String errorMessage = "Formato de telefone inválido";

        if ("+55".equals(countryCode)) {
            // Brasil
            if (BRAZIL_MOBILE.matcher(fullNumber).matches()) {
                valid = true;
            } else if (BRAZIL_LANDLINE.matcher(fullNumber).matches()) {
                valid = true;
            } else {
                errorMessage = "Formato inválido para telefone brasileiro. " +
                        "Celular: +55 (DD) 9 XXXX-XXXX. Fixo: +55 (DD) XXXX-XXXX";
            }
        } else if ("+351".equals(countryCode)) {
            // Portugal
            if (PORTUGAL_MOBILE.matcher(fullNumber).matches()) {
                valid = true;
            } else if (PORTUGAL_LANDLINE.matcher(fullNumber).matches()) {
                valid = true;
            } else {
                errorMessage = "Formato inválido para telefone português. " +
                        "Celular: +351 9X XXX XXXX. Fixo: +351 2XX XXX XXX";
            }
        } else {
            // Outros países - validação genérica E.164
            if (E164_GENERIC.matcher(fullNumber).matches()) {
                valid = true;
            } else {
                errorMessage = "Formato inválido. Use formato internacional E.164: +[código do país][número]";
            }
        }

        if (!valid) {
            log.warnf("Invalid phone number format: %s", fullNumber);
            throw new BadRequestException(errorMessage);
        }

        log.debugf("Phone number validated successfully: %s", fullNumber);
    }
}
