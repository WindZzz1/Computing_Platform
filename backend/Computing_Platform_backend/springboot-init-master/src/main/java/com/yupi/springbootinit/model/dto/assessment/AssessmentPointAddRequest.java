package com.yupi.springbootinit.model.dto.assessment;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

//考核点新增请求

@Data
public class AssessmentPointAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 考核点编号
     */
    private String pointCode;

    /**
     * 考核点名称
     */
    private String pointName;

    /**
     * 满分值
     */
    private BigDecimal fullScore;

    /**
     * 关联课程目标ID
     */
    private Long objectiveId;

    /**
     * 关联课程目标ID列表
     */
    private List<Long> objectiveIds;
}
