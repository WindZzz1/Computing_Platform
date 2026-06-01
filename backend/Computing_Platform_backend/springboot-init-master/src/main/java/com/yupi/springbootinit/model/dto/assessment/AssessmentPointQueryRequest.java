package com.yupi.springbootinit.model.dto.assessment;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

//考核点查询请求

@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentPointQueryRequest extends PageRequest {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程目标ID
     */
    private Long objectiveId;

    /**
     * 考核点编号
     */
    private String pointCode;

    /**
     * 考核点名称
     */
    private String pointName;
}
