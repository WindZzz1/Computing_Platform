package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生课程目标达成度表（一级达成度）
 * 存储每个学生每个课程目标的达成度
 * @TableName result_student_objective
 */
@TableName("result_student_objective")
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
    private Long teachingClassId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 课程目标ID
     */
    private Long objectiveId;

    /**
     * 达成度值
     * 计算公式：Σ(考核点得分 / 考核点满分 * 权重) / Σ(权重)
     */
    private BigDecimal achievement;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}
