package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程目标-指标点内部权重表
 * @TableName weight_objective_indicator
 */
@TableName("weight_objective_indicator")
@Data
public class WeightObjectiveIndicator {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程目标ID
     */
    private Long objectiveId;

    /**
     * 指标点ID
     */
    private Long indicatorId;

    /**
     * 内部贡献权重 wjk
     */
    private BigDecimal innerWeight;

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

