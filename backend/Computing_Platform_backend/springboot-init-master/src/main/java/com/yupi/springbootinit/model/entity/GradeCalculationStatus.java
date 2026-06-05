package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 成绩计算状态表
 * @TableName grade_calculation_status
 */
@TableName(value = "grade_calculation_status")
@Data
public class GradeCalculationStatus {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 教学班级ID
     */
    @TableField("teaching_class_id")
    private Long classId;

    /**
     * 是否已锁定：0-未锁定，1-已锁定
     */
    private Integer isLocked;

    /**
     * 计算状态：0-未计算，1-计算中，2-计算完成，3-计算失败
     */
    @TableField("calc_status")
    private Integer calcStatus;

    /**
     * 计算开始时间
     */
    @TableField("calc_start_time")
    private Date calcStartTime;

    /**
     * 计算完成时间
     */
    @TableField("calc_end_time")
    private Date calcEndTime;

    /**
     * 锁定时间
     */
    @TableField("lock_time")
    private Date lockTime;

    /**
     * 锁定人ID
     */
    @TableField("locked_by")
    private Long lockedBy;

    /**
     * 锁定原因
     */
    @TableField("lock_reason")
    private String lockReason;

    /**
     * 错误信息（计算失败时记录）
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;
}
