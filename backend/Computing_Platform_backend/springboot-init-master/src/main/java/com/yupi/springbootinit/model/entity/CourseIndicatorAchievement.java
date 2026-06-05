package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程指标点达成度表（二级达成度）
 * @TableName course_indicator_achievement
 */
@TableName(value = "course_indicator_achievement")
@Data
public class CourseIndicatorAchievement {

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
     * 课程ID
     */
    private Long courseId;

    /**
     * 指标点ID
     */
    @TableField("indicator_id")
    private Long indicatorId;

    /**
     * 指标点编号
     */
    @TableField("indicator_code")
    private String indicatorCode;

    /**
     * 指标点名称
     */
    @TableField("indicator_name")
    private String indicatorName;

    /**
     * 二级达成度值
     */
    private BigDecimal achievement;

    /**
     * 计算时间
     */
    @TableField("calculate_time")
    private Date calculateTime;

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
