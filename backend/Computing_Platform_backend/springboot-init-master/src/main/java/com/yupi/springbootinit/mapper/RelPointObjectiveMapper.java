package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.RelPointObjective;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 考核点-课程目标关联Mapper
 */
@Mapper
public interface RelPointObjectiveMapper extends BaseMapper<RelPointObjective> {

    /**
     * 按考核点ID物理删除关联关系，避免逻辑删除数据占用唯一键
     *
     * @param pointId 考核点ID
     * @return 影响行数
     */
    @Delete("delete from rel_point_objective where point_id = #{pointId}")
    int deleteByPointIdPhysically(@Param("pointId") Long pointId);

    /**
     * 统计指定课程目标是否被考核点关联
     *
     * @param objectiveId 课程目标ID
     * @return 关联数量
     */
    @Select("select count(1) from rel_point_objective r " +
            "join assessment_point p on p.id = r.point_id and p.is_deleted = 0 " +
            "where r.objective_id = #{objectiveId} and r.is_deleted = 0")
    Long countByObjectiveId(@Param("objectiveId") Long objectiveId);
}
