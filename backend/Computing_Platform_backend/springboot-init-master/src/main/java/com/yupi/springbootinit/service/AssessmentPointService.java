package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointAddRequest;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointQueryRequest;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointUpdateRequest;
import com.yupi.springbootinit.model.entity.AssessmentPoint;
import com.yupi.springbootinit.model.vo.AssessmentPointVO;

//课程考核点服务

public interface AssessmentPointService extends IService<AssessmentPoint> {

    /**
     * 创建考核点
     *
     * @param request 新增请求
     * @return 考核点ID
     */
    Long createAssessmentPoint(AssessmentPointAddRequest request);

    /**
     * 更新考核点
     *
     * @param request 更新请求
     * @return 是否成功
     */
    Boolean updateAssessmentPoint(AssessmentPointUpdateRequest request);

    /**
     * 删除考核点
     *
     * @param id 考核点ID
     * @return 是否成功
     */
    Boolean deleteAssessmentPoint(Long id);

    /**
     * 根据ID获取考核点
     *
     * @param id 考核点ID
     * @return 考核点VO
     */
    AssessmentPointVO getAssessmentPointById(Long id);

    /**
     * 分页查询考核点
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<AssessmentPointVO> pageAssessmentPoint(AssessmentPointQueryRequest request);

    /**
     * 获取查询条件
     *
     * @param request 查询请求
     * @return 查询条件
     */
    QueryWrapper<AssessmentPoint> getQueryWrapper(AssessmentPointQueryRequest request);

    /**
     * 获取考核点VO
     *
     * @param assessmentPoint 考核点实体
     * @return 考核点VO
     */
    AssessmentPointVO getAssessmentPointVO(AssessmentPoint assessmentPoint);
}
