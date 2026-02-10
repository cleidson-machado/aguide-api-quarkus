package br.com.aguideptbr.features.phone;

import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.phone.dto.PhoneNumberRequest;
import br.com.aguideptbr.features.phone.dto.PhoneNumberResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Controller REST para gerenciamento de telefones dos usuários.
 *
 * Endpoints:
 * - POST /api/v1/users/{userId}/phones - Adicionar telefone
 * - GET /api/v1/users/{userId}/phones - Listar telefones do usuário
 * - GET /api/v1/phones/{id} - Buscar telefone por ID
 * - PUT /api/v1/phones/{id} - Atualizar telefone
 * - DELETE /api/v1/phones/{id} - Remover telefone
 * - PUT /api/v1/users/{userId}/phones/{phoneId}/primary - Marcar como principal
 */
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PhoneNumberController {

    private final PhoneNumberService phoneService;
    private final Logger log;

    public PhoneNumberController(PhoneNumberService phoneService, Logger log) {
        this.phoneService = phoneService;
        this.log = log;
    }

    /**
     * Adiciona um novo telefone para um usuário.
     *
     * POST /api/v1/users/{userId}/phones
     *
     * Body:
     * {
     * "countryCode": "+55",
     * "areaCode": "67",
     * "number": "984073221",
     * "type": "MOBILE",
     * "isPrimary": false,
     * "hasWhatsApp": true,
     * "hasTelegram": false,
     * "hasSignal": false
     * }
     */
    @POST
    @Path("/users/{userId}/phones")
    @RolesAllowed({ "USER", "ADMIN" })
    public Response create(
            @PathParam("userId") UUID userId,
            @Valid PhoneNumberRequest request) {

        log.infof("POST /api/v1/users/%s/phones - Creating phone", userId);

        // Converter DTO para entidade
        PhoneNumberModel phone = new PhoneNumberModel();
        phone.countryCode = request.getCountryCode();
        phone.areaCode = request.getAreaCode();
        phone.number = request.getNumber();
        phone.type = request.getType();
        phone.isPrimary = request.getIsPrimary();
        phone.hasWhatsApp = request.getHasWhatsApp();
        phone.hasTelegram = request.getHasTelegram();
        phone.hasSignal = request.getHasSignal();

        PhoneNumberModel created = phoneService.create(userId, phone);
        PhoneNumberResponse response = new PhoneNumberResponse(created);

        log.infof("Phone created: id=%s, fullNumber=%s", created.id, created.fullNumber);
        return Response.status(Status.CREATED).entity(response).build();
    }

    /**
     * Lista todos os telefones de um usuário.
     *
     * GET /api/v1/users/{userId}/phones
     */
    @GET
    @Path("/users/{userId}/phones")
    @RolesAllowed({ "USER", "ADMIN" })
    public Response listByUser(@PathParam("userId") UUID userId) {
        log.infof("GET /api/v1/users/%s/phones - Listing phones", userId);

        List<PhoneNumberModel> phones = phoneService.findByUser(userId);
        List<PhoneNumberResponse> response = phones.stream()
                .map(PhoneNumberResponse::new)
                .toList();

        log.infof("Found %d phones for user %s", response.size(), userId);
        return Response.ok(response).build();
    }

    /**
     * Busca um telefone por ID.
     *
     * GET /api/v1/phones/{id}
     */
    @GET
    @Path("/phones/{id}")
    @RolesAllowed({ "USER", "ADMIN" })
    public Response findById(@PathParam("id") UUID id) {
        log.infof("GET /api/v1/phones/%s - Finding phone", id);

        PhoneNumberModel phone = phoneService.findById(id);
        PhoneNumberResponse response = new PhoneNumberResponse(phone);

        return Response.ok(response).build();
    }

    /**
     * Atualiza um telefone existente.
     *
     * PUT /api/v1/phones/{id}
     */
    @PUT
    @Path("/phones/{id}")
    @RolesAllowed({ "USER", "ADMIN" })
    public Response update(
            @PathParam("id") UUID id,
            @Valid PhoneNumberRequest request) {

        log.infof("PUT /api/v1/phones/%s - Updating phone", id);

        // Converter DTO para entidade
        PhoneNumberModel phone = new PhoneNumberModel();
        phone.countryCode = request.getCountryCode();
        phone.areaCode = request.getAreaCode();
        phone.number = request.getNumber();
        phone.type = request.getType();
        phone.isPrimary = request.getIsPrimary();
        phone.hasWhatsApp = request.getHasWhatsApp();
        phone.hasTelegram = request.getHasTelegram();
        phone.hasSignal = request.getHasSignal();

        PhoneNumberModel updated = phoneService.update(id, phone);
        PhoneNumberResponse response = new PhoneNumberResponse(updated);

        log.infof("Phone updated: id=%s", id);
        return Response.ok(response).build();
    }

    /**
     * Define um telefone como principal.
     *
     * PUT /api/v1/users/{userId}/phones/{phoneId}/primary
     */
    @PUT
    @Path("/users/{userId}/phones/{phoneId}/primary")
    @RolesAllowed({ "USER", "ADMIN" })
    public Response setPrimary(
            @PathParam("userId") UUID userId,
            @PathParam("phoneId") UUID phoneId) {

        log.infof("PUT /api/v1/users/%s/phones/%s/primary - Setting as primary", userId, phoneId);

        phoneService.setPrimary(userId, phoneId);

        log.infof("Phone %s set as primary for user %s", phoneId, userId);
        return Response.noContent().build();
    }

    /**
     * Remove um telefone.
     *
     * DELETE /api/v1/phones/{id}
     */
    @DELETE
    @Path("/phones/{id}")
    @RolesAllowed({ "USER", "ADMIN" })
    public Response delete(@PathParam("id") UUID id) {
        log.infof("DELETE /api/v1/phones/%s - Deleting phone", id);

        phoneService.delete(id);

        log.infof("Phone deleted: id=%s", id);
        return Response.noContent().build();
    }
}
