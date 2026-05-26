package com.yupi.springbootinit.model.dto.student;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 学生批量导入请求
 *
 * @author YU
 */
@Data
public class StudentImportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

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
        private String name;

        /**
         * 年级
         */
        private String grade;

        /**
         * 所属专业代码
         */
        private String majorCode;

        /**
         * 班级
         */
        private String className;

        /**
         * 联系方式
         */
        private String phone;

        /**
         * 邮箱
         */
        private String email;
    }
}