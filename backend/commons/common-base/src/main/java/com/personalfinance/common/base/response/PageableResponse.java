package com.personalfinance.common.base.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic paginated response wrapper.
 * Converts Spring Data's Page into a serializable response.
 * <p>
 * Usage:
 * <pre>
 * Page&lt;TransactionResponse&gt; page = repository.findAll(pageable).map(mapper::toResponse);
 * return PageableResponse.from(page);
 * </pre>
 *
 * @param <T> The type of items in the page
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PageableResponse<T> implements BaseResponse {

  private List<T> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean last;
  private boolean first;

  /**
   * Factory method from Spring Data Page.
   */
  public static <T> PageableResponse<T> from(Page<T> page) {
    return PageableResponse.<T>builder()
      .content(page.getContent())
      .page(page.getNumber())
      .size(page.getSize())
      .totalElements(page.getTotalElements())
      .totalPages(page.getTotalPages())
      .last(page.isLast())
      .first(page.isFirst())
      .build();
  }
}
