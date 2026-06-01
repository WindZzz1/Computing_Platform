package com.yupi.springbootinit.model.dto.teachingClass;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 班级学生关联请求
 *
 * @author YU
 */
@Data
public class ClassStudentBindRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教学班级ID
     */
    private Long classId;

    /**
     * 学生ID列表
     */
    private List<Long> studentIds;
}