package com.yupi.springbootinit.model.dto.assessment;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

//考核点更新请求

@Data
public class AssessmentPointUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 考核点ID
     */
    private Long id;

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
}
