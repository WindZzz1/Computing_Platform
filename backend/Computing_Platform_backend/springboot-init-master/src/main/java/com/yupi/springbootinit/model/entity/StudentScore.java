package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生考核点原始成绩表
 * @TableName student_score
 */
@TableName("student_score")
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
    private Long teachingClassId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 考核点ID
     */
    private Long pointId;

    /**
     * 实际得分
     */
    private BigDecimal actualScore;

    /**
     * 满分值
     */
    private BigDecimal fullScore;

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
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}
