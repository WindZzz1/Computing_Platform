package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.AssessmentPoint;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

// 课程考核点Mapper

@Mapper
public interface AssessmentPointMapper extends BaseMapper<AssessmentPoint> {

    /**
     * 按课程ID和考核点编号物理删除已逻辑删除的旧考核点
     *
     * @param courseId  课程ID
     * @param pointCode 考核点编号
     * @return 影响行数
     */
    @Delete("delete from assessment_point where course_id = #{courseId} and point_code = #{pointCode} and is_deleted = 1")
    int deleteDeletedByCourseIdAndPointCode(@Param("courseId") Long courseId, @Param("pointCode") String pointCode);

    /**
     * 统计指定考核点下是否已有学生成绩
     *
     * @param pointId 考核点ID
     * @return 学生成绩数量
     */
    @Select("select count(1) from student_score where point_id = #{pointId}")
    Long countStudentScoreByPointId(@Param("pointId") Long pointId);
}
