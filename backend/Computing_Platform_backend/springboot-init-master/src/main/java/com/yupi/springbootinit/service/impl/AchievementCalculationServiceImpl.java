package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.*;
import com.yupi.springbootinit.model.dto.gradeEntry.AchievementCalculationRequest;
import com.yupi.springbootinit.model.entity.*;
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
 */
@Service
@Slf4j
public class AchievementCalculationServiceImpl implements AchievementCalculationService {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Resource
    private TeachingClassMapper teachingClassMapper;

    @Resource
    private ClassStudentMapper classStudentMapper;

    @Resource
    private StudentScoreMapper studentScoreMapper;

    @Resource
    private CourseObjectiveMapper courseObjectiveMapper;

    @Resource
    private AssessmentPointMapper assessmentPointMapper;

    @Resource
    private RelPointObjectiveMapper relPointObjectiveMapper;

    @Resource
    private StudentObjectiveAchievementMapper studentObjectiveAchievementMapper;

    @Resource
    private WeightObjectiveIndicatorMapper weightObjectiveIndicatorMapper;

    @Resource
    private IndicatorPointMapper indicatorPointMapper;

    @Resource
    private CourseIndicatorAchievementMapper courseIndicatorAchievementMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> calculateAchievement(AchievementCalculationRequest request) {
        Map<String, Object> result = new HashMap<>();

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

            log.info("开始计算达成度：班级ID={}", classId);
            Date calcStartTime = new Date();

            // 1. 计算一级达成度（学生课程目标达成度）
            Map<String, Object> levelOneStats = calculateLevelOneAchievement(classId, teachingClass.getCourseId());

            // 2. 计算二级达成度（课程级指标点达成度）
            Map<String, Object> levelTwoStats = calculateLevelTwoAchievement(classId, teachingClass.getCourseId());

            result.put("success", true);
            result.put("classId", classId);
            result.put("calcStartTime", calcStartTime);
            result.put("calcEndTime", new Date());
            result.put("levelOneStats", levelOneStats);
            result.put("levelTwoStats", levelTwoStats);

            log.info("达成度计算完成：班级ID={}", classId);

        } catch (Exception e) {
            log.error("达成度计算失败：班级ID=" + request.getClassId(), e);
            result.put("success", false);
            result.put("errorMessage", "计算失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 计算一级达成度（学生课程目标达成度）
     */
    private Map<String, Object> calculateLevelOneAchievement(Long classId, Long courseId) {
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
        QueryWrapper<RelPointObjective> relQuery = new QueryWrapper<>();
        relQuery.in("objective_id", objectives.stream().map(CourseObjective::getId).collect(Collectors.toList()));
        List<RelPointObjective> relations = relPointObjectiveMapper.selectList(relQuery);

        if (relations.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该课程暂未配置考核点-课程目标关联关系");
        }

        // 查询考核点信息
        Set<Long> pointIds = relations.stream().map(RelPointObjective::getPointId).collect(Collectors.toSet());
        Map<Long, AssessmentPoint> assessmentPointMap = assessmentPointMapper.selectBatchIds(pointIds).stream()
                .collect(Collectors.toMap(AssessmentPoint::getId, p -> p));

        // 构建成绩映射
        Map<Long, Map<Long, StudentScore>> scoreMap = new HashMap<>();
        for (StudentScore score : studentScores) {
            scoreMap.computeIfAbsent(score.getStudentId(), k -> new HashMap<>()).put(score.getPointId(), score);
        }

        // 计算每个学生每个课程目标的达成度
        List<StudentObjectiveAchievement> achievements = new ArrayList<>();

        for (ClassStudent classStudent : classStudents) {
            Long studentId = classStudent.getStudentId();

            for (CourseObjective objective : objectives) {
                // 找到支撑该课程目标的所有考核点
                List<RelPointObjective> objectiveRelations = relations.stream()
                        .filter(r -> r.getObjectiveId().equals(objective.getId()))
                        .collect(Collectors.toList());

                if (!objectiveRelations.isEmpty()) {
                    // 计算达成度：Σ(考核点得分/考核点满分 × 支撑权重) / Σ(支撑权重)
                    BigDecimal numerator = BigDecimal.ZERO;
                    BigDecimal denominator = BigDecimal.ZERO;

                    for (RelPointObjective relation : objectiveRelations) {
                        AssessmentPoint point = assessmentPointMap.get(relation.getPointId());
                        if (point != null && scoreMap.containsKey(studentId)) {
                            StudentScore score = scoreMap.get(studentId).get(point.getId());
                            if (score != null && score.getActualScore() != null) {
                                // 考核点得分/考核点满分 × 支撑权重
                                BigDecimal scoreRatio = score.getActualScore().divide(point.getFullScore(), SCALE, ROUNDING_MODE);
                                BigDecimal contribution = scoreRatio.multiply(relation.getWeight());
                                numerator = numerator.add(contribution);
                            }
                        }
                        denominator = denominator.add(relation.getWeight());
                    }

                    if (denominator.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal achievement = numerator.divide(denominator, SCALE, ROUNDING_MODE);

                        StudentObjectiveAchievement studentAchievement = new StudentObjectiveAchievement();
                        studentAchievement.setClassId(classId);
                        studentAchievement.setStudentId(studentId);
                        studentAchievement.setObjectiveId(objective.getId());
                        studentAchievement.setObjectiveCode(objective.getObjCode());
                        studentAchievement.setObjectiveName(objective.getObjName());
                        studentAchievement.setAchievement(achievement);
                        studentAchievement.setCalculateTime(new Date());

                        achievements.add(studentAchievement);
                    }
                }
            }
        }

        // 删除旧数据并插入新数据
        QueryWrapper<StudentObjectiveAchievement> deleteQuery = new QueryWrapper<>();
        deleteQuery.eq("teaching_class_id", classId);
        studentObjectiveAchievementMapper.delete(deleteQuery);

        if (!achievements.isEmpty()) {
            for (StudentObjectiveAchievement achievement : achievements) {
                studentObjectiveAchievementMapper.insert(achievement);
            }
        }

        // 统计信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", classStudents.size());
        stats.put("totalObjectives", objectives.size());
        stats.put("totalRecords", achievements.size());

        if (!achievements.isEmpty()) {
            List<BigDecimal> achievementValues = achievements.stream()
                    .map(StudentObjectiveAchievement::getAchievement)
                    .collect(Collectors.toList());
            stats.put("averageAchievement", calculateAverage(achievementValues));
            stats.put("minAchievement", Collections.min(achievementValues));
            stats.put("maxAchievement", Collections.max(achievementValues));
        }

        log.info("一级达成度计算完成：学生数={}, 课程目标数={}, 达成度记录数={}",
                classStudents.size(), objectives.size(), achievements.size());

        return stats;
    }

    /**
     * 计算二级达成度（课程级指标点达成度）
     */
    private Map<String, Object> calculateLevelTwoAchievement(Long classId, Long courseId) {
        log.info("开始计算二级达成度：班级ID={}, 课程ID={}", classId, courseId);

        // 查询课程目标
        QueryWrapper<CourseObjective> objectiveQuery = new QueryWrapper<>();
        objectiveQuery.eq("course_id", courseId);
        List<CourseObjective> objectives = courseObjectiveMapper.selectList(objectiveQuery);

        // 查询课程目标-指标点权重关系
        QueryWrapper<WeightObjectiveIndicator> weightQuery = new QueryWrapper<>();
        weightQuery.in("objective_id", objectives.stream().map(CourseObjective::getId).collect(Collectors.toList()));
        List<WeightObjectiveIndicator> weights = weightObjectiveIndicatorMapper.selectList(weightQuery);

        if (weights.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该课程暂未配置课程目标-指标点权重关系");
        }

        // 查询指标点信息
        Set<Long> indicatorIds = weights.stream().map(WeightObjectiveIndicator::getIndicatorId).collect(Collectors.toSet());
        Map<Long, IndicatorPoint> indicatorMap = indicatorPointMapper.selectBatchIds(indicatorIds).stream()
                .collect(Collectors.toMap(IndicatorPoint::getId, i -> i));

        // 查询一级达成度数据
        QueryWrapper<StudentObjectiveAchievement> achievementQuery = new QueryWrapper<>();
        achievementQuery.eq("teaching_class_id", classId);
        List<StudentObjectiveAchievement> studentAchievements = studentObjectiveAchievementMapper.selectList(achievementQuery);

        if (studentAchievements.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请先计算一级达成度");
        }

        // 按课程目标分组统计平均达成度
        Map<Long, List<BigDecimal>> objectiveAchievementMap = new HashMap<>();
        for (StudentObjectiveAchievement achievement : studentAchievements) {
            objectiveAchievementMap.computeIfAbsent(achievement.getObjectiveId(), k -> new ArrayList<>())
                    .add(achievement.getAchievement());
        }

        Map<Long, BigDecimal> objectiveAverageMap = new HashMap<>();
        for (Map.Entry<Long, List<BigDecimal>> entry : objectiveAchievementMap.entrySet()) {
            objectiveAverageMap.put(entry.getKey(), calculateAverage(entry.getValue()));
        }

        // 计算每个指标点的达成度
        List<CourseIndicatorAchievement> indicatorAchievements = new ArrayList<>();

        for (IndicatorPoint indicator : indicatorMap.values()) {
            // 找到支撑该指标点的所有课程目标
            List<WeightObjectiveIndicator> indicatorWeights = weights.stream()
                    .filter(w -> w.getIndicatorId().equals(indicator.getId()))
                    .collect(Collectors.toList());

            if (!indicatorWeights.isEmpty()) {
                // 计算加权平均：Σ(平均一级达成度 × 内部权重) / Σ(内部权重)
                BigDecimal numerator = BigDecimal.ZERO;
                BigDecimal denominator = BigDecimal.ZERO;

                for (WeightObjectiveIndicator weight : indicatorWeights) {
                    BigDecimal avgAchievement = objectiveAverageMap.get(weight.getObjectiveId());
                    if (avgAchievement != null) {
                        BigDecimal contribution = avgAchievement.multiply(weight.getInnerWeight());
                        numerator = numerator.add(contribution);
                        denominator = denominator.add(weight.getInnerWeight());
                    }
                }

                if (denominator.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal achievement = numerator.divide(denominator, SCALE, ROUNDING_MODE);

                    CourseIndicatorAchievement courseAchievement = new CourseIndicatorAchievement();
                    courseAchievement.setClassId(classId);
                    courseAchievement.setCourseId(courseId);
                    courseAchievement.setIndicatorId(indicator.getId());
                    courseAchievement.setIndicatorCode(indicator.getIndicatorCode());
                    courseAchievement.setIndicatorName(indicator.getIndicatorName());
                    courseAchievement.setAchievement(achievement);
                    courseAchievement.setCalculateTime(new Date());

                    indicatorAchievements.add(courseAchievement);
                }
            }
        }

        // 删除旧数据并插入新数据
        QueryWrapper<CourseIndicatorAchievement> deleteQuery = new QueryWrapper<>();
        deleteQuery.eq("teaching_class_id", classId);
        courseIndicatorAchievementMapper.delete(deleteQuery);

        if (!indicatorAchievements.isEmpty()) {
            for (CourseIndicatorAchievement achievement : indicatorAchievements) {
                courseIndicatorAchievementMapper.insert(achievement);
            }
        }

        // 统计信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIndicators", indicatorMap.size());
        stats.put("totalRecords", indicatorAchievements.size());

        if (!indicatorAchievements.isEmpty()) {
            List<BigDecimal> achievementValues = indicatorAchievements.stream()
                    .map(CourseIndicatorAchievement::getAchievement)
                    .collect(Collectors.toList());
            stats.put("averageAchievement", calculateAverage(achievementValues));
            stats.put("minAchievement", Collections.min(achievementValues));
            stats.put("maxAchievement", Collections.max(achievementValues));
        }

        log.info("二级达成度计算完成：指标点数={}, 记录数={}", indicatorMap.size(), indicatorAchievements.size());

        return stats;
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

        return sum.divide(new BigDecimal(values.size()), SCALE, ROUNDING_MODE);
    }

    @Override
    public Map<String, Object> getCalculationStatus(Long classId) {
        Map<String, Object> result = new HashMap<>();

        // 参数校验
        if (classId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        // 查询教学班级信息
        TeachingClass teachingClass = teachingClassMapper.selectById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        result.put("classId", classId);
        result.put("className", teachingClass.getClassName());
        result.put("courseId", teachingClass.getCourseId());
        result.put("termId", teachingClass.getTermId());

        // 查询一级达成度统计
        QueryWrapper<StudentObjectiveAchievement> levelOneQuery = new QueryWrapper<>();
        levelOneQuery.eq("teaching_class_id", classId);
        long levelOneCount = studentObjectiveAchievementMapper.selectCount(levelOneQuery);
        result.put("levelOneRecordCount", levelOneCount);

        // 查询二级达成度统计
        QueryWrapper<CourseIndicatorAchievement> levelTwoQuery = new QueryWrapper<>();
        levelTwoQuery.eq("teaching_class_id", classId);
        long levelTwoCount = courseIndicatorAchievementMapper.selectCount(levelTwoQuery);
        result.put("levelTwoRecordCount", levelTwoCount);

        // 判断是否有计算结果
        result.put("hasCalculationResult", levelOneCount > 0 || levelTwoCount > 0);

        return result;
    }
}