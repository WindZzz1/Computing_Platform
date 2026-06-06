package com.yupi.springbootinit.model.dto.gradeEntry;

import lombok.Data;

import java.io.Serializable;

/**
 * 成绩查询请求
 *
 * @author YU
 */
@Data
public class GradeEntryQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教学班级ID
     */
    private Long classId;

    /**
     * 学生ID（可选）
     */
    private Long studentId;

    /**
     * 考核点ID（可选）
     */
    private Long pointId;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long pageSize;
}