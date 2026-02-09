package br.com.aguideptbr.features.phone.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.aguideptbr.features.phone.PhoneNumberModel;

/**
 * DTO de resposta para um número de telefone.
 */
public class PhoneNumberResponse {

    private UUID id;
    private String countryCode;
    private String areaCode;
    private String number;
    private String fullNumber;
    private String formattedNumber;
    private String type;
    private Boolean isPrimary;
    private Boolean isVerified;
    private Boolean hasWhatsApp;
    private Boolean hasTelegram;
    private Boolean hasSignal;
    private List<String> availableMessagingApps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor a partir da entidade
    public PhoneNumberResponse(PhoneNumberModel phone) {
        this.id = phone.id;
        this.countryCode = phone.countryCode;
        this.areaCode = phone.areaCode;
        this.number = phone.number;
        this.fullNumber = phone.fullNumber;
        this.formattedNumber = phone.getFormattedNumber();
        this.type = phone.type;
        this.isPrimary = phone.isPrimary;
        this.isVerified = phone.isVerified;
        this.hasWhatsApp = phone.hasWhatsApp;
        this.hasTelegram = phone.hasTelegram;
        this.hasSignal = phone.hasSignal;
        this.availableMessagingApps = phone.getAvailableMessagingApps();
        this.createdAt = phone.createdAt;
        this.updatedAt = phone.updatedAt;
    }

    // Getters e Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getFullNumber() {
        return fullNumber;
    }

    public void setFullNumber(String fullNumber) {
        this.fullNumber = fullNumber;
    }

    public String getFormattedNumber() {
        return formattedNumber;
    }

    public void setFormattedNumber(String formattedNumber) {
        this.formattedNumber = formattedNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Boolean getHasWhatsApp() {
        return hasWhatsApp;
    }

    public void setHasWhatsApp(Boolean hasWhatsApp) {
        this.hasWhatsApp = hasWhatsApp;
    }

    public Boolean getHasTelegram() {
        return hasTelegram;
    }

    public void setHasTelegram(Boolean hasTelegram) {
        this.hasTelegram = hasTelegram;
    }

    public Boolean getHasSignal() {
        return hasSignal;
    }

    public void setHasSignal(Boolean hasSignal) {
        this.hasSignal = hasSignal;
    }

    public List<String> getAvailableMessagingApps() {
        return availableMessagingApps;
    }

    public void setAvailableMessagingApps(List<String> availableMessagingApps) {
        this.availableMessagingApps = availableMessagingApps;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
