package com.yupi.springbootinit.service.impl;

import com.yupi.springbootinit.model.vo.report.CourseAchievementReportVO;
import com.yupi.springbootinit.model.vo.report.CourseIndicatorAchievementVO;
import com.yupi.springbootinit.model.vo.report.ObjectiveAchievementSummaryVO;
import com.yupi.springbootinit.model.vo.report.StudentAchievementDetailVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

/**
 * {@link CourseAchievementReportServiceImpl#exportPdfReport} 单元测试。
 * <p>
 * 用 @Spy 桩 generateReportData（数据组装已在别处），这里验证 PDF 渲染：
 * 嵌入 CJK 字体不抛异常、返回非空字节、首4字节为 "%PDF"。
 */
@ExtendWith(MockitoExtension.class)
class CourseReportPdfTest {

    @Spy
    @InjectMocks
    private CourseAchievementReportServiceImpl courseAchievementReportService;

    private CourseAchievementReportVO sampleVO() {
        CourseAchievementReportVO vo = new CourseAchievementReportVO();
        vo.setClassId(1L);
        vo.setClassName("软工21-1班");
        vo.setCourseCode("CS101");
        vo.setCourseName("数据结构");
        vo.setTeacherName("王老师");
        vo.setYearName("2024-2025");
        vo.setSemesterName("第一学期");
        vo.setStudentCount(1);
        vo.setCalculationTime(new Date());

        ObjectiveAchievementSummaryVO s1 = new ObjectiveAchievementSummaryVO();
        s1.setObjectiveCode("CO1");
        s1.setObjectiveName("掌握数据结构基本概念");
        s1.setClassAverage(new BigDecimal("0.8000"));
        s1.setPassRate(new BigDecimal("0.9000"));
        vo.setObjectiveSummaries(Collections.singletonList(s1));

        CourseIndicatorAchievementVO i1 = new CourseIndicatorAchievementVO();
        i1.setIndicatorCode("1.1");
        i1.setIndicatorName("指标点1.1");
        i1.setAchievement(new BigDecimal("0.7500"));
        vo.setIndicatorAchievements(Collections.singletonList(i1));

        StudentAchievementDetailVO d1 = new StudentAchievementDetailVO();
        d1.setStudentNo("S001");
        d1.setStudentName("张三");
        Map<String, BigDecimal> m = new LinkedHashMap<>();
        m.put("CO1", new BigDecimal("0.8000"));
        d1.setObjectiveAchievements(m);
        d1.setAverageAchievement(new BigDecimal("0.8000"));
        vo.setStudentDetails(Arrays.asList(d1));
        return vo;
    }

    @Test
    void exportPdf_returnsValidPdfWithCjkFont() {
        doReturn(sampleVO()).when(courseAchievementReportService).generateReportData(anyLong());

        byte[] bytes = courseAchievementReportService.exportPdfReport(1L);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        // PDF 魔数 "%PDF"
        assertEquals('%', (char) (bytes[0] & 0xFF));
        assertEquals('P', (char) (bytes[1] & 0xFF));
        assertEquals('D', (char) (bytes[2] & 0xFF));
        assertEquals('F', (char) (bytes[3] & 0xFF));
    }
}
