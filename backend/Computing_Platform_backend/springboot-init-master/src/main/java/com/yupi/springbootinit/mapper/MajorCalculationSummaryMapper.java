package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.MajorCalculationSummary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专业级计算汇总Mapper
 *
 * @author YU
 */
@Mapper
public interface MajorCalculationSummaryMapper extends BaseMapper<MajorCalculationSummary> {
}
