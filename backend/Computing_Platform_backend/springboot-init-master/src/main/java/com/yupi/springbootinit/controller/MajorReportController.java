package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.report.MajorReportRequest;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.model.vo.report.MajorAchievementRadarVO;
import com.yupi.springbootinit.model.vo.report.PenetrationAccountVO;
import com.yupi.springbootinit.service.MajorReportService;
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
 * 专业报告控制器
 *
 * @author YU
 */
@RestController
@RequestMapping("/major-report")
@Slf4j
public class MajorReportController {

    @Resource
    private MajorReportService majorReportService;

    /**
     * 获取专业达成度雷达图数据
     */
    @PostMapping("/radar-data")
    @AuthCheck(anyRole = SysUserConstant.ROLE_LEADER + "," + SysUserConstant.ROLE_EDU)
    public BaseResponse<MajorAchievementRadarVO> getRadarChartData(
            @RequestBody MajorReportRequest request,
            HttpServletRequest httpRequest) {

        // 参数验证
        if (request == null || request.getMajorId() == null ||
                request.getTermId() == null || request.getGrade() == null) {
            return ResultUtils.error(400, "专业ID、学年学期ID和年级不能为空");
        }

        // 权限验证
        Long userId = getCurrentUserId(httpRequest);
        String userRole = getCurrentUserRole(httpRequest);

        if (!majorReportService.validateMajorPermission(
                request.getMajorId(), userId, userRole)) {
            return ResultUtils.error(403, "无权访问该专业数据");
        }

        try {
            MajorAchievementRadarVO radarData =
                    majorReportService.getRadarChartData(request);
            return ResultUtils.success(radarData);
        } catch (Exception e) {
            log.error("获取雷达图数据失败", e);
            return ResultUtils.error(500, "获取雷达图数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取穿透式台账数据
     */
    @PostMapping("/penetration-account")
    @AuthCheck(anyRole = SysUserConstant.ROLE_LEADER + "," + SysUserConstant.ROLE_EDU)
    public BaseResponse<PenetrationAccountVO> getPenetrationAccount(
            @RequestBody MajorReportRequest request,
            HttpServletRequest httpRequest) {

        // 参数验证
        if (request == null || request.getMajorId() == null ||
                request.getTermId() == null || request.getGrade() == null) {
            return ResultUtils.error(400, "专业ID、学年学期ID和年级不能为空");
        }

        // 权限验证
        Long userId = getCurrentUserId(httpRequest);
        String userRole = getCurrentUserRole(httpRequest);

        if (!majorReportService.validateMajorPermission(
                request.getMajorId(), userId, userRole)) {
            return ResultUtils.error(403, "无权访问该专业数据");
        }

        try {
            PenetrationAccountVO account =
                    majorReportService.getPenetrationAccount(request);
            return ResultUtils.success(account);
        } catch (Exception e) {
            log.error("获取穿透式台账失败", e);
            return ResultUtils.error(500, "获取穿透式台账失败: " + e.getMessage());
        }
    }

    /**
     * 导出穿透式台账Excel
     */
    @PostMapping("/export/account-excel")
    @AuthCheck(anyRole = SysUserConstant.ROLE_LEADER + "," + SysUserConstant.ROLE_EDU)
    public void exportPenetrationAccountExcel(
            @RequestBody MajorReportRequest request,
            HttpServletResponse response,
            HttpServletRequest httpRequest) {

        // 参数验证
        if (request == null || request.getMajorId() == null ||
                request.getTermId() == null || request.getGrade() == null) {
            handleError(response, 400, "专业ID、学年学期ID和年级不能为空");
            return;
        }

        // 权限验证
        Long userId = getCurrentUserId(httpRequest);
        String userRole = getCurrentUserRole(httpRequest);

        if (!majorReportService.validateMajorPermission(
                request.getMajorId(), userId, userRole)) {
            handleError(response, 403, "无权访问该专业数据");
            return;
        }

        try {
            byte[] excelBytes =
                    majorReportService.exportPenetrationAccountExcel(request);

            // 设置响应头
            String fileName = "专业穿透式台账_" + request.getMajorId() + "_" +
                    request.getGrade() + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName,
                    StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            response.setContentType("application/vnd.openxmlformats-" +
                    "officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + encodedFileName);
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            // 写入响应流
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(excelBytes);
            outputStream.flush();

            log.info("穿透式台账Excel导出成功，专业ID：{}，年级：{}",
                    request.getMajorId(), request.getGrade());

        } catch (Exception e) {
            log.error("Excel导出失败", e);
            handleError(response, 500, "Excel导出失败: " + e.getMessage());
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

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        SysUser currentUser = getCurrentUser(request);
        if (currentUser != null) {
            return currentUser.getId();
        }
        return (Long) request.getSession().getAttribute("userId");
    }

    /**
     * 获取当前用户角色
     */
    private String getCurrentUserRole(HttpServletRequest request) {
        SysUser currentUser = getCurrentUser(request);
        if (currentUser != null) {
            return currentUser.getRoleCode();
        }
        return (String) request.getSession().getAttribute("userRole");
    }

    private SysUser getCurrentUser(HttpServletRequest request) {
        Object currentUser = request.getAttribute("currentUser");
        if (currentUser instanceof SysUser) {
            return (SysUser) currentUser;
        }
        return null;
    }
}
