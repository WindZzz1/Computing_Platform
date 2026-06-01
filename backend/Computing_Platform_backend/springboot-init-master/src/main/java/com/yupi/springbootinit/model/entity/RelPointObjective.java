package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 考核点-课程目标关联表
 * @TableName rel_point_objective
 */
@TableName("rel_point_objective")
@Data
public class RelPointObjective {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 考核点ID
     */
    private Long pointId;

    /**
     * 课程目标ID
     */
    private Long objectiveId;

    /**
     * 支撑权重
     */
    private BigDecimal weight;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}
