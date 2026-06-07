package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.SysDictSchoolYear;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 学年学期字典表 Mapper
 *
 * @author YU
 */
@Mapper
public interface SysDictSchoolYearMapper extends BaseMapper<SysDictSchoolYear> {

    @Select("select count(1) from teaching_class where term_id = #{termId} and is_deleted = 0")
    Long countTeachingClassByTermId(@Param("termId") Long termId);

    @Select("select count(1) from major_indicator_achievement where term_id = #{termId} and is_deleted = 0")
    Long countMajorResultByTermId(@Param("termId") Long termId);
}
