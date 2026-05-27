package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Indicator;
import org.apache.ibatis.annotations.Mapper;

// 毕业要求指标点Mapper

@Mapper
public interface IndicatorMapper extends BaseMapper<Indicator> {
}
