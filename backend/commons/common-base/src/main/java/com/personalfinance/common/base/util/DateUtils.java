package com.personalfinance.common.base.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

/**
 * Date/time utilities for Vietnam timezone (Asia/Ho_Chi_Minh).
 */
public final class DateUtils {

  public static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
  public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  public static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

  private DateUtils() {
  }

  public static LocalDateTime now() {
    return LocalDateTime.now(VN_ZONE);
  }

  public static LocalDate today() {
    return LocalDate.now(VN_ZONE);
  }

  public static YearMonth currentMonth() {
    return YearMonth.now(VN_ZONE);
  }

  /**
   * Get the start of a given day (00:00:00).
   */
  public static LocalDateTime startOfDay(LocalDate date) {
    return date.atStartOfDay();
  }

  /**
   * Get the end of a given day (23:59:59.999999999).
   */
  public static LocalDateTime endOfDay(LocalDate date) {
    return date.atTime(LocalTime.MAX);
  }

  /**
   * Get the first day of a month.
   */
  public static LocalDate firstDayOfMonth(YearMonth yearMonth) {
    return yearMonth.atDay(1);
  }

  /**
   * Get the last day of a month.
   */
  public static LocalDate lastDayOfMonth(YearMonth yearMonth) {
    return yearMonth.atEndOfMonth();
  }

  /**
   * Check if a date falls within a month.
   */
  public static boolean isInMonth(LocalDate date, YearMonth yearMonth) {
    return YearMonth.from(date).equals(yearMonth);
  }

  /**
   * Get remaining days in the month from a given date.
   */
  public static int remainingDaysInMonth(LocalDate fromDate) {
    LocalDate lastDay = fromDate.with(TemporalAdjusters.lastDayOfMonth());
    return (int) (lastDay.toEpochDay() - fromDate.toEpochDay());
  }

  /**
   * Format a date to yyyy-MM string.
   */
  public static String toYearMonth(LocalDate date) {
    return YearMonth.from(date).format(MONTH_FORMAT);
  }
}
