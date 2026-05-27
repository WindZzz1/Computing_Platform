package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 毕业要求二级指标点表
 * @TableName indicator
 */
@TableName("indicator")
@Data
public class Indicator {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 毕业要求ID
     */
    private Long reqId;

    /**
     * 指标点编号，例如 1.1、1.2
     */
    private String indicatorCode;

    /**
     * 指标点名称
     */
    private String indicatorName;

    /**
     * 指标点描述
     */
    private String indicatorDesc;

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

