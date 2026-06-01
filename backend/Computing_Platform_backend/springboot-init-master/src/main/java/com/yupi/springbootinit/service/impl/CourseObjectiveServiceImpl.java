package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.AssessmentPointMapper;
import com.yupi.springbootinit.mapper.CourseObjectiveMapper;
import com.yupi.springbootinit.mapper.WeightObjectiveIndicatorMapper;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveAddRequest;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveQueryRequest;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveUpdateRequest;
import com.yupi.springbootinit.model.entity.AssessmentPoint;
import com.yupi.springbootinit.model.entity.CourseObjective;
import com.yupi.springbootinit.model.entity.WeightObjectiveIndicator;
import com.yupi.springbootinit.model.vo.CourseObjectiveVO;
import com.yupi.springbootinit.service.CourseObjectiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

//课程目标服务实现

@Service
@Slf4j
public class CourseObjectiveServiceImpl extends ServiceImpl<CourseObjectiveMapper, CourseObjective>
        implements CourseObjectiveService {

    @Resource
    private AssessmentPointMapper assessmentPointMapper;

    @Resource
    private WeightObjectiveIndicatorMapper weightObjectiveIndicatorMapper;

    @Override
    public Long createCourseObjective(CourseObjectiveAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long courseId = request.getCourseId();
        String objCode = request.getObjCode();
        String objName = request.getObjName();
        if (courseId == null || courseId <= 0 || StringUtils.isAnyBlank(objCode, objName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID、目标编号和目标名称不能为空");
        }
        validateDuplicateObjCode(courseId, objCode, null);
        baseMapper.deleteDeletedByCourseIdAndObjCode(courseId, objCode);

        CourseObjective courseObjective = new CourseObjective();
        BeanUtils.copyProperties(request, courseObjective);
        boolean saveResult = this.save(courseObjective);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建课程目标失败");
        }
        return courseObjective.getId();
    }

    @Override
    public Boolean updateCourseObjective(CourseObjectiveUpdateRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        CourseObjective exist = this.getById(request.getId());
        if (exist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程目标不存在");
        }
        Long courseId = request.getCourseId() == null ? exist.getCourseId() : request.getCourseId();
        String objCode = StringUtils.isBlank(request.getObjCode()) ? exist.getObjCode() : request.getObjCode();
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID不合法");
        }
        validateDuplicateObjCode(courseId, objCode, request.getId());

        CourseObjective courseObjective = new CourseObjective();
        BeanUtils.copyProperties(request, courseObjective);
        return this.updateById(courseObjective);
    }

    @Override
    public Boolean deleteCourseObjective(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程目标ID不合法");
        }
        CourseObjective exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程目标不存在");
        }
        validateObjectiveNotReferenced(id);
        return this.removeById(id);
    }

    @Override
    public CourseObjectiveVO getCourseObjectiveById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程目标ID不合法");
        }
        CourseObjective courseObjective = this.getById(id);
        if (courseObjective == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程目标不存在");
        }
        return getCourseObjectiveVO(courseObjective);
    }

    @Override
    public Page<CourseObjectiveVO> pageCourseObjective(CourseObjectiveQueryRequest request) {
        long current = request == null ? 1 : request.getCurrent();
        long size = request == null ? 10 : request.getPageSize();
        QueryWrapper<CourseObjective> queryWrapper = this.getQueryWrapper(request);
        Page<CourseObjective> page = this.page(new Page<>(current, size), queryWrapper);
        Page<CourseObjectiveVO> voPage = new Page<>(current, size, page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::getCourseObjectiveVO)
                .collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    public QueryWrapper<CourseObjective> getQueryWrapper(CourseObjectiveQueryRequest request) {
        QueryWrapper<CourseObjective> queryWrapper = new QueryWrapper<>();
        if (request == null) {
            queryWrapper.orderByAsc("course_id", "obj_code");
            return queryWrapper;
        }
        queryWrapper.eq(request.getCourseId() != null, "course_id", request.getCourseId());
        queryWrapper.like(StringUtils.isNotBlank(request.getObjCode()), "obj_code", request.getObjCode());
        queryWrapper.like(StringUtils.isNotBlank(request.getObjName()), "obj_name", request.getObjName());
        queryWrapper.orderByAsc("course_id", "obj_code");
        return queryWrapper;
    }

    @Override
    public CourseObjectiveVO getCourseObjectiveVO(CourseObjective courseObjective) {
        if (courseObjective == null) {
            return null;
        }
        CourseObjectiveVO vo = new CourseObjectiveVO();
        BeanUtils.copyProperties(courseObjective, vo);
        return vo;
    }



    /**
     * 校验同一课程下目标编号是否重复
     *
     * @param courseId  课程ID
     * @param objCode   目标编号
     * @param excludeId 更新时需要排除的课程目标ID
     */
    private void validateDuplicateObjCode(Long courseId, String objCode, Long excludeId) {
        QueryWrapper<CourseObjective> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        queryWrapper.eq("obj_code", objCode);
        queryWrapper.ne(excludeId != null, "id", excludeId);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该课程下目标编号已存在");
        }
    }

    /**
     * 校验课程目标是否已被下游数据引用
     *
     * @param objectiveId 课程目标ID
     */
    private void validateObjectiveNotReferenced(Long objectiveId) {
        QueryWrapper<AssessmentPoint> assessmentQueryWrapper = new QueryWrapper<>();
        assessmentQueryWrapper.eq("objective_id", objectiveId);
        Long assessmentCount = assessmentPointMapper.selectCount(assessmentQueryWrapper);
        if (assessmentCount != null && assessmentCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "课程目标已被考核点引用，不能删除");
        }

        QueryWrapper<WeightObjectiveIndicator> weightQueryWrapper = new QueryWrapper<>();
        weightQueryWrapper.eq("objective_id", objectiveId);
        Long weightCount = weightObjectiveIndicatorMapper.selectCount(weightQueryWrapper);
        if (weightCount != null && weightCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "课程目标已被内部权重配置引用，不能删除");
        }
    }
}
