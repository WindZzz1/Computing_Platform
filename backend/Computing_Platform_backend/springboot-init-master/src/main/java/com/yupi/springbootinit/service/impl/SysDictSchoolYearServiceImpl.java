package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.SysDictSchoolYearMapper;
import com.yupi.springbootinit.model.dto.dict.SysDictSchoolYearAddRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictSchoolYearQueryRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictSchoolYearUpdateRequest;
import com.yupi.springbootinit.model.entity.SysDictSchoolYear;
import com.yupi.springbootinit.model.vo.SysDictSchoolYearVO;
import com.yupi.springbootinit.service.SysDictSchoolYearService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 学年学期字典服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class SysDictSchoolYearServiceImpl extends ServiceImpl<SysDictSchoolYearMapper, SysDictSchoolYear> implements SysDictSchoolYearService {

    @Resource
    private SysDictSchoolYearMapper sysDictSchoolYearMapper;

    @Override
    public Long createSchoolYear(SysDictSchoolYearAddRequest sysDictSchoolYearAddRequest) {
        if (sysDictSchoolYearAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String yearName = sysDictSchoolYearAddRequest.getYearName();
        String semesterName = sysDictSchoolYearAddRequest.getSemesterName();
        if (StringUtils.isAnyBlank(yearName, semesterName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "学年名称或学期名称不能为空");
        }
        SysDictSchoolYear sysDictSchoolYear = new SysDictSchoolYear();
        BeanUtils.copyProperties(sysDictSchoolYearAddRequest, sysDictSchoolYear);
        boolean saveResult = this.save(sysDictSchoolYear);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建学年学期失败");
        }
        return sysDictSchoolYear.getId();
    }

    @Override
    public Boolean updateSchoolYear(SysDictSchoolYearUpdateRequest sysDictSchoolYearUpdateRequest) {
        if (sysDictSchoolYearUpdateRequest == null || sysDictSchoolYearUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        SysDictSchoolYear existSchoolYear = this.getById(sysDictSchoolYearUpdateRequest.getId());
        if (existSchoolYear == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学年学期不存在");
        }
        SysDictSchoolYear sysDictSchoolYear = new SysDictSchoolYear();
        BeanUtils.copyProperties(sysDictSchoolYearUpdateRequest, sysDictSchoolYear);
        return this.updateById(sysDictSchoolYear);
    }

    @Override
    public Boolean deleteSchoolYear(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "学年学期ID不能为空");
        }
        boolean result = this.removeById(id);
        return result;
    }

    @Override
    public SysDictSchoolYearVO getSchoolYearById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "学年学期ID不能为空");
        }
        SysDictSchoolYear sysDictSchoolYear = this.getById(id);
        if (sysDictSchoolYear == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学年学期不存在");
        }
        return this.getSchoolYearVO(sysDictSchoolYear);
    }

    @Override
    public Page<SysDictSchoolYearVO> pageSchoolYear(SysDictSchoolYearQueryRequest sysDictSchoolYearQueryRequest) {
        long current = sysDictSchoolYearQueryRequest.getCurrent();
        long size = sysDictSchoolYearQueryRequest.getPageSize();
        QueryWrapper<SysDictSchoolYear> queryWrapper = this.getQueryWrapper(sysDictSchoolYearQueryRequest);
        Page<SysDictSchoolYear> schoolYearPage = this.page(new Page<>(current, size), queryWrapper);
        Page<SysDictSchoolYearVO> schoolYearVOPage = new Page<>(current, size, schoolYearPage.getTotal());
        schoolYearVOPage.setRecords(schoolYearPage.getRecords().stream().map(this::getSchoolYearVO).collect(java.util.stream.Collectors.toList()));
        return schoolYearVOPage;
    }

    @Override
    public QueryWrapper<SysDictSchoolYear> getQueryWrapper(SysDictSchoolYearQueryRequest sysDictSchoolYearQueryRequest) {
        QueryWrapper<SysDictSchoolYear> queryWrapper = new QueryWrapper<>();
        if (sysDictSchoolYearQueryRequest == null) {
            return queryWrapper;
        }
        String yearName = sysDictSchoolYearQueryRequest.getYearName();
        String semesterName = sysDictSchoolYearQueryRequest.getSemesterName();
        queryWrapper.like(StringUtils.isNotBlank(yearName), "year_name", yearName);
        queryWrapper.like(StringUtils.isNotBlank(semesterName), "semester_name", semesterName);
//        queryWrapper.orderByDesc("create_time");
        return queryWrapper;
    }

    @Override
    public SysDictSchoolYearVO getSchoolYearVO(SysDictSchoolYear sysDictSchoolYear) {
        if (sysDictSchoolYear == null) {
            return null;
        }
        SysDictSchoolYearVO sysDictSchoolYearVO = new SysDictSchoolYearVO();
        BeanUtils.copyProperties(sysDictSchoolYear, sysDictSchoolYearVO);
        return sysDictSchoolYearVO;
    }
}