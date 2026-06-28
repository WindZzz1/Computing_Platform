package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointAddRequest;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointQueryRequest;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointUpdateRequest;
import com.yupi.springbootinit.model.vo.AssessmentPointVO;
import com.yupi.springbootinit.service.AssessmentPointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

// 课程考核点接口

@RestController
@RequestMapping("/assessment/point")
@Slf4j
public class AssessmentPointController {

    @Resource
    private AssessmentPointService assessmentPointService;

    /**
     * 创建考核点
     *
     * @param request 新增请求
     * @return 考核点ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Long> addAssessmentPoint(@RequestBody AssessmentPointAddRequest request) {
        return ResultUtils.success(assessmentPointService.createAssessmentPoint(request));
    }

    /**
     * 更新考核点
     *
     * @param request 更新请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Boolean> updateAssessmentPoint(@RequestBody AssessmentPointUpdateRequest request) {
        return ResultUtils.success(assessmentPointService.updateAssessmentPoint(request));
    }

    /**
     * 删除考核点
     *
     * @param request 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Boolean> deleteAssessmentPoint(@RequestBody DeleteRequest request) {
        return ResultUtils.success(assessmentPointService.deleteAssessmentPoint(request.getId()));
    }

    /**
     * 根据ID获取考核点
     *
     * @param request ID请求
     * @return 考核点信息
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<AssessmentPointVO> getAssessmentPoint(@RequestBody DeleteRequest request) {
        return ResultUtils.success(assessmentPointService.getAssessmentPointById(request.getId()));
    }

    /**
     * 分页查询考核点
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Page<AssessmentPointVO>> pageAssessmentPoint(@RequestBody AssessmentPointQueryRequest request) {
        return ResultUtils.success(assessmentPointService.pageAssessmentPoint(request));
    }

    /**
     * 获取考核点列表
     *
     * @param request 查询请求
     * @return 考核点列表
     */
    @PostMapping("/list")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Page<AssessmentPointVO>> listAssessmentPoint(@RequestBody AssessmentPointQueryRequest request) {
        if (request == null) {
            request = new AssessmentPointQueryRequest();
        }
        request.setCurrent(1);
        request.setPageSize(1000);
        return ResultUtils.success(assessmentPointService.pageAssessmentPoint(request));
    }

    /**
     * 通过Excel批量导入考核点（含支撑权重）
     *
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/import/excel")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Map<String, Object>> importAssessmentPointsFromExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = assessmentPointService.importAssessmentPointsFromExcel(file);
        return ResultUtils.success(result);
    }

    /**
     * 下载考核点导入模板
     */
    @GetMapping("/template")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    @com.yupi.springbootinit.annotation.NoLog
    public void downloadAssessmentPointTemplate(HttpServletResponse response) throws Exception {
        String filename = "考核点导入模板.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        byte[] templateBytes = assessmentPointService.generateAssessmentPointTemplate();
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(templateBytes);
        outputStream.flush();
    }
}
