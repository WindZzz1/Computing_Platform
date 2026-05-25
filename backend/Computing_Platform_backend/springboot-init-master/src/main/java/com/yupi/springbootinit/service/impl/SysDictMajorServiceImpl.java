package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.SysDictCollegeMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.model.dto.dict.SysDictMajorAddRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictMajorQueryRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictMajorUpdateRequest;
import com.yupi.springbootinit.model.entity.SysDictCollege;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.vo.SysDictMajorVO;
import com.yupi.springbootinit.service.SysDictMajorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 专业字典服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class SysDictMajorServiceImpl extends ServiceImpl<SysDictMajorMapper, SysDictMajor> implements SysDictMajorService {

    @Resource
    private SysDictMajorMapper sysDictMajorMapper;

    @Resource
    private SysDictCollegeMapper sysDictCollegeMapper;

    @Override
    public Long createMajor(SysDictMajorAddRequest sysDictMajorAddRequest) {
        if (sysDictMajorAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String majorCode = sysDictMajorAddRequest.getMajorCode();
        String majorName = sysDictMajorAddRequest.getMajorName();
        Long collegeId = sysDictMajorAddRequest.getCollegeId();
        if (StringUtils.isAnyBlank(majorCode, majorName) || collegeId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业代码、专业名称或学院ID不能为空");
        }
        SysDictCollege college = sysDictCollegeMapper.selectById(collegeId);
        if (college == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学院不存在");
        }
        synchronized (majorCode.intern()) {
            QueryWrapper<SysDictMajor> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("major_code", majorCode);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业代码已存在");
            }
            SysDictMajor sysDictMajor = new SysDictMajor();
            BeanUtils.copyProperties(sysDictMajorAddRequest, sysDictMajor);
            boolean saveResult = this.save(sysDictMajor);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建专业失败");
            }
            return sysDictMajor.getId();
        }
    }

    @Override
    public Boolean updateMajor(SysDictMajorUpdateRequest sysDictMajorUpdateRequest) {
        if (sysDictMajorUpdateRequest == null || sysDictMajorUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        SysDictMajor existMajor = this.getById(sysDictMajorUpdateRequest.getId());
        if (existMajor == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "专业不存在");
        }
        Long collegeId = sysDictMajorUpdateRequest.getCollegeId();
        if (collegeId != null) {
            SysDictCollege college = sysDictCollegeMapper.selectById(collegeId);
            if (college == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学院不存在");
            }
        }
        String majorCode = sysDictMajorUpdateRequest.getMajorCode();
        if (StringUtils.isNotBlank(majorCode) && !majorCode.equals(existMajor.getMajorCode())) {
            QueryWrapper<SysDictMajor> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("major_code", majorCode);
            queryWrapper.ne("id", sysDictMajorUpdateRequest.getId());
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业代码已存在");
            }
        }
        SysDictMajor sysDictMajor = new SysDictMajor();
        BeanUtils.copyProperties(sysDictMajorUpdateRequest, sysDictMajor);
        return this.updateById(sysDictMajor);
    }

    @Override
    public Boolean deleteMajor(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业ID不能为空");
        }
        boolean result = this.removeById(id);
        return result;
    }

    @Override
    public SysDictMajorVO getMajorById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业ID不能为空");
        }
        SysDictMajor sysDictMajor = this.getById(id);
        if (sysDictMajor == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "专业不存在");
        }
        return this.getMajorVO(sysDictMajor);
    }

    @Override
    public Page<SysDictMajorVO> pageMajor(SysDictMajorQueryRequest sysDictMajorQueryRequest) {
        long current = sysDictMajorQueryRequest.getCurrent();
        long size = sysDictMajorQueryRequest.getPageSize();
        QueryWrapper<SysDictMajor> queryWrapper = this.getQueryWrapper(sysDictMajorQueryRequest);
        Page<SysDictMajor> majorPage = this.page(new Page<>(current, size), queryWrapper);
        Page<SysDictMajorVO> majorVOPage = new Page<>(current, size, majorPage.getTotal());
        majorVOPage.setRecords(majorPage.getRecords().stream().map(this::getMajorVO).collect(java.util.stream.Collectors.toList()));
        return majorVOPage;
    }

    @Override
    public QueryWrapper<SysDictMajor> getQueryWrapper(SysDictMajorQueryRequest sysDictMajorQueryRequest) {
        QueryWrapper<SysDictMajor> queryWrapper = new QueryWrapper<>();
        if (sysDictMajorQueryRequest == null) {
            return queryWrapper;
        }
        String majorCode = sysDictMajorQueryRequest.getMajorCode();
        String majorName = sysDictMajorQueryRequest.getMajorName();
        Long collegeId = sysDictMajorQueryRequest.getCollegeId();
        queryWrapper.like(StringUtils.isNotBlank(majorCode), "major_code", majorCode);
        queryWrapper.like(StringUtils.isNotBlank(majorName), "major_name", majorName);
        queryWrapper.eq(collegeId != null, "college_id", collegeId);
//        queryWrapper.orderByDesc("create_time");
        return queryWrapper;
    }

    @Override
    public SysDictMajorVO getMajorVO(SysDictMajor sysDictMajor) {
        if (sysDictMajor == null) {
            return null;
        }
        SysDictMajorVO sysDictMajorVO = new SysDictMajorVO();
        BeanUtils.copyProperties(sysDictMajor, sysDictMajorVO);
        if (sysDictMajor.getCollegeId() != null) {
            SysDictCollege college = sysDictCollegeMapper.selectById(sysDictMajor.getCollegeId());
            if (college != null) {
                sysDictMajorVO.setCollegeName(college.getCollegeName());
            }
        }
        return sysDictMajorVO;
    }
}