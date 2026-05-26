package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.GraduationRequirementMapper;
import com.yupi.springbootinit.mapper.IndicatorPointMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.model.dto.indicator.IndicatorPointAddRequest;
import com.yupi.springbootinit.model.dto.indicator.IndicatorPointQueryRequest;
import com.yupi.springbootinit.model.dto.indicator.IndicatorPointUpdateRequest;
import com.yupi.springbootinit.model.entity.GraduationRequirement;
import com.yupi.springbootinit.model.entity.IndicatorPoint;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.vo.IndicatorPointVO;
import com.yupi.springbootinit.service.IndicatorPointService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 二级指标点服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class IndicatorPointServiceImpl extends ServiceImpl<IndicatorPointMapper, IndicatorPoint> implements IndicatorPointService {

    @Resource
    private IndicatorPointMapper indicatorPointMapper;

    @Resource
    private GraduationRequirementMapper graduationRequirementMapper;

    @Resource
    private SysDictMajorMapper sysDictMajorMapper;

    @Override
    public Long createIndicatorPoint(IndicatorPointAddRequest indicatorPointAddRequest) {
        if (indicatorPointAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String indicatorCode = indicatorPointAddRequest.getIndicatorCode();
        String indicatorName = indicatorPointAddRequest.getIndicatorName();
        Long requirementId = indicatorPointAddRequest.getRequirementId();
        if (StringUtils.isAnyBlank(indicatorCode, indicatorName) || requirementId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "指标点编号、名称或毕业要求ID不能为空");
        }
        GraduationRequirement requirement = graduationRequirementMapper.selectById(requirementId);
        if (requirement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "毕业要求不存在");
        }
        synchronized (indicatorCode.intern()) {
            QueryWrapper<IndicatorPoint> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("indicator_code", indicatorCode);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该专业下指标点编号已存在");
            }
            IndicatorPoint indicatorPoint = new IndicatorPoint();
            BeanUtils.copyProperties(indicatorPointAddRequest, indicatorPoint);
            boolean saveResult = this.save(indicatorPoint);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建指标点失败");
            }
            return indicatorPoint.getId();
        }
    }

    @Override
    public Boolean updateIndicatorPoint(IndicatorPointUpdateRequest indicatorPointUpdateRequest) {
        if (indicatorPointUpdateRequest == null || indicatorPointUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        IndicatorPoint existIndicatorPoint = this.getById(indicatorPointUpdateRequest.getId());
        if (existIndicatorPoint == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "指标点不存在");
        }
        Long requirementId = indicatorPointUpdateRequest.getRequirementId();
        if (requirementId != null) {
            GraduationRequirement requirement = graduationRequirementMapper.selectById(requirementId);
            if (requirement == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "毕业要求不存在");
            }
        }
        String indicatorCode = indicatorPointUpdateRequest.getIndicatorCode();
        if (StringUtils.isNotBlank(indicatorCode) && !indicatorCode.equals(existIndicatorPoint.getIndicatorCode())) {
            QueryWrapper<IndicatorPoint> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("indicator_code", indicatorCode);
            queryWrapper.ne("id", indicatorPointUpdateRequest.getId());
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该专业下指标点编号已存在");
            }
        }
        IndicatorPoint indicatorPoint = new IndicatorPoint();
        BeanUtils.copyProperties(indicatorPointUpdateRequest, indicatorPoint);
        return this.updateById(indicatorPoint);
    }

    @Override
    public Boolean deleteIndicatorPoint(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "指标点ID不能为空");
        }
        boolean result = this.removeById(id);
        return result;
    }

    @Override
    public IndicatorPointVO getIndicatorPointById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "指标点ID不能为空");
        }
        IndicatorPoint indicatorPoint = this.getById(id);
        if (indicatorPoint == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "指标点不存在");
        }
        return this.getIndicatorPointVO(indicatorPoint);
    }

    @Override
    public Page<IndicatorPointVO> pageIndicatorPoint(IndicatorPointQueryRequest indicatorPointQueryRequest) {
        long current = indicatorPointQueryRequest.getCurrent();
        long size = indicatorPointQueryRequest.getPageSize();
        QueryWrapper<IndicatorPoint> queryWrapper = this.getQueryWrapper(indicatorPointQueryRequest);
        Page<IndicatorPoint> indicatorPointPage = this.page(new Page<>(current, size), queryWrapper);
        Page<IndicatorPointVO> indicatorPointVOPage = new Page<>(current, size, indicatorPointPage.getTotal());
        indicatorPointVOPage.setRecords(indicatorPointPage.getRecords().stream().map(this::getIndicatorPointVO).collect(java.util.stream.Collectors.toList()));
        return indicatorPointVOPage;
    }

    @Override
    public QueryWrapper<IndicatorPoint> getQueryWrapper(IndicatorPointQueryRequest indicatorPointQueryRequest) {
        QueryWrapper<IndicatorPoint> queryWrapper = new QueryWrapper<>();
        if (indicatorPointQueryRequest == null) {
            return queryWrapper;
        }
        String indicatorCode = indicatorPointQueryRequest.getIndicatorCode();
        String indicatorName = indicatorPointQueryRequest.getIndicatorName();
        Long requirementId = indicatorPointQueryRequest.getRequirementId();
        java.util.Date createTimeStart = indicatorPointQueryRequest.getCreateTimeStart();
        java.util.Date createTimeEnd = indicatorPointQueryRequest.getCreateTimeEnd();
        queryWrapper.like(StringUtils.isNotBlank(indicatorCode), "indicator_code", indicatorCode);
        queryWrapper.like(StringUtils.isNotBlank(indicatorName), "indicator_name", indicatorName);
        queryWrapper.eq(requirementId != null, "requirement_id", requirementId);
        queryWrapper.ge(createTimeStart != null, "create_time", createTimeStart);
        queryWrapper.le(createTimeEnd != null, "create_time", createTimeEnd);
        queryWrapper.orderByAsc("indicator_code");
        return queryWrapper;
    }

    @Override
    public IndicatorPointVO getIndicatorPointVO(IndicatorPoint indicatorPoint) {
        if (indicatorPoint == null) {
            return null;
        }
        IndicatorPointVO indicatorPointVO = new IndicatorPointVO();
        BeanUtils.copyProperties(indicatorPoint, indicatorPointVO);
        if (indicatorPoint.getRequirementId() != null) {
            GraduationRequirement requirement = graduationRequirementMapper.selectById(indicatorPoint.getRequirementId());
            if (requirement != null) {
                indicatorPointVO.setRequirementCode(requirement.getRequirementCode());
                indicatorPointVO.setRequirementName(requirement.getRequirementName());
            }
        }
        return indicatorPointVO;
    }
}