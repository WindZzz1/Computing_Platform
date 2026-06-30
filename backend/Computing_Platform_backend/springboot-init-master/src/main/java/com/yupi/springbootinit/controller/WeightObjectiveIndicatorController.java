package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.weight.AvailableIndicatorRequest;
import com.yupi.springbootinit.model.dto.weight.WeightObjectiveIndicatorCheckRequest;
import com.yupi.springbootinit.model.dto.weight.WeightObjectiveIndicatorSaveRequest;
import com.yupi.springbootinit.model.vo.IndicatorPointVO;
import com.yupi.springbootinit.model.vo.WeightCheckVO;
import com.yupi.springbootinit.model.vo.WeightObjectiveIndicatorVO;
import com.yupi.springbootinit.service.WeightObjectiveIndicatorService;
import com.yupi.springbootinit.annotation.NoLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

// 课程目标-指标点内部权重接口

@RestController
@RequestMapping("/weight/objective-indicator")
@Slf4j
public class WeightObjectiveIndicatorController {

    @Resource
    private WeightObjectiveIndicatorService weightObjectiveIndicatorService;

    /**
     * 获取指定课程可配置的指标点
     *
     * @param request 查询请求
     * @return 指标点列表
     */
    @PostMapping("/available")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<List<IndicatorPointVO>> listAvailableIndicators(@RequestBody AvailableIndicatorRequest request) {
        return ResultUtils.success(weightObjectiveIndicatorService.listAvailableIndicators(request.getCourseId()));
    }

    /**
     * 保存课程内部权重配置
     *
     * @param request 保存请求
     * @return 是否成功
     */
    @PostMapping("/save")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Boolean> saveWeights(@RequestBody WeightObjectiveIndicatorSaveRequest request) {
        return ResultUtils.success(weightObjectiveIndicatorService.saveWeights(request));
    }

    /**
     * 获取指定课程的内部权重配置
     *
     * @param request 查询请求
     * @return 内部权重列表
     */
    @PostMapping("/list")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<List<WeightObjectiveIndicatorVO>> listWeights(@RequestBody AvailableIndicatorRequest request) {
        return ResultUtils.success(weightObjectiveIndicatorService.listWeights(request.getCourseId()));
    }

    /**
     * 校验内部权重合计是否为1
     *
     * @param request 校验请求
     * @return 校验结果
     */
    @PostMapping("/check")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<WeightCheckVO> checkWeights(@RequestBody WeightObjectiveIndicatorCheckRequest request) {
        return ResultUtils.success(weightObjectiveIndicatorService.checkWeights(request));
    }

    /**
     * 通过 Excel 批量导入内部贡献权重
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    @PostMapping("/import/excel")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Map<String, Object>> importWeightsFromExcel(@RequestParam("file") MultipartFile file) {
        return ResultUtils.success(weightObjectiveIndicatorService.importWeightsFromExcel(file));
    }

    /**
     * 下载内部贡献权重导入模板
     */
    @GetMapping("/template")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    @NoLog
    public void downloadWeightTemplate(HttpServletResponse response) throws Exception {
        String filename = "内部贡献权重导入模板.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(weightObjectiveIndicatorService.generateWeightTemplate());
        outputStream.flush();
    }
}
