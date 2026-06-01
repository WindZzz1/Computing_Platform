package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.MatrixCourseIndicator;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 课程-指标点宏观支撑矩阵Mapper

@Mapper
public interface MatrixCourseIndicatorMapper extends BaseMapper<MatrixCourseIndicator> {

    @Delete("delete from matrix_course_indicator where major_id = #{majorId}")
    int deleteByMajorIdPhysically(@Param("majorId") Long majorId);
}
