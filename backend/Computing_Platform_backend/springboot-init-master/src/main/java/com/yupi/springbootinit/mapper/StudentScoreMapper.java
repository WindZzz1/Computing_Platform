package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.StudentScore;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学生考核点成绩Mapper
 */
@Mapper
public interface StudentScoreMapper extends BaseMapper<StudentScore> {

    /**
     * 根据教学班级ID物理删除学生成绩
     *
     * @param classId 教学班级ID
     * @return 影响行数
     */
    @Delete("DELETE FROM student_score WHERE teaching_class_id = #{classId}")
    int deleteByClassId(@Param("classId") Long classId);

    /**
     * 根据教学班级ID查询学生成绩列表
     *
     * @param classId 教学班级ID
     * @return 学生成绩列表
     */
    @Select("SELECT * FROM student_score WHERE teaching_class_id = #{classId}")
    List<StudentScore> selectByClassId(@Param("classId") Long classId);
}
