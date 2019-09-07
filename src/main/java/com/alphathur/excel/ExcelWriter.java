package com.alphathur.excel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Slf4j
public class ExcelWriter {

  private static ThreadLocal<ExcelValueFormatter> valueFormatter = ThreadLocal
      .withInitial(() -> new DateValueFormatter("yyyy-MM-dd"));

  @SuppressWarnings("unused")
  public static void setExcelValueFormatter(ExcelValueFormatter excelValueFormatter) {
    valueFormatter.set(excelValueFormatter);
  }

  public static <E> void writeToExcel(List<E> list, Class<E> clazz, String fileName)
      throws InvocationTargetException, IllegalAccessException {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    List<Object[]> dataList = new ArrayList<>();
    Map<String, Method> fieldMethodMap = buildFieldMethodMap(clazz);
    Map<String, String> fieldTitleMap = buildFieldTitleMap(clazz, fieldMethodMap);
    List<Entry<String, Method>> methodEntrySet = new ArrayList<>(fieldMethodMap.entrySet());
    int addMark = 0;
    int itemSize = fieldTitleMap.size();
    String[] titleArr = new String[itemSize];
    for (E obj : list) {
      Object[] item = new Object[itemSize];
      for (int i = 0; i < methodEntrySet.size(); i++) {
        Entry<String, Method> methodEntry = methodEntrySet.get(i);
        String field = methodEntry.getKey();
        if (addMark < itemSize) {
          titleArr[addMark] = fieldTitleMap.get(field);
          addMark++;
        }
        Method method = methodEntry.getValue();
        Object value = formatValue(method, obj, valueFormatter.get());
        if (value != null) {
          item[i] = value;
        }
      }
      dataList.add(item);
    }
    writeObjectToExcel(dataList, titleArr, fileName);
  }

  private static Object formatValue(Method method, Object obj,
      ExcelValueFormatter excelValueFormatter)
      throws InvocationTargetException, IllegalAccessException {
    Object value = method.invoke(obj);
    if (value == null) {
      return null;
    }
    if(excelValueFormatter == null) {
      return value;
    }
    Class<?> returnType = method.getReturnType();
    return excelValueFormatter.formatValue(returnType, value);
  }

  private static <E> Map<String, Method> buildFieldMethodMap(Class<E> clazz) {
    List<Method> getMethods = Arrays.stream(clazz.getMethods())
        .filter(
            method -> method.getName().startsWith("get") && !method.getName().equals("getClass"))
        .collect(
            Collectors.toList());
    Map<String, Method> fieldMethodMap = new LinkedHashMap<>();
    for (Method getMethod : getMethods) {
      String m = getMethod.getName().replace("get", "");
      String field = m.substring(0, 1).toLowerCase() + m.substring(1);
      fieldMethodMap.put(field, getMethod);
    }
    return fieldMethodMap;
  }

  public static <E> Field[] getAllFields(Class<E> clazz){
    List<Field> fieldList = new ArrayList<>();
    while (clazz != null){
      fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
      clazz = (Class<E>) clazz.getSuperclass();
    }
    Field[] fields = new Field[fieldList.size()];
    fieldList.toArray(fields);
    return fields;
  }

  private static <E> Map<String, String> buildFieldTitleMap(Class<E> clazz,
      Map<String, Method> fieldMethodMap) {
    Map<String, String> fieldTitleMap = new LinkedHashMap<>();
    Field[] fields = getAllFields(clazz);
    Arrays.stream(fields).forEach(field -> {
      if (fieldMethodMap.containsKey(field.getName())) {
        ExcelTitle excelTitle = field.getAnnotation(ExcelTitle.class);
        String title = excelTitle == null ? field.getName() : excelTitle.value();
        fieldTitleMap.put(field.getName(), title);
      }
    });
    return fieldTitleMap;
  }

  private static void writeObjectToExcel(List<Object[]> list, String[]
      excelTitle, String fileName) {
    //在内存中创建Excel文件
    Workbook workbook;
    if (fileName.endsWith("xls")) {
      workbook = new HSSFWorkbook();
    } else if (fileName.endsWith("xlsx")) {
      workbook = new XSSFWorkbook();
    } else {
      throw new IllegalArgumentException("fileName not legal");
    }
    Sheet sheet = workbook.createSheet();
    //标题行
    Row titleRow = sheet.createRow(0);
    for (int i = 0; i < excelTitle.length; i++) {
      titleRow.createCell(i).setCellValue(excelTitle[i]);
    }
    //创建数据行并写入值
    for (Object[] dataArr : list) {
      int lastRowNum = sheet.getLastRowNum();
      Row dataRow = sheet.createRow(lastRowNum + 1);
      for (int i = 0; i < dataArr.length; i++) {
        Cell cell = dataRow.createCell(i);
        Object cellValue = dataArr[i];
        if(cellValue != null) {
          setCellValue(cellValue, cell);
        }
      }
    }
    //创建输出流对象
    FileOutputStream outputStream = null;
    try {
      outputStream = new FileOutputStream(new File(fileName));
    } catch (FileNotFoundException e) {
      log.error("file not found", e);
    }
    try {
      workbook.write(outputStream);
    } catch (IOException e) {
      log.error("write to file failed", e);
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException ignore) {
        }
      }
    }
  }

  private static void setCellValue(Object cellValue, Cell cell) {
    if (cellValue instanceof Boolean) {
      cell.setCellValue((boolean) cellValue);
    } else if (cellValue instanceof String) {
      cell.setCellValue(cellValue.toString());
    } else if (cellValue instanceof Double || cellValue instanceof Integer
        || cellValue instanceof Long) {
      cell.setCellValue(Double.valueOf(cellValue.toString()));
    } else if (cellValue instanceof Date) {
      cell.setCellValue((Date) cellValue);
    } else if (cellValue instanceof Calendar) {
      cell.setCellValue((Calendar) cellValue);
    } else if (cellValue instanceof RichTextString) {
      cell.setCellValue((RichTextString) cellValue);
    } else {
      cell.setCellValue(cellValue.toString());
    }
  }
}
