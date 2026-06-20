package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.mapper.CourseIndicatorAchievementMapper;
import com.yupi.springbootinit.mapper.StudentScoreMapper;
import com.yupi.springbootinit.manager.GradesheetStatusHelper.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link GradesheetStatusHelper} 三态状态推断测试。
 */
@ExtendWith(MockitoExtension.class)
class GradesheetStatusHelperTest {

    @InjectMocks
    private GradesheetStatusHelper helper;

    @Mock
    private StudentScoreMapper studentScoreMapper;

    @Mock
    private CourseIndicatorAchievementMapper courseIndicatorAchievementMapper;

    @Test
    void locked_whenAchievementExists() {
        // 有达成度结果 → LOCKED；且不应再查成绩
        when(courseIndicatorAchievementMapper.selectCount(any())).thenReturn(5L);

        assertEquals(Status.LOCKED, helper.getStatus(1L));
        assertTrue(helper.isLocked(1L));
        verifyNoInteractions(studentScoreMapper);
    }

    @Test
    void submitted_whenScoresButNoAchievement() {
        // 有成绩、无达成度结果 → SUBMITTED（已提交未计算）
        when(courseIndicatorAchievementMapper.selectCount(any())).thenReturn(0L);
        when(studentScoreMapper.selectCount(any())).thenReturn(10L);

        assertEquals(Status.SUBMITTED, helper.getStatus(1L));
        assertFalse(helper.isLocked(1L));
    }

    @Test
    void notSubmitted_whenNoData() {
        // 都没有 → NOT_SUBMITTED
        when(courseIndicatorAchievementMapper.selectCount(any())).thenReturn(0L);
        when(studentScoreMapper.selectCount(any())).thenReturn(0L);

        assertEquals(Status.NOT_SUBMITTED, helper.getStatus(1L));
        assertFalse(helper.isLocked(1L));
    }

    @Test
    void nullClassId_returnsNotSubmitted() {
        assertEquals(Status.NOT_SUBMITTED, helper.getStatus(null));
        // classId 为空时不应查库
        verifyNoInteractions(studentScoreMapper, courseIndicatorAchievementMapper);
    }
}
