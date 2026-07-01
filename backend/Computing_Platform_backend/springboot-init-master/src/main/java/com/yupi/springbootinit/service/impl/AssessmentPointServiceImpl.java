package com.yupi.springbootinit.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.OwnershipHelper;
import com.yupi.springbootinit.mapper.AssessmentPointMapper;
import com.yupi.springbootinit.mapper.CourseMapper;
import com.yupi.springbootinit.mapper.CourseObjectiveMapper;
import com.yupi.springbootinit.mapper.RelPointObjectiveMapper;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointAddRequest;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointQueryRequest;
import com.yupi.springbootinit.model.dto.assessment.AssessmentPointUpdateRequest;
import com.yupi.springbootinit.model.entity.AssessmentPoint;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.entity.CourseObjective;
import com.yupi.springbootinit.model.entity.RelPointObjective;
import com.yupi.springbootinit.model.excel.AssessmentPointExcel;
import com.yupi.springbootinit.model.vo.AssessmentPointVO;
import com.yupi.springbootinit.model.vo.CourseObjectiveVO;
import com.yupi.springbootinit.service.AssessmentPointService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    @Resource
    private CourseMapper courseMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importAssessmentPointsFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式不正确，请上传Excel文件");
        }

        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failDetails = new ArrayList<>();

        try {
            List<AssessmentPointExcel> pointExcels = EasyExcel.read(file.getInputStream())
                    .head(AssessmentPointExcel.class)
                    .sheet(0)
                    .doReadSync();

            if (pointExcels == null || pointExcels.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel中没有数据");
            }

            for (int i = 0; i < pointExcels.size(); i++) {
                AssessmentPointExcel excel = pointExcels.get(i);
                try {
                    // 1. 验证必填字段
                    if (StringUtils.isAnyBlank(excel.getCourseCode(), excel.getPointCode(),
                            excel.getPointName(), excel.getObjectiveCodes(), excel.getWeights())
                            || excel.getFullScore() == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode() != null ? excel.getCourseCode() : "");
                        detail.put("reason", "必填字段为空");
                        failDetails.add(detail);
                        continue;
                    }

                    // 2. 查询课程
                    QueryWrapper<Course> courseWrapper = new QueryWrapper<>();
                    courseWrapper.eq("course_code", excel.getCourseCode());
                    Course course = courseMapper.selectOne(courseWrapper);
                    if (course == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "课程代码不存在");
                        failDetails.add(detail);
                        continue;
                    }

                    // 3. 检查课程归属
                    ownershipHelper.checkCourseOwnership(course.getId());

                    // 4. 验证满分值
                    if (excel.getFullScore().compareTo(BigDecimal.ZERO) <= 0) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "满分值必须大于0");
                        failDetails.add(detail);
                        continue;
                    }

                    // 5. 解析关联目标编号和权重
                    String[] objCodeArr = excel.getObjectiveCodes().split(",");
                    String[] weightArr = excel.getWeights().split(",");
                    if (objCodeArr.length != weightArr.length) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "关联目标编号数量(" + objCodeArr.length + ")与支撑权重数量(" + weightArr.length + ")不一致");
                        failDetails.add(detail);
                        continue;
                    }

                    // 6. 解析权重并查找目标
                    List<ObjectiveRelationParam> relations = new ArrayList<>();
                    boolean resolveFail = false;
                    for (int j = 0; j < objCodeArr.length; j++) {
                        String objCode = objCodeArr[j].trim();
                        String weightStr = weightArr[j].trim();
                        BigDecimal weight;
                        try {
                            weight = new BigDecimal(weightStr);
                        } catch (NumberFormatException e) {
                            failCount++;
                            Map<String, String> detail = new HashMap<>();
                            detail.put("row", String.valueOf(i + 2));
                            detail.put("courseCode", excel.getCourseCode());
                            detail.put("reason", "支撑权重格式不正确: " + weightStr);
                            failDetails.add(detail);
                            resolveFail = true;
                            break;
                        }
                        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
                            failCount++;
                            Map<String, String> detail = new HashMap<>();
                            detail.put("row", String.valueOf(i + 2));
                            detail.put("courseCode", excel.getCourseCode());
                            detail.put("reason", "支撑权重必须大于0: " + weightStr);
                            failDetails.add(detail);
                            resolveFail = true;
                            break;
                        }

                        QueryWrapper<CourseObjective> objWrapper = new QueryWrapper<>();
                        objWrapper.eq("course_id", course.getId());
                        objWrapper.eq("obj_code", objCode);
                        CourseObjective objective = courseObjectiveMapper.selectOne(objWrapper);
                        if (objective == null) {
                            failCount++;
                            Map<String, String> detail = new HashMap<>();
                            detail.put("row", String.valueOf(i + 2));
                            detail.put("courseCode", excel.getCourseCode());
                            detail.put("reason", "目标编号 " + objCode + " 在该课程中不存在");
                            failDetails.add(detail);
                            resolveFail = true;
                            break;
                        }
                        relations.add(new ObjectiveRelationParam(objective.getId(), weight));
                    }
                    if (resolveFail) {
                        continue;
                    }

                    // 7. 检查重复
                    QueryWrapper<AssessmentPoint> dupWrapper = new QueryWrapper<>();
                    dupWrapper.eq("course_id", course.getId());
                    dupWrapper.eq("point_code", excel.getPointCode());
                    if (this.count(dupWrapper) > 0) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "该课程下考核点编号 " + excel.getPointCode() + " 已存在");
                        failDetails.add(detail);
                        continue;
                    }
                    baseMapper.deleteDeletedByCourseIdAndPointCode(course.getId(), excel.getPointCode());

                    // 8. 保存考核点
                    AssessmentPoint assessmentPoint = new AssessmentPoint();
                    assessmentPoint.setCourseId(course.getId());
                    assessmentPoint.setPointCode(excel.getPointCode());
                    assessmentPoint.setPointName(excel.getPointName());
                    assessmentPoint.setFullScore(excel.getFullScore());
                    if (!this.save(assessmentPoint)) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "保存考核点失败");
                        failDetails.add(detail);
                        continue;
                    }

                    // 9. 保存关联关系（含权重）
                    savePointObjectiveRelations(assessmentPoint.getId(), relations);
                    successCount++;
                } catch (BusinessException e) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 2));
                    detail.put("courseCode", excel.getCourseCode() != null ? excel.getCourseCode() : "");
                    detail.put("reason", e.getMessage());
                    failDetails.add(detail);
                }
            }

            if (failCount > 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,
                        "考核点导入存在 " + failCount + " 条失败，已整体回滚，请修正后重新导入");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", pointExcels.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("failDetails", failDetails);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件读取失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateAssessmentPointTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/assessment_point_template.xlsx");
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, bytesRead);
                    }
                    return buffer.toByteArray();
                }
            }
        } catch (Exception e) {
            log.warn("使用ClassPathResource读取考核点模板失败: {}", e.getMessage());
        }

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/assessment_point_template.xlsx")) {
            if (inputStream != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                return buffer.toByteArray();
            }
        } catch (Exception e) {
            log.warn("使用ClassLoader读取考核点模板失败: {}", e.getMessage());
        }

        log.info("静态模板不存在，使用动态生成");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            EasyExcel.write(outputStream, AssessmentPointExcel.class)
                    .sheet("考核点导入模板")
                    .doWrite(new ArrayList<>());
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("动态生成考核点模板失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成模板失败");
        }
    }
}
