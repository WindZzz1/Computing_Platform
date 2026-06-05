package com.yupi.springbootinit.model.dto.gradeEntry;

import lombok.Data;

import java.io.Serializable;

/**
 * 达成度计算请求
 *
 * @author YU
 */
@Data
public class AchievementCalculationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教学班级ID
     */
    private Long classId;

    /**
     * 是否强制重新计算（即使已锁定）
     */
    private Boolean forceRecalculate;
}
