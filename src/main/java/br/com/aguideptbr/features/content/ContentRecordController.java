package br.com.aguideptbr.features.content;

import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

import br.com.aguideptbr.util.PaginatedResponse;
import jakarta.transaction.Transactional;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * REST Controller para gerenciamento de conteúdos (vídeos, artigos, podcasts).
 *
 * Esta classe implementa endpoints REST para operações CRUD completas
 * e busca paginada de conteúdos educacionais.
 *
 * @author Cleidson Machado
 * @since 1.0
 * @see ContentService
 */
@Path("/api/v1/contents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContentRecordController {

    private final Logger log;
    private final ContentService contentService;

    public ContentRecordController(Logger log, ContentService contentService) {
        this.log = log;
        this.contentService = contentService;
    }

    // **
    // Examples of usage:
    // GET /contents?sort=title&order=asc - Alphabetical order
    // GET /contents?sort=title&order=desc - Descending alphabetical order
    // GET /contents?sort=publishedAt&order=desc - Most recent published content
    // first
    // GET /contents?page=0&size=10&sort=title&order=asc - Paginated by title
    // GET /contents?page=0&size=10&sort=publishedAt&order=desc - Recent content
    // with pagination
    // **

    /**
     * Lista conteúdos com suporte a paginação e ordenação.
     *
     * Se page/size forem especificados, retorna resposta paginada completa.
     * Caso contrário, retorna os últimos 50 itens para melhor performance.
     *
     * @param page      Número da página (opcional, inicia em 0)
     * @param size      Tamanho da página (opcional, recomendado 10-50)
     * @param sortField Campo para ordenação (padrão: title)
     * @param sortOrder Direção da ordenação: asc ou desc (padrão: asc)
     * @return Response com lista de conteúdos ou mensagem de erro
     */
    @GET
    public Response listContents(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @QueryParam("sort") @DefaultValue("title") String sortField,
            @QueryParam("order") @DefaultValue("asc") String sortOrder) {
        log.info("GET /contents - Início da requisição (page=" + page + ", size=" + size + ", sort=" + sortField
                + ", order=" + sortOrder + ")");
        try {
            if (page != null && size != null) {
                log.debug("Chamando contentService.getPaginatedContents");
                var pagedResponse = contentService.getPaginatedContents(page, size, sortField, sortOrder);
                log.info("GET /contents - Retornando resposta paginada com " + pagedResponse.getContent().size()
                        + " itens");
                return Response.ok(pagedResponse).build();
            } else {
                log.debug("Chamando contentService.getLimitedContents");
                var limitedResponse = contentService.getLimitedContents(sortField, sortOrder);
                log.info("GET /contents - Retornando resposta limitada");
                return Response.ok(limitedResponse).build();
            }
        } catch (IllegalArgumentException err) {
            log.error("GET /contents - IllegalArgumentException: " + err.getMessage(), err);
            return Response.status(Status.BAD_REQUEST)
                    .entity(err.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("GET /contents - Erro inesperado: " + e.getMessage(), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno do servidor: " + e.getMessage())
                    .build();
        }
    }

    // **
    // This endpoint is redundant with the paginated option in listContents,
    // but is kept here for educational purposes
    // This method don't list by order because it's redundant with the main
    // listContents method
    // Example: GET /contents/paged?page=0&size=10
    // **

    @GET
    @Path("/paged")
    public PaginatedResponse<ContentRecordModel> listPaginatedWithMeta(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        var query = ContentRecordModel.findAll();
        long totalItems = query.count();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<ContentRecordModel> items = query.page(page, size).list();

        return new PaginatedResponse<>(items, totalItems, totalPages, page);
    }

    @GET
    @Path("/find-first-title/{title}")
    public Response findByTitle(@PathParam("title") String title) {
        ContentRecordModel result = ContentRecordModel.findByTitle(title);
        if (result == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/search")
    public Response searchContentsByTitle(@QueryParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return Response.status(Status.BAD_REQUEST)
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
                .status(Status.CREATED)
                .entity(contentRecordModel)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") UUID id, @Valid ContentRecordModel dataFromRequest) {
        ContentRecordModel existing = ContentRecordModel.findById(id);
        if (existing == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        existing.title = dataFromRequest.title;
        existing.description = dataFromRequest.description;
        existing.videoUrl = dataFromRequest.videoUrl;
        existing.type = dataFromRequest.type;
        existing.videoThumbnailUrl = dataFromRequest.videoThumbnailUrl;
        existing.setPublishedAt(dataFromRequest.getPublishedAt());

        return Response.ok(existing).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") UUID id) {
        ContentRecordModel existing = ContentRecordModel.findById(id);
        if (existing == null) {
            // Create a more detailed response for not found
            return Response.status(Status.NOT_FOUND)
                    .entity("The Content with ID " + id + " was not found to DELETE!.")
                    .build();
        }

        boolean deleted = ContentRecordModel.deleteById(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            // Rare case: content existed but was not deleted....
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to delete the Content with ID " + id + ".")
                    .build();
        }
    }

    // New method to show content with plusInfoMsg when querying by ID
    @GET
    @Path("/{id}")
    public Response getContentById(@PathParam("id") String idStr) {
        UUID idHash;
        try {
            idHash = UUID.fromString(idStr);
        } catch (IllegalArgumentException err) {
            String plusInfoMsg = "Found a error: " + err.getMessage();
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ContentWithComment(null, plusInfoMsg))
                    .build();
        }

        ContentRecordModel result = ContentRecordModel.findById(idHash);
        if (result == null) {
            String plusInfoMsg = "No content found for the provided ID: " + idHash;
            return Response.status(Status.NOT_FOUND)
                    .entity(new ContentWithComment(null, plusInfoMsg))
                    .build();
        }

        String plusInfoMsg = "Query executed successfully.";
        return Response.ok()
                .entity(new ContentWithComment(result, plusInfoMsg))
                .build();
    }

    // Internal record class to hold content and plusInfoMsg
    public record ContentWithComment(ContentRecordModel content, String plusInfoMsg) {
    }
}
