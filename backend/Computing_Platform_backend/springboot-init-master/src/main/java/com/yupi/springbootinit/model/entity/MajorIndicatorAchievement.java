package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 专业级指标点达成度表（三级达成度）
 * @TableName major_indicator_achievement
 */
@TableName(value = "major_indicator_achievement")
@Data
public class MajorIndicatorAchievement {

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
     * 三级达成度值
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
