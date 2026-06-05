package com.yupi.springbootinit.model.vo.majorCalculation;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 专业级达成度计算结果VO
 *
 * @author YU
 */
@Data
public class MajorCalculationResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 学年学期ID
     */
    private Long termId;

    /**
     * 学年学期名称
     */
    private String termName;

    /**
     * 年级
     */
    private String grade;

    /**
     * 计算状态：0-未计算，1-计算中，2-计算完成，3-计算失败
     */
    private Integer calcStatus;

    /**
     * 涉及课程总数
     */
    private Integer totalCourses;

    /**
     * 已计算课程数
     */
    private Integer calculatedCourses;

    /**
     * 已锁定课程数
     */
    private Integer lockedCourses;

    /**
     * 计算开始时间
     */
    private Date calcStartTime;

    /**
     * 计算完成时间
     */
    private Date calcEndTime;

    /**
     * 计算人ID
     */
    private Long calculatedBy;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 三级达成度统计
     */
    private LevelThreeAchievementStats achievementStats;

    /**
     * 课程状态列表
     */
    private List<CourseCalculationStatusVO> courseStatusList;

    @Data
    public static class LevelThreeAchievementStats implements Serializable {
        /**
         * 指标点总数
         */
        private Integer totalIndicators;

        /**
         * 三级达成度记录总数
         */
        private Integer totalRecords;

        /**
         * 平均达成度
         */
        private BigDecimal averageAchievement;

        /**
         * 最低达成度
         */
        private BigDecimal minAchievement;

        /**
         * 最高达成度
         */
        private BigDecimal maxAchievement;

        /**
         * 达成度详情列表
         */
        private List<IndicatorAchievementDetail> achievements;

        /**
         * 是否满足毕业要求（所有指标点都达到阈值）
         */
        private Boolean meetsGraduationRequirement;

        /**
         * 达成度阈值（默认0.7）
         */
        private BigDecimal threshold;
    }

    @Data
    public static class IndicatorAchievementDetail implements Serializable {
        /**
         * 指标点ID
         */
        private Long indicatorId;

        /**
         * 指标点编号
         */
        private String indicatorCode;

        /**
         * 指标点名称
         */
        private String indicatorName;

        /**
         * 毕业要求ID
         */
        private Long requirementId;

        /**
         * 毕业要求编号
         */
        private String requirementCode;

        /**
         * 毕业要求名称
         */
        private String requirementName;

        /**
         * 三级达成度值
         */
        private BigDecimal achievement;

        /**
         * 是否达到阈值
         */
        private Boolean meetsThreshold;

        /**
         * 支撑课程数量
         */
        private Integer supportingCourseCount;
    }
}
