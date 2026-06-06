package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.StudentObjectiveAchievement;
import com.yupi.springbootinit.model.vo.report.ObjectiveAchievementSummaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 根据教学班级ID查询学生达成度明细
     *
     * @param classId 教学班级ID
     * @return 学生达成度列表
     */
    @Select("SELECT * FROM student_objective_achievement WHERE teaching_class_id = #{classId} AND is_deleted = 0 ORDER BY student_id, objective_id")
    List<StudentObjectiveAchievement> selectByClassId(@Param("classId") Long classId);

    /**
     * 统计课程目标班级达成度汇总
     *
     * @param classId 教学班级ID
     * @return 课程目标达成度汇总列表
     */
    @Select("SELECT " +
            "objective_id as objectiveId, " +
            "objective_code as objectiveCode, " +
            "objective_name as objectiveName, " +
            "AVG(achievement) as classAverage, " +
            "MAX(achievement) as maxScore, " +
            "MIN(achievement) as minScore, " +
            "COUNT(*) as studentCount, " +
            "SUM(CASE WHEN achievement >= 0.7 THEN 1 ELSE 0 END) * 1.0 / COUNT(*) as passRate " +
            "FROM student_objective_achievement " +
            "WHERE teaching_class_id = #{classId} AND is_deleted = 0 " +
            "GROUP BY objective_id, objective_code, objective_name " +
            "ORDER BY objective_id")
    List<ObjectiveAchievementSummaryVO> selectObjectiveSummaries(@Param("classId") Long classId);
}
