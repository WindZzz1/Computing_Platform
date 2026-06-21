package com.yupi.springbootinit.service.impl;

import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.MajorIndicatorAchievementMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.mapper.SysDictSchoolYearMapper;
import com.yupi.springbootinit.model.dto.report.MajorReportRequest;
import com.yupi.springbootinit.model.entity.MajorIndicatorAchievement;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.entity.SysDictSchoolYear;
import com.yupi.springbootinit.model.vo.report.MajorAchievementRadarVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link MajorReportServiceImpl#getRadarChartData} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class MajorReportServiceImplTest {

    @InjectMocks
    private MajorReportServiceImpl majorReportService;

    @Mock
    private MajorIndicatorAchievementMapper majorIndicatorAchievementMapper;
    @Mock
    private SysDictMajorMapper sysDictMajorMapper;
    @Mock
    private SysDictSchoolYearMapper sysDictSchoolYearMapper;

    private MajorReportRequest request(Long majorId, Long termId, String grade) {
        MajorReportRequest r = new MajorReportRequest();
        r.setMajorId(majorId);
        r.setTermId(termId);
        r.setGrade(grade);
        return r;
    }

    private MajorIndicatorAchievement achievement(Long indicatorId, String code, String name,
                                                  BigDecimal value, String reqCode) {
        MajorIndicatorAchievement a = new MajorIndicatorAchievement();
        a.setIndicatorId(indicatorId);
        a.setIndicatorCode(code);
        a.setIndicatorName(name);
        a.setAchievement(value);
        a.setRequirementId(100L);
        a.setRequirementCode(reqCode);
        a.setRequirementName("毕业要求" + reqCode);
        return a;
    }

    /**
     * 三级达成度未计算 → 抛业务异常，引导先执行专业级计算。
     */
    @Test
    void radarData_notCalculated_throws() {
        when(majorIndicatorAchievementMapper.selectByMajorTermGrade(eq(1L), eq(2L), eq("2021")))
                .thenReturn(Collections.emptyList());

        assertThrows(BusinessException.class,
                () -> majorReportService.getRadarChartData(request(1L, 2L, "2021")));
    }

    /**
     * 三级达成度已计算 → 专业/学期/指标点字段正确映射。
     */
    @Test
    void radarData_calculated_mapsFields() {
        when(majorIndicatorAchievementMapper.selectByMajorTermGrade(eq(1L), eq(2L), eq("2021")))
                .thenReturn(Arrays.asList(
                        achievement(11L, "1.1", "指标1.1", new BigDecimal("0.8123"), "GR1"),
                        achievement(12L, "1.2", "指标1.2", new BigDecimal("0.6700"), "GR1")));

        SysDictMajor major = new SysDictMajor();
        major.setMajorName("软件工程");
        major.setMajorCode("080902");
        when(sysDictMajorMapper.selectById(1L)).thenReturn(major);

        SysDictSchoolYear term = new SysDictSchoolYear();
        term.setYearName("2024-2025");
        term.setSemesterName("第一学期");
        when(sysDictSchoolYearMapper.selectById(2L)).thenReturn(term);

        MajorAchievementRadarVO vo = majorReportService.getRadarChartData(request(1L, 2L, "2021"));

        assertEquals(1L, vo.getMajorId());
        assertEquals("软件工程", vo.getMajorName());
        assertEquals("080902", vo.getMajorCode());
        assertEquals("2024-2025", vo.getYearName());
        assertEquals("第一学期", vo.getSemesterName());
        assertEquals("2021", vo.getGrade());
        assertNotNull(vo.getGeneratedTime());
        assertEquals(2, vo.getIndicatorPoints().size());
        assertEquals("1.1", vo.getIndicatorPoints().get(0).getIndicatorCode());
        assertEquals(0, new BigDecimal("0.8123").compareTo(vo.getIndicatorPoints().get(0).getAchievement()));
        assertEquals("GR1", vo.getIndicatorPoints().get(0).getRequirementCode());
    }

    /**
     * 学期字典为空（termId 查不到）→ 不抛异常，yearName/semesterName 为 null，雷达图照常返回。
     */
    @Test
    void radarData_termMissing_keepsIndicatorPoints() {
        when(majorIndicatorAchievementMapper.selectByMajorTermGrade(anyLong(), anyLong(), eq("2021")))
                .thenReturn(Collections.singletonList(
                        achievement(11L, "1.1", "指标1.1", new BigDecimal("0.5"), "GR1")));
        when(sysDictMajorMapper.selectById(anyLong())).thenReturn(null);
        when(sysDictSchoolYearMapper.selectById(anyLong())).thenReturn(null);

        MajorAchievementRadarVO vo = majorReportService.getRadarChartData(request(1L, 2L, "2021"));

        assertEquals(1, vo.getIndicatorPoints().size());
    }

    /**
     * 三级达成度已计算 → 导出 Excel 为合法 xlsx（zip 流，PK 文件头）。
     */
    @Test
    void exportIndicatorExcel_returnsValidXlsx() {
        when(majorIndicatorAchievementMapper.selectByMajorTermGrade(eq(1L), eq(2L), eq("2021")))
                .thenReturn(Arrays.asList(
                        achievement(11L, "1.1", "指标1.1", new BigDecimal("0.8123"), "GR1"),
                        achievement(12L, "1.2", "指标1.2", new BigDecimal("0.6700"), "GR1")));
        SysDictMajor major = new SysDictMajor();
        major.setMajorName("软件工程");
        major.setMajorCode("080902");
        when(sysDictMajorMapper.selectById(1L)).thenReturn(major);
        SysDictSchoolYear term = new SysDictSchoolYear();
        term.setYearName("2024-2025");
        term.setSemesterName("第一学期");
        when(sysDictSchoolYearMapper.selectById(2L)).thenReturn(term);

        byte[] bytes = majorReportService.exportIndicatorAchievementExcel(request(1L, 2L, "2021"));

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        // xlsx 本质是 zip 流，文件头为 "PK"
        assertEquals(0x50, bytes[0] & 0xFF); // P
        assertEquals(0x4B, bytes[1] & 0xFF); // K
    }

    /**
     * 三级达成度已计算 → 导出 PDF 为合法 PDF（%PDF 文件头），且中文字体可正常加载。
     */
    @Test
    void exportIndicatorPdf_returnsValidPdf() {
        when(majorIndicatorAchievementMapper.selectByMajorTermGrade(eq(1L), eq(2L), eq("2021")))
                .thenReturn(Arrays.asList(
                        achievement(11L, "1.1", "指标1.1", new BigDecimal("0.8123"), "GR1"),
                        achievement(12L, "1.2", "指标1.2", new BigDecimal("0.6700"), "GR1")));
        SysDictMajor major = new SysDictMajor();
        major.setMajorName("软件工程");
        major.setMajorCode("080902");
        when(sysDictMajorMapper.selectById(1L)).thenReturn(major);
        SysDictSchoolYear term = new SysDictSchoolYear();
        term.setYearName("2024-2025");
        term.setSemesterName("第一学期");
        when(sysDictSchoolYearMapper.selectById(2L)).thenReturn(term);

        byte[] bytes = majorReportService.exportIndicatorAchievementPdf(request(1L, 2L, "2021"));

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        // PDF 文件头 "%PDF"
        assertEquals(0x25, bytes[0] & 0xFF); // %
        assertEquals(0x50, bytes[1] & 0xFF); // P
        assertEquals(0x44, bytes[2] & 0xFF); // D
        assertEquals(0x46, bytes[3] & 0xFF); // F
    }
}
