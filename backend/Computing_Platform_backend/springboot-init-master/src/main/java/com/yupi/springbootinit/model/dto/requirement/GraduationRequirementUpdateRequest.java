package com.yupi.springbootinit.model.dto.requirement;

import lombok.Data;

import java.io.Serializable;

/**
 * 毕业要求更新请求
 *
 * @author YU
 */
@Data
public class GraduationRequirementUpdateRequest implements Serializable {

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
}