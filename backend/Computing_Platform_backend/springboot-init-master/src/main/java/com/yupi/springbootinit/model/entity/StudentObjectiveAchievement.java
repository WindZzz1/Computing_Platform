package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生课程目标达成度表（一级达成度）
 * @TableName student_objective_achievement
 */
@TableName(value = "student_objective_achievement")
@Data
public class StudentObjectiveAchievement {

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
     * 学生ID
     */
    private Long studentId;

    /**
     * 课程目标ID
     */
    @TableField("objective_id")
    private Long objectiveId;

    /**
     * 课程目标编号
     */
    @TableField("objective_code")
    private String objectiveCode;

    /**
     * 课程目标名称
     */
    @TableField("objective_name")
    private String objectiveName;

    /**
     * 一级达成度值
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
