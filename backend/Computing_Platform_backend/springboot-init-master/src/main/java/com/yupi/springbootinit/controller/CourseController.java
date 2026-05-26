package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.course.CourseAddRequest;
import com.yupi.springbootinit.model.dto.course.CourseImportRequest;
import com.yupi.springbootinit.model.dto.course.CourseQueryRequest;
import com.yupi.springbootinit.model.dto.course.CourseUpdateRequest;
import com.yupi.springbootinit.model.vo.CourseSimpleVO;
import com.yupi.springbootinit.model.vo.CourseVO;
import com.yupi.springbootinit.model.vo.PageResultVO;
import com.yupi.springbootinit.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 课程接口
 *
 * @author YU
 */
@RestController
@RequestMapping("/course")
@Slf4j
public class CourseController {

    @Resource
    private CourseService courseService;

    /**
     * 创建课程
     *
     * @param courseAddRequest 新增请求
     * @return 课程ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Long> addCourse(@RequestBody CourseAddRequest courseAddRequest) {
        Long courseId = courseService.createCourse(courseAddRequest);
        return ResultUtils.success(courseId);
    }

    /**
     * 更新课程
     *
     * @param courseUpdateRequest 更新请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Boolean> updateCourse(@RequestBody CourseUpdateRequest courseUpdateRequest) {
        Boolean result = courseService.updateCourse(courseUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除课程
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Boolean> deleteCourse(@RequestBody DeleteRequest deleteRequest) {
        Boolean result = courseService.deleteCourse(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取课程
     *
     * @param deleteRequest ID请求
     * @return 课程信息
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<CourseVO> getCourseById(@RequestBody DeleteRequest deleteRequest) {
        CourseVO courseVO = courseService.getCourseById(deleteRequest.getId());
        return ResultUtils.success(courseVO);
    }

    /**
     * 分页查询课程
     *
     * @param courseQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<PageResultVO<CourseVO>> pageCourse(@RequestBody CourseQueryRequest courseQueryRequest) {
        Page<CourseVO> coursePage = courseService.pageCourse(courseQueryRequest);
        return ResultUtils.success(PageResultVO.from(coursePage));
    }

    /**
     * 批量导入课程（JSON方式）
     *
     * @param courseImportRequest 导入请求
     * @return 导入结果
     */
//    @PostMapping("/import")
//    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
//    public BaseResponse<Map<String, Object>> importCourses(@RequestBody CourseImportRequest courseImportRequest) {
//        Map<String, Object> result = courseService.importCourses(courseImportRequest);
//        return ResultUtils.success(result);
//    }

    /**
     * 通过Excel批量导入课程
     *
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/import/excel")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Map<String, Object>> importCoursesFromExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = courseService.importCoursesFromExcel(file);
        return ResultUtils.success(result);
    }

    /**
     * 下载课程导入模板
     *
     * @param response HTTP响应
     */
    @GetMapping("/template")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public void downloadCourseTemplate(javax.servlet.http.HttpServletResponse response) throws Exception {
        byte[] template = courseService.generateCourseTemplate();
        log.info("模板下载请求处理成功，模板大小: {} bytes", template.length);

        // 设置响应头（注意：不要设置 CharacterEncoding，避免二进制数据被转换）
        String filename = "课程导入模板.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + encodedFilename);
        response.setHeader("Content-Length", String.valueOf(template.length));
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // 直接以二进制方式写入响应流
        javax.servlet.ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(template);
        outputStream.flush();
        log.info("模板文件已写入响应流");
    }

    /**
     * 获取所有课程列表（简化）
     *
     * @return 课程简化列表
     */
    @PostMapping("/list")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<List<CourseSimpleVO>> listCourse() {
        List<CourseSimpleVO> list = courseService.listCourseSimple();
        return ResultUtils.success(list);
    }
}
