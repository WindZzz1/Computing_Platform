package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.TeachingClass;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教学班级表 Mapper
 *
 * @author YU
 */
@Mapper
public interface TeachingClassMapper extends BaseMapper<TeachingClass> {

}