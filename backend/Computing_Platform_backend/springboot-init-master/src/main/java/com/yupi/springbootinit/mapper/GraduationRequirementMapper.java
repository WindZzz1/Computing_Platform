package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.GraduationRequirement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 毕业要求表 Mapper
 *
 * @author YU
 */
@Mapper
public interface GraduationRequirementMapper extends BaseMapper<GraduationRequirement> {

}