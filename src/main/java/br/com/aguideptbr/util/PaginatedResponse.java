package br.com.aguideptbr.util;

import java.util.List;

public class PaginatedResponse<T> {
    private final List<T> content;
    private final long totalItems;
    private final int totalPages;
    private final int currentPage;

    public PaginatedResponse(List<T> content, long totalItems, int totalPages, int currentPage) {
        this.content = content;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    public List<T> getContent() {
        return content;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
