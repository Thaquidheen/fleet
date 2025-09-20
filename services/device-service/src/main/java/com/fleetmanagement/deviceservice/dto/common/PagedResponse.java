package com.fleetmanagement.deviceservice.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response wrapper
 * 
 * Provides pagination metadata along with the content list
 * for paginated API responses.
 * 
 * @param <T> Type of the content items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {
    
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;
    private int numberOfElements;

    /**
     * Check if there is a next page
     * 
     * @return true if there is a next page
     */
    public boolean isHasNext() {
        return !last;
    }

    /**
     * Check if there is a previous page
     * 
     * @return true if there is a previous page
     */
    public boolean isHasPrevious() {
        return !first;
    }

    /**
     * Get the number of elements in current page
     * 
     * @return number of elements
     */
    public int getNumberOfElements() {
        return content != null ? content.size() : 0;
    }
}

