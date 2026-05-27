package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.AssessmentPointMapper;
import com.yupi.springbootinit.mapper.CourseObjectiveMapper;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointAddRequest;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointQueryRequest;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointUpdateRequest;
import com.yupi.springbootinit.model.entity.AssessmentPoint;
import com.yupi.springbootinit.model.entity.CourseObjective;
import com.yupi.springbootinit.model.vo.AssessmentPointVO;
import com.yupi.springbootinit.service.AssessmentPointService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

//课程考核点服务实现

@Service
@Slf4j
public class AssessmentPointServiceImpl extends ServiceImpl<AssessmentPointMapper, AssessmentPoint>
        implements AssessmentPointService {

    @Resource
    private CourseObjectiveMapper courseObjectiveMapper;

    @Override
    public Long createAssessmentPoint(AssessmentPointAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validateAssessmentPoint(request.getCourseId(), request.getPointCode(), request.getPointName(),
                request.getFullScore(), request.getObjectiveId(), null);
        baseMapper.deleteDeletedByCourseIdAndPointCode(request.getCourseId(), request.getPointCode());
        AssessmentPoint assessmentPoint = new AssessmentPoint();
        BeanUtils.copyProperties(request, assessmentPoint);
        boolean saveResult = this.save(assessmentPoint);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建考核点失败");
        }
        return assessmentPoint.getId();
    }

    @Override
    public Boolean updateAssessmentPoint(AssessmentPointUpdateRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AssessmentPoint exist = this.getById(request.getId());
        if (exist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "考核点不存在");
        }
        Long courseId = request.getCourseId() == null ? exist.getCourseId() : request.getCourseId();
        String pointCode = StringUtils.isBlank(request.getPointCode()) ? exist.getPointCode() : request.getPointCode();
        String pointName = StringUtils.isBlank(request.getPointName()) ? exist.getPointName() : request.getPointName();
        BigDecimal fullScore = request.getFullScore() == null ? exist.getFullScore() : request.getFullScore();
        Long objectiveId = request.getObjectiveId() == null ? exist.getObjectiveId() : request.getObjectiveId();
        validateAssessmentPoint(courseId, pointCode, pointName, fullScore, objectiveId, request.getId());

        AssessmentPoint assessmentPoint = new AssessmentPoint();
        BeanUtils.copyProperties(request, assessmentPoint);
        return this.updateById(assessmentPoint);
    }

    @Override
    public Boolean deleteAssessmentPoint(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "考核点ID不合法");
        }
        return this.removeById(id);
    }

    @Override
    public AssessmentPointVO getAssessmentPointById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "考核点ID不合法");
        }
        AssessmentPoint assessmentPoint = this.getById(id);
        if (assessmentPoint == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "考核点不存在");
        }
        return getAssessmentPointVO(assessmentPoint);
    }

    @Override
    public Page<AssessmentPointVO> pageAssessmentPoint(AssessmentPointQueryRequest request) {
        long current = request == null ? 1 : request.getCurrent();
        long size = request == null ? 10 : request.getPageSize();
        QueryWrapper<AssessmentPoint> queryWrapper = this.getQueryWrapper(request);
        Page<AssessmentPoint> page = this.page(new Page<>(current, size), queryWrapper);
        Page<AssessmentPointVO> voPage = new Page<>(current, size, page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::getAssessmentPointVO)
                .collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    public QueryWrapper<AssessmentPoint> getQueryWrapper(AssessmentPointQueryRequest request) {
        QueryWrapper<AssessmentPoint> queryWrapper = new QueryWrapper<>();
        if (request == null) {
            queryWrapper.orderByAsc("course_id", "point_code");
            return queryWrapper;
        }
        queryWrapper.eq(request.getCourseId() != null, "course_id", request.getCourseId());
        queryWrapper.eq(request.getObjectiveId() != null, "objective_id", request.getObjectiveId());
        queryWrapper.like(StringUtils.isNotBlank(request.getPointCode()), "point_code", request.getPointCode());
        queryWrapper.like(StringUtils.isNotBlank(request.getPointName()), "point_name", request.getPointName());
        queryWrapper.orderByAsc("course_id", "point_code");
        return queryWrapper;
    }

    @Override
    public AssessmentPointVO getAssessmentPointVO(AssessmentPoint assessmentPoint) {
        if (assessmentPoint == null) {
            return null;
        }
        AssessmentPointVO vo = new AssessmentPointVO();
        BeanUtils.copyProperties(assessmentPoint, vo);
        CourseObjective objective = courseObjectiveMapper.selectById(assessmentPoint.getObjectiveId());
        if (objective != null) {
            vo.setObjCode(objective.getObjCode());
            vo.setObjName(objective.getObjName());
        }
        return vo;
    }


    /**
     * 校验考核点基础参数、满分、课程目标归属和考核点编号唯一性
     *
     * @param courseId    课程ID
     * @param pointCode   考核点编号
     * @param pointName   考核点名称
     * @param fullScore   满分
     * @param objectiveId 课程目标ID
     * @param excludeId   更新时需要排除的考核点ID
     */
    private void validateAssessmentPoint(Long courseId, String pointCode, String pointName, BigDecimal fullScore,
                                         Long objectiveId, Long excludeId) {
        if (courseId == null || courseId <= 0 || objectiveId == null || objectiveId <= 0
                || StringUtils.isAnyBlank(pointCode, pointName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID、课程目标ID、考核点编号和考核点名称不能为空");
        }
        if (fullScore == null || fullScore.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "满分必须大于0");
        }
        CourseObjective objective = courseObjectiveMapper.selectById(objectiveId);
        if (objective == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程目标不存在");
        }
        if (!courseId.equals(objective.getCourseId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程目标不属于该课程");
        }
        QueryWrapper<AssessmentPoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        queryWrapper.eq("point_code", pointCode);
        queryWrapper.ne(excludeId != null, "id", excludeId);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该课程下考核点编号已存在");
        }
    }
}
