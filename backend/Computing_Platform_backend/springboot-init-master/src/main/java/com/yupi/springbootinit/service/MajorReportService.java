package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.dto.report.MajorReportRequest;
import com.yupi.springbootinit.model.vo.report.MajorAchievementRadarVO;
import com.yupi.springbootinit.model.vo.report.PenetrationAccountVO;

/**
 * 专业报告服务接口
 *
 * @author YU
 */
public interface MajorReportService {

    /**
     * 获取专业达成度雷达图数据
     *
     * @param request 查询请求
     * @return 雷达图数据
     */
    MajorAchievementRadarVO getRadarChartData(MajorReportRequest request);

    /**
     * 获取穿透式台账数据
     *
     * @param request 查询请求
     * @return 穿透式台账数据
     */
    PenetrationAccountVO getPenetrationAccount(MajorReportRequest request);

    /**
     * 导出穿透式台账Excel
     *
     * @param request 查询请求
     * @return Excel文件字节数组
     */
    byte[] exportPenetrationAccountExcel(MajorReportRequest request);

    /**
     * 导出专业指标点达成度（三级）Excel
     *
     * @param request 查询请求
     * @return Excel 文件字节数组
     */
    byte[] exportIndicatorAchievementExcel(MajorReportRequest request);

    /**
     * 导出专业指标点达成度（三级）PDF
     *
     * @param request 查询请求
     * @return PDF 文件字节数组
     */
    byte[] exportIndicatorAchievementPdf(MajorReportRequest request);

    /**
     * 验证用户权限
     *
     * @param majorId 专业ID
     * @param userId 用户ID
     * @param userRole 用户角色
     * @return 是否有权限
     */
    boolean validateMajorPermission(Long majorId, Long userId, String userRole);
}