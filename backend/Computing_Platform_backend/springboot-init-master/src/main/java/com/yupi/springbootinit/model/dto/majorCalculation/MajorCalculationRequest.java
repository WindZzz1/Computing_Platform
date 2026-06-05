package com.yupi.springbootinit.model.dto.majorCalculation;

import lombok.Data;

import java.io.Serializable;

/**
 * 专业级达成度计算请求
 *
 * @author YU
 */
@Data
public class MajorCalculationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 学年学期ID
     */
    private Long termId;

    /**
     * 年级
     */
    private String grade;

    /**
     * 是否强制重新计算
     */
    private Boolean forceRecalculate;
}
