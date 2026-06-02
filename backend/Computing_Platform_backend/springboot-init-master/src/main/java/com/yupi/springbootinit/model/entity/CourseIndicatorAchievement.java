package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程级指标点达成度表（二级达成度）
 * 存储教学班级的课程指标点达成度
 * @TableName result_course_indicator
 */
@TableName("result_course_indicator")
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
    private Long teachingClassId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 指标点ID
     */
    private Long indicatorId;

    /**
     * 课程达成度Ek
     * 计算公式：Σ(学生课程目标达成度 * 权重) / 班级人数
     */
    private BigDecimal courseAchievement;

    /**
     * 计算时间
     */
    private Date calculateTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}
