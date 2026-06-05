package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生成绩表
 * @TableName student_score
 */
@TableName(value = "student_score")
@Data
public class StudentScore {
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
     * 考核点ID
     */
    private Long assessmentPointId;

    /**
     * 得分
     */
    private BigDecimal score;

    /**
     * 是否锁定：0-未锁定，1-已锁定
     */
    private Integer isLocked;

    /**
     * 录入人ID
     */
    @TableField("entered_by")
    private Long enteredBy;

    /**
     * 录入时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date enterTime;

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
