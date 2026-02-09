package br.com.aguideptbr.features.phone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para criar/atualizar um número de telefone.
 */
public class PhoneNumberRequest {

    @NotBlank(message = "Código do país é obrigatório")
    @Pattern(regexp = "^\\+[0-9]{1,3}$", message = "Código do país deve estar no formato +XX")
    private String countryCode;

    @Size(max = 5, message = "Código de área deve ter no máximo 5 caracteres")
    private String areaCode;

    @NotBlank(message = "Número é obrigatório")
    @Size(min = 7, max = 20, message = "Número deve ter entre 7 e 20 dígitos")
    private String number;

    @NotBlank(message = "Tipo é obrigatório")
    @Pattern(regexp = "^(MOBILE|LANDLINE)$", message = "Tipo deve ser MOBILE ou LANDLINE")
    private String type;

    private Boolean isPrimary = false;
    private Boolean hasWhatsApp = false;
    private Boolean hasTelegram = false;
    private Boolean hasSignal = false;

    // Getters e Setters

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
}
