package com.personalfinance.common.base.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Interface for requests that support pagination.
 */
public interface PageableRequest extends BaseRequest {

  int getPage();

  void setPage(int page);

  int getSize();

  void setSize(int size);

  String getSortBy();

  void setSortBy(String sortBy);

  String getSortDir();

  void setSortDir(String sortDir);

  /**
   * Convert to Spring Data Pageable with safe defaults.
   */
  default Pageable toPageable() {
    int safePage = Math.max(getPage(), 0);
    int safeSize = getSize() > 0 ? Math.min(getSize(), 100) : 20;
    String field = getSortBy() != null ? getSortBy() : "createdAt";
    Sort.Direction direction = "asc".equalsIgnoreCase(getSortDir())
      ? Sort.Direction.ASC : Sort.Direction.DESC;
    return PageRequest.of(safePage, safeSize, Sort.by(direction, field));
  }
}
