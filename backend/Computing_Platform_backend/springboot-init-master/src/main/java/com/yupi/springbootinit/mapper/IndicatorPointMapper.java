package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.IndicatorPoint;
import org.apache.ibatis.annotations.Mapper;

/**
 * 二级指标点表 Mapper
 *
 * @author YU
 */
@Mapper
public interface IndicatorPointMapper extends BaseMapper<IndicatorPoint> {

}