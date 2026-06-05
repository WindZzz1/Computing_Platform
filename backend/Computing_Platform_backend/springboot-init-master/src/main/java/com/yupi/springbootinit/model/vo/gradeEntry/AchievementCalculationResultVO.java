package com.yupi.springbootinit.model.vo.gradeEntry;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 达成度计算结果VO
 *
 * @author YU
 */
@Data
public class AchievementCalculationResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 计算状态：0-未计算，1-计算中，2-计算完成，3-计算失败
     */
    private Integer calcStatus;

    /**
     * 是否已锁定
     */
    private Boolean isLocked;

    /**
     * 锁定时间
     */
    private Date lockTime;

    /**
     * 计算开始时间
     */
    private Date calcStartTime;

    /**
     * 计算完成时间
     */
    private Date calcEndTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 一级达成度统计
     */
    private LevelOneAchievementStats levelOneStats;

    /**
     * 二级达成度统计
     */
    private LevelTwoAchievementStats levelTwoStats;

    @Data
    public static class LevelOneAchievementStats implements Serializable {
        /**
         * 学生总数
         */
        private Integer totalStudents;

        /**
         * 课程目标总数
         */
        private Integer totalObjectives;

        /**
         * 一级达成度记录总数
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
    }

    @Data
    public static class LevelTwoAchievementStats implements Serializable {
        /**
         * 指标点总数
         */
        private Integer totalIndicators;

        /**
         * 二级达成度记录总数
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
         * 达成度值
         */
        private BigDecimal achievement;
    }
}
