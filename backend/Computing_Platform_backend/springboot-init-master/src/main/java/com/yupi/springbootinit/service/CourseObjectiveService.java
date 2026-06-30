package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveAddRequest;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveQueryRequest;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveUpdateRequest;
import com.yupi.springbootinit.model.entity.CourseObjective;
import com.yupi.springbootinit.model.vo.CourseObjectiveVO;

//课程目标服务

public interface CourseObjectiveService extends IService<CourseObjective> {

    /**
     * 创建课程目标
     *
     * @param request 新增请求
     * @return 课程目标ID
     */
    Long createCourseObjective(CourseObjectiveAddRequest request);

    /**
     * 更新课程目标
     *
     * @param request 更新请求
     * @return 是否成功
     */
    Boolean updateCourseObjective(CourseObjectiveUpdateRequest request);

    /**
     * 删除课程目标
     *
     * @param id 课程目标ID
     * @return 是否成功
     */
    Boolean deleteCourseObjective(Long id);

    /**
     * 根据ID获取课程目标
     *
     * @param id 课程目标ID
     * @return 课程目标VO
     */
    CourseObjectiveVO getCourseObjectiveById(Long id);

    /**
     * 分页查询课程目标
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<CourseObjectiveVO> pageCourseObjective(CourseObjectiveQueryRequest request);

    /**
     * 获取查询条件
     *
     * @param request 查询请求
     * @return 查询条件
     */
    QueryWrapper<CourseObjective> getQueryWrapper(CourseObjectiveQueryRequest request);

    /**
     * 获取课程目标VO
     *
     * @param courseObjective 课程目标实体
     * @return 课程目标VO
     */
    CourseObjectiveVO getCourseObjectiveVO(CourseObjective courseObjective);
}
