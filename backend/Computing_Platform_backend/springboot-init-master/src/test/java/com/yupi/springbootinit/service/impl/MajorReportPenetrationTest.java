package com.yupi.springbootinit.service.impl;

import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.MajorScopeHelper;
import com.yupi.springbootinit.mapper.AssessmentPointMapper;
import com.yupi.springbootinit.mapper.ClassStudentMapper;
import com.yupi.springbootinit.mapper.CourseIndicatorAchievementMapper;
import com.yupi.springbootinit.mapper.CourseMapper;
import com.yupi.springbootinit.mapper.CourseObjectiveMapper;
import com.yupi.springbootinit.mapper.MajorIndicatorAchievementMapper;
import com.yupi.springbootinit.mapper.RelPointObjectiveMapper;
import com.yupi.springbootinit.mapper.StudentMapper;
import com.yupi.springbootinit.mapper.StudentObjectiveAchievementMapper;
import com.yupi.springbootinit.mapper.StudentScoreMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.mapper.SysDictSchoolYearMapper;
import com.yupi.springbootinit.mapper.SysUserMapper;
import com.yupi.springbootinit.mapper.TeachingClassMapper;
import com.yupi.springbootinit.model.dto.report.MajorReportRequest;
import com.yupi.springbootinit.model.entity.AssessmentPoint;
import com.yupi.springbootinit.model.entity.ClassStudent;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.entity.CourseIndicatorAchievement;
import com.yupi.springbootinit.model.entity.CourseObjective;
import com.yupi.springbootinit.model.entity.MajorIndicatorAchievement;
import com.yupi.springbootinit.model.entity.RelPointObjective;
import com.yupi.springbootinit.model.entity.Student;
import com.yupi.springbootinit.model.entity.StudentObjectiveAchievement;
import com.yupi.springbootinit.model.entity.StudentScore;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.entity.SysDictSchoolYear;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.model.entity.TeachingClass;
import com.yupi.springbootinit.model.vo.report.PenetrationAccountVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * {@link MajorReportServiceImpl#getPenetrationAccount} 穿透式台账单元测试。
 */
@ExtendWith(MockitoExtension.class)
class MajorReportPenetrationTest {

    @InjectMocks
    private MajorReportServiceImpl majorReportService;

    @Mock private MajorIndicatorAchievementMapper majorIndicatorAchievementMapper;
    @Mock private SysDictMajorMapper sysDictMajorMapper;
    @Mock private SysDictSchoolYearMapper sysDictSchoolYearMapper;
    @Mock private MajorScopeHelper majorScopeHelper;
    @Mock private CourseMapper courseMapper;
    @Mock private TeachingClassMapper teachingClassMapper;
    @Mock private SysUserMapper sysUserMapper;
    @Mock private ClassStudentMapper classStudentMapper;
    @Mock private StudentMapper studentMapper;
    @Mock private CourseIndicatorAchievementMapper courseIndicatorAchievementMapper;
    @Mock private StudentObjectiveAchievementMapper studentObjectiveAchievementMapper;
    @Mock private StudentScoreMapper studentScoreMapper;
    @Mock private AssessmentPointMapper assessmentPointMapper;
    @Mock private RelPointObjectiveMapper relPointObjectiveMapper;
    @Mock private CourseObjectiveMapper courseObjectiveMapper;

    private MajorReportRequest req() {
        MajorReportRequest r = new MajorReportRequest();
        r.setMajorId(1L);
        r.setTermId(2L);
        r.setGrade("2021");
        return r;
    }

    /**
     * 三级达成度未计算 → 抛业务异常。
     */
    @Test
    void penetration_notCalculated_throws() {
        when(majorIndicatorAchievementMapper.selectByMajorTermGrade(anyLong(), anyLong(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(BusinessException.class, () -> majorReportService.getPenetrationAccount(req()));
    }

    /**
     * 五层完整组装 + 考核点多对多（关联两个目标）展开为两行。
     */
    @Test
    void penetration_fullAssembly_mapsAllLayers() {
        // 三级结果（非空 → 通过前置校验）
        MajorIndicatorAchievement ma = new MajorIndicatorAchievement();
        ma.setIndicatorId(11L);
        ma.setIndicatorCode("1.1");
        ma.setAchievement(new BigDecimal("0.7200"));
        when(majorIndicatorAchievementMapper.selectByMajorTermGrade(anyLong(), anyLong(), any()))
                .thenReturn(Collections.singletonList(ma));

        // scope 教学班
        TeachingClass tc = new TeachingClass();
        tc.setId(1L);
        tc.setCourseId(10L);
        tc.setTeacherId(20L);
        tc.setClassName("软工21-1班");
        when(majorScopeHelper.getTeachingClasses(anyLong(), anyLong(), any()))
                .thenReturn(Collections.singletonList(tc));

        // 课程 / 教师 / 学生
        Course course = new Course();
        course.setId(10L);
        course.setCourseCode("CS101");
        course.setCourseName("数据结构");
        when(courseMapper.selectBatchIds(any())).thenReturn(Collections.singletonList(course));

        SysUser teacher = new SysUser();
        teacher.setId(20L);
        teacher.setUsername("王老师");
        when(sysUserMapper.selectBatchIds(any())).thenReturn(Collections.singletonList(teacher));

        Student stu = new Student();
        stu.setId(100L);
        stu.setStudentNo("S001");
        stu.setName("张三");
        when(studentMapper.selectBatchIds(any())).thenReturn(Collections.singletonList(stu));

        // 一级结果（学生×课程目标）
        StudentObjectiveAchievement soa = new StudentObjectiveAchievement();
        soa.setStudentId(100L);
        soa.setClassId(1L);
        soa.setObjectiveId(40L);
        soa.setObjectiveCode("CO1");
        soa.setAchievement(new BigDecimal("0.8000"));
        when(studentObjectiveAchievementMapper.selectList(any())).thenReturn(Collections.singletonList(soa));

        // 二级结果（课程指标点）
        CourseIndicatorAchievement cia = new CourseIndicatorAchievement();
        cia.setClassId(1L);
        cia.setCourseId(10L);
        cia.setIndicatorId(11L);
        cia.setAchievement(new BigDecimal("0.7500"));
        when(courseIndicatorAchievementMapper.selectList(any())).thenReturn(Collections.singletonList(cia));

        // 原始成绩
        StudentScore score = new StudentScore();
        score.setClassId(1L);
        score.setStudentId(100L);
        score.setPointId(30L);
        score.setActualScore(new BigDecimal("80"));
        score.setFullScore(new BigDecimal("100"));
        when(studentScoreMapper.selectList(any())).thenReturn(Collections.singletonList(score));

        // 班级学生
        ClassStudent cs = new ClassStudent();
        cs.setClassId(1L);
        cs.setStudentId(100L);
        when(classStudentMapper.selectList(any())).thenReturn(Collections.singletonList(cs));

        // 考核点（关联两个目标 → 多对多）
        AssessmentPoint pt = new AssessmentPoint();
        pt.setId(30L);
        pt.setCourseId(10L);
        pt.setPointCode("AP1");
        pt.setPointName("期末大题1");
        pt.setFullScore(new BigDecimal("100"));
        when(assessmentPointMapper.selectList(any())).thenReturn(Collections.singletonList(pt));

        RelPointObjective rel1 = new RelPointObjective();
        rel1.setPointId(30L);
        rel1.setObjectiveId(40L);
        rel1.setWeight(new BigDecimal("1.0"));
        RelPointObjective rel2 = new RelPointObjective();
        rel2.setPointId(30L);
        rel2.setObjectiveId(41L);
        rel2.setWeight(new BigDecimal("1.0"));
        when(relPointObjectiveMapper.selectList(any())).thenReturn(Arrays.asList(rel1, rel2));

        CourseObjective co1 = new CourseObjective();
        co1.setId(40L);
        co1.setObjCode("CO1");
        CourseObjective co2 = new CourseObjective();
        co2.setId(41L);
        co2.setObjCode("CO2");
        when(courseObjectiveMapper.selectBatchIds(any())).thenReturn(Arrays.asList(co1, co2));

        SysDictMajor major = new SysDictMajor();
        major.setMajorName("软件工程");
        when(sysDictMajorMapper.selectById(anyLong())).thenReturn(major);
        SysDictSchoolYear term = new SysDictSchoolYear();
        term.setYearName("2024-2025");
        term.setSemesterName("第一学期");
        when(sysDictSchoolYearMapper.selectById(anyLong())).thenReturn(term);

        PenetrationAccountVO vo = majorReportService.getPenetrationAccount(req());

        // L1 专业概况
        assertEquals("软件工程", vo.getMajorInfo().getMajorName());
        assertEquals("2024-2025", vo.getMajorInfo().getYearName());
        assertEquals(1, vo.getMajorInfo().getTotalCourses());
        assertEquals(1, vo.getMajorInfo().getTotalStudents());
        assertEquals(0.72, vo.getMajorInfo().getOverallAchievement(), 0.0001);

        // L2 课程
        assertEquals(1, vo.getCourses().size());
        assertEquals("CS101", vo.getCourses().get(0).getCourseCode());
        assertEquals("数据结构", vo.getCourses().get(0).getCourseName());
        assertEquals("软工21-1班", vo.getCourses().get(0).getClassName());
        assertEquals("王老师", vo.getCourses().get(0).getTeacherName());
        assertEquals(1, vo.getCourses().get(0).getStudentCount());
        assertEquals(0, new BigDecimal("0.7500").compareTo(vo.getCourses().get(0).getCourseIndicatorAchievement()));

        // L3 学生课程目标
        assertEquals(1, vo.getStudentObjectives().size());
        assertEquals("S001", vo.getStudentObjectives().get(0).getStudentNo());
        assertEquals("张三", vo.getStudentObjectives().get(0).getStudentName());
        assertEquals(new BigDecimal("0.8000"), vo.getStudentObjectives().get(0).getObjectiveAchievements().get("CO1"));
        assertEquals(0, new BigDecimal("0.8000").compareTo(vo.getStudentObjectives().get(0).getAverageAchievement()));

        // L4 考核点：多对多展开为 2 行
        assertEquals(2, vo.getAssessmentPoints().size());
        assertEquals("AP1", vo.getAssessmentPoints().get(0).getAssessmentPointCode());
        assertEquals("数据结构", vo.getAssessmentPoints().get(0).getCourseName());
        assertEquals(0, new BigDecimal("100").compareTo(vo.getAssessmentPoints().get(0).getFullScore()));
        assertTrue(vo.getAssessmentPoints().stream().map(a -> a.getObjectiveCode())
                .anyMatch("CO1"::equals) && vo.getAssessmentPoints().stream().map(a -> a.getObjectiveCode())
                .anyMatch("CO2"::equals));

        // L5 学生原始得分
        assertEquals(1, vo.getStudentScores().size());
        assertEquals("S001", vo.getStudentScores().get(0).getStudentNo());
        assertEquals("AP1", vo.getStudentScores().get(0).getAssessmentPointCode());
        assertEquals(0, new BigDecimal("80").compareTo(vo.getStudentScores().get(0).getScore()));
        assertEquals(0, new BigDecimal("0.8000").compareTo(vo.getStudentScores().get(0).getAchievement()));
    }
}
