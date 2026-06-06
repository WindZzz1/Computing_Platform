package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.entity.CourseIndicatorAchievement;
import com.yupi.springbootinit.model.entity.StudentObjectiveAchievement;
import com.yupi.springbootinit.model.entity.StudentScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 穿透式台账数据访问Mapper
 *
 * @author YU
 */
@Mapper
public interface PenetrationAccountMapper extends BaseMapper<Course> {

    /**
     * 查询专业相关课程列表
     *
     * @param majorId 专业ID
     * @return 课程列表
     */
    @Select("SELECT DISTINCT c.* FROM course c " +
            "INNER JOIN teaching_class tc ON c.id = tc.course_id " +
            "INNER JOIN sys_dict_major dm ON tc.major_id = dm.id " +
            "WHERE dm.id = #{majorId} AND c.is_deleted = 0")
    List<Course> selectCoursesByMajor(@Param("majorId") Long majorId);

    /**
     * 查询课程的指标点达成度
     *
     * @param courseIds 课程ID列表
     * @return 课程指标点达成度列表
     */
    @Select("<script>" +
            "SELECT * FROM course_indicator_achievement " +
            "WHERE teaching_class_id IN " +
            "<foreach item='classId' collection='classIds' open='(' separator=',' close=')'>" +
            "#{classId}" +
            "</foreach>" +
            "AND is_deleted = 0" +
            "</script>")
    List<CourseIndicatorAchievement> selectCourseIndicators(@Param("classIds") List<Long> classIds);

    /**
     * 查询学生课程目标达成度
     *
     * @param classIds 教学班级ID列表
     * @return 学生课程目标达成度列表
     */
    @Select("<script>" +
            "SELECT * FROM student_objective_achievement " +
            "WHERE teaching_class_id IN " +
            "<foreach item='classId' collection='classIds' open='(' separator=',' close=')'>" +
            "#{classId}" +
            "</foreach>" +
            "AND is_deleted = 0" +
            "</script>")
    List<StudentObjectiveAchievement> selectStudentObjectives(@Param("classIds") List<Long> classIds);

    /**
     * 查询考核点学生成绩
     *
     * @param classIds 教学班级ID列表
     * @return 学生成绩列表
     */
    @Select("<script>" +
            "SELECT * FROM student_score " +
            "WHERE class_id IN " +
            "<foreach item='classId' collection='classIds' open='(' separator=',' close=')'>" +
            "#{classId}" +
            "</foreach>" +
            "AND is_deleted = 0" +
            "</script>")
    List<StudentScore> selectAssessmentScores(@Param("classIds") List<Long> classIds);
}