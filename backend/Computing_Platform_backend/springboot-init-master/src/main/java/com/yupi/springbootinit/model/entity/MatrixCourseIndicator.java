package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程-指标点宏观支撑矩阵表
 * @TableName matrix_course_indicator
 */
@TableName("matrix_course_indicator")
@Data
public class MatrixCourseIndicator {

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
     * 课程ID
     */
    private Long courseId;

    /**
     * 指标点ID
     */
    private Long indicatorId;

    /**
     * 宏观总支撑权重 Wc
     */
    private BigDecimal totalWeight;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}

