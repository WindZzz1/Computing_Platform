package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 毕业要求视图对象
 *
 * @author YU
 */
@Data
public class GraduationRequirementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
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
     * 所属专业名称
     */
    private String majorName;

    /**
     * 所属学院ID
     */
    private Long collegeId;

    /**
     * 所属学院名称
     */
    private String collegeName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}