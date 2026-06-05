package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.MajorIndicatorAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

/**
 * 专业级指标点达成度Mapper
 *
 * @author YU
 */
@Mapper
public interface MajorIndicatorAchievementMapper extends BaseMapper<MajorIndicatorAchievement> {

    /**
     * 物理删除指定专业、学年、年级的所有三级达成度数据
     *
     * @param majorId 专业ID
     * @param termId 学年学期ID
     * @param grade 年级
     * @return 删除的记录数
     */
    @Delete("DELETE FROM major_indicator_achievement WHERE major_id = #{majorId} AND term_id = #{termId} AND grade = #{grade}")
    int deleteByMajorTermGradePhysically(@Param("majorId") Long majorId, @Param("termId") Long termId, @Param("grade") String grade);
}
