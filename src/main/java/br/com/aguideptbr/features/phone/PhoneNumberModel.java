package br.com.aguideptbr.features.phone;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.aguideptbr.features.user.UserModel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidade representando um número de telefone de um usuário.
 *
 * Suporta telefones internacionais no formato E.164 e múltiplos apps de
 * mensagem
 * (WhatsApp, Telegram, Signal).
 *
 * Exemplos:
 * - Brasil: +55 67 9 8407-3221 → +556798407322
 * - Portugal: +351 912 345 678 → +351912345678
 */
@Entity
@Table(name = "phone_numbers")
public class PhoneNumberModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * Usuário dono deste telefone.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public UserModel user;

    /**
     * Código do país (DDI).
     * Exemplos: "+55" (Brasil), "+351" (Portugal), "+1" (EUA/Canadá)
     */
    @Column(name = "country_code", nullable = false, length = 5)
    public String countryCode;

    /**
     * Código de área regional (DDD).
     * Obrigatório apenas para Brasil.
     * Exemplos: "67" (Mato Grosso do Sul), "11" (São Paulo)
     */
    @Column(name = "area_code", length = 5)
    public String areaCode;

    /**
     * Número do telefone sem formatação (apenas dígitos).
     * Exemplos: "984073221", "912345678"
     */
    @Column(nullable = false, length = 20)
    public String number;

    /**
     * Número completo no formato E.164 (internacional).
     * Este é o formato usado por WhatsApp, Telegram, Signal.
     * Exemplos: "+556798407322", "+351912345678"
     */
    @Column(name = "full_number", nullable = false, unique = true, length = 30)
    public String fullNumber;

    /**
     * Tipo de telefone.
     * Valores: "MOBILE" (celular), "LANDLINE" (fixo)
     */
    @Column(length = 20, nullable = false)
    public String type = "MOBILE";

    /**
     * Indica se este é o telefone principal do usuário.
     * Apenas um telefone por usuário pode ser primary.
     */
    @Column(name = "is_primary")
    public Boolean isPrimary = false;

    /**
     * Indica se o telefone foi verificado via SMS (2FA futuro).
     */
    @Column(name = "is_verified")
    public Boolean isVerified = false;

    /**
     * Indica se este número tem WhatsApp ativo.
     */
    @Column(name = "has_whatsapp")
    public Boolean hasWhatsApp = false;

    /**
     * Indica se este número tem Telegram ativo.
     */
    @Column(name = "has_telegram")
    public Boolean hasTelegram = false;

    /**
     * Indica se este número tem Signal ativo.
     */
    @Column(name = "has_signal")
    public Boolean hasSignal = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    /**
     * Data de exclusão lógica do telefone (soft delete).
     * Null significa que o telefone está ativo.
     */
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    // ========== Métodos de Consulta ==========

    /**
     * Busca um telefone pelo número completo (E.164).
     *
     * @param fullNumber Número completo (ex: "+556798407322")
     * @return PhoneNumberModel encontrado ou null
     */
    public static PhoneNumberModel findByFullNumber(String fullNumber) {
        return find("fullNumber = ?1 and deletedAt is null", fullNumber).firstResult();
    }

    /**
     * Busca todos os telefones de um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de telefones
     */
    public static List<PhoneNumberModel> findByUser(UUID userId) {
        return list("user.id = ?1 and deletedAt is null", userId);
    }

    /**
     * Busca o telefone principal de um usuário.
     *
     * @param userId ID do usuário
     * @return Telefone principal ou null
     */
    public static PhoneNumberModel findPrimaryByUser(UUID userId) {
        return find("user.id = ?1 and isPrimary = true and deletedAt is null", userId).firstResult();
    }

    /**
     * Busca telefones com WhatsApp ativo de um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de telefones com WhatsApp
     */
    public static List<PhoneNumberModel> findWhatsAppByUser(UUID userId) {
        return list("user.id = ?1 and hasWhatsApp = true and deletedAt is null", userId);
    }

    /**
     * Busca telefones com Telegram ativo de um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de telefones com Telegram
     */
    public static List<PhoneNumberModel> findTelegramByUser(UUID userId) {
        return list("user.id = ?1 and hasTelegram = true and deletedAt is null", userId);
    }

    // ========== Métodos Auxiliares ==========

    /**
     * Retorna uma representação formatada do telefone.
     *
     * Exemplos:
     * - Brasil: "+55 (67) 9 8407-3221"
     * - Portugal: "+351 912 345 678"
     *
     * @return Telefone formatado
     */
    public String getFormattedNumber() {
        if ("+55".equals(countryCode) && areaCode != null) {
            // Formato Brasil: +55 (67) 9 8407-3221
            if (number.length() == 9) {
                return String.format("%s (%s) %s %s-%s",
                        countryCode, areaCode,
                        number.substring(0, 1),
                        number.substring(1, 5),
                        number.substring(5));
            }
        } else if ("+351".equals(countryCode)) {
            // Formato Portugal: +351 912 345 678
            if (number.length() == 9) {
                return String.format("%s %s %s %s",
                        countryCode,
                        number.substring(0, 3),
                        number.substring(3, 6),
                        number.substring(6));
            }
        }

        // Formato genérico
        return fullNumber;
    }

    /**
     * Verifica se o telefone tem algum app de mensagem configurado.
     *
     * @return true se tem WhatsApp, Telegram ou Signal
     */
    public boolean hasMessagingApp() {
        return hasWhatsApp || hasTelegram || hasSignal;
    }

    /**
     * Retorna lista de apps de mensagem disponíveis neste número.
     *
     * @return Lista de apps (ex: ["WhatsApp", "Telegram"])
     */
    public List<String> getAvailableMessagingApps() {
        return List.of(
                hasWhatsApp ? "WhatsApp" : null,
                hasTelegram ? "Telegram" : null,
                hasSignal ? "Signal" : null).stream().filter(app -> app != null).toList();
    }

    /**
     * Verifica se é um telefone celular.
     *
     * @return true se type == "MOBILE"
     */
    public boolean isMobile() {
        return "MOBILE".equals(type);
    }

    /**
     * Verifica se é um telefone fixo.
     *
     * @return true se type == "LANDLINE"
     */
    public boolean isLandline() {
        return "LANDLINE".equals(type);
    }

    // ========== Métodos de Soft Delete ==========

    /**
     * Marca o telefone como deletado (soft delete).
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restaura um telefone deletado (undelete).
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * Verifica se o telefone está deletado.
     *
     * @return true se o telefone foi deletado
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Verifica se o telefone está ativo.
     *
     * @return true se o telefone não foi deletado
     */
    public boolean isActive() {
        return this.deletedAt == null;
    }
}
