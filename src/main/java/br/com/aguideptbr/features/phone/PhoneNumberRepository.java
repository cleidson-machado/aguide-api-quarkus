package br.com.aguideptbr.features.phone;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository para operações de banco de dados com telefones.
 * Especifica UUID como tipo do ID (não o Long padrão).
 */
@ApplicationScoped
public class PhoneNumberRepository implements PanacheRepositoryBase<PhoneNumberModel, UUID> {

    /**
     * Busca um telefone pelo número completo (E.164).
     *
     * @param fullNumber Número completo (ex: "+556798407322")
     * @return PhoneNumberModel encontrado ou null
     */
    public PhoneNumberModel findByFullNumber(String fullNumber) {
        return find("fullNumber", fullNumber).firstResult();
    }

    /**
     * Busca todos os telefones de um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de telefones
     */
    public List<PhoneNumberModel> findByUser(UUID userId) {
        return list("user.id = ?1 and deletedAt is null", userId);
    }

    /**
     * Busca o telefone principal de um usuário.
     *
     * @param userId ID do usuário
     * @return Telefone principal ou null
     */
    public PhoneNumberModel findPrimaryByUser(UUID userId) {
        return find("user.id = ?1 and isPrimary = true and deletedAt is null", userId).firstResult();
    }

    /**
     * Busca telefones com WhatsApp ativo de um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de telefones com WhatsApp
     */
    public List<PhoneNumberModel> findWhatsAppByUser(UUID userId) {
        return list("user.id = ?1 and hasWhatsApp = true and deletedAt is null", userId);
    }

    /**
     * Busca telefones com Telegram ativo de um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de telefones com Telegram
     */
    public List<PhoneNumberModel> findTelegramByUser(UUID userId) {
        return list("user.id = ?1 and hasTelegram = true and deletedAt is null", userId);
    }

    /**
     * Busca telefones com Signal ativo de um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de telefones com Signal
     */
    public List<PhoneNumberModel> findSignalByUser(UUID userId) {
        return list("user.id = ?1 and hasSignal = true and deletedAt is null", userId);
    }

    /**
     * Verifica se um número completo já está cadastrado (não deletado).
     *
     * @param fullNumber Número completo (ex: "+556798407322")
     * @return true se já existe
     */
    public boolean existsByFullNumber(String fullNumber) {
        return count("fullNumber = ?1 and deletedAt is null", fullNumber) > 0;
    }

    /**
     * Remove o flag de telefone principal de todos os telefones ativos de um
     * usuário.
     * Útil antes de definir um novo telefone principal.
     *
     * @param userId ID do usuário
     */
    public void removePrimaryFlagFromUser(UUID userId) {
        update("isPrimary = false where user.id = ?1 and deletedAt is null", userId);
    }

    /**
     * Busca telefones celulares de um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de telefones celulares
     */
    public List<PhoneNumberModel> findMobileByUser(UUID userId) {
        return list("user.id = ?1 and type = 'MOBILE' and deletedAt is null", userId);
    }

    /**
     * Busca telefones verificados de um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de telefones verificados
     */
    public List<PhoneNumberModel> findVerifiedByUser(UUID userId) {
        return list("user.id = ?1 and isVerified = true and deletedAt is null", userId);
    }

    /**
     * Conta quantos telefones ativos um usuário possui.
     *
     * @param userId ID do usuário
     * @return Quantidade de telefones ativos
     */
    public long countByUser(UUID userId) {
        return count("user.id = ?1 and deletedAt is null", userId);
    }
}
