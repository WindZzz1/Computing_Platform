package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 班级学生关联表
 * @TableName class_student
 */
@TableName(value = "class_student")
@Data
public class ClassStudent {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 教学班级ID
     */
    private Long classId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号（冗余字段，便于查询）
     */
    private String studentNo;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;
}