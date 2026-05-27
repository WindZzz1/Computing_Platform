package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

// 考核点视图对象

@Data
public class AssessmentPointVO implements Serializable {

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

    /**
     * 课程目标编号
     */
    private String objCode;

    /**
     * 课程目标名称
     */
    private String objName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
