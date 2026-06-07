package com.yupi.springbootinit.controller;

import com.alibaba.excel.EasyExcel;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.report.CourseAchievementReportRequest;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.model.vo.report.CourseAchievementReportVO;
import com.yupi.springbootinit.service.CourseAchievementReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 课程达成度报表控制器
 *
 * @author YU
 */
@RestController
@RequestMapping("/course-achievement-report")
@Slf4j
public class CourseAchievementReportController {

    @Resource
    private CourseAchievementReportService courseAchievementReportService;

    /**
     * 获取课程达成度报表数据
     *
     * @param request  查询请求
     * @param httpRequest HTTP请求
     * @return 报表数据
     */
    @PostMapping("/data")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<CourseAchievementReportVO> getReportData(
            @RequestBody CourseAchievementReportRequest request,
            HttpServletRequest httpRequest) {

        // 获取当前用户ID
        Long userId = getCurrentUserId(httpRequest);

        // 参数验证
        if (request == null || request.getClassId() == null) {
            return ResultUtils.error(400, "教学班级ID不能为空");
        }

        // 权限验证
        if (!courseAchievementReportService.validateReportPermission(request.getClassId(), userId)) {
            return ResultUtils.error(403, "您不是该课程的主讲教师，无权访问报表");
        }

        try {
            CourseAchievementReportVO report =
                    courseAchievementReportService.generateReportData(request.getClassId());
            return ResultUtils.success(report);
        } catch (Exception e) {
            log.error("获取报表数据失败", e);
            return ResultUtils.error(500, "获取报表数据失败: " + e.getMessage());
        }
    }

    /**
     * 导出Excel格式报表
     *
     * @param request  导出请求
     * @param httpResponse HTTP响应
     * @param httpRequest HTTP请求
     */
    @PostMapping("/export/excel")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public void exportExcel(
            @RequestBody CourseAchievementReportRequest request,
            HttpServletResponse httpResponse,
            HttpServletRequest httpRequest) {

        // 获取当前用户ID
        Long userId = getCurrentUserId(httpRequest);

        // 参数验证
        if (request == null || request.getClassId() == null) {
            handleError(httpResponse, 400, "教学班级ID不能为空");
            return;
        }

        // 权限验证
        if (!courseAchievementReportService.validateReportPermission(request.getClassId(), userId)) {
            handleError(httpResponse, 403, "您不是该课程的主讲教师，无权导出报表");
            return;
        }

        try {
            // 生成Excel文件
            byte[] excelBytes = courseAchievementReportService.exportExcelReport(request.getClassId());

            // 设置响应头
            String fileName = "课程目标达成情况评价表_" + request.getClassId() + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            httpResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            httpResponse.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Expires", "0");

            // 写入响应流
            OutputStream outputStream = httpResponse.getOutputStream();
            outputStream.write(excelBytes);
            outputStream.flush();

            log.info("Excel报表导出成功，班级ID：{}", request.getClassId());

        } catch (Exception e) {
            log.error("Excel导出失败", e);
            handleError(httpResponse, 500, "Excel导出失败: " + e.getMessage());
        }
    }

    /**
     * 下载课程达成度报表模板
     *
     * @param httpResponse HTTP响应
     */
    @GetMapping("/template")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    @com.yupi.springbootinit.annotation.NoLog
    public void downloadTemplate(HttpServletResponse httpResponse) {
        try {
            // 设置响应头
            String fileName = "课程目标达成情况评价报表模板.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            httpResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            httpResponse.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Expires", "0");

            // 读取并写入模板文件
            org.springframework.core.io.ClassPathResource resource =
                new org.springframework.core.io.ClassPathResource("templates/course_achievement_report_template.xlsx");
            java.io.InputStream inputStream = resource.getInputStream();
            javax.servlet.ServletOutputStream outputStream = httpResponse.getOutputStream();

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.flush();

            log.info("课程达成度报表模板下载成功");

        } catch (Exception e) {
            log.error("模板下载失败", e);
            handleError(httpResponse, 500, "模板下载失败: " + e.getMessage());
        }
    }

    /**
     * 导出PDF格式报表
     *
     * @param request  导出请求
     * @param httpResponse HTTP响应
     * @param httpRequest HTTP请求
     */
    @PostMapping("/export/pdf")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public void exportPdf(
            @RequestBody CourseAchievementReportRequest request,
            HttpServletResponse httpResponse,
            HttpServletRequest httpRequest) {

        // 获取当前用户ID
        Long userId = getCurrentUserId(httpRequest);

        // 参数验证
        if (request == null || request.getClassId() == null) {
            handleError(httpResponse, 400, "教学班级ID不能为空");
            return;
        }

        // 权限验证
        if (!courseAchievementReportService.validateReportPermission(request.getClassId(), userId)) {
            handleError(httpResponse, 403, "您不是该课程的主讲教师，无权导出报表");
            return;
        }

        try {
            // 生成PDF文件
            byte[] pdfBytes = courseAchievementReportService.exportPdfReport(request.getClassId());

            // 设置响应头
            String fileName = "课程目标达成情况评价表_" + request.getClassId() + ".pdf";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            httpResponse.setContentType("application/pdf");
            httpResponse.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Expires", "0");

            // 写入响应流
            OutputStream outputStream = httpResponse.getOutputStream();
            outputStream.write(pdfBytes);
            outputStream.flush();

            log.info("PDF报表导出成功，班级ID：{}", request.getClassId());

        } catch (Exception e) {
            log.error("PDF导出失败", e);
            handleError(httpResponse, 500, "PDF导出失败: " + e.getMessage());
        }
    }

    /**
     * 错误处理
     */
    private void handleError(HttpServletResponse response, int status, String message) {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        try {
            String errorMessage = "{\"code\":" + status + ",\"message\":\"" + message + "\"}";
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(errorMessage.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException e) {
            log.error("错误处理失败", e);
        }
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Object currentUser = request.getAttribute("currentUser");
        if (currentUser instanceof SysUser) {
            return ((SysUser) currentUser).getId();
        }
        return (Long) request.getSession().getAttribute("userId");
    }
}
