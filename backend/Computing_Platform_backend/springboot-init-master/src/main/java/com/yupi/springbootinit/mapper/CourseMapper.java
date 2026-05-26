package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程表 Mapper
 *
 * @author YU
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

}
