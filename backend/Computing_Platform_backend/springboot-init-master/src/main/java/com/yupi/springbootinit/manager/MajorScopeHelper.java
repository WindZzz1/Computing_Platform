package com.yupi.springbootinit.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.springbootinit.mapper.ClassStudentMapper;
import com.yupi.springbootinit.mapper.CourseMapper;
import com.yupi.springbootinit.mapper.StudentMapper;
import com.yupi.springbootinit.mapper.TeachingClassMapper;
import com.yupi.springbootinit.model.entity.ClassStudent;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.entity.Student;
import com.yupi.springbootinit.model.entity.TeachingClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 专业级数据范围解析工具。
 * <p>
 * 抽取自 {@link com.yupi.springbootinit.service.impl.MajorCalculationServiceImpl#getTeachingClasses}，
 * 供「专业级计算」与「专业级报表（雷达图 / 穿透台账）」共用同一套专业 / 学年学期 / 年级过滤逻辑，
 * 避免两处实现不同步导致跨专业串数据。
 *
 * @author YU
 */
@Component
@Slf4j
public class MajorScopeHelper {

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private TeachingClassMapper teachingClassMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private ClassStudentMapper classStudentMapper;

    /**
     * 获取某专业 / 学年学期 / 年级涉及的教学班级。
     * <p>
     * 专业过滤：教学班本身不挂专业（teaching_class 无 major_id），通过其所属课程的
     * major_id 关联（course.major_id = majorId）。
     * 年级过滤：教学班无 grade 字段，经 班级学生 → 学生.grade 关联。
     * <p>
     * 历史：修复前 majorId / grade 均未生效（只写了注释、空实现），导致 C-4 看板和专业级
     * 计算基于"全校该学期所有教学班"，跨专业串数据、canCalculate 永远误判。
     */
    public List<TeachingClass> getTeachingClasses(Long majorId, Long termId, String grade) {
        // 1. 专业过滤：取该专业下的所有课程 id（course.major_id）
        List<Long> courseIds = null;
        if (majorId != null) {
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("major_id", majorId).select("id");
            courseIds = courseMapper.selectList(courseQuery).stream()
                    .map(Course::getId).collect(Collectors.toList());
            if (courseIds.isEmpty()) {
                // 该专业无课程 → 无教学班
                return Collections.emptyList();
            }
        }

        // 2. 按课程 + 学年学期筛选教学班
        QueryWrapper<TeachingClass> query = new QueryWrapper<>();
        if (courseIds != null) {
            query.in("course_id", courseIds);
        }
        if (termId != null) {
            query.eq("term_id", termId);
        }
        List<TeachingClass> classes = teachingClassMapper.selectList(query);
        if (classes.isEmpty()) {
            return classes;
        }

        // 3. 年级过滤：教学班无 grade，取该专业该年级学生所在教学班，与上一步取交集
        if (grade != null && !grade.isEmpty()) {
            QueryWrapper<Student> studentQuery = new QueryWrapper<>();
            if (majorId != null) {
                studentQuery.eq("major_id", majorId);
            }
            studentQuery.eq("grade", grade).select("id");
            List<Long> studentIds = studentMapper.selectList(studentQuery).stream()
                    .map(Student::getId).collect(Collectors.toList());
            if (studentIds.isEmpty()) {
                return Collections.emptyList();
            }
            QueryWrapper<ClassStudent> classStudentQuery = new QueryWrapper<>();
            classStudentQuery.in("student_id", studentIds);
            Set<Long> classIdsOfGrade = classStudentMapper.selectList(classStudentQuery).stream()
                    .map(ClassStudent::getClassId).collect(Collectors.toSet());
            classes = classes.stream()
                    .filter(tc -> classIdsOfGrade.contains(tc.getId()))
                    .collect(Collectors.toList());
        }

        return classes;
    }
}
