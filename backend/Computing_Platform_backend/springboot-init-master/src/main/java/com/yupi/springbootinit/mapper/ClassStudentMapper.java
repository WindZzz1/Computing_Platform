package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.ClassStudent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 班级学生关联表 Mapper
 *
 * @author YU
 */
@Mapper
public interface ClassStudentMapper extends BaseMapper<ClassStudent> {

    @Select("select id, teaching_class_id, student_id, create_time, update_time, is_deleted " +
            "from class_student where teaching_class_id = #{classId} and student_id = #{studentId} limit 1")
    ClassStudent selectAnyByClassIdAndStudentId(@Param("classId") Long classId, @Param("studentId") Long studentId);

    @Update("update class_student set is_deleted = 0, update_time = now() where id = #{id}")
    int restoreById(@Param("id") Long id);
}
