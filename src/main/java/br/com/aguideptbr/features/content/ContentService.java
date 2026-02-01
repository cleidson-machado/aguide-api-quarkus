package br.com.aguideptbr.features.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.aguideptbr.util.PaginatedResponse;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Serviço de negócio para gerenciamento de conteúdos.
 *
 * Responsável por implementar a lógica de negócio relacionada aos conteúdos,
 * incluindo paginação, ordenação e validações.
 *
 * @author Cleidson Machado
 * @since 1.0
 */
@ApplicationScoped
public class ContentService {

    /** Limite padrão de itens retornados quando paginação não é especificada */
    private static final int DEFAULT_LIMIT = 50;

    /**
     * Retorna conteúdos paginados com ordenação customizada.
     *
     * @param page      Número da página (zero-based)
     * @param size      Quantidade de itens por página
     * @param sortField Campo para ordenação (title, channelName, publishedAt, etc)
     * @param sortOrder Direção da ordenação (asc ou desc)
     * @return PaginatedResponse contendo lista de conteúdos e metadados de
     *         paginação
     * @throws IllegalArgumentException se o sortField for inválido
     */
    public PaginatedResponse<ContentRecordModel> getPaginatedContents(int page, int size, String sortField,
            String sortOrder) {
        validateSortField(sortField);
        Sort sortBy = buildSort(sortField, sortOrder);

        var query = ContentRecordModel.findAll(sortBy).page(page, size);
        long totalItems = ContentRecordModel.count();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        return new PaginatedResponse<>(
                query.list(),
                totalItems,
                totalPages,
                page);
    }

    /**
     * Retorna lista limitada de conteúdos (máximo 50 itens).
     *
     * Utilizado quando paginação não é especificada na requisição,
     * evitando sobrecarga ao retornar muitos registros.
     *
     * @param sortField Campo para ordenação
     * @param sortOrder Direção da ordenação (asc ou desc)
     * @return Map contendo mensagem informativa e lista limitada de itens
     * @throws IllegalArgumentException se o sortField for inválido
     */
    public Map<String, Object> getLimitedContents(String sortField, String sortOrder) {
        validateSortField(sortField);
        Sort sortBy = buildSort(sortField, sortOrder);

        List<ContentRecordModel> limited = ContentRecordModel.findAll(sortBy)
                .page(0, DEFAULT_LIMIT)
                .list();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Limited to the 50 most recent items. Use pagination for full access.");
        response.put("totalItems", limited.size());
        response.put("items", limited);

        return response;
    }

    public void validateSortField(String field) {
        if (!isValidSortField(field)) {
            throw new IllegalArgumentException("There is an invalid sort field: " + field);
        }
    }

    private boolean isValidSortField(String field) {
        return List.of("title", "description", "channelName", "type", "publishedAt", "createdAt").contains(field);
    }

    private Sort buildSort(String sortField, String sortOrder) {
        return "desc".equalsIgnoreCase(sortOrder) ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
    }
}
