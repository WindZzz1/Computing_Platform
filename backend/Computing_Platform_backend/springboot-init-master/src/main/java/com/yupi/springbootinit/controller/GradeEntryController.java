package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.gradeEntry.GradeEntryImportRequest;
import com.yupi.springbootinit.model.dto.gradeEntry.GradeEntryQueryRequest;
import com.yupi.springbootinit.model.dto.gradeEntry.GradeEntryTemplateRequest;
import com.yupi.springbootinit.model.dto.gradeEntry.StudentScoreUpdateRequest;
import com.yupi.springbootinit.model.vo.gradeEntry.GradeImportResultVO;
import com.yupi.springbootinit.model.vo.gradeEntry.StudentScoreVO;
import com.yupi.springbootinit.service.GradeEntryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 成绩录入接口
 *
 * @author YU
 */
@RestController
@RequestMapping("/grade-entry")
@Slf4j
public class GradeEntryController {

    @Resource
    private GradeEntryService gradeEntryService;

    /**
     * 生成并下载成绩录入模板
     *
     * @param request 请求参数
     * @param response HTTP响应
     */
    @PostMapping("/template/download")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public void downloadTemplate(@RequestBody GradeEntryTemplateRequest request, HttpServletResponse response) {
        try {
            gradeEntryService.generateAndDownloadTemplate(request, response);
        } catch (Exception e) {
            log.error("生成成绩录入模板失败", e);
            throw new RuntimeException("生成成绩录入模板失败: " + e.getMessage());
        }
    }

    /**
     * 导入成绩数据
     *
     * @param request 导入请求
     * @return 导入结果
     */
    @PostMapping("/import")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<GradeImportResultVO> importGrades(@RequestBody GradeEntryImportRequest request) {
        GradeImportResultVO result = gradeEntryService.importGrades(request);
        return ResultUtils.success(result);
    }

    /**
     * 查询成绩数据
     *
     * @param request 查询请求
     * @return 成绩数据分页
     */
    @PostMapping("/query")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Page<StudentScoreVO>> queryGrades(@RequestBody GradeEntryQueryRequest request) {
        Page<StudentScoreVO> page = gradeEntryService.queryGrades(request);
        return ResultUtils.success(page);
    }

    /**
     * 更新成绩数据
     *
     * @param request 更新请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Boolean> updateGrades(@RequestBody StudentScoreUpdateRequest request) {
        Boolean result = gradeEntryService.updateGrades(request);
        return ResultUtils.success(result);
    }

    /**
     * 删除教学班级的所有成绩
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Boolean> deleteClassGrades(@RequestBody DeleteRequest deleteRequest) {
        Boolean result = gradeEntryService.deleteClassGrades(deleteRequest.getId());
        return ResultUtils.success(result);
    }
}
