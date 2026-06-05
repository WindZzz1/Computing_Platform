package com.yupi.springbootinit.model.vo.gradeEntry;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 成绩计算状态VO
 *
 * @author YU
 */
@Data
public class GradeCalculationStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教学班级ID
     */
    private Long classId;

    /**
     * 是否已锁定
     */
    private Boolean isLocked;

    /**
     * 计算状态：0-未计算，1-计算中，2-计算完成，3-计算失败
     */
    private Integer calcStatus;

    /**
     * 计算开始时间
     */
    private Date calcStartTime;

    /**
     * 计算完成时间
     */
    private Date calcEndTime;

    /**
     * 锁定时间
     */
    private Date lockTime;

    /**
     * 锁定人ID
     */
    private Long lockedBy;

    /**
     * 错误信息
     */
    private String errorMessage;
}
