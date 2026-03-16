package br.com.aguideptbr.features.userchoice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.userchoice.enuns.ContentFormat;
import br.com.aguideptbr.features.userchoice.enuns.InfoSource;
import br.com.aguideptbr.features.userchoice.enuns.UserProfileType;
import br.com.aguideptbr.features.userchoice.enuns.VisaTypeInterest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Service para lógica de negócio relacionada a escolhas de perfil de usuários.
 * Gerencia criação, atualização, busca e remoção de dados do formulário de
 * onboarding.
 *
 * Validações:
 * - Campos obrigatórios por tipo de perfil (CREATOR/CONSUMER)
 * - Validação de enums ContentFormat e InfoSource
 * - Consistência lógica para perfil CONSUMER (via
 * validateConsumerLogicalConsistency)
 * - Unicidade de escolha por usuário (um usuário = uma escolha)
 *
 * @see UserChoiceModel
 * @see UserChoiceRepository
 */
@ApplicationScoped
public class UserChoiceService {

    private final Logger log;
    private final UserChoiceRepository userChoiceRepository;

    public UserChoiceService(Logger log, UserChoiceRepository userChoiceRepository) {
        this.log = log;
        this.userChoiceRepository = userChoiceRepository;
    }

    /**
     * Cria uma nova escolha de perfil para um usuário.
     *
     * Validações aplicadas:
     * 1. Verifica se o usuário já possui uma escolha cadastrada (unicidade)
     * 2. Valida campos obrigatórios via isValid()
     * 3. Valida enums ContentFormat (CREATOR) e InfoSource (CONSUMER)
     * 4. Valida consistência lógica (CONSUMER via
     * validateConsumerLogicalConsistency)
     *
     * @param userChoice Modelo de escolha a ser criado
     * @return UserChoiceModel persisted with generated UUID
     * @throws WebApplicationException (400) se validação falhar
     * @throws WebApplicationException (409) se usuário já tiver escolha cadastrada
     */
    @Transactional
    public UserChoiceModel create(UserChoiceModel userChoice) {
        log.infof("📝 Creating user choice: userId=%s, profileType=%s",
                userChoice.userId, userChoice.profileType);

        // 1. Verificar se usuário já tem escolha cadastrada
        if (userChoiceRepository.existsByUserId(userChoice.userId)) {
            log.warnf("⚠️ User already has a profile choice: userId=%s", userChoice.userId);
            throw new WebApplicationException(
                    "User already has a profile choice. Use PUT to update.",
                    Response.Status.CONFLICT);
        }

        // 2. Validar campos obrigatórios e consistência lógica
        if (!userChoice.isValid()) {
            List<String> errors = userChoice.getValidationErrors();
            log.warnf("❌ Validation failed for user choice: %s", errors);
            throw new WebApplicationException(
                    buildErrorResponse("Validation failed", errors),
                    Response.Status.BAD_REQUEST);
        }

        // 3. Validar enums específicos para CREATOR
        if (userChoice.profileType == UserProfileType.CREATOR) {
            if (userChoice.contentFormats != null && !userChoice.contentFormats.isEmpty()) {
                if (!ContentFormat.isValidStringList(userChoice.contentFormats)) {
                    log.warnf("❌ Invalid content formats: %s", userChoice.contentFormats);
                    throw new WebApplicationException(
                            "Invalid content formats. Valid values: VLOG, TUTORIAL, INTERVIEW, NEWS_ANALYSIS, SHORTS, OTHER",
                            Response.Status.BAD_REQUEST);
                }
            }
        }

        // 4. Validar enums específicos para CONSUMER
        if (userChoice.profileType == UserProfileType.CONSUMER) {
            if (userChoice.currentInfoSources != null && !userChoice.currentInfoSources.isEmpty()) {
                if (!InfoSource.isValidStringList(userChoice.currentInfoSources)) {
                    log.warnf("❌ Invalid info sources: %s", userChoice.currentInfoSources);
                    throw new WebApplicationException(
                            "Invalid info sources. Valid values: YOUTUBE, WHATSAPP_TELEGRAM, BLOGS, LAWYERS_CONSULTANTS, FORUMS, SOCIAL_MEDIA",
                            Response.Status.BAD_REQUEST);
                }
            }
        }

        // 5. Persistir
        userChoiceRepository.persist(userChoice);
        log.infof("✅ User choice created successfully: id=%s", userChoice.id);

        return userChoice;
    }

    /**
     * Atualiza uma escolha de perfil existente.
     *
     * Validações aplicadas:
     * 1. Verifica se a escolha existe
     * 2. Valida campos obrigatórios via isValid()
     * 3. Valida enums ContentFormat (CREATOR) e InfoSource (CONSUMER)
     * 4. Valida consistência lógica (CONSUMER)
     *
     * @param id            ID da escolha a ser atualizada
     * @param updatedChoice Dados atualizados
     * @return UserChoiceModel atualizado
     * @throws WebApplicationException (404) se escolha não encontrada
     * @throws WebApplicationException (400) se validação falhar
     */
    @Transactional
    public UserChoiceModel update(UUID id, UserChoiceModel updatedChoice) {
        log.infof("📝 Updating user choice: id=%s", id);

        // 1. Buscar escolha existente
        UserChoiceModel existing = userChoiceRepository.findByIdOptional(id)
                .orElseThrow(() -> {
                    log.warnf("⚠️ User choice not found: id=%s", id);
                    return new WebApplicationException(
                            "User choice not found",
                            Response.Status.NOT_FOUND);
                });

        // 2. Verificar se não está deletada
        if (existing.deletedAt != null) {
            log.warnf("⚠️ Cannot update deleted user choice: id=%s", id);
            throw new WebApplicationException(
                    "User choice has been deleted",
                    Response.Status.GONE);
        }

        // 3. Atualizar campos
        existing.profileType = updatedChoice.profileType;
        existing.nicheContext = updatedChoice.nicheContext;

        // CREATOR fields
        existing.channelName = updatedChoice.channelName;
        existing.channelHandle = updatedChoice.channelHandle;
        existing.channelAgeRange = updatedChoice.channelAgeRange;
        existing.subscriberRange = updatedChoice.subscriberRange;
        existing.monetizationStatus = updatedChoice.monetizationStatus;
        existing.mainNiche = updatedChoice.mainNiche;
        existing.contentFormats = updatedChoice.contentFormats;
        existing.commercialIntent = updatedChoice.commercialIntent;
        existing.offeredService = updatedChoice.offeredService;
        existing.publishingFrequency = updatedChoice.publishingFrequency;
        existing.contentDifferential = updatedChoice.contentDifferential;

        // CONSUMER fields
        existing.currentSituation = updatedChoice.currentSituation;
        existing.mainObjective = updatedChoice.mainObjective;
        existing.visaTypeInterest = updatedChoice.visaTypeInterest;
        existing.knowledgeLevel = updatedChoice.knowledgeLevel;
        existing.currentInfoSources = updatedChoice.currentInfoSources;
        existing.mainDifficulty = updatedChoice.mainDifficulty;
        existing.preferredContentType = updatedChoice.preferredContentType;
        existing.serviceHiringIntent = updatedChoice.serviceHiringIntent;
        existing.immigrationTimeframe = updatedChoice.immigrationTimeframe;
        existing.platformExpectation = updatedChoice.platformExpectation;

        // 4. Validar atualização
        if (!existing.isValid()) {
            List<String> errors = existing.getValidationErrors();
            log.warnf("❌ Validation failed for user choice update: %s", errors);
            throw new WebApplicationException(
                    buildErrorResponse("Validation failed", errors),
                    Response.Status.BAD_REQUEST);
        }

        // 5. Validar enums específicos para CREATOR
        if (existing.profileType == UserProfileType.CREATOR) {
            if (existing.contentFormats != null && !existing.contentFormats.isEmpty()) {
                if (!ContentFormat.isValidStringList(existing.contentFormats)) {
                    log.warnf("❌ Invalid content formats: %s", existing.contentFormats);
                    throw new WebApplicationException(
                            "Invalid content formats. Valid values: VLOG, TUTORIAL, INTERVIEW, NEWS_ANALYSIS, SHORTS, OTHER",
                            Response.Status.BAD_REQUEST);
                }
            }
        }

        // 6. Validar enums específicos para CONSUMER
        if (existing.profileType == UserProfileType.CONSUMER) {
            if (existing.currentInfoSources != null && !existing.currentInfoSources.isEmpty()) {
                if (!InfoSource.isValidStringList(existing.currentInfoSources)) {
                    log.warnf("❌ Invalid info sources: %s", existing.currentInfoSources);
                    throw new WebApplicationException(
                            "Invalid info sources. Valid values: YOUTUBE, WHATSAPP_TELEGRAM, BLOGS, LAWYERS_CONSULTANTS, FORUMS, SOCIAL_MEDIA",
                            Response.Status.BAD_REQUEST);
                }
            }
        }

        log.infof("✅ User choice updated successfully: id=%s", id);
        return existing;
    }

    /**
     * Busca escolha de perfil por ID.
     *
     * @param id ID da escolha
     * @return Optional com escolha encontrada ou vazio
     */
    public Optional<UserChoiceModel> findById(UUID id) {
        log.infof("🔍 Finding user choice by id: %s", id);
        return userChoiceRepository.findByIdOptional(id)
                .filter(choice -> choice.deletedAt == null);
    }

    /**
     * Busca escolha de perfil por ID de usuário.
     *
     * @param userId ID do usuário
     * @return Optional com escolha encontrada ou vazio
     */
    public Optional<UserChoiceModel> findByUserId(UUID userId) {
        log.infof("🔍 Finding user choice by userId: %s", userId);
        return userChoiceRepository.findByUserId(userId);
    }

    /**
     * Busca todas as escolhas de um tipo de perfil específico.
     *
     * @param profileType Tipo do perfil (CREATOR ou CONSUMER)
     * @return Lista de escolhas do tipo especificado
     */
    public List<UserChoiceModel> findByProfileType(UserProfileType profileType) {
        log.infof("🔍 Finding user choices by profileType: %s", profileType);
        return userChoiceRepository.findByProfileType(profileType);
    }

    /**
     * Busca escolhas por contexto de nicho.
     *
     * @param nicheContext Contexto do nicho (busca parcial)
     * @return Lista de escolhas que contêm o contexto especificado
     */
    public List<UserChoiceModel> findByNicheContext(String nicheContext) {
        log.infof("🔍 Finding user choices by nicheContext: %s", nicheContext);
        return userChoiceRepository.findByNicheContext(nicheContext);
    }

    /**
     * Busca criadores monetizados.
     *
     * @return Lista de criadores com status de monetização MONETIZED
     */
    public List<UserChoiceModel> findMonetizedCreators() {
        log.info("🔍 Finding monetized creators");
        return userChoiceRepository.findMonetizedCreators();
    }

    /**
     * Busca consumidores interessados em um tipo de visto específico.
     *
     * @param visaType Tipo de visto
     * @return Lista de consumidores interessados no visto especificado
     */
    public List<UserChoiceModel> findConsumersByVisaInterest(VisaTypeInterest visaType) {
        log.infof("🔍 Finding consumers by visa interest: %s", visaType);
        return userChoiceRepository.findConsumersByVisaInterest(visaType);
    }

    /**
     * Busca todas as escolhas ativas.
     *
     * @return Lista de todas as escolhas não deletadas
     */
    public List<UserChoiceModel> findAll() {
        log.info("🔍 Finding all active user choices");
        return userChoiceRepository.findAllActive();
    }

    /**
     * Remove uma escolha de perfil (soft delete).
     *
     * @param id ID da escolha a ser removida
     * @throws WebApplicationException (404) se escolha não encontrada
     */
    @Transactional
    public void softDelete(UUID id) {
        log.infof("🗑️ Soft deleting user choice: id=%s", id);

        UserChoiceModel userChoice = userChoiceRepository.findByIdOptional(id)
                .orElseThrow(() -> {
                    log.warnf("⚠️ User choice not found for deletion: id=%s", id);
                    return new WebApplicationException(
                            "User choice not found",
                            Response.Status.NOT_FOUND);
                });

        if (userChoice.deletedAt != null) {
            log.warnf("⚠️ User choice already deleted: id=%s", id);
            throw new WebApplicationException(
                    "User choice already deleted",
                    Response.Status.GONE);
        }

        userChoice.deletedAt = LocalDateTime.now();
        log.infof("✅ User choice soft deleted successfully: id=%s", id);
    }

    /**
     * Constrói resposta de erro formatada.
     *
     * @param message Mensagem principal
     * @param errors  Lista de erros de validação
     * @return String JSON formatada
     */
    private String buildErrorResponse(String message, List<String> errors) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"error\":\"").append(message).append("\",");
        json.append("\"validationErrors\":[");
        for (int i = 0; i < errors.size(); i++) {
            json.append("\"").append(errors.get(i).replace("\"", "\\\"")).append("\"");
            if (i < errors.size() - 1) {
                json.append(",");
            }
        }
        json.append("],");
        json.append("\"timestamp\":\"").append(LocalDateTime.now()).append("\"");
        json.append("}");
        return json.toString();
    }
}
