package com.yupi.springbootinit.model.dto.student;

import lombok.Data;

import java.io.Serializable;

/**
 * 学生更新请求
 *
 * @author YU
 */
@Data
public class StudentUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

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