package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.GradeCalculationStatus;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成绩计算状态Mapper
 *
 * @author YU
 */
@Mapper
public interface GradeCalculationStatusMapper extends BaseMapper<GradeCalculationStatus> {
}
