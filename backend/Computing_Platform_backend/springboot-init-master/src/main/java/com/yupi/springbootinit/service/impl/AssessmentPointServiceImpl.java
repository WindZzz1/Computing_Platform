package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.OwnershipHelper;
import com.yupi.springbootinit.mapper.AssessmentPointMapper;
import com.yupi.springbootinit.mapper.CourseObjectiveMapper;
import com.yupi.springbootinit.mapper.RelPointObjectiveMapper;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointAddRequest;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointQueryRequest;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointUpdateRequest;
import com.yupi.springbootinit.model.entity.AssessmentPoint;
import com.yupi.springbootinit.model.entity.CourseObjective;
import com.yupi.springbootinit.model.entity.RelPointObjective;
import com.yupi.springbootinit.model.vo.AssessmentPointVO;
import com.yupi.springbootinit.model.vo.CourseObjectiveVO;
import com.yupi.springbootinit.service.AssessmentPointService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 课程考核点服务实现
@Service
@Slf4j
public class AssessmentPointServiceImpl extends ServiceImpl<AssessmentPointMapper, AssessmentPoint>
        implements AssessmentPointService {

    private static final BigDecimal DEFAULT_RELATION_WEIGHT = new BigDecimal("1.0000");

    @Resource
    private CourseObjectiveMapper courseObjectiveMapper;

    @Resource
    private RelPointObjectiveMapper relPointObjectiveMapper;

    @Resource
    private OwnershipHelper ownershipHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAssessmentPoint(AssessmentPointAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long courseId = request.getCourseId();
        validateCourseId(courseId);
        ownershipHelper.checkCourseOwnership(courseId);
        List<ObjectiveRelationParam> relations = resolveObjectiveRelations(request.getObjectiveId(),
                request.getObjectiveIds(), true);
        validateAssessmentPoint(request.getCourseId(), request.getPointCode(), request.getPointName(),
                request.getFullScore(), relations, null);
        baseMapper.deleteDeletedByCourseIdAndPointCode(request.getCourseId(), request.getPointCode());

        AssessmentPoint assessmentPoint = new AssessmentPoint();
        BeanUtils.copyProperties(request, assessmentPoint);
        boolean saveResult = this.save(assessmentPoint);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建考核点失败");
        }
        savePointObjectiveRelations(assessmentPoint.getId(), relations);
        return assessmentPoint.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAssessmentPoint(AssessmentPointUpdateRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AssessmentPoint exist = this.getById(request.getId());
        if (exist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "考核点不存在");
        }
        Long courseId = request.getCourseId() == null ? exist.getCourseId() : request.getCourseId();
        validateCourseId(courseId);
        ownershipHelper.checkCourseOwnership(exist.getCourseId());
        if (!courseId.equals(exist.getCourseId())) {
            ownershipHelper.checkCourseOwnership(courseId);
        }
        String pointCode = StringUtils.isBlank(request.getPointCode()) ? exist.getPointCode() : request.getPointCode();
        String pointName = StringUtils.isBlank(request.getPointName()) ? exist.getPointName() : request.getPointName();
        BigDecimal fullScore = request.getFullScore() == null ? exist.getFullScore() : request.getFullScore();
        boolean relationSpecified = request.getObjectiveId() != null || request.getObjectiveIds() != null;
        List<ObjectiveRelationParam> relations = relationSpecified
                ? resolveObjectiveRelations(request.getObjectiveId(), request.getObjectiveIds(), true)
                : listExistingRelationParams(request.getId());
        validateAssessmentPoint(courseId, pointCode, pointName, fullScore, relations, request.getId());

        AssessmentPoint assessmentPoint = new AssessmentPoint();
        BeanUtils.copyProperties(request, assessmentPoint);
        boolean updateResult = this.updateById(assessmentPoint);
        if (updateResult && relationSpecified) {
            savePointObjectiveRelations(request.getId(), relations);
        }
        return updateResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAssessmentPoint(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "考核点ID不合法");
        }
        AssessmentPoint exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "考核点不存在");
        }
        ownershipHelper.checkCourseOwnership(exist.getCourseId());
        validateAssessmentPointNotReferenced(id);
        boolean removeResult = this.removeById(id);
        if (removeResult) {
            relPointObjectiveMapper.deleteByPointIdPhysically(id);
        }
        return removeResult;
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
        ownershipHelper.checkCourseOwnership(assessmentPoint.getCourseId());
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
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public QueryWrapper<AssessmentPoint> getQueryWrapper(AssessmentPointQueryRequest request) {
        Long courseId = resolveRequiredCourseId(request);
        ownershipHelper.checkCourseOwnership(courseId);

        QueryWrapper<AssessmentPoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        if (request.getObjectiveId() != null) {
            List<Long> pointIds = listPointIdsByObjectiveId(request.getObjectiveId());
            if (pointIds.isEmpty()) {
                queryWrapper.eq("id", -1L);
            } else {
                queryWrapper.in("id", pointIds);
            }
        }
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

        List<RelPointObjective> relations = listRelationsByPointId(assessmentPoint.getId());
        if (relations.isEmpty()) {
            vo.setObjectiveIds(Collections.emptyList());
            vo.setObjectives(Collections.emptyList());
            return vo;
        }

        List<Long> objectiveIds = relations.stream().map(RelPointObjective::getObjectiveId).collect(Collectors.toList());
        Map<Long, CourseObjective> objectiveMap = courseObjectiveMapper.selectBatchIds(objectiveIds).stream()
                .collect(Collectors.toMap(CourseObjective::getId, item -> item));
        List<CourseObjectiveVO> objectives = new ArrayList<>();
        for (Long objectiveId : objectiveIds) {
            CourseObjective objective = objectiveMap.get(objectiveId);
            if (objective != null) {
                CourseObjectiveVO objectiveVO = new CourseObjectiveVO();
                BeanUtils.copyProperties(objective, objectiveVO);
                objectives.add(objectiveVO);
            }
        }

        vo.setObjectiveIds(objectiveIds);
        vo.setObjectives(objectives);
        if (!objectives.isEmpty()) {
            CourseObjectiveVO firstObjective = objectives.get(0);
            vo.setObjectiveId(firstObjective.getId());
            vo.setObjCode(firstObjective.getObjCode());
            vo.setObjName(firstObjective.getObjName());
        }
        return vo;
    }

    private Long resolveRequiredCourseId(AssessmentPointQueryRequest request) {
        if (request == null || request.getCourseId() == null || request.getCourseId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        return request.getCourseId();
    }

    private void validateCourseId(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
    }

    private void validateAssessmentPoint(Long courseId, String pointCode, String pointName, BigDecimal fullScore,
                                         List<ObjectiveRelationParam> relations, Long excludeId) {
        if (courseId == null || courseId <= 0 || StringUtils.isAnyBlank(pointCode, pointName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID、考核点编号和考核点名称不能为空");
        }
        if (fullScore == null || fullScore.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "满分必须大于0");
        }
        if (relations == null || relations.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程目标不能为空");
        }
        validateObjectivesBelongToCourse(courseId, relations);

        QueryWrapper<AssessmentPoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        queryWrapper.eq("point_code", pointCode);
        queryWrapper.ne(excludeId != null, "id", excludeId);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该课程下考核点编号已存在");
        }
    }

    private void validateObjectivesBelongToCourse(Long courseId, List<ObjectiveRelationParam> relations) {
        List<Long> objectiveIds = relations.stream().map(ObjectiveRelationParam::getObjectiveId)
                .collect(Collectors.toList());
        Map<Long, CourseObjective> objectiveMap = courseObjectiveMapper.selectBatchIds(objectiveIds).stream()
                .collect(Collectors.toMap(CourseObjective::getId, item -> item));
        if (objectiveMap.size() != objectiveIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程目标不存在");
        }
        for (Long objectiveId : objectiveIds) {
            CourseObjective objective = objectiveMap.get(objectiveId);
            if (!courseId.equals(objective.getCourseId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程目标不属于该课程");
            }
        }
    }

    private List<ObjectiveRelationParam> resolveObjectiveRelations(Long objectiveId, List<Long> objectiveIds,
                                                                   boolean required) {
        LinkedHashMap<Long, ObjectiveRelationParam> relationMap = new LinkedHashMap<>();
        if (objectiveIds != null) {
            for (Long currentObjectiveId : objectiveIds) {
                addObjectiveRelation(relationMap, currentObjectiveId);
            }
        } else if (objectiveId != null) {
            addObjectiveRelation(relationMap, objectiveId);
        }
        if (required && relationMap.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程目标不能为空");
        }
        return new ArrayList<>(relationMap.values());
    }

    private void addObjectiveRelation(LinkedHashMap<Long, ObjectiveRelationParam> relationMap, Long objectiveId) {
        if (objectiveId == null || objectiveId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程目标ID不合法");
        }
        if (relationMap.containsKey(objectiveId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程目标不能重复");
        }
        relationMap.put(objectiveId, new ObjectiveRelationParam(objectiveId, DEFAULT_RELATION_WEIGHT));
    }

    private List<ObjectiveRelationParam> listExistingRelationParams(Long pointId) {
        return listRelationsByPointId(pointId).stream()
                .map(relation -> new ObjectiveRelationParam(relation.getObjectiveId(), relation.getWeight()))
                .collect(Collectors.toList());
    }

    private List<RelPointObjective> listRelationsByPointId(Long pointId) {
        QueryWrapper<RelPointObjective> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("point_id", pointId);
        queryWrapper.orderByAsc("id");
        return relPointObjectiveMapper.selectList(queryWrapper);
    }

    private List<Long> listPointIdsByObjectiveId(Long objectiveId) {
        QueryWrapper<RelPointObjective> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("objective_id", objectiveId);
        return relPointObjectiveMapper.selectList(queryWrapper).stream()
                .map(RelPointObjective::getPointId).distinct().collect(Collectors.toList());
    }

    private void savePointObjectiveRelations(Long pointId, List<ObjectiveRelationParam> relations) {
        relPointObjectiveMapper.deleteByPointIdPhysically(pointId);
        for (ObjectiveRelationParam relationParam : relations) {
            RelPointObjective relation = new RelPointObjective();
            relation.setPointId(pointId);
            relation.setObjectiveId(relationParam.getObjectiveId());
            relation.setWeight(relationParam.getWeight());
            int insertResult = relPointObjectiveMapper.insert(relation);
            if (insertResult <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存考核点课程目标关联失败");
            }
        }
    }

    private void validateAssessmentPointNotReferenced(Long pointId) {
        Long scoreCount = baseMapper.countStudentScoreByPointId(pointId);
        if (scoreCount != null && scoreCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "考核点已被学生成绩引用，不能删除");
        }
    }

    @Data
    @AllArgsConstructor
    private static class ObjectiveRelationParam {

        private Long objectiveId;

        private BigDecimal weight;
    }
}
