package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.CourseObjective;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 课程目标Mapper

@Mapper
public interface CourseObjectiveMapper extends BaseMapper<CourseObjective> {

    /**
     * 按课程ID和目标编号物理删除已逻辑删除的旧课程目标
     *
     * @param courseId 课程ID
     * @param objCode  目标编号
     * @return 影响行数
     */
    @Delete("delete from course_objective where course_id = #{courseId} and obj_code = #{objCode} and is_deleted = 1")
    int deleteDeletedByCourseIdAndObjCode(@Param("courseId") Long courseId, @Param("objCode") String objCode);
}
