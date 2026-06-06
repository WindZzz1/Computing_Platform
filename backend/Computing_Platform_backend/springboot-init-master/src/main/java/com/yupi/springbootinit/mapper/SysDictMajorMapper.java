package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 专业字典表 Mapper
 *
 * @author YU
 */
@Mapper
public interface SysDictMajorMapper extends BaseMapper<SysDictMajor> {

    @Select("select count(1) from course where major_id = #{majorId} and is_deleted = 0")
    Long countCourseByMajorId(@Param("majorId") Long majorId);

    @Select("select count(1) from student where major_id = #{majorId} and is_deleted = 0")
    Long countStudentByMajorId(@Param("majorId") Long majorId);

    @Select("select count(1) from graduation_requirement where major_id = #{majorId} and is_deleted = 0")
    Long countRequirementByMajorId(@Param("majorId") Long majorId);

    @Select("select count(1) from matrix_course_indicator where major_id = #{majorId} and is_deleted = 0")
    Long countMatrixByMajorId(@Param("majorId") Long majorId);

    @Select("select count(1) from major_indicator_achievement where major_id = #{majorId} and is_deleted = 0")
    Long countMajorResultByMajorId(@Param("majorId") Long majorId);
}
