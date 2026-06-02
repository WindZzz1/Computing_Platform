package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.StudentObjectiveAchievement;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 学生课程目标达成度Mapper
 */
@Mapper
public interface StudentObjectiveAchievementMapper extends BaseMapper<StudentObjectiveAchievement> {

    /**
     * 根据教学班级ID删除学生课程目标达成度
     *
     * @param classId 教学班级ID
     * @return 影响行数
     */
    @Delete("DELETE FROM result_student_objective WHERE teaching_class_id = #{classId}")
    int deleteByClassId(@Param("classId") Long classId);
}
