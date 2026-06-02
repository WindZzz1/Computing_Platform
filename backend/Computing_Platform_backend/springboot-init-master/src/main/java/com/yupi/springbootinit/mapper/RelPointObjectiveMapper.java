package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.RelPointObjective;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 考核点-课程目标关联Mapper
 */
@Mapper
public interface RelPointObjectiveMapper extends BaseMapper<RelPointObjective> {

    /**
     * 根据课程目标ID查询关联的考核点列表
     *
     * @param objectiveId 课程目标ID
     * @return 考核点ID列表
     */
    @Select("SELECT point_id FROM rel_point_objective WHERE objective_id = #{objectiveId} AND is_deleted = 0")
    List<Long> selectPointIdsByObjectiveId(@Param("objectiveId") Long objectiveId);

    /**
     * 根据考核点ID查询关联的课程目标ID列表
     *
     * @param pointId 考核点ID
     * @return 课程目标ID列表
     */
    @Select("SELECT objective_id FROM rel_point_objective WHERE point_id = #{pointId} AND is_deleted = 0")
    List<Long> selectObjectiveIdsByPointId(@Param("pointId") Long pointId);
}
