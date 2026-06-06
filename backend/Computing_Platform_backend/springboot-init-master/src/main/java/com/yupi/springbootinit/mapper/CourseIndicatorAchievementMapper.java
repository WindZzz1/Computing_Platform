package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.CourseIndicatorAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程指标点达成度Mapper
 *
 * @author YU
 */
@Mapper
public interface CourseIndicatorAchievementMapper extends BaseMapper<CourseIndicatorAchievement> {

    /**
     * 物理删除指定教学班级的所有二级达成度数据
     *
     * @param classId 教学班级ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM course_indicator_achievement WHERE teaching_class_id = #{classId}")
    int deleteByClassIdPhysically(@Param("classId") Long classId);

    /**
     * 根据教学班级ID查询课程指标点达成度明细
     *
     * @param classId 教学班级ID
     * @return 课程指标点达成度列表
     */
    @Select("SELECT * FROM course_indicator_achievement WHERE teaching_class_id = #{classId} AND is_deleted = 0 ORDER BY indicator_id")
    List<CourseIndicatorAchievement> selectByClassId(@Param("classId") Long classId);
}
