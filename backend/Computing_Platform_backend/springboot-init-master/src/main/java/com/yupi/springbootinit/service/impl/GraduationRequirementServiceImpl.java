package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.GraduationRequirementMapper;
import com.yupi.springbootinit.mapper.IndicatorPointMapper;
import com.yupi.springbootinit.mapper.SysDictCollegeMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.model.dto.requirement.GraduationRequirementAddRequest;
import com.yupi.springbootinit.model.dto.requirement.GraduationRequirementQueryRequest;
import com.yupi.springbootinit.model.dto.requirement.GraduationRequirementUpdateRequest;
import com.yupi.springbootinit.model.entity.GraduationRequirement;
import com.yupi.springbootinit.model.entity.SysDictCollege;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.vo.GraduationRequirementVO;
import com.yupi.springbootinit.service.GraduationRequirementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 毕业要求服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class GraduationRequirementServiceImpl extends ServiceImpl<GraduationRequirementMapper, GraduationRequirement> implements GraduationRequirementService {

    @Resource
    private GraduationRequirementMapper graduationRequirementMapper;

    @Resource
    private SysDictMajorMapper sysDictMajorMapper;

    @Resource
    private SysDictCollegeMapper sysDictCollegeMapper;

    @Resource
    private IndicatorPointMapper indicatorPointMapper;

    @Override
    public Long createRequirement(GraduationRequirementAddRequest graduationRequirementAddRequest) {
        if (graduationRequirementAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String requirementCode = graduationRequirementAddRequest.getRequirementCode();
        String requirementName = graduationRequirementAddRequest.getRequirementName();
        Long majorId = graduationRequirementAddRequest.getMajorId();
        if (StringUtils.isAnyBlank(requirementCode, requirementName) || majorId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "毕业要求编号、名称或专业ID不能为空");
        }
        SysDictMajor major = sysDictMajorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "专业不存在");
        }
        synchronized (requirementCode.intern()) {
            QueryWrapper<GraduationRequirement> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("requirement_code", requirementCode);
            queryWrapper.eq("major_id", majorId);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该专业下毕业要求编号已存在");
            }
            GraduationRequirement graduationRequirement = new GraduationRequirement();
            BeanUtils.copyProperties(graduationRequirementAddRequest, graduationRequirement);
            boolean saveResult = this.save(graduationRequirement);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建毕业要求失败");
            }
            return graduationRequirement.getId();
        }
    }

    @Override
    public Boolean updateRequirement(GraduationRequirementUpdateRequest graduationRequirementUpdateRequest) {
        if (graduationRequirementUpdateRequest == null || graduationRequirementUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        GraduationRequirement existRequirement = this.getById(graduationRequirementUpdateRequest.getId());
        if (existRequirement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "毕业要求不存在");
        }
        Long majorId = graduationRequirementUpdateRequest.getMajorId();
        if (majorId != null) {
            SysDictMajor major = sysDictMajorMapper.selectById(majorId);
            if (major == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "专业不存在");
            }
        }
        String requirementCode = graduationRequirementUpdateRequest.getRequirementCode();
        Long updateMajorId = graduationRequirementUpdateRequest.getMajorId() != null ? graduationRequirementUpdateRequest.getMajorId() : existRequirement.getMajorId();
        if (StringUtils.isNotBlank(requirementCode) && !requirementCode.equals(existRequirement.getRequirementCode())) {
            QueryWrapper<GraduationRequirement> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("requirement_code", requirementCode);
            queryWrapper.eq("major_id", updateMajorId);
            queryWrapper.ne("id", graduationRequirementUpdateRequest.getId());
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该专业下毕业要求编号已存在");
            }
        }
        GraduationRequirement graduationRequirement = new GraduationRequirement();
        BeanUtils.copyProperties(graduationRequirementUpdateRequest, graduationRequirement);
        return this.updateById(graduationRequirement);
    }

    @Override
    public Boolean deleteRequirement(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "毕业要求ID不能为空");
        }
        GraduationRequirement requirement = this.getById(id);
        if (requirement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "毕业要求不存在");
        }
        validateRequirementNotReferenced(id);
        boolean result = this.removeById(id);
        return result;
    }

    private void validateRequirementNotReferenced(Long requirementId) {
        Long indicatorCount = indicatorPointMapper.countByRequirementId(requirementId);
        if (indicatorCount != null && indicatorCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "毕业要求已被指标点引用，不能删除");
        }
    }

    @Override
    public GraduationRequirementVO getRequirementById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "毕业要求ID不能为空");
        }
        GraduationRequirement graduationRequirement = this.getById(id);
        if (graduationRequirement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "毕业要求不存在");
        }
        return this.getRequirementVO(graduationRequirement);
    }

    @Override
    public Page<GraduationRequirementVO> pageRequirement(GraduationRequirementQueryRequest graduationRequirementQueryRequest) {
        long current = graduationRequirementQueryRequest.getCurrent();
        long size = graduationRequirementQueryRequest.getPageSize();
        QueryWrapper<GraduationRequirement> queryWrapper = this.getQueryWrapper(graduationRequirementQueryRequest);
        Page<GraduationRequirement> requirementPage = this.page(new Page<>(current, size), queryWrapper);
        Page<GraduationRequirementVO> requirementVOPage = new Page<>(current, size, requirementPage.getTotal());
        requirementVOPage.setRecords(requirementPage.getRecords().stream().map(this::getRequirementVO).collect(java.util.stream.Collectors.toList()));
        return requirementVOPage;
    }

    @Override
    public QueryWrapper<GraduationRequirement> getQueryWrapper(GraduationRequirementQueryRequest graduationRequirementQueryRequest) {
        QueryWrapper<GraduationRequirement> queryWrapper = new QueryWrapper<>();
        if (graduationRequirementQueryRequest == null) {
            return queryWrapper;
        }
        String requirementCode = graduationRequirementQueryRequest.getRequirementCode();
        String requirementName = graduationRequirementQueryRequest.getRequirementName();
        Long majorId = graduationRequirementQueryRequest.getMajorId();
        java.util.Date createTimeStart = graduationRequirementQueryRequest.getCreateTimeStart();
        java.util.Date createTimeEnd = graduationRequirementQueryRequest.getCreateTimeEnd();
        queryWrapper.like(StringUtils.isNotBlank(requirementCode), "requirement_code", requirementCode);
        queryWrapper.like(StringUtils.isNotBlank(requirementName), "requirement_name", requirementName);
        queryWrapper.eq(majorId != null, "major_id", majorId);
        queryWrapper.ge(createTimeStart != null, "create_time", createTimeStart);
        queryWrapper.le(createTimeEnd != null, "create_time", createTimeEnd);
        queryWrapper.orderByAsc("requirement_code");
        return queryWrapper;
    }

    @Override
    public GraduationRequirementVO getRequirementVO(GraduationRequirement graduationRequirement) {
        if (graduationRequirement == null) {
            return null;
        }
        GraduationRequirementVO graduationRequirementVO = new GraduationRequirementVO();
        BeanUtils.copyProperties(graduationRequirement, graduationRequirementVO);
        if (graduationRequirement.getMajorId() != null) {
            SysDictMajor major = sysDictMajorMapper.selectById(graduationRequirement.getMajorId());
            if (major != null) {
                graduationRequirementVO.setMajorName(major.getMajorName());
                graduationRequirementVO.setCollegeId(major.getCollegeId());
                if (major.getCollegeId() != null) {
                    SysDictCollege college = sysDictCollegeMapper.selectById(major.getCollegeId());
                    if (college != null) {
                        graduationRequirementVO.setCollegeName(college.getCollegeName());
                    }
                }
            }
        }
        return graduationRequirementVO;
    }
}
