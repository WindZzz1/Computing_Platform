package com.yupi.springbootinit.model.dto.student;

import lombok.Data;

import java.io.Serializable;

/**
 * 学生新增请求
 *
 * @author YU
 */
@Data
public class StudentAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 姓名
     */
    private String studentName;

    /**
     * 年级
     */
    private String grade;

    /**
     * 所属专业ID
     */
    private Long majorId;
}