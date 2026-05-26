package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveAddRequest;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveQueryRequest;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveUpdateRequest;
import com.yupi.springbootinit.model.vo.CourseObjectiveVO;
import com.yupi.springbootinit.service.CourseObjectiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

//课程目标接口

@RestController
@RequestMapping("/course/objective")
@Slf4j
public class CourseObjectiveController {

    @Resource
    private CourseObjectiveService courseObjectiveService;

    /**
     * 创建课程目标
     *
     * @param request 新增请求
     * @return 课程目标ID
     */
    @PostMapping("/add")
    @AuthCheck
    public BaseResponse<Long> addCourseObjective(@RequestBody CourseObjectiveAddRequest request) {
        return ResultUtils.success(courseObjectiveService.createCourseObjective(request));
    }

    /**
     * 更新课程目标
     *
     * @param request 更新请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck
    public BaseResponse<Boolean> updateCourseObjective(@RequestBody CourseObjectiveUpdateRequest request) {
        return ResultUtils.success(courseObjectiveService.updateCourseObjective(request));
    }

    /**
     * 删除课程目标
     *
     * @param request 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck
    public BaseResponse<Boolean> deleteCourseObjective(@RequestBody DeleteRequest request) {
        return ResultUtils.success(courseObjectiveService.deleteCourseObjective(request.getId()));
    }

    /**
     * 根据ID获取课程目标
     *
     * @param request ID请求
     * @return 课程目标信息
     */
    @PostMapping("/get")
    @AuthCheck
    public BaseResponse<CourseObjectiveVO> getCourseObjective(@RequestBody DeleteRequest request) {
        return ResultUtils.success(courseObjectiveService.getCourseObjectiveById(request.getId()));
    }

    /**
     * 分页查询课程目标
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @PostMapping("/page")
    @AuthCheck
    public BaseResponse<Page<CourseObjectiveVO>> pageCourseObjective(@RequestBody CourseObjectiveQueryRequest request) {
        return ResultUtils.success(courseObjectiveService.pageCourseObjective(request));
    }

    /**
     * 获取课程目标列表
     *
     * @param request 查询请求
     * @return 课程目标列表
     */
    @PostMapping("/list")
    @AuthCheck
    public BaseResponse<Page<CourseObjectiveVO>> listCourseObjective(@RequestBody CourseObjectiveQueryRequest request) {
        if (request == null) {
            request = new CourseObjectiveQueryRequest();
        }
        request.setCurrent(1);
        request.setPageSize(1000);
        return ResultUtils.success(courseObjectiveService.pageCourseObjective(request));
    }
}
