package com.yupi.springbootinit.service.impl;

import com.yupi.springbootinit.mapper.ClassStudentMapper;
import com.yupi.springbootinit.mapper.CourseMapper;
import com.yupi.springbootinit.mapper.StudentMapper;
import com.yupi.springbootinit.mapper.TeachingClassMapper;
import com.yupi.springbootinit.model.entity.ClassStudent;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.entity.Student;
import com.yupi.springbootinit.model.entity.TeachingClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link MajorCalculationServiceImpl#getTeachingClasses} 专业/年级过滤回归测试。
 * <p>
 * 修复前该方法对 majorId/grade 只写了注释、空实现，始终返回全校该学期所有教学班，
 * 导致专业级计算/看板跨专业串数据。这里用 mock 验证过滤控制流；
 * QueryWrapper 的实际过滤语义由 E2E 验证。
 */
@ExtendWith(MockitoExtension.class)
class MajorCalculationScopeTest {

    @InjectMocks
    private MajorCalculationServiceImpl service;

    @Mock
    private CourseMapper courseMapper;
    @Mock
    private TeachingClassMapper teachingClassMapper;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private ClassStudentMapper classStudentMapper;

    private TeachingClass tc(Long id, Long courseId) {
        TeachingClass t = new TeachingClass();
        t.setId(id);
        t.setCourseId(courseId);
        return t;
    }

    private Course course(Long id, Long majorId) {
        Course c = new Course();
        c.setId(id);
        c.setMajorId(majorId);
        return c;
    }

    private Student student(Long id, String grade) {
        Student s = new Student();
        s.setId(id);
        s.setGrade(grade);
        return s;
    }

    private ClassStudent cs(Long classId, Long studentId) {
        ClassStudent c = new ClassStudent();
        c.setClassId(classId);
        c.setStudentId(studentId);
        return c;
    }

    /**
     * 【核心修复点】给定 majorId 时，必须走课程查询分支（旧实现从不调用 courseMapper）。
     */
    @Test
    void majorIdGiven_shouldQueryCoursesByMajor() {
        when(courseMapper.selectList(any()))
                .thenReturn(Arrays.asList(course(10L, 1L), course(11L, 1L)));
        when(teachingClassMapper.selectList(any()))
                .thenReturn(Collections.singletonList(tc(1L, 10L)));

        List<TeachingClass> result = service.getTeachingClasses(1L, null, null);

        // 关键：courseMapper 被调用（旧 bug 完全不查课程）
        verify(courseMapper).selectList(any());
        assertEquals(1, result.size());
    }

    /**
     * 该专业无课程 → 直接返回空，且不应再查教学班
     */
    @Test
    void majorIdGiven_noCourses_shouldReturnEmptyAndSkipClassQuery() {
        when(courseMapper.selectList(any())).thenReturn(Collections.emptyList());

        assertTrue(service.getTeachingClasses(999L, null, null).isEmpty());
        verifyNoInteractions(teachingClassMapper);
    }

    /**
     * 年级过滤：取该年级学生所在教学班做交集
     */
    @Test
    void gradeGiven_shouldNarrowByStudentGrade() {
        when(courseMapper.selectList(any())).thenReturn(Collections.singletonList(course(10L, 1L)));
        when(teachingClassMapper.selectList(any()))
                .thenReturn(Arrays.asList(tc(1L, 10L), tc(2L, 10L)));
        when(studentMapper.selectList(any())).thenReturn(Collections.singletonList(student(100L, "2023")));
        when(classStudentMapper.selectList(any())).thenReturn(Collections.singletonList(cs(1L, 100L)));

        List<TeachingClass> result = service.getTeachingClasses(1L, null, "2023");

        verify(studentMapper).selectList(any());
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    /**
     * 该年级无学生 → 返回空
     */
    @Test
    void gradeGiven_noStudents_shouldReturnEmpty() {
        when(courseMapper.selectList(any())).thenReturn(Collections.singletonList(course(10L, 1L)));
        when(teachingClassMapper.selectList(any())).thenReturn(Collections.singletonList(tc(1L, 10L)));
        when(studentMapper.selectList(any())).thenReturn(Collections.emptyList());

        assertTrue(service.getTeachingClasses(1L, null, "2099").isEmpty());
        // 学生为空时不应再查班级学生关联
        verifyNoInteractions(classStudentMapper);
    }

    /**
     * 不传任何过滤 → 返回全部教学班（行为不变）
     */
    @Test
    void noFilters_shouldReturnAllClasses() {
        when(teachingClassMapper.selectList(any()))
                .thenReturn(Arrays.asList(tc(1L, 10L), tc(2L, 20L)));

        List<TeachingClass> result = service.getTeachingClasses(null, null, null);

        assertEquals(2, result.size());
        verifyNoInteractions(courseMapper);
    }
}
