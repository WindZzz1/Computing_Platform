package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.AssessmentPoint;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
