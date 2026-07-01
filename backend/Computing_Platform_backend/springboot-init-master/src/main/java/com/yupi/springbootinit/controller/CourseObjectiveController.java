package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveAddRequest;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveQueryRequest;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveUpdateRequest;
import com.yupi.springbootinit.model.vo.CourseObjectiveVO;
import com.yupi.springbootinit.service.CourseObjectiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
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
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
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
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
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
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
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
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
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
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Page<CourseObjectiveVO>> listCourseObjective(@RequestBody CourseObjectiveQueryRequest request) {
        if (request == null) {
            request = new CourseObjectiveQueryRequest();
        }
        request.setCurrent(1);
        request.setPageSize(1000);
        return ResultUtils.success(courseObjectiveService.pageCourseObjective(request));
    }

    /**
     * 通过Excel批量导入课程目标
     *
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/import/excel")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Map<String, Object>> importCourseObjectivesFromExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = courseObjectiveService.importCourseObjectivesFromExcel(file);
        return ResultUtils.success(result);
    }

    /**
     * 下载课程目标导入模板
     */
    @GetMapping("/template")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    @com.yupi.springbootinit.annotation.NoLog
    public void downloadCourseObjectiveTemplate(HttpServletResponse response) throws Exception {
        String filename = "课程目标导入模板.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        byte[] templateBytes = courseObjectiveService.generateCourseObjectiveTemplate();
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(templateBytes);
        outputStream.flush();
    }
}
