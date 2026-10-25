package br.com.aguideptbr.features.phone;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.user.UserModel;
import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class PhoneNumberService {

    private final PhoneNumberRepository phoneRepository;
    private final Logger log;

    public PhoneNumberService(PhoneNumberRepository phoneRepository, Logger log) {
        this.phoneRepository = phoneRepository;
        this.log = log;
    }

    // @Inject
    // PhoneNumberRepository phoneRepository;

    // @Inject
    // Logger log;

    private static final Pattern BRAZIL_MOBILE = Pattern.compile("^\\+55([1-9]{2})(9[0-9]{8})$");

    private static final Pattern BRAZIL_LANDLINE = Pattern.compile("^\\+55([1-9]{2})([2-5][0-9]{7})$");

    private static final Pattern PORTUGAL_MOBILE = Pattern.compile("^\\+351(9[1236][0-9]{7})$");

    private static final Pattern PORTUGAL_LANDLINE = Pattern.compile("^\\+351(2[0-9]{8})$");

    private static final Pattern E164_GENERIC = Pattern.compile("^\\+[1-9][0-9]{6,14}$");

    @Transactional
    public PhoneNumberModel create(UUID userId, PhoneNumberModel phone) {
        log.infof("Creating phone for user %s: country=%s, area=%s, number=%s",
                userId, phone.countryCode, phone.areaCode, phone.number);

        UserModel user = UserModel.findById(userId);
        if (user == null) {
            throw new NotFoundException("Usuário não encontrado");
        }

        phone.fullNumber = buildFullNumber(phone.countryCode, phone.areaCode, phone.number);

        validatePhoneNumber(phone.fullNumber, phone.countryCode);

        if (phoneRepository.existsByFullNumber(phone.fullNumber)) {
            throw new BadRequestException("Este número de telefone já está cadastrado");
        }

        phone.user = user;

        if (phoneRepository.countByUser(userId) == 0) {
            phone.isPrimary = true;
            log.infof("First phone for user %s, setting as primary", userId);
        }

        if (Boolean.TRUE.equals(phone.isPrimary)) {
            phoneRepository.removePrimaryFlagFromUser(userId);
        }

        phoneRepository.persist(phone);
        log.infof("Phone created successfully: id=%s, fullNumber=%s", phone.id, phone.fullNumber);

        return phone;
    }

    public PhoneNumberModel findById(UUID id) {
        PhoneNumberModel phone = phoneRepository.findById(id);
        if (phone == null) {
            throw new NotFoundException("Telefone não encontrado");
        }
        return phone;
    }

    public List<PhoneNumberModel> findByUser(UUID userId) {
        return phoneRepository.findByUser(userId);
    }

    public PhoneNumberModel findPrimaryByUser(UUID userId) {
        return phoneRepository.findPrimaryByUser(userId);
    }

    @Transactional
    public PhoneNumberModel update(UUID id, PhoneNumberModel updatedPhone) {
        log.infof("Updating phone %s", id);

        PhoneNumberModel phone = findById(id);

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

        phone.type = updatedPhone.type;
        phone.hasWhatsApp = updatedPhone.hasWhatsApp;
        phone.hasTelegram = updatedPhone.hasTelegram;
        phone.hasSignal = updatedPhone.hasSignal;

        if (Boolean.TRUE.equals(updatedPhone.isPrimary) && !Boolean.TRUE.equals(phone.isPrimary)) {
            phoneRepository.removePrimaryFlagFromUser(phone.user.id);
            phone.isPrimary = true;
        }

        phoneRepository.persist(phone);
        log.infof("Phone updated successfully: id=%s", id);

        return phone;
    }

    @Transactional
    public void setPrimary(UUID userId, UUID phoneId) {
        log.infof("Setting phone %s as primary for user %s", phoneId, userId);

        PhoneNumberModel phone = findById(phoneId);

        if (!phone.user.id.equals(userId)) {
            throw new BadRequestException("Este telefone não pertence ao usuário");
        }

        phoneRepository.removePrimaryFlagFromUser(userId);

        phone.isPrimary = true;
        phoneRepository.persist(phone);

        log.infof("Phone %s set as primary", phoneId);
    }

    @Transactional
    public void markAsVerified(UUID phoneId) {
        log.infof("Marking phone %s as verified", phoneId);

        PhoneNumberModel phone = findById(phoneId);
        phone.isVerified = true;
        phoneRepository.persist(phone);

        log.infof("Phone %s marked as verified", phoneId);
    }

    @Transactional
    public void delete(UUID id) {
        log.infof("Soft deleting phone %s", id);

        PhoneNumberModel phone = findById(id);

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

    private String buildFullNumber(String countryCode, String areaCode, String number) {
        String cleanCountry = countryCode.replaceAll("[^+0-9]", "");
        String cleanArea = areaCode != null ? areaCode.replaceAll("[^0-9]", "") : "";
        String cleanNumber = number.replaceAll("[^0-9]", "");

        if (!cleanCountry.startsWith("+")) {
            cleanCountry = "+" + cleanCountry;
        }

        return cleanCountry + cleanArea + cleanNumber;
    }

    private void validatePhoneNumber(String fullNumber, String countryCode) {
        log.debugf("Validating phone number: %s (country: %s)", fullNumber, countryCode);

        boolean valid = false;
        String errorMessage = "Formato de telefone inválido";

        if ("+55".equals(countryCode)) {
            if (BRAZIL_MOBILE.matcher(fullNumber).matches()) {
                valid = true;
            } else if (BRAZIL_LANDLINE.matcher(fullNumber).matches()) {
                valid = true;
            } else {
                errorMessage = "Formato inválido para telefone brasileiro. " +
                        "Celular: +55 (DD) 9 XXXX-XXXX. Fixo: +55 (DD) XXXX-XXXX";
            }
        } else if ("+351".equals(countryCode)) {
            if (PORTUGAL_MOBILE.matcher(fullNumber).matches()) {
                valid = true;
            } else if (PORTUGAL_LANDLINE.matcher(fullNumber).matches()) {
                valid = true;
            } else {
                errorMessage = "Formato inválido para telefone português. " +
                        "Celular: +351 9X XXX XXXX. Fixo: +351 2XX XXX XXX";
            }
        } else {
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
