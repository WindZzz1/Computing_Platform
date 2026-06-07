package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.SysDictCollegeMapper;
import com.yupi.springbootinit.model.dto.dict.SysDictCollegeAddRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictCollegeQueryRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictCollegeUpdateRequest;
import com.yupi.springbootinit.model.entity.SysDictCollege;
import com.yupi.springbootinit.model.vo.SysDictCollegeSimpleVO;
import com.yupi.springbootinit.model.vo.SysDictCollegeVO;
import com.yupi.springbootinit.service.SysDictCollegeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 学院字典服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class SysDictCollegeServiceImpl extends ServiceImpl<SysDictCollegeMapper, SysDictCollege> implements SysDictCollegeService {

    @Resource
    private SysDictCollegeMapper sysDictCollegeMapper;

    @Override
    public Long createCollege(SysDictCollegeAddRequest sysDictCollegeAddRequest) {
        if (sysDictCollegeAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String collegeName = sysDictCollegeAddRequest.getCollegeName();
        if (StringUtils.isAnyBlank(collegeName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "学院名称不能为空");
        }
        //防止并发问题
        synchronized (collegeName.intern()) {
            QueryWrapper<SysDictCollege> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("college_name", collegeName);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "学院名称已存在");
            }
            SysDictCollege sysDictCollege = new SysDictCollege();
            BeanUtils.copyProperties(sysDictCollegeAddRequest, sysDictCollege);
            boolean saveResult = this.save(sysDictCollege);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建学院失败");
            }
            return sysDictCollege.getId();
        }
    }

    @Override
    public Boolean updateCollege(SysDictCollegeUpdateRequest sysDictCollegeUpdateRequest) {
        if (sysDictCollegeUpdateRequest == null || sysDictCollegeUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        SysDictCollege existCollege = this.getById(sysDictCollegeUpdateRequest.getId());
        if (existCollege == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学院不存在");
        }
        String collegeName = sysDictCollegeUpdateRequest.getCollegeName();
        if (StringUtils.isNotBlank(collegeName) && !collegeName.equals(existCollege.getCollegeName())) {
            QueryWrapper<SysDictCollege> queryWrapper = new QueryWrapper<>();
            //检查新的学院名称是否已经存在
            queryWrapper.eq("college_name", collegeName);
            queryWrapper.ne("id", sysDictCollegeUpdateRequest.getId());
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "学院名称已存在");
            }
        }
        SysDictCollege sysDictCollege = new SysDictCollege();
        BeanUtils.copyProperties(sysDictCollegeUpdateRequest, sysDictCollege);
        return this.updateById(sysDictCollege);
    }

    @Override
    public Boolean deleteCollege(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "学院ID不能为空");
        }
        SysDictCollege college = this.getById(id);
        if (college == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学院不存在");
        }
        validateCollegeNotReferenced(id);
        boolean result = this.removeById(id);
        return result;
    }

    private void validateCollegeNotReferenced(Long collegeId) {
        Long majorCount = sysDictCollegeMapper.countMajorByCollegeId(collegeId);
        if (majorCount != null && majorCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "学院已被专业引用，不能删除");
        }
        Long userCount = sysDictCollegeMapper.countUserByCollegeId(collegeId);
        if (userCount != null && userCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "学院已被用户引用，不能删除");
        }
    }

    @Override
    public SysDictCollegeVO getCollegeById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "学院ID不能为空");
        }
        SysDictCollege sysDictCollege = this.getById(id);
        if (sysDictCollege == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学院不存在");
        }
        return this.getCollegeVO(sysDictCollege);
    }

    @Override
    public Page<SysDictCollegeVO> pageCollege(SysDictCollegeQueryRequest sysDictCollegeQueryRequest) {
        long current = sysDictCollegeQueryRequest.getCurrent();
        long size = sysDictCollegeQueryRequest.getPageSize();
        QueryWrapper<SysDictCollege> queryWrapper = this.getQueryWrapper(sysDictCollegeQueryRequest);
        Page<SysDictCollege> collegePage = this.page(new Page<>(current, size), queryWrapper);
        Page<SysDictCollegeVO> collegeVOPage = new Page<>(current, size, collegePage.getTotal());
        collegeVOPage.setRecords(collegePage.getRecords().stream().map(this::getCollegeVO).collect(java.util.stream.Collectors.toList()));
        return collegeVOPage;
    }

    @Override
    public QueryWrapper<SysDictCollege> getQueryWrapper(SysDictCollegeQueryRequest sysDictCollegeQueryRequest) {
        QueryWrapper<SysDictCollege> queryWrapper = new QueryWrapper<>();
        if (sysDictCollegeQueryRequest == null) {
            return queryWrapper;
        }
        String collegeName = sysDictCollegeQueryRequest.getCollegeName();
        queryWrapper.like(StringUtils.isNotBlank(collegeName), "college_name", collegeName);
//        queryWrapper.orderByDesc("create_time");
        return queryWrapper;
    }

    @Override
    public SysDictCollegeVO getCollegeVO(SysDictCollege sysDictCollege) {
        if (sysDictCollege == null) {
            return null;
        }
        SysDictCollegeVO sysDictCollegeVO = new SysDictCollegeVO();
        BeanUtils.copyProperties(sysDictCollege, sysDictCollegeVO);
        return sysDictCollegeVO;
    }

    @Override
    public java.util.List<SysDictCollegeSimpleVO> listCollegeSimple() {
        QueryWrapper<SysDictCollege> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "college_name");
        queryWrapper.orderByAsc("id");
        return this.list(queryWrapper).stream().map(college -> {
            SysDictCollegeSimpleVO simpleVO = new SysDictCollegeSimpleVO();
            simpleVO.setId(college.getId());
            simpleVO.setCollegeName(college.getCollegeName());
            return simpleVO;
        }).collect(java.util.stream.Collectors.toList());
    }
}
