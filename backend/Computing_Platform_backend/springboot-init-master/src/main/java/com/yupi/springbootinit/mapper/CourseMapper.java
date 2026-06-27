package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.vo.CourseSimpleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程表 Mapper
 *
 * @author YU
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("select count(1) from teaching_class where course_id = #{courseId} and is_deleted = 0")
    Long countTeachingClassByCourseId(@Param("courseId") Long courseId);

    @Select("select count(1) from course_objective where course_id = #{courseId} and is_deleted = 0")
    Long countObjectiveByCourseId(@Param("courseId") Long courseId);

    @Select("select count(1) from assessment_point where course_id = #{courseId} and is_deleted = 0")
    Long countAssessmentPointByCourseId(@Param("courseId") Long courseId);

    @Select("select count(1) from matrix_course_indicator where course_id = #{courseId} and is_deleted = 0")
    Long countMatrixByCourseId(@Param("courseId") Long courseId);

    @Select("select count(1) from weight_objective_indicator where course_id = #{courseId} and is_deleted = 0")
    Long countWeightByCourseId(@Param("courseId") Long courseId);

    @Select("select count(1) from course_indicator_achievement where course_id = #{courseId} and is_deleted = 0")
    Long countCourseResultByCourseId(@Param("courseId") Long courseId);

    /**
     * 按主讲教师反查其讲授的课程（去重）。
     * 通过 teaching_class.teacher_id 关联，依赖全局 map-underscore-to-camel-case 映射列名。
     */
    @Select("select distinct c.id, c.course_code, c.course_name "
            + "from course c "
            + "inner join teaching_class tc on c.id = tc.course_id "
            + "where tc.teacher_id = #{teacherId} and tc.is_deleted = 0 and c.is_deleted = 0 "
            + "order by c.id")
    List<CourseSimpleVO> listSimpleByTeacherId(@Param("teacherId") Long teacherId);
}
