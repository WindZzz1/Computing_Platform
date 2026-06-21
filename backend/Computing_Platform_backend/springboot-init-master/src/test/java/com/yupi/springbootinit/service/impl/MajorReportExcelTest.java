package com.yupi.springbootinit.service.impl;

import com.yupi.springbootinit.model.dto.report.MajorReportRequest;
import com.yupi.springbootinit.model.vo.report.AssessmentPointAccount;
import com.yupi.springbootinit.model.vo.report.CourseAccountInfo;
import com.yupi.springbootinit.model.vo.report.MajorAccountInfo;
import com.yupi.springbootinit.model.vo.report.PenetrationAccountVO;
import com.yupi.springbootinit.model.vo.report.StudentObjectiveAccount;
import com.yupi.springbootinit.model.vo.report.StudentScoreAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * {@link MajorReportServiceImpl#exportPenetrationAccountExcel} 单元测试。
 * <p>
 * 用 @Spy 桩掉 getPenetrationAccount（台账数据组装在 PR-2 已测），这里只验证
 * Excel 渲染：返回非空字节数、且是合法 xlsx（zip 魔数 PK）。
 */
@ExtendWith(MockitoExtension.class)
class MajorReportExcelTest {

    @Spy
    @InjectMocks
    private MajorReportServiceImpl majorReportService;

    private MajorReportRequest req() {
        MajorReportRequest r = new MajorReportRequest();
        r.setMajorId(1L);
        r.setTermId(2L);
        r.setGrade("2021");
        return r;
    }

    private PenetrationAccountVO sampleVO() {
        PenetrationAccountVO vo = new PenetrationAccountVO();
        MajorAccountInfo info = new MajorAccountInfo();
        info.setMajorName("软件工程");
        info.setYearName("2024-2025");
        info.setSemesterName("第一学期");
        info.setGrade("2021");
        info.setTotalCourses(1);
        info.setTotalStudents(1);
        info.setOverallAchievement(0.72);
        vo.setMajorInfo(info);

        CourseAccountInfo c = new CourseAccountInfo();
        c.setCourseCode("CS101");
        c.setCourseName("数据结构");
        c.setClassName("软工21-1班");
        c.setTeacherName("王老师");
        c.setStudentCount(1);
        c.setCourseIndicatorAchievement(new BigDecimal("0.7500"));
        vo.setCourses(Collections.singletonList(c));

        StudentObjectiveAccount soa = new StudentObjectiveAccount();
        soa.setStudentNo("S001");
        soa.setStudentName("张三");
        soa.setCourseName("数据结构");
        Map<String, BigDecimal> objMap = new LinkedHashMap<>();
        objMap.put("CO1", new BigDecimal("0.8000"));
        soa.setObjectiveAchievements(objMap);
        soa.setAverageAchievement(new BigDecimal("0.8000"));
        vo.setStudentObjectives(Collections.singletonList(soa));

        AssessmentPointAccount ap = new AssessmentPointAccount();
        ap.setCourseName("数据结构");
        ap.setAssessmentPointCode("AP1");
        ap.setAssessmentPointName("期末大题1");
        ap.setObjectiveCode("CO1");
        ap.setFullScore(new BigDecimal("100"));
        ap.setWeight(new BigDecimal("1.0"));
        ap.setClassAverageScore(new BigDecimal("80"));
        vo.setAssessmentPoints(Collections.singletonList(ap));

        StudentScoreAccount ss = new StudentScoreAccount();
        ss.setStudentNo("S001");
        ss.setStudentName("张三");
        ss.setCourseName("数据结构");
        ss.setAssessmentPointCode("AP1");
        ss.setAssessmentPointName("期末大题1");
        ss.setFullScore(new BigDecimal("100"));
        ss.setScore(new BigDecimal("80"));
        ss.setAchievement(new BigDecimal("0.8000"));
        vo.setStudentScores(Collections.singletonList(ss));
        return vo;
    }

    @Test
    void export_returnsValidXlsxBytes() {
        doReturn(sampleVO()).when(majorReportService).getPenetrationAccount(any());

        byte[] bytes = majorReportService.exportPenetrationAccountExcel(req());

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        // xlsx 本质是 zip，魔数 "PK" (0x50 0x4B)
        assertEquals(0x50, bytes[0] & 0xFF);
        assertEquals(0x4B, bytes[1] & 0xFF);
    }
}
