package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.*;
import com.yupi.springbootinit.model.dto.gradeEntry.AchievementCalculationRequest;
import com.yupi.springbootinit.model.entity.*;
import com.yupi.springbootinit.model.vo.gradeEntry.AchievementCalculationResultVO;
import com.yupi.springbootinit.service.AchievementCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 达成度计算服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class AchievementCalculationServiceImpl implements AchievementCalculationService {

    @Resource
    private TeachingClassMapper teachingClassMapper;

    @Resource
    private ClassStudentMapper classStudentMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private StudentScoreMapper studentScoreMapper;

    @Resource
    private AssessmentPointMapper assessmentPointMapper;

    @Resource
    private CourseObjectiveMapper courseObjectiveMapper;

    @Resource
    private RelPointObjectiveMapper relPointObjectiveMapper;

    @Resource
    private WeightObjectiveIndicatorMapper weightObjectiveIndicatorMapper;

    @Resource
    private IndicatorPointMapper indicatorPointMapper;

    @Resource
    private StudentObjectiveAchievementMapper studentObjectiveAchievementMapper;

    @Resource
    private CourseIndicatorAchievementMapper courseIndicatorAchievementMapper;

    @Resource
    private GradeCalculationStatusMapper gradeCalculationStatusMapper;

    private static final int SCALE = 4; // 计算精度：4位小数

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AchievementCalculationResultVO calculateAchievement(AchievementCalculationRequest request) {
        AchievementCalculationResultVO result = new AchievementCalculationResultVO();

        try {
            // 参数校验
            if (request == null || request.getClassId() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
            }

            Long classId = request.getClassId();

            // 查询教学班级信息
            TeachingClass teachingClass = teachingClassMapper.selectById(classId);
            if (teachingClass == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
            }

            // 检查计算状态
            GradeCalculationStatus existingStatus = getCalculationStatusEntity(classId);
            if (existingStatus != null && existingStatus.getCalcStatus() == 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "计算正在进行中，请勿重复触发");
            }

            // 检查是否已锁定
            if (existingStatus != null && existingStatus.getIsLocked() == 1 && !Boolean.TRUE.equals(request.getForceRecalculate())) {
                result.setSuccess(false);
                result.setCalcStatus(existingStatus.getCalcStatus());
                result.setIsLocked(true);
                result.setLockTime(existingStatus.getLockTime());
                result.setErrorMessage("成绩已锁定，无法重新计算。如需重新计算，请联系管理员或使用强制重新计算功能。");
                return result;
            }

            // 创建或更新计算状态
            GradeCalculationStatus status = createOrUpdateCalculationStatus(classId, getCurrentUserId());

            result.setCalcStatus(status.getCalcStatus());
            result.setCalcStartTime(status.getCalcStartTime());

            // 1. 计算一级达成度（学生课程目标达成度）
            AchievementCalculationResultVO.LevelOneAchievementStats levelOneStats = calculateLevelOneAchievement(classId, teachingClass.getCourseId());
            result.setLevelOneStats(levelOneStats);

            // 2. 计算二级达成度（课程级指标点达成度）
            AchievementCalculationResultVO.LevelTwoAchievementStats levelTwoStats = calculateLevelTwoAchievement(classId, teachingClass.getCourseId(), levelOneStats);
            result.setLevelTwoStats(levelTwoStats);

            // 3. 锁定成绩
            lockGrades(classId, getCurrentUserId());

            // 4. 更新计算状态为完成
            updateCalculationStatus(classId, 2, null); // 2-计算完成

            // 设置返回结果
            result.setSuccess(true);
            result.setCalcStatus(2);
            result.setIsLocked(true);
            result.setLockTime(new Date());
            result.setCalcEndTime(new Date());

            log.info("达成度计算完成：班级ID={}, 一级达成度记录数={}, 二级达成度记录数={}",
                    classId, levelOneStats.getTotalRecords(), levelTwoStats.getTotalRecords());

        } catch (Exception e) {
            log.error("达成度计算失败：班级ID=" + request.getClassId(), e);
            updateCalculationStatus(request.getClassId(), 3, e.getMessage()); // 3-计算失败

            result.setSuccess(false);
            result.setCalcStatus(3);
            result.setErrorMessage("计算失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 计算一级达成度（学生课程目标达成度）
     * 公式：学生课程目标达成度 = Σ(考核点得分/考核点满分 × 支撑权重) / Σ(支撑权重)
     */
    private AchievementCalculationResultVO.LevelOneAchievementStats calculateLevelOneAchievement(Long classId, Long courseId) {
        log.info("开始计算一级达成度：班级ID={}, 课程ID={}", classId, courseId);

        // 查询班级学生
        QueryWrapper<ClassStudent> classStudentQuery = new QueryWrapper<>();
        classStudentQuery.eq("teaching_class_id", classId);
        List<ClassStudent> classStudents = classStudentMapper.selectList(classStudentQuery);

        if (classStudents.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该班级暂无学生");
        }

        // 查询学生成绩
        QueryWrapper<StudentScore> scoreQuery = new QueryWrapper<>();
        scoreQuery.eq("teaching_class_id", classId);
        List<StudentScore> studentScores = studentScoreMapper.selectList(scoreQuery);

        if (studentScores.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该班级暂无成绩数据");
        }

        // 查询课程目标
        QueryWrapper<CourseObjective> objectiveQuery = new QueryWrapper<>();
        objectiveQuery.eq("course_id", courseId);
        List<CourseObjective> objectives = courseObjectiveMapper.selectList(objectiveQuery);

        if (objectives.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该课程暂无课程目标");
        }

        // 查询考核点-课程目标关联关系
        List<Long> objectiveIds = objectives.stream().map(CourseObjective::getId).collect(Collectors.toList());
        QueryWrapper<RelPointObjective> relQuery = new QueryWrapper<>();
        relQuery.in("objective_id", objectiveIds);
        List<RelPointObjective> relations = relPointObjectiveMapper.selectList(relQuery);

        // 查询考核点信息
        List<Long> pointIds = relations.stream().map(RelPointObjective::getPointId).distinct().collect(Collectors.toList());
        Map<Long, AssessmentPoint> pointMap = assessmentPointMapper.selectBatchIds(pointIds).stream()
                .collect(Collectors.toMap(AssessmentPoint::getId, p -> p));

        // 按课程目标分组关联关系
        Map<Long, List<RelPointObjective>> objectiveRelationsMap = relations.stream()
                .collect(Collectors.groupingBy(RelPointObjective::getObjectiveId));

        // 按学生分组成绩
        Map<Long, List<StudentScore>> studentScoresMap = studentScores.stream()
                .collect(Collectors.groupingBy(StudentScore::getStudentId));

        // 删除已有的一级达成度数据
        studentObjectiveAchievementMapper.deleteByClassIdPhysically(classId);

        // 计算每个学生在每个课程目标上的达成度
        List<BigDecimal> allAchievements = new ArrayList<>();

        for (ClassStudent classStudent : classStudents) {
            Long studentId = classStudent.getStudentId();
            List<StudentScore> scores = studentScoresMap.get(studentId);

            if (scores == null || scores.isEmpty()) {
                continue; // 该学生没有成绩
            }

            // 按考核点ID分组成绩
            Map<Long, StudentScore> scoreMap = scores.stream()
                    .collect(Collectors.toMap(StudentScore::getAssessmentPointId, s -> s));

            for (CourseObjective objective : objectives) {
                List<RelPointObjective> objRelations = objectiveRelationsMap.get(objective.getId());

                if (objRelations == null || objRelations.isEmpty()) {
                    continue; // 该课程目标没有关联考核点
                }

                BigDecimal numerator = BigDecimal.ZERO; // 分子
                BigDecimal denominator = BigDecimal.ZERO; // 分母

                for (RelPointObjective relation : objRelations) {
                    AssessmentPoint point = pointMap.get(relation.getPointId());
                    if (point == null) {
                        continue;
                    }

                    StudentScore score = scoreMap.get(point.getId());
                    if (score != null && score.getScore() != null) {
                        // 考核点得分/考核点满分 × 支撑权重
                        BigDecimal scoreRatio = score.getScore().divide(point.getFullScore(), SCALE, RoundingMode.HALF_UP);
                        BigDecimal contribution = scoreRatio.multiply(relation.getWeight());
                        numerator = numerator.add(contribution);
                    }

                    denominator = denominator.add(relation.getWeight());
                }

                // 计算达成度
                BigDecimal achievement = BigDecimal.ZERO;
                if (denominator.compareTo(BigDecimal.ZERO) > 0) {
                    achievement = numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
                    allAchievements.add(achievement);
                }

                // 保存一级达成度
                StudentObjectiveAchievement studentAchievement = new StudentObjectiveAchievement();
                studentAchievement.setClassId(classId);
                studentAchievement.setStudentId(studentId);
                studentAchievement.setObjectiveId(objective.getId());
                studentAchievement.setObjectiveCode(objective.getObjCode());
                studentAchievement.setObjectiveName(objective.getObjName());
                studentAchievement.setAchievement(achievement);
                studentAchievement.setCalculateTime(new Date());

                studentObjectiveAchievementMapper.insert(studentAchievement);
            }
        }

        // 计算统计信息
        AchievementCalculationResultVO.LevelOneAchievementStats stats = new AchievementCalculationResultVO.LevelOneAchievementStats();
        stats.setTotalStudents(classStudents.size());
        stats.setTotalObjectives(objectives.size());
        stats.setTotalRecords(studentObjectiveAchievementMapper.selectCount(
                new QueryWrapper<StudentObjectiveAchievement>().eq("teaching_class_id", classId)).intValue());

        if (!allAchievements.isEmpty()) {
            stats.setAverageAchievement(calculateAverage(allAchievements));
            stats.setMinAchievement(Collections.min(allAchievements));
            stats.setMaxAchievement(Collections.max(allAchievements));
        }

        log.info("一级达成度计算完成：学生数={}, 课程目标数={}, 记录数={}, 平均达成度={}",
                stats.getTotalStudents(), stats.getTotalObjectives(), stats.getTotalRecords(), stats.getAverageAchievement());

        return stats;
    }

    /**
     * 计算二级达成度（课程级指标点达成度）
     * 公式：课程指标点达成度 = Σ(平均一级达成度 × 内部贡献权重) / Σ(内部贡献权重)
     */
    private AchievementCalculationResultVO.LevelTwoAchievementStats calculateLevelTwoAchievement(
            Long classId, Long courseId, AchievementCalculationResultVO.LevelOneAchievementStats levelOneStats) {

        log.info("开始计算二级达成度：班级ID={}, 课程ID={}", classId, courseId);

        // 查询课程目标
        QueryWrapper<CourseObjective> objectiveQuery = new QueryWrapper<>();
        objectiveQuery.eq("course_id", courseId);
        List<CourseObjective> objectives = courseObjectiveMapper.selectList(objectiveQuery);

        // 查询课程目标-指标点权重关系
        List<Long> objectiveIds = objectives.stream().map(CourseObjective::getId).collect(Collectors.toList());
        QueryWrapper<WeightObjectiveIndicator> weightQuery = new QueryWrapper<>();
        weightQuery.in("objective_id", objectiveIds);
        List<WeightObjectiveIndicator> weights = weightObjectiveIndicatorMapper.selectList(weightQuery);

        if (weights.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该课程暂无指标点权重配置");
        }

        // 查询指标点信息
        List<Long> indicatorIds = weights.stream().map(WeightObjectiveIndicator::getIndicatorId).distinct().collect(Collectors.toList());
        Map<Long, IndicatorPoint> indicatorMap = indicatorPointMapper.selectBatchIds(indicatorIds).stream()
                .collect(Collectors.toMap(IndicatorPoint::getId, i -> i));

        // 按指标点分组权重
        Map<Long, List<WeightObjectiveIndicator>> indicatorWeightsMap = weights.stream()
                .collect(Collectors.groupingBy(WeightObjectiveIndicator::getIndicatorId));

        // 查询一级达成度数据
        QueryWrapper<StudentObjectiveAchievement> achievementQuery = new QueryWrapper<>();
        achievementQuery.eq("teaching_class_id", classId);
        List<StudentObjectiveAchievement> achievements = studentObjectiveAchievementMapper.selectList(achievementQuery);

        if (achievements.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "一级达成度数据为空");
        }

        // 按课程目标分组一级达成度
        Map<Long, List<StudentObjectiveAchievement>> objectiveAchievementsMap = achievements.stream()
                .collect(Collectors.groupingBy(StudentObjectiveAchievement::getObjectiveId));

        // 删除已有的二级达成度数据
        courseIndicatorAchievementMapper.deleteByClassIdPhysically(classId);

        // 计算每个指标点的达成度
        List<BigDecimal> allAchievements = new ArrayList<>();
        List<AchievementCalculationResultVO.IndicatorAchievementDetail> details = new ArrayList<>();

        for (Map.Entry<Long, List<WeightObjectiveIndicator>> entry : indicatorWeightsMap.entrySet()) {
            Long indicatorId = entry.getKey();
            List<WeightObjectiveIndicator> indicatorWeights = entry.getValue();
            IndicatorPoint indicator = indicatorMap.get(indicatorId);

            if (indicator == null) {
                continue;
            }

            BigDecimal numerator = BigDecimal.ZERO; // 分子
            BigDecimal denominator = BigDecimal.ZERO; // 分母

            for (WeightObjectiveIndicator weight : indicatorWeights) {
                List<StudentObjectiveAchievement> objAchievements = objectiveAchievementsMap.get(weight.getObjectiveId());

                if (objAchievements == null || objAchievements.isEmpty()) {
                    continue;
                }

                // 计算该课程目标的平均一级达成度
                BigDecimal sumAchievement = objAchievements.stream()
                        .map(StudentObjectiveAchievement::getAchievement)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal avgAchievement = sumAchievement.divide(
                        new BigDecimal(objAchievements.size()), SCALE, RoundingMode.HALF_UP);

                // 平均一级达成度 × 内部贡献权重
                numerator = numerator.add(avgAchievement.multiply(weight.getInnerWeight()));
                denominator = denominator.add(weight.getInnerWeight());
            }

            // 计算指标点达成度
            BigDecimal achievement = BigDecimal.ZERO;
            if (denominator.compareTo(BigDecimal.ZERO) > 0) {
                achievement = numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
                allAchievements.add(achievement);
            }

            // 保存二级达成度
            CourseIndicatorAchievement courseAchievement = new CourseIndicatorAchievement();
            courseAchievement.setClassId(classId);
            courseAchievement.setCourseId(courseId);
            courseAchievement.setIndicatorId(indicatorId);
            courseAchievement.setIndicatorCode(indicator.getIndicatorCode());
            courseAchievement.setIndicatorName(indicator.getIndicatorName());
            courseAchievement.setAchievement(achievement);
            courseAchievement.setCalculateTime(new Date());

            courseIndicatorAchievementMapper.insert(courseAchievement);

            // 添加到详情列表
            AchievementCalculationResultVO.IndicatorAchievementDetail detail =
                    new AchievementCalculationResultVO.IndicatorAchievementDetail();
            detail.setIndicatorId(indicatorId);
            detail.setIndicatorCode(indicator.getIndicatorCode());
            detail.setIndicatorName(indicator.getIndicatorName());
            detail.setAchievement(achievement);
            details.add(detail);
        }

        // 计算统计信息
        AchievementCalculationResultVO.LevelTwoAchievementStats stats = new AchievementCalculationResultVO.LevelTwoAchievementStats();
        stats.setTotalIndicators(indicatorIds.size());
        stats.setTotalRecords(courseIndicatorAchievementMapper.selectCount(
                new QueryWrapper<CourseIndicatorAchievement>().eq("teaching_class_id", classId)).intValue());
        stats.setAchievements(details);

        if (!allAchievements.isEmpty()) {
            stats.setAverageAchievement(calculateAverage(allAchievements));
            stats.setMinAchievement(Collections.min(allAchievements));
            stats.setMaxAchievement(Collections.max(allAchievements));
        }

        log.info("二级达成度计算完成：指标点数={}, 记录数={}, 平均达成度={}",
                stats.getTotalIndicators(), stats.getTotalRecords(), stats.getAverageAchievement());

        return stats;
    }

    /**
     * 锁定成绩
     */
    private void lockGrades(Long classId, Long userId) {
        // 锁定所有成绩记录
        QueryWrapper<StudentScore> scoreQuery = new QueryWrapper<>();
        scoreQuery.eq("teaching_class_id", classId);
        List<StudentScore> scores = studentScoreMapper.selectList(scoreQuery);

        for (StudentScore score : scores) {
            score.setIsLocked(1);
            studentScoreMapper.updateById(score);
        }

        log.info("成绩锁定完成：班级ID={}, 锁定记录数={}", classId, scores.size());
    }

    /**
     * 创建或更新计算状态
     */
    private GradeCalculationStatus createOrUpdateCalculationStatus(Long classId, Long userId) {
        GradeCalculationStatus status = getCalculationStatusEntity(classId);

        if (status == null) {
            status = new GradeCalculationStatus();
            status.setClassId(classId);
            status.setIsLocked(0);
            status.setCalcStatus(1); // 1-计算中
            status.setCalcStartTime(new Date());
            gradeCalculationStatusMapper.insert(status);
        } else {
            status.setCalcStatus(1); // 1-计算中
            status.setCalcStartTime(new Date());
            status.setErrorMessage(null);
            gradeCalculationStatusMapper.updateById(status);
        }

        return status;
    }

    /**
     * 更新计算状态
     */
    private void updateCalculationStatus(Long classId, Integer calcStatus, String errorMessage) {
        GradeCalculationStatus status = getCalculationStatusEntity(classId);
        if (status != null) {
            status.setCalcStatus(calcStatus);
            if (calcStatus == 2) { // 计算完成
                status.setCalcEndTime(new Date());
            }
            if (errorMessage != null) {
                status.setErrorMessage(errorMessage);
            }
            gradeCalculationStatusMapper.updateById(status);
        }
    }

    /**
     * 获取计算状态实体
     */
    private GradeCalculationStatus getCalculationStatusEntity(Long classId) {
        QueryWrapper<GradeCalculationStatus> query = new QueryWrapper<>();
        query.eq("teaching_class_id", classId);
        return gradeCalculationStatusMapper.selectOne(query);
    }

    /**
     * 计算平均值
     */
    private BigDecimal calculateAverage(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(new BigDecimal(values.size()), SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            org.springframework.web.context.request.RequestAttributes attributes =
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                Object userObj = attributes.getAttribute("currentUser", 0);
                if (userObj instanceof SysUser) {
                    return ((SysUser) userObj).getId();
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败", e);
        }
        return null;
    }

    @Override
    public AchievementCalculationResultVO getCalculationStatus(Long classId) {
        if (classId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        GradeCalculationStatus status = getCalculationStatusEntity(classId);
        AchievementCalculationResultVO result = new AchievementCalculationResultVO();

        if (status != null) {
            result.setCalcStatus(status.getCalcStatus());
            result.setIsLocked(status.getIsLocked() == 1);
            result.setCalcStartTime(status.getCalcStartTime());
            result.setCalcEndTime(status.getCalcEndTime());
            result.setLockTime(status.getLockTime());
            result.setErrorMessage(status.getErrorMessage());
        } else {
            result.setCalcStatus(0); // 0-未计算
            result.setIsLocked(false);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unlockGrades(Long classId, String reason) {
        if (classId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        // 解锁所有成绩记录
        QueryWrapper<StudentScore> scoreQuery = new QueryWrapper<>();
        scoreQuery.eq("teaching_class_id", classId);
        List<StudentScore> scores = studentScoreMapper.selectList(scoreQuery);

        for (StudentScore score : scores) {
            score.setIsLocked(0);
            studentScoreMapper.updateById(score);
        }

        // 更新计算状态
        GradeCalculationStatus status = getCalculationStatusEntity(classId);
        if (status != null) {
            status.setIsLocked(0);
            status.setLockReason(reason);
            gradeCalculationStatusMapper.updateById(status);
        }

        log.info("成绩解锁完成：班级ID={}, 解锁记录数={}, 原因={}", classId, scores.size(), reason);

        return true;
    }
}
