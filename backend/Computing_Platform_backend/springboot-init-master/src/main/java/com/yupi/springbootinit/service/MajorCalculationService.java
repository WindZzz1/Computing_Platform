package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.model.dto.majorCalculation.MajorCalculationRequest;
import com.yupi.springbootinit.model.dto.majorCalculation.MajorDashboardQueryRequest;
import com.yupi.springbootinit.model.vo.majorCalculation.CourseCalculationStatusVO;
import com.yupi.springbootinit.model.vo.majorCalculation.MajorCalculationResultVO;

/**
 * 专业级达成度计算服务
 *
 * @author YU
 */
public interface MajorCalculationService {

    /**
     * 获取课程计算状态监控看板
     *
     * @param request 查询请求
     * @return 监控看板数据
     */
    MajorCalculationResultVO getDashboardOverview(MajorDashboardQueryRequest request);

    /**
     * 分页查询课程计算状态
     *
     * @param request 查询请求
     * @return 课程计算状态分页
     */
    Page<CourseCalculationStatusVO> getCourseCalculationStatus(MajorDashboardQueryRequest request);

    /**
     * 执行专业级达成度计算
     *
     * @param request 计算请求
     * @return 计算结果
     */
    MajorCalculationResultVO calculateMajorAchievement(MajorCalculationRequest request);

    /**
     * 查询专业级计算结果
     *
     * @param request 查询请求
     * @return 计算结果
     */
    MajorCalculationResultVO getMajorCalculationResult(MajorCalculationRequest request);

    /**
     * 删除专业级计算结果
     *
     * @param request 删除请求
     * @return 是否成功
     */
    Boolean deleteMajorCalculationResult(MajorCalculationRequest request);
}
