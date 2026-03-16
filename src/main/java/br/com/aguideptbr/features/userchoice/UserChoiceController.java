package br.com.aguideptbr.features.userchoice;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.userchoice.dto.CreateUserChoiceRequest;
import br.com.aguideptbr.features.userchoice.dto.UpdateUserChoiceRequest;
import br.com.aguideptbr.features.userchoice.dto.UserChoiceResponse;
import br.com.aguideptbr.features.userchoice.enuns.UserProfileType;
import br.com.aguideptbr.features.userchoice.enuns.VisaTypeInterest;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST Controller para gerenciamento de escolhas de perfil de usuários.
 *
 * Endpoints:
 * - POST /api/v1/user-choices - Criar nova escolha de perfil
 * - GET /api/v1/user-choices/{id} - Buscar escolha por ID
 * - GET /api/v1/user-choices/user/{userId} - Buscar escolha de um usuário
 * - GET /api/v1/user-choices - Listar todas as escolhas (com filtros opcionais)
 * - GET /api/v1/user-choices/creators/monetized - Listar criadores monetizados
 * - GET /api/v1/user-choices/consumers/visa/{visaType} - Listar consumidores
 * por tipo de visto
 * - PUT /api/v1/user-choices/{id} - Atualizar escolha existente
 * - DELETE /api/v1/user-choices/{id} - Remover escolha (soft delete)
 *
 * Features:
 * - Validação completa de campos obrigatórios por perfil (CREATOR/CONSUMER)
 * - Validação de enums ContentFormat e InfoSource
 * - Validação de consistência lógica para CONSUMER
 * - Garantia de unicidade (um usuário = uma escolha)
 * - Soft delete para preservação de dados históricos
 *
 * Security:
 * - Autenticado via JWT (AuthenticationFilter)
 * - Usuário só pode criar/editar sua própria escolha
 *
 * @author System
 * @since 1.0
 * @see UserChoiceService
 * @see UserChoiceModel
 */
@Path("/api/v1/user-choices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserChoiceController {

    private final Logger log;
    private final UserChoiceService userChoiceService;

    public UserChoiceController(Logger log, UserChoiceService userChoiceService) {
        this.log = log;
        this.userChoiceService = userChoiceService;
    }

    /**
     * Criar nova escolha de perfil para um usuário.
     *
     * Validações:
     * - userId, profileType e nicheContext são obrigatórios
     * - Usuário não pode ter mais de uma escolha (retorna 409 se já existir)
     * - Campos obrigatórios validados por tipo de perfil
     * - Enums ContentFormat e InfoSource validados
     * - Consistência lógica para CONSUMER validada
     *
     * Exemplo de request (CREATOR):
     * POST /api/v1/user-choices
     * {
     * "userId": "550e8400-e29b-41d4-a716-446655440000",
     * "profileType": "CREATOR",
     * "nicheContext": "Imigração Portugal",
     * "channelName": "Meu Canal Portugal",
     * "channelHandle": "@meucanal",
     * "channelAgeRange": "BETWEEN_1_AND_3_YEARS",
     * "subscriberRange": "BETWEEN_10K_AND_100K",
     * "monetizationStatus": "MONETIZED",
     * "mainNiche": "Visto D7",
     * "contentFormats": ["VLOG", "TUTORIAL"],
     * "commercialIntent": "YES",
     * "publishingFrequency": "WEEKLY"
     * }
     *
     * @param request CreateUserChoiceRequest com dados da escolha
     * @return UserChoiceResponse com a escolha criada (HTTP 201)
     */
    @POST
    public Response create(@Valid CreateUserChoiceRequest request) {
        log.infof("POST /api/v1/user-choices - userId=%s, profileType=%s",
                request.getUserId(), request.getProfileType());

        try {
            // Converter DTO para Model
            UserChoiceModel userChoice = toModel(request);

            // Criar escolha
            UserChoiceModel created = userChoiceService.create(userChoice);

            // Converter Model para Response
            UserChoiceResponse response = toResponse(created);

            log.infof("✅ User choice created successfully: id=%s", created.id);
            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .build();

        } catch (WebApplicationException e) {
            log.warnf("⚠️ Failed to create user choice: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.errorf(e, "❌ Error creating user choice: %s", e.getMessage());
            throw new WebApplicationException(
                    "Internal server error",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Buscar escolha de perfil por ID.
     *
     * Exemplo:
     * GET /api/v1/user-choices/550e8400-e29b-41d4-a716-446655440000
     *
     * @param id ID da escolha
     * @return UserChoiceResponse se encontrada (HTTP 200), 404 se não encontrada
     */
    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        log.infof("GET /api/v1/user-choices/%s", id);

        return userChoiceService.findById(id)
                .map(this::toResponse)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElseThrow(() -> {
                    log.warnf("⚠️ User choice not found: id=%s", id);
                    return new WebApplicationException(
                            "User choice not found",
                            Response.Status.NOT_FOUND);
                });
    }

    /**
     * Buscar escolha de perfil de um usuário específico.
     *
     * Exemplo:
     * GET /api/v1/user-choices/user/550e8400-e29b-41d4-a716-446655440000
     *
     * @param userId ID do usuário
     * @return UserChoiceResponse se encontrada (HTTP 200), 404 se não encontrada
     */
    @GET
    @Path("/user/{userId}")
    public Response findByUserId(@PathParam("userId") UUID userId) {
        log.infof("GET /api/v1/user-choices/user/%s", userId);

        return userChoiceService.findByUserId(userId)
                .map(this::toResponse)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElseThrow(() -> {
                    log.warnf("⚠️ User choice not found for userId: %s", userId);
                    return new WebApplicationException(
                            "User choice not found",
                            Response.Status.NOT_FOUND);
                });
    }

    /**
     * Listar escolhas de perfil com filtros opcionais.
     *
     * Filtros disponíveis:
     * - profileType: Filtrar por tipo de perfil (CREATOR ou CONSUMER)
     * - nicheContext: Filtrar por contexto de nicho (busca parcial
     * case-insensitive)
     *
     * Exemplos:
     * GET /api/v1/user-choices
     * GET /api/v1/user-choices?profileType=CREATOR
     * GET /api/v1/user-choices?nicheContext=Imigração
     * GET /api/v1/user-choices?profileType=CONSUMER&nicheContext=Portugal
     *
     * @param profileType  Tipo de perfil (opcional)
     * @param nicheContext Contexto de nicho (opcional)
     * @return Lista de UserChoiceResponse (HTTP 200)
     */
    @GET
    public Response findAll(
            @QueryParam("profileType") UserProfileType profileType,
            @QueryParam("nicheContext") String nicheContext) {

        log.infof("GET /api/v1/user-choices - profileType=%s, nicheContext=%s",
                profileType, nicheContext);

        List<UserChoiceModel> choices;

        // Aplicar filtros
        if (profileType != null) {
            choices = userChoiceService.findByProfileType(profileType);
        } else if (nicheContext != null && !nicheContext.isBlank()) {
            choices = userChoiceService.findByNicheContext(nicheContext);
        } else {
            choices = userChoiceService.findAll();
        }

        // Converter para Response
        List<UserChoiceResponse> responses = choices.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        log.infof("🔍 Found %d user choices", responses.size());
        return Response.ok(responses).build();
    }

    /**
     * Listar criadores monetizados.
     *
     * Útil para funcionalidades premium ou listagem de criadores verificados.
     *
     * Exemplo:
     * GET /api/v1/user-choices/creators/monetized
     *
     * @return Lista de UserChoiceResponse de criadores monetizados (HTTP 200)
     */
    @GET
    @Path("/creators/monetized")
    public Response findMonetizedCreators() {
        log.info("GET /api/v1/user-choices/creators/monetized");

        List<UserChoiceResponse> responses = userChoiceService.findMonetizedCreators().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        log.infof("🔍 Found %d monetized creators", responses.size());
        return Response.ok(responses).build();
    }

    /**
     * Listar consumidores interessados em um tipo de visto específico.
     *
     * Útil para segmentação de conteúdo ou ofertas de serviços direcionados.
     *
     * Exemplo:
     * GET /api/v1/user-choices/consumers/visa/D7_VISA
     *
     * @param visaType Tipo de visto de interesse
     * @return Lista de UserChoiceResponse de consumidores interessados (HTTP 200)
     */
    @GET
    @Path("/consumers/visa/{visaType}")
    public Response findConsumersByVisaInterest(@PathParam("visaType") VisaTypeInterest visaType) {
        log.infof("GET /api/v1/user-choices/consumers/visa/%s", visaType);

        List<UserChoiceResponse> responses = userChoiceService.findConsumersByVisaInterest(visaType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        log.infof("🔍 Found %d consumers interested in %s", responses.size(), visaType);
        return Response.ok(responses).build();
    }

    /**
     * Atualizar escolha de perfil existente.
     *
     * Validações:
     * - Escolha deve existir (404 se não encontrada)
     * - Escolha não pode estar deletada (410 se deletada)
     * - Todas as validações de criação aplicadas
     *
     * Exemplo de request:
     * PUT /api/v1/user-choices/550e8400-e29b-41d4-a716-446655440000
     * {
     * "profileType": "CREATOR",
     * "nicheContext": "Imigração Portugal",
     * "channelName": "Meu Canal Atualizado",
     * "monetizationStatus": "MONETIZED",
     * ...
     * }
     *
     * @param id      ID da escolha a ser atualizada
     * @param request UpdateUserChoiceRequest com dados atualizados
     * @return UserChoiceResponse atualizado (HTTP 200)
     */
    @PUT
    @Path("/{id}")
    public Response update(
            @PathParam("id") UUID id,
            @Valid UpdateUserChoiceRequest request) {

        log.infof("PUT /api/v1/user-choices/%s - profileType=%s",
                id, request.getProfileType());

        try {
            // Converter DTO para Model
            UserChoiceModel updatedModel = toModelFromUpdate(request);

            // Atualizar escolha
            UserChoiceModel updated = userChoiceService.update(id, updatedModel);

            // Converter Model para Response
            UserChoiceResponse response = toResponse(updated);

            log.infof("✅ User choice updated successfully: id=%s", id);
            return Response.ok(response).build();

        } catch (WebApplicationException e) {
            log.warnf("⚠️ Failed to update user choice: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.errorf(e, "❌ Error updating user choice: %s", e.getMessage());
            throw new WebApplicationException(
                    "Internal server error",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Remover escolha de perfil (soft delete).
     *
     * Não remove fisicamente o registro, apenas marca deletedAt.
     * Escolhas deletadas não aparecem mais em buscas.
     *
     * Exemplo:
     * DELETE /api/v1/user-choices/550e8400-e29b-41d4-a716-446655440000
     *
     * @param id ID da escolha a ser removida
     * @return HTTP 204 (No Content) se removida com sucesso, 404 se não encontrada
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        log.infof("DELETE /api/v1/user-choices/%s", id);

        try {
            userChoiceService.softDelete(id);
            log.infof("✅ User choice soft deleted successfully: id=%s", id);
            return Response.noContent().build();

        } catch (WebApplicationException e) {
            log.warnf("⚠️ Failed to delete user choice: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.errorf(e, "❌ Error deleting user choice: %s", e.getMessage());
            throw new WebApplicationException(
                    "Internal server error",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== Métodos auxiliares de conversão ==========

    /**
     * Converte CreateUserChoiceRequest para UserChoiceModel.
     */
    private UserChoiceModel toModel(CreateUserChoiceRequest request) {
        UserChoiceModel model = new UserChoiceModel();

        // Campos comuns
        model.userId = request.getUserId();
        model.profileType = request.getProfileType();
        model.nicheContext = request.getNicheContext();

        // CREATOR fields
        model.channelName = request.getChannelName();
        model.channelHandle = request.getChannelHandle();
        model.channelAgeRange = request.getChannelAgeRange();
        model.subscriberRange = request.getSubscriberRange();
        model.monetizationStatus = request.getMonetizationStatus();
        model.mainNiche = request.getMainNiche();
        model.contentFormats = request.getContentFormats();
        model.commercialIntent = request.getCommercialIntent();
        model.offeredService = request.getOfferedService();
        model.publishingFrequency = request.getPublishingFrequency();
        model.contentDifferential = request.getContentDifferential();

        // CONSUMER fields
        model.currentSituation = request.getCurrentSituation();
        model.mainObjective = request.getMainObjective();
        model.visaTypeInterest = request.getVisaTypeInterest();
        model.knowledgeLevel = request.getKnowledgeLevel();
        model.currentInfoSources = request.getCurrentInfoSources();
        model.mainDifficulty = request.getMainDifficulty();
        model.preferredContentType = request.getPreferredContentType();
        model.serviceHiringIntent = request.getServiceHiringIntent();
        model.immigrationTimeframe = request.getImmigrationTimeframe();
        model.platformExpectation = request.getPlatformExpectation();

        return model;
    }

    /**
     * Converte UpdateUserChoiceRequest para UserChoiceModel.
     */
    private UserChoiceModel toModelFromUpdate(UpdateUserChoiceRequest request) {
        UserChoiceModel model = new UserChoiceModel();

        // Campos comuns
        model.profileType = request.getProfileType();
        model.nicheContext = request.getNicheContext();

        // CREATOR fields
        model.channelName = request.getChannelName();
        model.channelHandle = request.getChannelHandle();
        model.channelAgeRange = request.getChannelAgeRange();
        model.subscriberRange = request.getSubscriberRange();
        model.monetizationStatus = request.getMonetizationStatus();
        model.mainNiche = request.getMainNiche();
        model.contentFormats = request.getContentFormats();
        model.commercialIntent = request.getCommercialIntent();
        model.offeredService = request.getOfferedService();
        model.publishingFrequency = request.getPublishingFrequency();
        model.contentDifferential = request.getContentDifferential();

        // CONSUMER fields
        model.currentSituation = request.getCurrentSituation();
        model.mainObjective = request.getMainObjective();
        model.visaTypeInterest = request.getVisaTypeInterest();
        model.knowledgeLevel = request.getKnowledgeLevel();
        model.currentInfoSources = request.getCurrentInfoSources();
        model.mainDifficulty = request.getMainDifficulty();
        model.preferredContentType = request.getPreferredContentType();
        model.serviceHiringIntent = request.getServiceHiringIntent();
        model.immigrationTimeframe = request.getImmigrationTimeframe();
        model.platformExpectation = request.getPlatformExpectation();

        return model;
    }

    /**
     * Converte UserChoiceModel para UserChoiceResponse.
     */
    private UserChoiceResponse toResponse(UserChoiceModel model) {
        UserChoiceResponse response = new UserChoiceResponse();

        // Metadados
        response.setId(model.id);
        response.setUserId(model.userId);
        response.setCreatedAt(model.createdAt);
        response.setUpdatedAt(model.updatedAt);

        // Campos comuns
        response.setProfileType(model.profileType);
        response.setNicheContext(model.nicheContext);

        // CREATOR fields
        response.setChannelName(model.channelName);
        response.setChannelHandle(model.channelHandle);
        response.setChannelAgeRange(model.channelAgeRange);
        response.setSubscriberRange(model.subscriberRange);
        response.setMonetizationStatus(model.monetizationStatus);
        response.setMainNiche(model.mainNiche);
        response.setContentFormats(model.contentFormats);
        response.setCommercialIntent(model.commercialIntent);
        response.setOfferedService(model.offeredService);
        response.setPublishingFrequency(model.publishingFrequency);
        response.setContentDifferential(model.contentDifferential);

        // CONSUMER fields
        response.setCurrentSituation(model.currentSituation);
        response.setMainObjective(model.mainObjective);
        response.setVisaTypeInterest(model.visaTypeInterest);
        response.setKnowledgeLevel(model.knowledgeLevel);
        response.setCurrentInfoSources(model.currentInfoSources);
        response.setMainDifficulty(model.mainDifficulty);
        response.setPreferredContentType(model.preferredContentType);
        response.setServiceHiringIntent(model.serviceHiringIntent);
        response.setImmigrationTimeframe(model.immigrationTimeframe);
        response.setPlatformExpectation(model.platformExpectation);

        return response;
    }
}
