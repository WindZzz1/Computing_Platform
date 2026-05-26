package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生信息表 Mapper
 *
 * @author YU
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

}
