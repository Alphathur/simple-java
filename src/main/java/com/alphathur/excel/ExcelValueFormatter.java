package com.alphathur.excel;

public interface ExcelValueFormatter {
  Object formatValue(Class<?> returnType, Object value);
}
