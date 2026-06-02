package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.annotation.NoLog;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.score.ScoreUpdateRequest;
import com.yupi.springbootinit.model.vo.AchievementCalculationResultVO;
import com.yupi.springbootinit.model.vo.ScoreImportResultVO;
import com.yupi.springbootinit.model.vo.ScorePreviewVO;
import com.yupi.springbootinit.service.AchievementCalculationService;
import com.yupi.springbootinit.service.StudentScoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 学生考核点成绩控制器
 *
 * @author YU
 */
@RestController
@RequestMapping("/student-score")
@Slf4j
public class StudentScoreController {

    @Resource
    private StudentScoreService studentScoreService;

    @Resource
    private AchievementCalculationService achievementCalculationService;

    /**
     * 下载成绩录入模板
     * 模板包含：
     * 1. 两行复合表头（第一行显示课程信息，第二行显示具体列名）
     * 2. 学号、姓名列
     * 3. 所有考核点列（格式：A1 期末考试(满分100)）
     * 4. 预填充该班级所有学生的学号和姓名
     *
     * @param classId 教学班级ID
     * @param response HTTP响应
     * @throws Exception 异常
     */
    @GetMapping("/template/{classId}")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    @NoLog
    public void downloadScoreTemplate(
            @PathVariable Long classId,
            HttpServletResponse response) throws Exception {

        log.info("开始下载成绩录入模板，班级ID：{}", classId);

        // 生成模板字节数组
        byte[] templateBytes = studentScoreService.generateScoreTemplate(classId);

        // 生成文件名（带时间戳）
        String filename = String.format("成绩录入模板_%s.xlsx",
                DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));

        // URL编码文件名（支持中文）
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // 写入响应流
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(templateBytes);
        outputStream.flush();

        log.info("成绩录入模板下载成功，班级ID：{}，文件大小：{} bytes", classId, templateBytes.length);
    }

    /**
     * 导入学生成绩
     * 上传填写完成的Excel模板，系统解析并校验数据，存入数据库
     *
     * @param classId 教学班级ID
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/import/{classId}")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<ScoreImportResultVO> importScores(
            @PathVariable Long classId,
            @RequestPart("file") MultipartFile file) {

        log.info("开始导入成绩，班级ID：{}，文件名：{}，文件大小：{} bytes",
                classId, file.getOriginalFilename(), file.getSize());

        ScoreImportResultVO result = studentScoreService.importScores(classId, file);

        log.info("成绩导入完成，班级ID：{}，总数：{}，成功：{}，失败：{}",
                classId, result.getTotal(), result.getSuccessCount(), result.getFailedCount());

        return ResultUtils.success(result);
    }

    /**
     * 获取教学班级的成绩预览数据
     * 返回所有学生的所有考核点成绩，用于前端电子表格展示
     *
     * @param classId 教学班级ID
     * @return 成绩预览列表
     */
    @GetMapping("/preview/{classId}")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<List<ScorePreviewVO>> getScoresPreview(@PathVariable Long classId) {

        log.info("获取成绩预览，班级ID：{}", classId);

        List<ScorePreviewVO> scores = studentScoreService.getScoresByClass(classId);

        log.info("获取成绩预览成功，班级ID：{}，记录数：{}", classId, scores.size());

        return ResultUtils.success(scores);
    }

    /**
     * 更新单条成绩记录
     * 用于在线修改和补录成绩
     *
     * @param scoreUpdateRequest 成绩更新请求
     * @return 是否成功
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Boolean> updateScore(@Valid @RequestBody ScoreUpdateRequest scoreUpdateRequest) {

        log.info("更新成绩，记录ID：{}，得分：{}", scoreUpdateRequest.getId(), scoreUpdateRequest.getActualScore());

        Boolean success = studentScoreService.updateScore(
                scoreUpdateRequest.getId(),
                scoreUpdateRequest.getActualScore()
        );

        log.info("更新成绩{}", success ? "成功" : "失败");

        return ResultUtils.success(success);
    }

    /**
     * 删除教学班级的所有成绩
     * 用于重新导入前的数据清理
     *
     * @param classId 教学班级ID
     * @return 是否成功
     */
    @DeleteMapping("/delete/{classId}")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Boolean> deleteScores(@PathVariable Long classId) {

        log.info("删除教学班级成绩，班级ID：{}", classId);

        Boolean success = studentScoreService.deleteScoresByClassId(classId);

        log.info("删除教学班级成绩{}", success ? "成功" : "失败");

        return ResultUtils.success(success);
    }

    /**
     * 一键计算课程达成度
     * 自动完成一级和二级达成度计算，并自动锁定成绩
     *
     * @param classId 教学班级ID
     * @return 计算结果
     */
    @PostMapping("/calculate/{classId}")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<AchievementCalculationResultVO> calculateAchievements(@PathVariable Long classId) {

        log.info("开始一键计算达成度，班级ID：{}", classId);

        AchievementCalculationResultVO result = achievementCalculationService.calculateAchievements(classId);

        if (result.getSuccess()) {
            log.info("达成度计算成功，班级ID：{}", classId);
        } else {
            log.error("达成度计算失败，班级ID：{}，错误：{}", classId, result.getErrorMessage());
        }

        return ResultUtils.success(result);
    }

    /**
     * 获取达成度计算结果
     *
     * @param classId 教学班级ID
     * @return 计算结果
     */
    @GetMapping("/calculation-result/{classId}")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<AchievementCalculationResultVO> getCalculationResult(@PathVariable Long classId) {

        log.info("获取达成度计算结果，班级ID：{}", classId);

        AchievementCalculationResultVO result = achievementCalculationService.getCalculationResult(classId);

        return ResultUtils.success(result);
    }

    /**
     * 获取一级达成度（学生课程目标达成度）
     *
     * @param classId 教学班级ID
     * @return 一级达成度
     */
    @GetMapping("/first-level/{classId}")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<AchievementCalculationResultVO> getFirstLevelAchievements(@PathVariable Long classId) {

        log.info("获取一级达成度，班级ID：{}", classId);

        AchievementCalculationResultVO result = achievementCalculationService.getFirstLevelAchievements(classId);

        return ResultUtils.success(result);
    }

    /**
     * 获取二级达成度（课程指标点达成度）
     *
     * @param classId 教学班级ID
     * @return 二级达成度
     */
    @GetMapping("/second-level/{classId}")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<AchievementCalculationResultVO> getSecondLevelAchievements(@PathVariable Long classId) {

        log.info("获取二级达成度，班级ID：{}", classId);

        AchievementCalculationResultVO result = achievementCalculationService.getSecondLevelAchievements(classId);

        return ResultUtils.success(result);
    }
}
