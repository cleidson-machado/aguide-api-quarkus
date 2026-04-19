// Template for REST Controller in usermessage feature
// Replace [Resource] with actual resource name (e.g., Conversation, Message)

package br.com.aguideptbr.features.usermessage;

@Path("/api/v1/[resources]")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class [Resource]Controller {

    // ✅ CONSTRUCTOR INJECTION (not field injection)
    private final [Resource]Service service;
    private final Logger log;

    public [Resource]Controller([Resource]Service service, Logger log) {
        this.service = service;
        this.log = log;
    }

    // CREATE endpoint
    @POST
    public Response create(@Valid Create[Resource]Request request) {
        log.infof("POST /api/v1/[resources] - Creating new [resource]");
        var response = service.create(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    // READ ALL endpoint
    @GET
    public Response findAll() {
        log.info("GET /api/v1/[resources] - Listing all [resources]");
        var response = service.findAll();
        return Response.ok(response).build();
    }

    // READ ONE endpoint
    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        log.infof("GET /api/v1/[resources]/%s - Finding [resource] by ID", id);
        var response = service.findById(id);
        return Response.ok(response).build();
    }

    // UPDATE endpoint
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid Update[Resource]Request request) {
        log.infof("PUT /api/v1/[resources]/%s - Updating [resource]", id);
        var response = service.update(id, request);
        return Response.ok(response).build();
    }

    // DELETE endpoint (soft delete)
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        log.infof("DELETE /api/v1/[resources]/%s - Deleting [resource]", id);
        service.delete(id);
        return Response.noContent().build();
    }
}
