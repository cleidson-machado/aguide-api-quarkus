package br.com.aguideptbr.features.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.aguideptbr.features.phone.PhoneNumberModel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entidade representando um usuário da aplicação.
 *
 * Suporta autenticação local (email/senha) e OAuth2 (futuro).
 */
@Entity
@Table(name = "app_user")
public class UserModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false, length = 100)
    public String name;

    @Column(nullable = false, length = 100)
    public String surname;

    @Column(unique = true, nullable = false, length = 255)
    public String email;

    /**
     * Hash BCrypt da senha do usuário.
     * NUNCA armazene senhas em texto plano!
     * Este campo é WRITE_ONLY (não aparece em respostas JSON).
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", length = 255)
    public String passwordHash;

    /**
     * Role do usuário (ADMIN, MANAGER, CHANNEL_OWNER, PREMIUM_USER, FREE).
     * Armazenado como String no banco, mas manipulado como Enum no código.
     * Usado para controle de acesso com @RolesAllowed.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    public UserRole role = UserRole.FREE;

    /**
     * Data de criação do usuário.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    /**
     * Data da última atualização do usuário.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    /**
     * Data de exclusão lógica do usuário (soft delete).
     * Null significa que o usuário está ativo.
     */
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    // ========== Campos para OAuth2 (Fase 2 - Futuro) ==========

    /**
     * Provedor OAuth2 (google, microsoft, linkedin).
     * Null para autenticação local.
     */
    @Column(name = "oauth_provider", length = 50)
    public String oauthProvider;

    /**
     * ID do usuário no provedor OAuth2.
     * Null para autenticação local.
     */
    @Column(name = "oauth_id", length = 255)
    public String oauthId;

    /**
     * YouTube User ID ou Channel ID (formato: UCxxxxx ou UXxxxxx).
     * Capturado durante login OAuth com Google.
     * Null se o usuário não tiver canal YouTube.
     */
    @Column(name = "youtube_user_id", length = 255)
    public String youtubeUserId;

    /**
     * YouTube Channel ID (formato: UCxxxxx).
     * Capturado durante login OAuth com Google.
     * Null se o usuário não tiver canal YouTube.
     */
    @Column(name = "youtube_channel_id", length = 255)
    public String youtubeChannelId;

    /**
     * Título do canal YouTube do usuário.
     * Null se o usuário não tiver canal YouTube.
     */
    @Column(name = "youtube_channel_title", length = 255)
    public String youtubeChannelTitle;

    /**
     * Telefones do usuário.
     * Um usuário pode ter múltiplos telefones (Brasil, Portugal, trabalho, etc).
     * IMPORTANTE: Não serializado no JSON por padrão (@JsonIgnore).
     * Use o endpoint /api/v1/users/{userId}/phones para buscar telefones.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PhoneNumberModel> phoneNumbers = new ArrayList<>();

    // ========== Métodos de Consulta ==========

    /**
     * Busca um usuário ativo pelo email (ignora deletados).
     *
     * @param email Email do usuário
     * @return UserModel encontrado ou null
     */
    public static UserModel findByEmail(String email) {
        return find("email = ?1 and deletedAt is null", email).firstResult();
    }

    /**
     * Busca todos os usuários ativos (não deletados).
     *
     * @return Lista de usuários ativos
     */
    public static List<UserModel> findAllActive() {
        return list("deletedAt is null");
    }

    /**
     * Busca um usuário ativo por ID.
     *
     * @param id ID do usuário
     * @return UserModel encontrado ou null
     */
    public static UserModel findByIdActive(UUID id) {
        return find("id = ?1 and deletedAt is null", id).firstResult();
    }

    /**
     * Busca um usuário pelo provedor OAuth2 e ID externo.
     *
     * @param provider Provedor OAuth2 (google, microsoft, linkedin)
     * @param oauthId  ID do usuário no provedor
     * @return UserModel encontrado ou null
     */
    public static UserModel findByOAuth(String provider, String oauthId) {
        return find("oauthProvider = ?1 and oauthId = ?2", provider, oauthId).firstResult();
    }

    // ========== Métodos Auxiliares ==========

    /**
     * Retorna o nome completo do usuário.
     *
     * @return Nome completo (nome + sobrenome)
     */
    public String getFullName() {
        return name + " " + surname;
    }

    /**
     * Verifica se o usuário usa autenticação OAuth2.
     *
     * @return true se o usuário se autenticou via OAuth2
     */
    public boolean isOAuthUser() {
        return oauthProvider != null && oauthId != null;
    }

    /**
     * Verifica se o usuário usa autenticação local (email/senha).
     *
     * @return true se o usuário usa autenticação local
     */
    public boolean isLocalUser() {
        return !isOAuthUser();
    }

    // ========== Métodos de Soft Delete ==========

    /**
     * Marca o usuário como deletado (soft delete).
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restaura um usuário deletado (undelete).
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * Verifica se o usuário está deletado.
     *
     * @return true se o usuário foi deletado
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Verifica se o usuário está ativo.
     *
     * @return true se o usuário não foi deletado
     */
    public boolean isActive() {
        return this.deletedAt == null;
    }

    // ========== Métodos de Telefone ==========

    /**
     * Retorna o telefone principal do usuário.
     *
     * @return Telefone principal ou null
     */
    @JsonIgnore
    public PhoneNumberModel getPrimaryPhone() {
        return phoneNumbers.stream()
                .filter(p -> Boolean.TRUE.equals(p.isPrimary))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retorna telefones com WhatsApp ativo.
     *
     * @return Lista de telefones com WhatsApp
     */
    @JsonIgnore
    public List<PhoneNumberModel> getWhatsAppPhones() {
        return phoneNumbers.stream()
                .filter(p -> Boolean.TRUE.equals(p.hasWhatsApp))
                .toList();
    }

    /**
     * Retorna telefones com Telegram ativo.
     *
     * @return Lista de telefones com Telegram
     */
    @JsonIgnore
    public List<PhoneNumberModel> getTelegramPhones() {
        return phoneNumbers.stream()
                .filter(p -> Boolean.TRUE.equals(p.hasTelegram))
                .toList();
    }
}
