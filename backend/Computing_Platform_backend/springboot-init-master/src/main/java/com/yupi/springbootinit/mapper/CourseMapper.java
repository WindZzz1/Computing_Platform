package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
