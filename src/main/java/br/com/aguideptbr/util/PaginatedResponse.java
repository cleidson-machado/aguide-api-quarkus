package br.com.aguideptbr.util;

import java.util.List;

public class PaginatedResponse<T> {
    public List<T> content;
    public long totalItems;
    public int totalPages;
    public int currentPage;

    public PaginatedResponse(List<T> content, long totalItems, int totalPages, int currentPage) {
        this.content = content;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }
}
