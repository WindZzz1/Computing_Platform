package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.SysDictSchoolYear;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学年学期字典表 Mapper
 *
 * @author YU
 */
@Mapper
public interface SysDictSchoolYearMapper extends BaseMapper<SysDictSchoolYear> {

}