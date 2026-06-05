package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 专业级计算汇总表
 * @TableName major_calculation_summary
 */
@TableName(value = "major_calculation_summary")
@Data
public class MajorCalculationSummary {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 学年学期ID
     */
    @TableField("term_id")
    private Long termId;

    /**
     * 年级
     */
    private String grade;

    /**
     * 涉及课程总数
     */
    @TableField("total_courses")
    private Integer totalCourses;

    /**
     * 已计算课程数
     */
    @TableField("calculated_courses")
    private Integer calculatedCourses;

    /**
     * 已锁定课程数
     */
    @TableField("locked_courses")
    private Integer lockedCourses;

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
     * 计算人ID
     */
    @TableField("calculated_by")
    private Long calculatedBy;

    /**
     * 错误信息
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
