package com.yupi.springbootinit.model.dto.student;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentQueryRequest extends PageRequest {

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 姓名
     */
    private String studentName;

    /**
     * 专业 id
     */
    private Long majorId;

    /**
     * 班级名称
     */
    private String className;
}
