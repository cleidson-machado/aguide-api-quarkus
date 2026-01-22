package br.com.aguideptbr.features.user;

import br.com.aguideptbr.util.PaginatedResponse;
import io.quarkus.panache.common.Page;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

//This is a comment!! Just to try git commit triggering jenkins job!
//This is a comment!! Just to try git commit triggering jenkins job! Try! 01 no Jenkins Pipeline!!

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    public List<UserModel> list(){
        return UserModel.listAll();
    }

    @GET
    @Path("/paginated")
    public PaginatedResponse<UserModel> listPaginated(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        
        long totalItems = UserModel.count();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        List<UserModel> users = UserModel.findAll()
                .page(Page.of(page, size))
                .list();
        
        return new PaginatedResponse<>(users, totalItems, totalPages, page);
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") UUID id) {
        UserModel user = UserModel.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(user).build();
    }
  
    @POST
    @Transactional
    public Response createUser(UserModel userModel){
        System.out.println("THIS IS THE NAME: " + userModel.name);
        System.out.println("THIS IS THE SURNAME: " + userModel.surname);
        System.out.println("THIS IS THE EMAIL: " + userModel.email);
        System.out.println("THIS IS THE PASSWD: " + userModel.passwd);
        userModel.persist();
        return Response
                .status(Response.Status.CREATED)
                .entity(userModel)
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") UUID id) {
        boolean deleted = UserModel.deleteById(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateUser(@PathParam("id") UUID id, UserModel dataFromRequest) {
        UserModel userToUpdate = UserModel.findById(id);
        if (userToUpdate == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        userToUpdate.name = dataFromRequest.name;
        userToUpdate.surname = dataFromRequest.surname;
        userToUpdate.email = dataFromRequest.email;
        return Response.ok(userToUpdate).build();
    }
}