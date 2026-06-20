package com.yupi.springbootinit.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.springbootinit.mapper.CourseIndicatorAchievementMapper;
import com.yupi.springbootinit.mapper.StudentScoreMapper;
import com.yupi.springbootinit.model.entity.CourseIndicatorAchievement;
import com.yupi.springbootinit.model.entity.StudentScore;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 成绩单锁定状态推断（"不改表"方案）。
 * <p>
 * 数据库无显式锁定字段，按现有数据三态推断：
 * <ul>
 *   <li>{@link Status#LOCKED} 已锁定（已计算）：该教学班有课程级达成度结果
 *       （course_indicator_achievement 有记录）</li>
 *   <li>{@link Status#SUBMITTED} 已提交未计算：有原始成绩（student_score）但无达成度结果</li>
 *   <li>{@link Status#NOT_SUBMITTED} 未提交：无原始成绩</li>
 * </ul>
 * 用于：C-3 防重算、C-3 成绩单锁定后不可改、C-4 看板"已锁定/未提交"展示。
 *
 * @author YU
 */
@Component
public class GradesheetStatusHelper {

    public enum Status {
        /** 未提交（无成绩） */
        NOT_SUBMITTED,
        /** 已提交未计算 */
        SUBMITTED,
        /** 已锁定（已计算） */
        LOCKED
    }

    @Resource
    private StudentScoreMapper studentScoreMapper;

    @Resource
    private CourseIndicatorAchievementMapper courseIndicatorAchievementMapper;

    /**
     * 推断教学班成绩单状态。
     */
    public Status getStatus(Long classId) {
        if (classId == null) {
            return Status.NOT_SUBMITTED;
        }
        if (countAchievement(classId) > 0) {
            return Status.LOCKED;
        }
        if (countScores(classId) > 0) {
            return Status.SUBMITTED;
        }
        return Status.NOT_SUBMITTED;
    }

    /**
     * 是否已锁定（已计算达成度）。锁定后禁止重复计算、禁止修改成绩。
     */
    public boolean isLocked(Long classId) {
        return getStatus(classId) == Status.LOCKED;
    }

    private long countAchievement(Long classId) {
        QueryWrapper<CourseIndicatorAchievement> query = new QueryWrapper<>();
        query.eq("teaching_class_id", classId);
        return courseIndicatorAchievementMapper.selectCount(query);
    }

    private long countScores(Long classId) {
        QueryWrapper<StudentScore> query = new QueryWrapper<>();
        query.eq("teaching_class_id", classId);
        return studentScoreMapper.selectCount(query);
    }
}
