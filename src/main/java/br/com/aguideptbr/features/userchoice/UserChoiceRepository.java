package br.com.aguideptbr.features.userchoice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.aguideptbr.features.userchoice.enuns.MonetizationStatus;
import br.com.aguideptbr.features.userchoice.enuns.UserProfileType;
import br.com.aguideptbr.features.userchoice.enuns.VisaTypeInterest;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository para operações de banco de dados com escolhas de perfil de
 * usuários.
 * Gerencia dados do formulário de onboarding para perfis CREATOR e CONSUMER.
 *
 * @see UserChoiceModel
 * @see UserProfileType
 */
@ApplicationScoped
public class UserChoiceRepository implements PanacheRepositoryBase<UserChoiceModel, UUID> {

    /**
     * Busca a escolha de perfil de um usuário específico.
     *
     * @param userId ID do usuário
     * @return Optional contendo a escolha do usuário ou vazio se não encontrada
     */
    public Optional<UserChoiceModel> findByUserId(UUID userId) {
        return find("userId = ?1 and deletedAt is null", userId).firstResultOptional();
    }

    /**
     * Busca todas as escolhas de um determinado tipo de perfil.
     *
     * @param profileType Tipo do perfil (CREATOR ou CONSUMER)
     * @return Lista de escolhas do tipo especificado
     */
    public List<UserChoiceModel> findByProfileType(UserProfileType profileType) {
        return list("profileType = ?1 and deletedAt is null", profileType);
    }

    /**
     * Busca escolhas por contexto de nicho (com busca parcial case-insensitive).
     *
     * @param nicheContext Contexto do nicho (pode ser parcial)
     * @return Lista de escolhas que contêm o contexto especificado
     */
    public List<UserChoiceModel> findByNicheContext(String nicheContext) {
        return list("LOWER(nicheContext) LIKE LOWER(?1) and deletedAt is null", "%" + nicheContext + "%");
    }

    /**
     * Busca criadores (CREATOR) que já são monetizados.
     * Útil para listagem de criadores premium ou verificados.
     *
     * @return Lista de criadores monetizados
     */
    public List<UserChoiceModel> findMonetizedCreators() {
        return list("profileType = ?1 and monetizationStatus = ?2 and deletedAt is null",
                UserProfileType.CREATOR,
                MonetizationStatus.MONETIZED);
    }

    /**
     * Busca consumidores (CONSUMER) interessados em um tipo específico de visto.
     * Útil para segmentação de conteúdo ou ofertas de serviços.
     *
     * @param visaType Tipo de visto de interesse
     * @return Lista de consumidores interessados no visto especificado
     */
    public List<UserChoiceModel> findConsumersByVisaInterest(VisaTypeInterest visaType) {
        return list("profileType = ?1 and visaTypeInterest = ?2 and deletedAt is null",
                UserProfileType.CONSUMER,
                visaType);
    }

    /**
     * Busca criadores por handle do canal (identificador único do YouTube).
     *
     * @param channelHandle Handle do canal (ex: "@MeuCanal")
     * @return Optional contendo o criador com o handle especificado
     */
    public Optional<UserChoiceModel> findByChannelHandle(String channelHandle) {
        return find("channelHandle = ?1 and deletedAt is null", channelHandle).firstResultOptional();
    }

    /**
     * Conta total de escolhas por tipo de perfil (ativas apenas).
     *
     * @param profileType Tipo do perfil
     * @return Quantidade de escolhas para o tipo especificado
     */
    public long countByProfileType(UserProfileType profileType) {
        return count("profileType = ?1 and deletedAt is null", profileType);
    }

    /**
     * Busca todas as escolhas ativas (não deletadas).
     *
     * @return Lista de todas as escolhas ativas
     */
    public List<UserChoiceModel> findAllActive() {
        return list("deletedAt is null");
    }

    /**
     * Verifica se um usuário já tem uma escolha de perfil cadastrada.
     *
     * @param userId ID do usuário
     * @return true se já existe uma escolha, false caso contrário
     */
    public boolean existsByUserId(UUID userId) {
        return count("userId = ?1 and deletedAt is null", userId) > 0;
    }
}
