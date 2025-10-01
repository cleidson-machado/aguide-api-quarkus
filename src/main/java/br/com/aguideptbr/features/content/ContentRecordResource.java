package br.com.aguideptbr.features.content;

import br.com.aguideptbr.util.PaginatedResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


import java.util.List;

import java.util.UUID;

@Path("/contents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContentRecordResource {

    @Inject
    ContentService contentService;

    //**
    // Examples of usage:
    // GET /contents?sort=title&order=asc - Alphabetical order
    // GET /contents?sort=title&order=desc - Descending alphabetical order
    // GET /contents?sort=channelName&order=asc - / By channel
    // GET /contents?page=0&size=10&sort=title&order=asc - By page
    //**

    // ✅ Paginated list if page/size is specified, otherwise returns the last 50...
    @GET
    public Response listContents(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @QueryParam("sort") @DefaultValue("title") String sortField,
            @QueryParam("order") @DefaultValue("asc") String sortOrder
    ) {
        try {
            if (page != null && size != null) {
                var pagedResponse = contentService.getPaginatedContents(page, size, sortField, sortOrder);
                return Response.ok(pagedResponse).build();
            } else {
                var limitedResponse = contentService.getLimitedContents(sortField, sortOrder);
                return Response.ok(limitedResponse).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    //**
    // This endpoint is redundant with the paginated option in listContents,
    // but is kept here for educational purposes
    // This method don't list by order because it's redundant with the main listContents method
    // Example: GET /contents/paged?page=0&size=10
    //**

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
    @Path("/find-first-title/{title}")
    public Response findByTitle(@PathParam("title") String title) {
        ContentRecordModel content = ContentRecordModel.findByTitle(title);
        if (content == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(content).build();
    }

    @GET
    @Path("/search")
    public Response searchContentsByTitle(@QueryParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("O parâmetro de busca 'q' não pode ser vazio.")
                    .build();
        }
        List<ContentRecordModel> results = ContentRecordModel.searchByTitle(query);
        return Response.ok(results).build();
    }

    @POST
    @Transactional
    public Response create(@Valid ContentRecordModel contentRecordModel) {
        contentRecordModel.persist();
        return Response
                .status(Response.Status.CREATED)
                .entity(contentRecordModel)
                .build();
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
