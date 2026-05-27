package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.WeightObjectiveIndicator;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 课程目标-指标点内部权重Mapper

@Mapper
public interface WeightObjectiveIndicatorMapper extends BaseMapper<WeightObjectiveIndicator> {

    /**
     * 按课程ID物理删除内部权重配置，避免逻辑删除数据占用唯一键
     *
     * @param courseId 课程ID
     * @return 影响行数
     */
    @Delete("delete from weight_objective_indicator where course_id = #{courseId}")
    int deleteByCourseIdPhysically(@Param("courseId") Long courseId);
}
