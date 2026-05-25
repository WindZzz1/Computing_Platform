package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 毕业要求表
 * @TableName graduation_requirement
 */
@TableName(value = "graduation_requirement")
@Data
public class GraduationRequirement {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 毕业要求编号(如GR1、GR2)
     */
    private String requirementCode;

    /**
     * 毕业要求名称(如工程知识、问题分析)
     */
    private String requirementName;

    /**
     * 毕业要求描述
     */
    private String description;

    /**
     * 所属专业ID
     */
    private Long majorId;

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
