package com.alphathur.excel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DateValueFormatter implements ExcelValueFormatter {

  private String dateFormat;

  @Override
  public Object formatValue(Class<?> returnType, Object value) {
    if (returnType.equals(Date.class)) {
      return DateTimeFormatter.ofPattern(dateFormat)
          .format(toLocalDateTime((Date) value));
    } else {
      return value;
    }
  }

  private static LocalDateTime toLocalDateTime(Date date) {
    Instant instant = date.toInstant();
    ZoneId zoneId = ZoneId.systemDefault();
    return instant.atZone(zoneId).toLocalDateTime();
  }
}