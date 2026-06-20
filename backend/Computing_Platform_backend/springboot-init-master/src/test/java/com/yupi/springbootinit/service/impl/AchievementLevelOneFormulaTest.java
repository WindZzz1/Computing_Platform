package com.yupi.springbootinit.service.impl;

import com.yupi.springbootinit.model.entity.AssessmentPoint;
import com.yupi.springbootinit.model.entity.StudentScore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link AchievementCalculationServiceImpl#computeObjectiveAchievement} 一级达成度公式测试。
 * <p>
 * 规约 3.1：C_ij = Σ(支撑目标 j 的考核点实际得分) / Σ(支撑目标 j 的考核点目标满分)。
 * 修复前为加权平均 Σ(得分/满分×权重)/Σ权重，二者结果不同。
 */
class AchievementLevelOneFormulaTest {

    private AssessmentPoint point(long id, String fullScore) {
        AssessmentPoint p = new AssessmentPoint();
        p.setId(id);
        p.setFullScore(new BigDecimal(fullScore));
        return p;
    }

    private StudentScore score(long pointId, String actual) {
        StudentScore s = new StudentScore();
        s.setPointId(pointId);
        s.setActualScore(new BigDecimal(actual));
        return s;
    }

    private Map<Long, StudentScore> scoreMap(StudentScore... scores) {
        Map<Long, StudentScore> map = new HashMap<>();
        for (StudentScore s : scores) {
            map.put(s.getPointId(), s);
        }
        return map;
    }

    /**
     * 规约示例：满分10得9 + 满分100得60 → (9+60)/(10+100) = 69/110 = 0.6273。
     * 旧加权实现会得到 0.75，借此验证公式已切换为得分之比。
     */
    @Test
    void ratioOfSums_matchesSpecExample() {
        List<AssessmentPoint> points = Arrays.asList(point(1, "10"), point(2, "100"));
        Map<Long, StudentScore> scores = scoreMap(score(1, "9"), score(2, "60"));

        assertEquals(new BigDecimal("0.6273"),
                AchievementCalculationServiceImpl.computeObjectiveAchievement(points, scores));
    }

    /**
     * 缺考：满分10得9 + 满分10无成绩 → 9/20 = 0.45（缺考按 0 计入分子、满分计入分母）。
     */
    @Test
    void missingScore_countsAsZeroInNumerator() {
        List<AssessmentPoint> points = Arrays.asList(point(1, "10"), point(2, "10"));
        Map<Long, StudentScore> scores = scoreMap(score(1, "9")); // 考核点2 缺考

        assertEquals(new BigDecimal("0.4500"),
                AchievementCalculationServiceImpl.computeObjectiveAchievement(points, scores));
    }

    /**
     * 满分：20/20 = 1.0000
     */
    @Test
    void perfectScore_returnsOne() {
        List<AssessmentPoint> points = Arrays.asList(point(1, "10"), point(2, "10"));
        Map<Long, StudentScore> scores = scoreMap(score(1, "10"), score(2, "10"));

        assertEquals(new BigDecimal("1.0000"),
                AchievementCalculationServiceImpl.computeObjectiveAchievement(points, scores));
    }

    /**
     * 所有考核点满分为 0 / 无有效满分 → 返回 null（不生成达成度记录）。
     */
    @Test
    void noFullScore_returnsNull() {
        List<AssessmentPoint> points = Arrays.asList(point(1, "0"));
        assertNull(AchievementCalculationServiceImpl.computeObjectiveAchievement(points, scoreMap()));
    }

    /**
     * 无考核点 → null。
     */
    @Test
    void emptyPoints_returnsNull() {
        assertNull(AchievementCalculationServiceImpl.computeObjectiveAchievement(
                Collections.emptyList(), scoreMap()));
    }

    /**
     * 学生成绩映射为空（学生无任何成绩）→ 0/Σ满分 = 0。
     */
    @Test
    void noScores_returnsZero() {
        List<AssessmentPoint> points = Arrays.asList(point(1, "10"), point(2, "10"));
        assertEquals(new BigDecimal("0.0000"),
                AchievementCalculationServiceImpl.computeObjectiveAchievement(points, Collections.emptyMap()));
    }
}