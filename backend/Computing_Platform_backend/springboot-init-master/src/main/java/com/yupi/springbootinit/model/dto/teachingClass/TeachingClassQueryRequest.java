package com.yupi.springbootinit.model.dto.teachingClass;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教学班级查询请求
 *
 * @author YU
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeachingClassQueryRequest extends PageRequest {

    /**
     * 班级名称
     */
    private String className;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 主讲教师ID
     */
    private Long teacherId;

    /**
     * 学年ID
     */
    private Long schoolYearId;

    /**
     * 学期
     */
    private Integer semester;

    /**
     * 所属专业ID
     */
    private Long majorId;
}