package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 教学班级表
 * @TableName teaching_class
 */
@TableName(value = "teaching_class")
@Data
public class TeachingClass {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 主讲教师ID
     */
    private Long teacherId;

    /**
     * 学年学期ID
     */
    private Long termId;

    /**
     * 计算状态：0-未计算，1-已计算
     */
    private Integer calculatedStatus;

    /**
     * 锁定状态：0-未锁定，1-已锁定
     */
    private Integer lockedStatus;

    /**
     * 计算时间
     */
    private Date calculateTime;

    /**
     * 锁定时间
     */
    private Date lockTime;

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