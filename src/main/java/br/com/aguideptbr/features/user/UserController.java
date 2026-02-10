package br.com.aguideptbr.features.user;

import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.user.dto.UserDetailResponse;
import br.com.aguideptbr.util.PaginatedResponse;
import io.quarkus.panache.common.Page;
import jakarta.transaction.Transactional;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST Controller para gerenciamento de usuários do sistema.
 *
 * Esta classe implementa endpoints REST para operações CRUD completas
 * de usuários, incluindo soft delete, paginação e restauração de usuários.
 *
 * @author Cleidson Machado
 * @since 1.0
 * @see UserModel
 */
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    private final Logger log;

    public UserController(Logger log) {
        this.log = log;
    }

    /**
     * Lista todos os usuários ativos COM seus telefones.
     * Limitado a 50 usuários para evitar sobrecarga.
     *
     * GET /api/v1/users
     *
     * Para listas maiores, use /api/v1/users/paginated
     */
    @GET
    public Response list() {
        log.info("GET /api/v1/users - Listando usuários com telefones (limite 50)");

        List<UserModel> users = UserModel.find("deletedAt is null")
                .page(Page.of(0, 50))
                .list();

        List<UserDetailResponse> response = users.stream()
                .map(UserDetailResponse::new)
                .toList();

        log.infof("Retornando %d usuários com telefones", response.size());
        return Response.ok(response).build();
    }

    /**
     * Lista usuários ativos COM seus telefones (paginado).
     *
     * GET /api/v1/users/paginated?page=0&size=10
     *
     * Ideal para data tables no frontend.
     */
    @GET
    @Path("/paginated")
    public Response listPaginated(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        log.infof("GET /api/v1/users/paginated - page=%d, size=%d", page, size);

        long totalItems = UserModel.count("deletedAt is null");
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<UserModel> users = UserModel.find("deletedAt is null")
                .page(Page.of(page, size))
                .list();

        List<UserDetailResponse> usersWithPhones = users.stream()
                .map(UserDetailResponse::new)
                .toList();

        PaginatedResponse<UserDetailResponse> response = new PaginatedResponse<>(
                usersWithPhones,
                totalItems,
                totalPages,
                page);

        log.infof("Retornando página %d com %d usuários (total: %d)", page, usersWithPhones.size(), totalItems);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") UUID id) {
        UserModel user = UserModel.findByIdActive(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(user).build();
    }

    /**
     * Retorna usuário com telefones incluídos.
     *
     * Use este endpoint quando precisar do usuário + telefones em uma única
     * chamada.
     * Evita múltiplas requisições do frontend.
     *
     * GET /api/v1/users/{id}/details
     *
     * Response:
     * {
     * "id": "uuid",
     * "name": "João",
     * "email": "joao@example.com",
     * "phones": [
     * {
     * "id": "uuid",
     * "fullNumber": "+556798407322",
     * "formattedNumber": "+55 (67) 9 8407-3221",
     * "isPrimary": true,
     * "isVerified": true,
     * "hasWhatsApp": true,
     * "hasTelegram": false,
     * "hasSignal": false
     * }
     * ]
     * }
     */
    @GET
    @Path("/{id}/details")
    public Response getUserWithPhones(@PathParam("id") UUID id) {
        log.infof("GET /api/v1/users/%s/details - Buscando usuário com telefones", id);

        UserModel user = UserModel.findByIdActive(id);
        if (user == null) {
            log.warnf("Usuário %s não encontrado", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Usuário não encontrado\"}")
                    .build();
        }

        UserDetailResponse response = new UserDetailResponse(user);
        log.infof("Usuário %s encontrado com %d telefones", id, response.getPhones().size());

        return Response.ok(response).build();
    }

    @POST
    @Transactional
    public Response createUser(UserModel userModel) {
        log.info("POST /users - Criando usuário");
        userModel.persist();
        return Response
                .status(Response.Status.CREATED)
                .entity(userModel)
                .build();
    }

    /**
     * Deleta um usuário (soft delete).
     * O usuário não é removido do banco, apenas marcado como deletado.
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") UUID id) {
        UserModel user = UserModel.findByIdActive(id);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Usuário não encontrado ou já foi deletado\"}")
                    .build();
        }

        user.softDelete();
        user.persist();

        return Response.noContent().build();
    }

    /**
     * Restaura um usuário deletado (undelete).
     * Apenas usuários com role ADMIN podem restaurar usuários.
     */
    @PUT
    @Path("/{id}/restore")
    @Transactional
    public Response restoreUser(@PathParam("id") UUID id) {
        UserModel user = UserModel.findById(id);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Usuário não encontrado\"}")
                    .build();
        }

        if (user.isActive()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Usuário já está ativo\"}")
                    .build();
        }

        user.restore();
        user.persist();

        return Response.ok(user).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateUser(@PathParam("id") UUID id, UserModel dataFromRequest) {
        UserModel userToUpdate = UserModel.findByIdActive(id);

        if (userToUpdate == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Usuário não encontrado ou foi deletado\"}")
                    .build();
        }

        userToUpdate.name = dataFromRequest.name;
        userToUpdate.surname = dataFromRequest.surname;
        userToUpdate.email = dataFromRequest.email;

        return Response.ok(userToUpdate).build();
    }
}
