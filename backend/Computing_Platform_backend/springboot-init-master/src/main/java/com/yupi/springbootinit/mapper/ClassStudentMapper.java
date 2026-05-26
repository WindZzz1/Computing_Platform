package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.ClassStudent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级学生关联表 Mapper
 *
 * @author YU
 */
@Mapper
public interface ClassStudentMapper extends BaseMapper<ClassStudent> {

}
