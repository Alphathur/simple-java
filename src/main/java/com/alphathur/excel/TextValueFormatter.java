package com.alphathur.excel;

import lombok.Data;

@Data
public class TextValueFormatter implements ExcelValueFormatter {

  private DateValueFormatter dateValueFormatter;

  @Override
  public Object formatValue(Class<?> returnType, Object value) {
    if (dateValueFormatter != null) {
      value = dateValueFormatter.formatValue(returnType, value);
    }
    return value.toString();
  }
}
