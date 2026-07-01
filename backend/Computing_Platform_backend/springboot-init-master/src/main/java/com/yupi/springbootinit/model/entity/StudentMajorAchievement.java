package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生专业达成度表（学生×指标点）
 * @TableName student_major_achievement
 */
@TableName(value = "student_major_achievement")
@Data
public class StudentMajorAchievement {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

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
     * 毕业要求ID
     */
    @TableField("requirement_id")
    private Long requirementId;

    /**
     * 毕业要求编号
     */
    @TableField("requirement_code")
    private String requirementCode;

    /**
     * 毕业要求名称
     */
    @TableField("requirement_name")
    private String requirementName;

    /**
     * 学生专业达成度值
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

    /**
     * 学号（join student 表填充，非本表字段）
     */
    @TableField(exist = false)
    private String studentNo;

    /**
     * 学生姓名（join student 表填充，非本表字段）
     */
    @TableField(exist = false)
    private String studentName;
}
