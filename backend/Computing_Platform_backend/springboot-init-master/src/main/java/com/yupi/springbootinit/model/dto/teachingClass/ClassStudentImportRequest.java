package com.yupi.springbootinit.model.dto.teachingClass;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 教学班级学生批量导入请求
 *
 * @author YU
 */
@Data
public class ClassStudentImportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教学班级ID
     */
    private Long classId;

    /**
     * 学生列表
     */
    private List<StudentItem> students;

    /**
     * 学生导入项
     */
    @Data
    public static class StudentItem implements Serializable {
        /**
         * 学号
         */
        private String studentNo;

        /**
         * 姓名
         */
        private String studentName;
    }
}
