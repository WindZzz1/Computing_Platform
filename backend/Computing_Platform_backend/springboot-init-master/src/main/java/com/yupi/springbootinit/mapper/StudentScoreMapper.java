package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.StudentScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

/**
 * 学生成绩Mapper
 *
 * @author YU
 */
@Mapper
public interface StudentScoreMapper extends BaseMapper<StudentScore> {

    /**
     * 物理删除指定教学班级的所有成绩
     *
     * @param classId 教学班级ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM student_score WHERE teaching_class_id = #{classId}")
    int deleteByClassIdPhysically(@Param("classId") Long classId);

    /**
     * 物理删除指定学生的成绩
     *
     * @param studentId 学生ID
     * @param classId 教学班级ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM student_score WHERE student_id = #{studentId} AND teaching_class_id = #{classId}")
    int deleteByStudentIdAndClassIdPhysically(@Param("studentId") Long studentId, @Param("classId") Long classId);
}
