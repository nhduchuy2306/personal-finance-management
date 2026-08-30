package com.personalfinance.common.base.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Pagination request DTO.
 * Used as query parameters in list endpoints.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {

  @Builder.Default
  private int page = 0;

  @Builder.Default
  private int size = 20;

  private String sortBy;

  @Builder.Default
  private String sortDirection = "DESC";

  /**
   * Convert to Spring Data PageRequest.
   */
  public PageRequest toSpringPageRequest() {
    Sort sort = sortBy != null
      ? Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
      : Sort.by(Sort.Direction.DESC, "createdAt");
    return PageRequest.of(page, size, sort);
  }
}
