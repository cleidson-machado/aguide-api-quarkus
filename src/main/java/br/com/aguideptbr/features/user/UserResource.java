package br.com.aguideptbr.features.user;

import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.util.PaginatedResponse;
import io.quarkus.panache.common.Page;
import jakarta.inject.Inject;
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

//This is a comment!! Just to try git commit triggering jenkins job!
//This is a comment!! Just to try git commit triggering jenkins job! Try! 01 no Jenkins Pipeline!!

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    Logger log;

    @GET
    public List<UserModel> list() {
        return UserModel.findAllActive();
    }

    @GET
    @Path("/paginated")
    public PaginatedResponse<UserModel> listPaginated(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        long totalItems = UserModel.count("deletedAt is null");
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<UserModel> users = UserModel.find("deletedAt is null")
                .page(Page.of(page, size))
                .list();

        return new PaginatedResponse<>(users, totalItems, totalPages, page);
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
