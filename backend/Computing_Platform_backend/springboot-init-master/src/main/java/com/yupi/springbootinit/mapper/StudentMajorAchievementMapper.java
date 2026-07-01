package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.StudentMajorAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学生专业达成度 Mapper
 *
 * @author YU
 */
@Mapper
public interface StudentMajorAchievementMapper extends BaseMapper<StudentMajorAchievement> {

    /**
     * 物理删除指定专业、学年、年级的所有学生专业达成度数据
     */
    @Delete("DELETE FROM student_major_achievement WHERE major_id = #{majorId} AND term_id = #{termId} AND grade = #{grade}")
    int deleteByMajorTermGradePhysically(@Param("majorId") Long majorId, @Param("termId") Long termId, @Param("grade") String grade);

    /**
     * 根据专业、学年、年级查询所有学生的专业达成度（join student 取学号、姓名）
     */
    @Select("SELECT sma.*, s.student_no, s.student_name " +
            "FROM student_major_achievement sma " +
            "LEFT JOIN student s ON sma.student_id = s.id AND s.is_deleted = 0 " +
            "WHERE sma.major_id = #{majorId} " +
            "AND sma.term_id = #{termId} " +
            "AND sma.grade = #{grade} " +
            "AND sma.is_deleted = 0 " +
            "ORDER BY sma.student_id, sma.indicator_id")
    List<StudentMajorAchievement> selectByMajorTermGrade(
            @Param("majorId") Long majorId,
            @Param("termId") Long termId,
            @Param("grade") String grade
    );
}
