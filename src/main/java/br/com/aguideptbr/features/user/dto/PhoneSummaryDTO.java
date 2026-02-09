package br.com.aguideptbr.features.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.aguideptbr.features.phone.PhoneNumberModel;

/**
 * DTO simplificado de telefone para incluir em respostas de usuário.
 * Contém apenas as informações essenciais.
 */
public class PhoneSummaryDTO {

    private UUID id;
    private String fullNumber;
    private String formattedNumber;
    private String type;
    private Boolean isPrimary;
    private Boolean isVerified;
    private Boolean hasWhatsApp;
    private Boolean hasTelegram;
    private Boolean hasSignal;
    private LocalDateTime createdAt;

    public PhoneSummaryDTO(PhoneNumberModel phone) {
        this.id = phone.id;
        this.fullNumber = phone.fullNumber;
        this.formattedNumber = phone.getFormattedNumber();
        this.type = phone.type;
        this.isPrimary = phone.isPrimary;
        this.isVerified = phone.isVerified;
        this.hasWhatsApp = phone.hasWhatsApp;
        this.hasTelegram = phone.hasTelegram;
        this.hasSignal = phone.hasSignal;
        this.createdAt = phone.createdAt;
    }

    // Getters e Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
