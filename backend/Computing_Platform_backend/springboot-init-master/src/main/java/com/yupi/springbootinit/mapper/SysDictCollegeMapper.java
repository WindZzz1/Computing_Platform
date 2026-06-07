package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.SysDictCollege;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 学院字典表 Mapper
 *
 * @author YU
 */
@Mapper
public interface SysDictCollegeMapper extends BaseMapper<SysDictCollege> {

    @Select("select count(1) from sys_dict_major where college_id = #{collegeId} and is_deleted = 0")
    Long countMajorByCollegeId(@Param("collegeId") Long collegeId);

    @Select("select count(1) from sys_user where college_id = #{collegeId} and is_deleted = 0")
    Long countUserByCollegeId(@Param("collegeId") Long collegeId);
}
