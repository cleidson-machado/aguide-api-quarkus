package br.com.aguideptbr.features.content;

import br.com.aguideptbr.util.PaginatedResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/contents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContentRecordResource {

    // ✅ Paginated list if page/size is specified, otherwise returns the last 50...
    @GET
    public Response listContents(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size
    ) {
        if (page != null && size != null) {
            var query = ContentRecordModel.findAll().page(page, size);
            long totalItems = ContentRecordModel.count();
            int totalPages = (int) Math.ceil((double) totalItems / size);

            var pagedResponse = new PaginatedResponse<>(
                    query.list(),
                    totalItems,
                    totalPages,
                    page
            );
            return Response.ok(pagedResponse).build();
        } else {
            List<ContentRecordModel> limited = ContentRecordModel.findAll()
                    .page(0, 50)
                    .list();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Limited to the 50 most recent items. Use pagination for full access.");
            response.put("totalItems", limited.size());
            response.put("items", limited);

            return Response.ok(response).build();
        }
    }

    @GET
    @Path("/paged")
    public PaginatedResponse<ContentRecordModel> listPaginatedWithMeta(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        var query = ContentRecordModel.findAll();
        long totalItems = query.count();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<ContentRecordModel> items = query.page(page, size).list();

        return new PaginatedResponse<>(items, totalItems, totalPages, page);
    }


    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        ContentRecordModel content = ContentRecordModel.findById(id);
        if (content == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(content).build();
    }

    @POST
    @Transactional
    public Response create(@Valid ContentRecordModel contentRecordModel) {
        contentRecordModel.persist();
        return Response.status(Response.Status.CREATED).entity(contentRecordModel).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") UUID id, @Valid ContentRecordModel dataFromRequest) {
        ContentRecordModel existing = ContentRecordModel.findById(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        existing.title = dataFromRequest.title;
        existing.description = dataFromRequest.description;
        existing.url = dataFromRequest.url;
        existing.channelName = dataFromRequest.channelName;
        existing.type = dataFromRequest.type;
        existing.thumbnailUrl = dataFromRequest.thumbnailUrl;

        return Response.ok(existing).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") UUID id) {
        boolean deleted = ContentRecordModel.deleteById(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
