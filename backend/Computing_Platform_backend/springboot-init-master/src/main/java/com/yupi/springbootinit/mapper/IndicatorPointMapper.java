package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.IndicatorPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 二级指标点表 Mapper
 *
 * @author YU
 */
@Mapper
public interface IndicatorPointMapper extends BaseMapper<IndicatorPoint> {

    @Select("select count(1) from indicator_point where requirement_id = #{requirementId} and is_deleted = 0")
    Long countByRequirementId(@Param("requirementId") Long requirementId);

    @Select("select count(1) from matrix_course_indicator where indicator_id = #{indicatorId} and is_deleted = 0")
    Long countMatrixByIndicatorId(@Param("indicatorId") Long indicatorId);

    @Select("select count(1) from weight_objective_indicator where indicator_id = #{indicatorId} and is_deleted = 0")
    Long countWeightByIndicatorId(@Param("indicatorId") Long indicatorId);

    @Select("select count(1) from result_course_indicator where indicator_id = #{indicatorId} and is_deleted = 0")
    Long countCourseResultByIndicatorId(@Param("indicatorId") Long indicatorId);

    @Select("select count(1) from result_major_indicator where indicator_id = #{indicatorId} and is_deleted = 0")
    Long countMajorResultByIndicatorId(@Param("indicatorId") Long indicatorId);
}
