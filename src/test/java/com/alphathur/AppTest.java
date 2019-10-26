package com.alphathur;

import static org.junit.Assert.assertTrue;

import com.alphathur.excel.ExcelTitle;
import com.alphathur.excel.ExcelWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {


    @Test
    public void shouldAnswerWithTrue() {
        try {
            List<Student> students = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < 100; i++) {
                students.add(new Student(i, "member" + i, i * 55D, new Date(), calendar));
            }
//            ExcelWritter.setExcelValueFormatter(new TextValueFormatter());
            ExcelWriter
                .writeToExcel(students, Student.class, "/Users/zhuhuiyuan/Downloads/2.xlsx");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        assertTrue(true);
    }

    @Data
    @AllArgsConstructor
    public static class Student {
        @ExcelTitle("id")
        private Integer id;

        @ExcelTitle("姓名")
        private String name;

        @ExcelTitle("薪水")
        private Double salary;

        @ExcelTitle("生日")
        private Date birthDay;

        private Calendar calendar;
    }
}
