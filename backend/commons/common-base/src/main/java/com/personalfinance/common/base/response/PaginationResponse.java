package com.personalfinance.common.base.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Pagination response wrapper.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> {

  private List<T> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean first;
  private boolean last;

  public static <T> PaginationResponse<T> of(Page<T> springPage) {
    return PaginationResponse.<T>builder()
      .content(springPage.getContent())
      .page(springPage.getNumber())
      .size(springPage.getSize())
      .totalElements(springPage.getTotalElements())
      .totalPages(springPage.getTotalPages())
      .first(springPage.isFirst())
      .last(springPage.isLast())
      .build();
  }

  public static <T, S> PaginationResponse<T> of(Page<S> springPage, List<T> mappedContent) {
    return PaginationResponse.<T>builder()
      .content(mappedContent)
      .page(springPage.getNumber())
      .size(springPage.getSize())
      .totalElements(springPage.getTotalElements())
      .totalPages(springPage.getTotalPages())
      .first(springPage.isFirst())
      .last(springPage.isLast())
      .build();
  }
}
