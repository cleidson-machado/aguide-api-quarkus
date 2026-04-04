package br.com.aguideptbr.features.userposition;

import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.userposition.dto.AddPointsRequest;
import br.com.aguideptbr.features.userposition.dto.CreateUserRankingRequest;
import br.com.aguideptbr.features.userposition.dto.PointsHistoryResponse;
import br.com.aguideptbr.features.userposition.dto.UpdateUserRankingRequest;
import br.com.aguideptbr.features.userposition.dto.UserRankingResponse;
import br.com.aguideptbr.features.userposition.enuns.ConversionPotential;
import br.com.aguideptbr.features.userposition.enuns.EngagementLevel;
import br.com.aguideptbr.util.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST Controller para gerenciamento de rankings de usuários.
 *
 * Endpoints disponíveis:
 * - GET /api/v1/user-rankings - Lista todos os rankings ativos
 * - GET /api/v1/user-rankings/{id} - Busca ranking por ID
 * - GET /api/v1/user-rankings/user/{userId} - Busca ranking por userId
 * - GET /api/v1/user-rankings/top - Busca top usuários por score
 * - GET /api/v1/user-rankings/engagement/{level} - Busca por nível de
 * engajamento
 * - GET /api/v1/user-rankings/conversion/{potential} - Busca por potencial de
 * conversão
 * - POST /api/v1/user-rankings - Cria novo ranking
 * - PUT /api/v1/user-rankings/{id} - Atualiza ranking
 * - PUT /api/v1/user-rankings/{id}/restore - Restaura ranking deletado
 * - POST /api/v1/user-rankings/user/{userId}/add-points - Adiciona pontos ao
 * usuário
 * - DELETE /api/v1/user-rankings/{id} - Remove ranking (soft delete)
 *
 * @see UserRankingModel
 * @see UserRankingService
 */
@Path("/api/v1/user-rankings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserRankingController {

    private final Logger log;
    private final UserRankingService userRankingService;

    public UserRankingController(Logger log, UserRankingService userRankingService) {
        this.log = log;
        this.userRankingService = userRankingService;
    }

    /**
     * Lista todos os rankings ativos.
     *
     * GET /api/v1/user-rankings
     *
     * @return Lista de rankings
     */
    @GET
    public Response findAll() {
        log.info("GET /api/v1/user-rankings - Listing all active rankings");

        List<UserRankingResponse> responses = userRankingService.findAllActive().stream()
                .map(UserRankingResponse::new)
                .toList();

        log.infof("🔍 Found %d active rankings", responses.size());
        return Response.ok(responses).build();
    }

    /**
     * Busca um ranking por ID.
     *
     * GET /api/v1/user-rankings/{id}
     *
     * @param id ID do ranking
     * @return Ranking encontrado
     */
    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        log.infof("GET /api/v1/user-rankings/%s", id);

        return userRankingService.findById(id)
                .map(UserRankingResponse::new)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElseThrow(() -> {
                    log.warnf("⚠️ Ranking not found: id=%s", id);
                    return new WebApplicationException(
                            "Ranking not found",
                            Response.Status.NOT_FOUND);
                });
    }

    /**
     * Busca o ranking de um usuário específico.
     *
     * GET /api/v1/user-rankings/user/{userId}
     *
     * SECURITY: Valida que userId corresponde ao usuário autenticado no JWT.
     *
     * @param userId  ID do usuário
     * @param headers HTTP headers (para extração do JWT)
     * @return Ranking do usuário
     */
    @GET
    @Path("/user/{userId}")
    public Response findByUserId(
            @PathParam("userId") UUID userId,
            @Context HttpHeaders headers) {
        log.infof("GET /api/v1/user-rankings/user/%s", userId);

        // Validação de segurança: userId deve corresponder ao token JWT
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        SecurityUtils.validateUserIdMatchesToken(userId, authHeader);

        return userRankingService.findByUserId(userId)
                .map(UserRankingResponse::new)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElseThrow(() -> {
                    log.warnf("⚠️ Ranking not found for userId: %s", userId);
                    return new WebApplicationException(
                            "Ranking not found for user",
                            Response.Status.NOT_FOUND);
                });
    }

    /**
     * Busca top usuários por pontuação.
     *
     * GET /api/v1/user-rankings/top?limit=10
     *
     * @param limit Número máximo de resultados (padrão: 10, máx: 100)
     * @return Lista dos usuários com maior pontuação
     */
    @GET
    @Path("/top")
    public Response findTopByScore(@QueryParam("limit") @DefaultValue("10") int limit) {
        log.infof("GET /api/v1/user-rankings/top?limit=%d", limit);

        List<UserRankingResponse> responses = userRankingService.findTopByScore(limit).stream()
                .map(UserRankingResponse::new)
                .toList();

        log.infof("🔍 Found %d top users", responses.size());
        return Response.ok(responses).build();
    }

    /**
     * Busca rankings por nível de engajamento.
     *
     * GET /api/v1/user-rankings/engagement/{level}
     *
     * @param level Nível de engajamento (LOW, MEDIUM, HIGH, VERY_HIGH)
     * @return Lista de rankings com o nível especificado
     */
    @GET
    @Path("/engagement/{level}")
    public Response findByEngagementLevel(@PathParam("level") String level) {
        log.infof("GET /api/v1/user-rankings/engagement/%s", level);

        try {
            EngagementLevel engagementLevel = EngagementLevel.valueOf(level.toUpperCase());

            List<UserRankingResponse> responses = userRankingService.findByEngagementLevel(engagementLevel)
                    .stream()
                    .map(UserRankingResponse::new)
                    .toList();

            log.infof("🔍 Found %d rankings with engagement level: %s", responses.size(), level);
            return Response.ok(responses).build();

        } catch (IllegalArgumentException e) {
            log.warnf("❌ Invalid engagement level: %s", level);
            throw new WebApplicationException(
                    "Invalid engagement level. Valid values: LOW, MEDIUM, HIGH, VERY_HIGH",
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Busca rankings por potencial de conversão.
     *
     * GET /api/v1/user-rankings/conversion/{potential}
     *
     * @param potential Potencial de conversão (VERY_LOW, LOW, MEDIUM, HIGH,
     *                  VERY_HIGH)
     * @return Lista de rankings com o potencial especificado
     */
    @GET
    @Path("/conversion/{potential}")
    public Response findByConversionPotential(@PathParam("potential") String potential) {
        log.infof("GET /api/v1/user-rankings/conversion/%s", potential);

        try {
            ConversionPotential conversionPotential = ConversionPotential.valueOf(potential.toUpperCase());

            List<UserRankingResponse> responses = userRankingService.findByConversionPotential(conversionPotential)
                    .stream()
                    .map(UserRankingResponse::new)
                    .toList();

            log.infof("🔍 Found %d rankings with conversion potential: %s", responses.size(), potential);
            return Response.ok(responses).build();

        } catch (IllegalArgumentException e) {
            log.warnf("❌ Invalid conversion potential: %s", potential);
            throw new WebApplicationException(
                    "Invalid conversion potential. Valid values: VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH",
                    Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Cria um novo ranking para um usuário.
     *
     * POST /api/v1/user-rankings
     *
     * Validações:
     * - userId é obrigatório
     * - Usuário não pode ter mais de um ranking (retorna 409 se já existir)
     * - Engagement level e conversion potential são calculados automaticamente
     *
     * @param request Dados do ranking
     * @return Ranking criado (201 Created)
     */
    @POST
    public Response create(@Valid CreateUserRankingRequest request) {
        log.infof("POST /api/v1/user-rankings - userId=%s", request.getUserId());

        try {
            UserRankingModel model = toModel(request);
            UserRankingModel created = userRankingService.create(model);
            UserRankingResponse response = new UserRankingResponse(created);

            log.infof("✅ Ranking created successfully: id=%s, userId=%s", created.getId(), created.getUserId());
            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (WebApplicationException e) {
            log.warnf("⚠️ Failed to create ranking: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.errorf(e, "❌ Error creating ranking: %s", e.getMessage());
            throw new WebApplicationException(
                    "Internal server error",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Atualiza um ranking existente.
     *
     * PUT /api/v1/user-rankings/{id}
     *
     * Atualização parcial: apenas campos fornecidos serão atualizados.
     * Engagement level e conversion potential são recalculados automaticamente.
     *
     * @param id      ID do ranking
     * @param request Dados atualizados
     * @return Ranking atualizado
     */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid UpdateUserRankingRequest request) {
        log.infof("PUT /api/v1/user-rankings/%s", id);

        try {
            UserRankingModel updatedData = toModelFromUpdate(request);
            UserRankingModel updated = userRankingService.update(id, updatedData);
            UserRankingResponse response = new UserRankingResponse(updated);

            log.infof("✅ Ranking updated successfully: id=%s", id);
            return Response.ok(response).build();

        } catch (WebApplicationException e) {
            log.warnf("⚠️ Failed to update ranking: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.errorf(e, "❌ Error updating ranking: %s", e.getMessage());
            throw new WebApplicationException(
                    "Internal server error",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adiciona pontos ao ranking de um usuário.
     *
     * POST /api/v1/user-rankings/user/{userId}/add-points
     *
     * BACKWARD COMPATIBILITY: Suporta dois formatos:
     * 1. Body JSON (Phase B): {"points": 50, "reason": "DAILY_LOGIN"}
     * 2. Query param (Phase A): ?points=50 (reason será "UNSPECIFIED")
     *
     * SECURITY: Valida que userId corresponde ao usuário autenticado no JWT.
     *
     * @param userId           ID do usuário
     * @param request          Requisição com pontos e motivo (reason opcional, pode
     *                         ser null se usar query param)
     * @param pointsQueryParam Pontos via query param (para compatibilidade com
     *                         Phase A)
     * @param headers          HTTP headers (para extração do JWT, IP e User-Agent)
     * @return Ranking atualizado
     */
    @POST
    @Path("/user/{userId}/add-points")
    public Response addPoints(
            @PathParam("userId") UUID userId,
            AddPointsRequest request,
            @QueryParam("points") Integer pointsQueryParam,
            @Context HttpHeaders headers) {

        // BACKWARD COMPATIBILITY: Aceita query param ou body JSON
        Integer points;
        String reason;

        if (pointsQueryParam != null) {
            // Formato antigo (Phase A): query param
            points = pointsQueryParam;
            reason = "UNSPECIFIED";
            log.infof("POST /api/v1/user-rankings/user/%s/add-points (legacy format: points=%d)",
                    userId, points);
        } else if (request != null && request.getPoints() != null) {
            // Formato novo (Phase B): body JSON
            points = request.getPoints();
            reason = request.getReason();
            log.infof("POST /api/v1/user-rankings/user/%s/add-points (points=%d, reason=%s)",
                    userId, points, reason);
        } else {
            log.warn("⚠️ No points provided in query param or body");
            throw new WebApplicationException(
                    "Points must be provided either in query param (?points=50) or body JSON ({\"points\": 50, \"reason\": \"DAILY_LOGIN\"})",
                    Response.Status.BAD_REQUEST);
        }

        // Validação Jakarta Bean Validation (quando usar body JSON)
        if (request != null && pointsQueryParam == null) {
            // Validar points range (1-1000)
            if (points < 1 || points > 1000) {
                throw new WebApplicationException(
                        "Points must be between 1 and 1000",
                        Response.Status.BAD_REQUEST);
            }
            // Validar reason length (max 100)
            if (reason != null && reason.length() > 100) {
                throw new WebApplicationException(
                        "Reason must not exceed 100 characters",
                        Response.Status.BAD_REQUEST);
            }
        }

        // Validação de segurança: userId deve corresponder ao token JWT
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        SecurityUtils.validateUserIdMatchesToken(userId, authHeader);

        // Extrair metadata HTTP para auditoria
        String ipAddress = extractIpAddress(headers);
        String userAgent = headers.getHeaderString(HttpHeaders.USER_AGENT);
        String requestId = UUID.randomUUID().toString();

        try {
            UserRankingModel updated = userRankingService.addPoints(
                    userId,
                    points,
                    reason,
                    ipAddress,
                    userAgent,
                    requestId);
            UserRankingResponse response = new UserRankingResponse(updated);

            log.infof("✅ Points added successfully: userId=%s, points=%d, reason=%s, newScore=%d, requestId=%s",
                    userId, points, reason, updated.getTotalScore(), requestId);
            return Response.ok(response).build();

        } catch (WebApplicationException e) {
            log.warnf("⚠️ Failed to add points: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.errorf(e, "❌ Error adding points: %s", e.getMessage());
            throw new WebApplicationException(
                    "Internal server error",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Extrai o endereço IP do cliente a partir dos headers HTTP.
     * Verifica primeiro X-Forwarded-For (para proxies/load balancers),
     * depois X-Real-IP, e finalmente usa o IP da conexão direta.
     *
     * @param headers HTTP headers
     * @return Endereço IP do cliente ou "unknown"
     */
    private String extractIpAddress(HttpHeaders headers) {
        // Verificar X-Forwarded-For (usado por proxies/load balancers)
        String xForwardedFor = headers.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Pegar o primeiro IP da lista (cliente original)
            return xForwardedFor.split(",")[0].trim();
        }

        // Verificar X-Real-IP (usado por alguns proxies)
        String xRealIp = headers.getHeaderString("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp.trim();
        }

        // Fallback: IP desconhecido (não há forma confiável de obter via JAX-RS sem
        // injetar ServletRequest)
        return "unknown";
    }

    /**
     * Busca histórico de adição de pontos de um usuário.
     *
     * GET /api/v1/user-rankings/user/{userId}/points-history?limit=10
     *
     * Retorna lista de todas as adições de pontos com timestamp, motivo e score
     * após cada adição.
     * SECURITY: Valida que userId corresponde ao usuário autenticado no JWT.
     *
     * @param userId  ID do usuário
     * @param limit   Quantidade máxima de registros (default: 10, max: 100)
     * @param headers HTTP headers (para extração do JWT)
     * @return Lista de histórico de pontos
     */
    @GET
    @Path("/user/{userId}/points-history")
    public Response getPointsHistory(
            @PathParam("userId") UUID userId,
            @QueryParam("limit") @DefaultValue("10") int limit,
            @Context HttpHeaders headers) {
        log.infof("GET /api/v1/user-rankings/user/%s/points-history?limit=%d", userId, limit);

        // Validação de segurança: userId deve corresponder ao token JWT
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        SecurityUtils.validateUserIdMatchesToken(userId, authHeader);

        // Limitar query entre 1 e 100 registros
        int safeLimit = Math.max(1, Math.min(limit, 100));

        try {
            List<UserRankingAuditModel> auditRecords = userRankingService.findPointsHistory(userId, safeLimit);

            // Converter para DTO de resposta
            List<PointsHistoryResponse> history = auditRecords.stream()
                    .map(audit -> new PointsHistoryResponse(
                            audit.getCreatedAt(),
                            audit.getPointsAdded(),
                            audit.getPointsReason(),
                            null, // totalScoreAfter - seria necessário buscar do ranking no momento
                            audit.getIpAddress()))
                    .toList();

            log.infof("✅ Found %d points history records for userId=%s", history.size(), userId);
            return Response.ok(history).build();

        } catch (WebApplicationException e) {
            log.warnf("⚠️ Failed to fetch points history: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.errorf(e, "❌ Error fetching points history: %s", e.getMessage());
            throw new WebApplicationException(
                    "Internal server error",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Remove um ranking (soft delete).
     *
     * DELETE /api/v1/user-rankings/{id}
     *
     * @param id ID do ranking
     * @return 204 No Content
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        log.infof("DELETE /api/v1/user-rankings/%s", id);

        try {
            userRankingService.softDelete(id);
            log.infof("✅ Ranking soft deleted successfully: id=%s", id);
            return Response.noContent().build();

        } catch (WebApplicationException e) {
            log.warnf("⚠️ Failed to delete ranking: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.errorf(e, "❌ Error deleting ranking: %s", e.getMessage());
            throw new WebApplicationException(
                    "Internal server error",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Restaura um ranking deletado.
     *
     * PUT /api/v1/user-rankings/{id}/restore
     *
     * @param id ID do ranking
     * @return Ranking restaurado
     */
    @PUT
    @Path("/{id}/restore")
    public Response restore(@PathParam("id") UUID id) {
        log.infof("PUT /api/v1/user-rankings/%s/restore", id);

        try {
            UserRankingModel restored = userRankingService.restore(id);
            UserRankingResponse response = new UserRankingResponse(restored);

            log.infof("✅ Ranking restored successfully: id=%s", id);
            return Response.ok(response).build();

        } catch (WebApplicationException e) {
            log.warnf("⚠️ Failed to restore ranking: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.errorf(e, "❌ Error restoring ranking: %s", e.getMessage());
            throw new WebApplicationException(
                    "Internal server error",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Converte CreateUserRankingRequest para UserRankingModel.
     */
    private UserRankingModel toModel(CreateUserRankingRequest request) {
        UserRankingModel model = new UserRankingModel();
        model.setUserId(request.getUserId());
        model.setTotalScore(request.getTotalScore() != null ? request.getTotalScore() : 0);
        model.setTotalContentViews(request.getTotalContentViews() != null ? request.getTotalContentViews() : 0L);
        model.setUniqueContentViews(request.getUniqueContentViews() != null ? request.getUniqueContentViews() : 0L);
        model.setAvgDailyUsageMinutes(
                request.getAvgDailyUsageMinutes() != null ? request.getAvgDailyUsageMinutes() : 0);
        model.setConsecutiveDaysStreak(
                request.getConsecutiveDaysStreak() != null ? request.getConsecutiveDaysStreak() : 0);
        model.setTotalActiveDays(request.getTotalActiveDays() != null ? request.getTotalActiveDays() : 0L);
        model.setTotalMessagesSent(request.getTotalMessagesSent() != null ? request.getTotalMessagesSent() : 0L);
        model.setTotalConversationsStarted(
                request.getTotalConversationsStarted() != null ? request.getTotalConversationsStarted() : 0L);
        model.setUniqueContactsMessaged(
                request.getUniqueContactsMessaged() != null ? request.getUniqueContactsMessaged() : 0L);
        model.setActiveConversations(
                request.getActiveConversations() != null ? request.getActiveConversations() : 0);
        model.setHasPhones(request.getHasPhones() != null ? request.getHasPhones() : false);
        model.setTotalPhones(request.getTotalPhones() != null ? request.getTotalPhones() : 0);
        model.setHasWhatsapp(request.getHasWhatsapp() != null ? request.getHasWhatsapp() : false);
        model.setHasTelegram(request.getHasTelegram() != null ? request.getHasTelegram() : false);
        model.setLastActivityAt(request.getLastActivityAt());
        model.setLastContentViewAt(request.getLastContentViewAt());
        model.setLastMessageSentAt(request.getLastMessageSentAt());
        model.setLastLoginAt(request.getLastLoginAt());
        model.setFavoriteCategory(request.getFavoriteCategory());
        model.setFavoriteContentType(request.getFavoriteContentType());
        model.setPreferredUsageTime(request.getPreferredUsageTime());
        return model;
    }

    /**
     * Converte UpdateUserRankingRequest para UserRankingModel.
     */
    private UserRankingModel toModelFromUpdate(UpdateUserRankingRequest request) {
        UserRankingModel model = new UserRankingModel();
        model.setTotalScore(request.getTotalScore());
        model.setTotalContentViews(request.getTotalContentViews());
        model.setUniqueContentViews(request.getUniqueContentViews());
        model.setAvgDailyUsageMinutes(request.getAvgDailyUsageMinutes());
        model.setConsecutiveDaysStreak(request.getConsecutiveDaysStreak());
        model.setTotalActiveDays(request.getTotalActiveDays());
        model.setTotalMessagesSent(request.getTotalMessagesSent());
        model.setTotalConversationsStarted(request.getTotalConversationsStarted());
        model.setUniqueContactsMessaged(request.getUniqueContactsMessaged());
        model.setActiveConversations(request.getActiveConversations());
        model.setHasPhones(request.getHasPhones());
        model.setTotalPhones(request.getTotalPhones());
        model.setHasWhatsapp(request.getHasWhatsapp());
        model.setHasTelegram(request.getHasTelegram());
        model.setLastActivityAt(request.getLastActivityAt());
        model.setLastContentViewAt(request.getLastContentViewAt());
        model.setLastMessageSentAt(request.getLastMessageSentAt());
        model.setLastLoginAt(request.getLastLoginAt());
        model.setFavoriteCategory(request.getFavoriteCategory());
        model.setFavoriteContentType(request.getFavoriteContentType());
        model.setPreferredUsageTime(request.getPreferredUsageTime());
        return model;
    }
}
