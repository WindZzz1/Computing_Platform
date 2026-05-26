package com.yupi.springbootinit.model.dto.course;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

// 课程目标查询请求

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseObjectiveQueryRequest extends PageRequest {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程目标编号
     */
    private String objCode;

    /**
     * 课程目标名称
     */
    private String objName;
}
