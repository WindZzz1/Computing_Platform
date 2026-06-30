package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.requirement.GraduationRequirementAddRequest;
import com.yupi.springbootinit.model.dto.requirement.GraduationRequirementQueryRequest;
import com.yupi.springbootinit.model.dto.requirement.GraduationRequirementUpdateRequest;
import com.yupi.springbootinit.model.entity.GraduationRequirement;
import com.yupi.springbootinit.model.vo.GraduationRequirementVO;

/**
 * 毕业要求服务
 *
 * @author YU
 */
public interface GraduationRequirementService extends IService<GraduationRequirement> {

    /**
     * 创建毕业要求
     *
     * @param graduationRequirementAddRequest 新增请求
     * @return 毕业要求ID
     */
    Long createRequirement(GraduationRequirementAddRequest graduationRequirementAddRequest);

    /**
     * 更新毕业要求
     *
     * @param graduationRequirementUpdateRequest 更新请求
     * @return 是否成功
     */
    Boolean updateRequirement(GraduationRequirementUpdateRequest graduationRequirementUpdateRequest);

    /**
     * 删除毕业要求
     *
     * @param id 毕业要求ID
     * @return 是否成功
     */
    Boolean deleteRequirement(Long id);

    /**
     * 根据ID获取毕业要求
     *
     * @param id 毕业要求ID
     * @return 毕业要求VO
     */
    GraduationRequirementVO getRequirementById(Long id);

    /**
     * 分页查询毕业要求
     *
     * @param graduationRequirementQueryRequest 查询请求
     * @return 分页结果
     */
    Page<GraduationRequirementVO> pageRequirement(GraduationRequirementQueryRequest graduationRequirementQueryRequest);

    /**
     * 获取查询条件
     *
     * @param graduationRequirementQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<GraduationRequirement> getQueryWrapper(GraduationRequirementQueryRequest graduationRequirementQueryRequest);

    /**
     * 获取毕业要求VO
     *
     * @param graduationRequirement 毕业要求实体
     * @return 毕业要求VO
     */
    GraduationRequirementVO getRequirementVO(GraduationRequirement graduationRequirement);
}