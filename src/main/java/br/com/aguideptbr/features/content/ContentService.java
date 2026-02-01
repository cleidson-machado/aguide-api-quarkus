package br.com.aguideptbr.features.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.aguideptbr.util.PaginatedResponse;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContentService {

    private static final int DEFAULT_LIMIT = 50;

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
