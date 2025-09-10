package com.fleetmanagement.companyservice.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * MISSING DTO: PagedResponse
 *
 * This class was missing and causing compilation errors in CompanyUserManagementService
 * where PagedResponse.builder() was being called
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;

    // Additional metadata
    private String sortBy;
    private String sortDirection;

    /**
     * Helper method to create an empty paged response
     */
    public static <T> PagedResponse<T> empty() {
        return PagedResponse.<T>builder()
                .content(List.of())
                .page(0)
                .size(0)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .empty(true)
                .build();
    }

    /**
     * Helper method to create a paged response from Spring's Page object
     */
    public static <T> PagedResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}