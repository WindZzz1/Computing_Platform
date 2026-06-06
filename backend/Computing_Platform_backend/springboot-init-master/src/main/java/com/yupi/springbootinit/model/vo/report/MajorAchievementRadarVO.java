package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 专业达成度雷达图数据VO
 *
 * @author YU
 */
@Data
public class MajorAchievementRadarVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 专业代码
     */
    private String majorCode;

    /**
     * 学年名称
     */
    private String yearName;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 年级
     */
    private String grade;

    /**
     * 指标点达成度数据
     */
    private List<IndicatorPointAchievementVO> indicatorPoints;

    /**
     * 数据生成时间
     */
    private Date generatedTime;
}