package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.StudentObjectiveAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

/**
 * 学生课程目标达成度Mapper
 *
 * @author YU
 */
@Mapper
public interface StudentObjectiveAchievementMapper extends BaseMapper<StudentObjectiveAchievement> {

    /**
     * 物理删除指定教学班级的所有一级达成度数据
     *
     * @param classId 教学班级ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM student_objective_achievement WHERE teaching_class_id = #{classId}")
    int deleteByClassIdPhysically(@Param("classId") Long classId);
}
