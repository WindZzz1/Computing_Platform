package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.CourseIndicatorAchievement;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 课程级指标点达成度Mapper
 */
@Mapper
public interface CourseIndicatorAchievementMapper extends BaseMapper<CourseIndicatorAchievement> {

    /**
     * 根据教学班级ID删除课程指标点达成度
     *
     * @param classId 教学班级ID
     * @return 影响行数
     */
    @Delete("DELETE FROM result_course_indicator WHERE teaching_class_id = #{classId}")
    int deleteByClassId(@Param("classId") Long classId);
}
