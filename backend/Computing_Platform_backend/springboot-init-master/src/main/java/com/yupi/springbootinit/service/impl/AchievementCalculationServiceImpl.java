package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.*;
import com.yupi.springbootinit.model.entity.*;
import com.yupi.springbootinit.model.vo.AchievementCalculationResultVO;
import com.yupi.springbootinit.service.AchievementCalculationService;
import com.yupi.springbootinit.service.TeachingClassService;
import com.yupi.springbootinit.mapper.RelPointObjectiveMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
    private TeachingClassService teachingClassService;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private StudentScoreMapper studentScoreMapper;

    @Resource
    private AssessmentPointMapper assessmentPointMapper;

    @Resource
    private CourseObjectiveMapper courseObjectiveMapper;

    @Resource
    private WeightObjectiveIndicatorMapper weightObjectiveIndicatorMapper;

    @Resource
    private IndicatorPointMapper indicatorPointMapper;

    @Resource
    private StudentObjectiveAchievementMapper studentObjectiveAchievementMapper;

    @Resource
    private CourseIndicatorAchievementMapper courseIndicatorAchievementMapper;

    @Resource
    private ClassStudentMapper classStudentMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private RelPointObjectiveMapper relPointObjectiveMapper;

    /**
     * 一键计算课程达成度
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AchievementCalculationResultVO calculateAchievements(Long classId) {
        log.info("开始一键计算达成度，班级ID：{}", classId);

        // 1. 校验教学班级存在性
        if (classId == null || classId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        TeachingClass teachingClass = teachingClassService.getById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        // 2. 检查是否已计算
        if (teachingClass.getCalculatedStatus() != null && teachingClass.getCalculatedStatus() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该班级已计算过达成度，如需重新计算请先解锁");
        }

        // 3. 获取课程信息
        Course course = courseMapper.selectById(teachingClass.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }

        // 4. 获取班级学生列表
        QueryWrapper<ClassStudent> classStudentQueryWrapper = new QueryWrapper<>();
        classStudentQueryWrapper.eq("teaching_class_id", classId);
        List<ClassStudent> classStudents = classStudentMapper.selectList(classStudentQueryWrapper);

        if (classStudents == null || classStudents.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该班级暂无学生");
        }

        // 5. 获取课程的考核点
        QueryWrapper<AssessmentPoint> pointQueryWrapper = new QueryWrapper<>();
        pointQueryWrapper.eq("course_id", teachingClass.getCourseId());
        pointQueryWrapper.orderByAsc("point_code");
        List<AssessmentPoint> assessmentPoints = assessmentPointMapper.selectList(pointQueryWrapper);

        if (assessmentPoints == null || assessmentPoints.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该课程暂无考核点");
        }

        // 6. 获取课程目标列表
        QueryWrapper<CourseObjective> objectiveQueryWrapper = new QueryWrapper<>();
        objectiveQueryWrapper.eq("course_id", teachingClass.getCourseId());
        objectiveQueryWrapper.orderByAsc("obj_code");
        List<CourseObjective> courseObjectives = courseObjectiveMapper.selectList(objectiveQueryWrapper);

        if (courseObjectives == null || courseObjectives.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该课程暂无课程目标");
        }

        // 7. 检查是否所有学生都有成绩
        QueryWrapper<StudentScore> scoreCountWrapper = new QueryWrapper<>();
        scoreCountWrapper.eq("teaching_class_id", classId);
        Long scoreCount = studentScoreMapper.selectCount(scoreCountWrapper);

        long expectedScoreCount = (long) classStudents.size() * assessmentPoints.size();
        if (scoreCount < expectedScoreCount) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    String.format("成绩数据不完整，预期%d条，实际%d条，请先完善所有学生成绩", expectedScoreCount, scoreCount));
        }

        AchievementCalculationResultVO result = new AchievementCalculationResultVO();
        result.setClassId(classId);
        result.setClassName(teachingClass.getClassName());
        result.setCourseName(course.getCourseName());
        result.setStudentCount(classStudents.size());

        try {
            // 8. 计算一级达成度（学生课程目标达成度）
            List<AchievementCalculationResultVO.StudentObjectiveAchievementVO> firstLevelAchievements =
                    calculateFirstLevelAchievements(classId, teachingClass, classStudents, assessmentPoints, courseObjectives);
            result.setFirstLevelAchievements(firstLevelAchievements);

            // 9. 计算二级达成度（课程指标点达成度）
            List<AchievementCalculationResultVO.CourseIndicatorAchievementVO> secondLevelAchievements =
                    calculateSecondLevelAchievements(classId, teachingClass, courseObjectives, firstLevelAchievements);
            result.setSecondLevelAchievements(secondLevelAchievements);

            // 10. 更新教学班级状态
            teachingClass.setCalculatedStatus(1);
            teachingClass.setLockedStatus(1);
            teachingClass.setCalculateTime(new Date());
            teachingClass.setLockTime(new Date());
            teachingClassService.updateById(teachingClass);

            result.setCalculateTime(teachingClass.getCalculateTime());
            result.setCalculatedStatus(1);
            result.setLockedStatus(1);
            result.setSuccess(true);

            log.info("达成度计算成功，班级ID：{}，学生数：{}", classId, classStudents.size());

        } catch (Exception e) {
            log.error("达成度计算失败，班级ID：{}", classId, e);
            result.setSuccess(false);
            result.setErrorMessage("计算失败：" + e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "达成度计算失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 计算一级达成度（学生课程目标达成度）
     */
    private List<AchievementCalculationResultVO.StudentObjectiveAchievementVO> calculateFirstLevelAchievements(
            Long classId, TeachingClass teachingClass, List<ClassStudent> classStudents,
            List<AssessmentPoint> assessmentPoints, List<CourseObjective> courseObjectives) {

        List<AchievementCalculationResultVO.StudentObjectiveAchievementVO> resultList = new ArrayList<>();

        // 先删除旧的达成度数据
        studentObjectiveAchievementMapper.deleteByClassId(classId);

        // 为每个学生的每个课程目标计算达成度
        for (ClassStudent classStudent : classStudents) {
            Student student = studentMapper.selectById(classStudent.getStudentId());
            if (student == null) {
                continue;
            }

            for (CourseObjective objective : courseObjectives) {
                // 获取该课程目标关联的考核点ID列表（从rel_point_objective表）
                List<Long> relatedPointIds = relPointObjectiveMapper.selectPointIdsByObjectiveId(objective.getId());

                if (relatedPointIds.isEmpty()) {
                    log.warn("课程目标{}没有关联的考核点，跳过", objective.getObjCode());
                    continue;
                }

                // 获取考核点对象列表
                List<AssessmentPoint> relatedPoints = assessmentPoints.stream()
                        .filter(point -> relatedPointIds.contains(point.getId()))
                        .collect(Collectors.toList());

                // 计算达成度：Σ(得分/满分*权重) / Σ(权重)
                BigDecimal numerator = BigDecimal.ZERO;
                BigDecimal denominator = BigDecimal.ZERO;

                for (AssessmentPoint point : relatedPoints) {
                    // 查询关联表中的权重
                    QueryWrapper<RelPointObjective> relWrapper = new QueryWrapper<>();
                    relWrapper.eq("point_id", point.getId());
                    relWrapper.eq("objective_id", objective.getId());
                    RelPointObjective rel = relPointObjectiveMapper.selectOne(relWrapper);

                    BigDecimal weight = (rel != null && rel.getWeight() != null) ? rel.getWeight() : BigDecimal.ONE;

                    // 查询学生该考核点的成绩
                    QueryWrapper<StudentScore> scoreWrapper = new QueryWrapper<>();
                    scoreWrapper.eq("teaching_class_id", classId);
                    scoreWrapper.eq("student_id", student.getId());
                    scoreWrapper.eq("point_id", point.getId());
                    StudentScore score = studentScoreMapper.selectOne(scoreWrapper);

                    if (score != null && score.getActualScore() != null) {
                        BigDecimal scoreRatio = score.getActualScore()
                                .divide(score.getFullScore(), 4, RoundingMode.HALF_UP);
                        numerator = numerator.add(scoreRatio.multiply(weight));
                        denominator = denominator.add(weight);
                    }
                }

                BigDecimal achievementValue = BigDecimal.ZERO;
                if (denominator.compareTo(BigDecimal.ZERO) > 0) {
                    achievementValue = numerator.divide(denominator, 4, RoundingMode.HALF_UP);
                }

                // 保存到数据库
                StudentObjectiveAchievement achievement = new StudentObjectiveAchievement();
                achievement.setTeachingClassId(classId);
                achievement.setStudentId(student.getId());
                achievement.setObjectiveId(objective.getId());
                achievement.setAchievement(achievementValue);

                studentObjectiveAchievementMapper.insert(achievement);

                // 添加到结果列表
                AchievementCalculationResultVO.StudentObjectiveAchievementVO vo =
                        new AchievementCalculationResultVO.StudentObjectiveAchievementVO();
                vo.setStudentId(student.getId());
                vo.setStudentNo(student.getStudentNo());
                vo.setStudentName(student.getName());
                vo.setObjectiveId(objective.getId());
                vo.setObjectiveCode(objective.getObjCode());
                vo.setObjectiveName(objective.getObjName());
                vo.setAchievementValue(achievementValue);

                resultList.add(vo);
            }
        }

        log.info("一级达成度计算完成，班级ID：{}，记录数：{}", classId, resultList.size());
        return resultList;
    }

    /**
     * 计算二级达成度（课程指标点达成度）
     */
    private List<AchievementCalculationResultVO.CourseIndicatorAchievementVO> calculateSecondLevelAchievements(
            Long classId, TeachingClass teachingClass, List<CourseObjective> courseObjectives,
            List<AchievementCalculationResultVO.StudentObjectiveAchievementVO> firstLevelAchievements) {

        List<AchievementCalculationResultVO.CourseIndicatorAchievementVO> resultList = new ArrayList<>();

        // 先删除旧的达成度数据
        courseIndicatorAchievementMapper.deleteByClassId(classId);

        // 获取班级学生数
        QueryWrapper<ClassStudent> classStudentQueryWrapper = new QueryWrapper<>();
        classStudentQueryWrapper.eq("teaching_class_id", classId);
        Integer studentCount = classStudentMapper.selectCount(classStudentQueryWrapper).intValue();

        // 获取课程目标与指标点的权重关系
        QueryWrapper<WeightObjectiveIndicator> weightWrapper = new QueryWrapper<>();
        weightWrapper.eq("course_id", teachingClass.getCourseId());
        List<WeightObjectiveIndicator> weightRelations = weightObjectiveIndicatorMapper.selectList(weightWrapper);

        if (weightRelations == null || weightRelations.isEmpty()) {
            log.warn("课程{}没有配置课程目标与指标点的权重关系", teachingClass.getCourseId());
            return resultList;
        }

        // 按指标点分组
        Map<Long, List<WeightObjectiveIndicator>> indicatorGroupMap = weightRelations.stream()
                .collect(Collectors.groupingBy(WeightObjectiveIndicator::getIndicatorId));

        // 为每个指标点计算达成度
        for (Map.Entry<Long, List<WeightObjectiveIndicator>> entry : indicatorGroupMap.entrySet()) {
            Long indicatorId = entry.getKey();
            List<WeightObjectiveIndicator> relations = entry.getValue();

            // 获取指标点信息
            IndicatorPoint indicator = indicatorPointMapper.selectById(indicatorId);
            if (indicator == null) {
                continue;
            }

            // 计算该指标点的达成度
            BigDecimal totalAchievement = BigDecimal.ZERO;
            BigDecimal totalWeight = BigDecimal.ZERO;

            for (WeightObjectiveIndicator relation : relations) {
                Long objectiveId = relation.getObjectiveId();
                BigDecimal innerWeight = relation.getInnerWeight();

                if (innerWeight == null) {
                    innerWeight = BigDecimal.ZERO;
                }

                // 汇总该课程目标所有学生的达成度
                List<AchievementCalculationResultVO.StudentObjectiveAchievementVO> objectiveAchievements =
                        firstLevelAchievements.stream()
                                .filter(a -> a.getObjectiveId().equals(objectiveId))
                                .collect(Collectors.toList());

                for (AchievementCalculationResultVO.StudentObjectiveAchievementVO achievement : objectiveAchievements) {
                    totalAchievement = totalAchievement.add(achievement.getAchievementValue().multiply(innerWeight));
                    totalWeight = totalWeight.add(innerWeight);
                }
            }

            // 计算课程级指标点达成度 = 总达成度 / 班级人数
            BigDecimal achievementValue = BigDecimal.ZERO;
            if (studentCount > 0 && totalWeight.compareTo(BigDecimal.ZERO) > 0) {
                achievementValue = totalAchievement.divide(totalWeight, 4, RoundingMode.HALF_UP);
            }

            // 保存到数据库
            CourseIndicatorAchievement achievement = new CourseIndicatorAchievement();
            achievement.setTeachingClassId(classId);
            achievement.setCourseId(teachingClass.getCourseId());
            achievement.setIndicatorId(indicatorId);
            achievement.setCourseAchievement(achievementValue);
            achievement.setCalculateTime(new Date());

            courseIndicatorAchievementMapper.insert(achievement);

            // 添加到结果列表
            AchievementCalculationResultVO.CourseIndicatorAchievementVO vo =
                    new AchievementCalculationResultVO.CourseIndicatorAchievementVO();
            vo.setIndicatorId(indicatorId);
            vo.setIndicatorCode(indicator.getIndicatorCode());
            vo.setIndicatorName(indicator.getIndicatorName());
            vo.setAchievementValue(achievementValue);
            vo.setStudentCount(studentCount);

            resultList.add(vo);
        }

        log.info("二级达成度计算完成，班级ID：{}，记录数：{}", classId, resultList.size());
        return resultList;
    }

    /**
     * 锁定成绩
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean lockScores(Long classId) {
        if (classId == null || classId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        TeachingClass teachingClass = teachingClassService.getById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        teachingClass.setLockedStatus(1);
        teachingClass.setLockTime(new Date());

        boolean success = teachingClassService.updateById(teachingClass);

        if (success) {
            log.info("成绩锁定成功，班级ID：{}", classId);
        }

        return success;
    }

    /**
     * 解锁成绩
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unlockScores(Long classId) {
        if (classId == null || classId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        TeachingClass teachingClass = teachingClassService.getById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        teachingClass.setLockedStatus(0);
        teachingClass.setCalculatedStatus(0);

        boolean success = teachingClassService.updateById(teachingClass);

        if (success) {
            log.info("成绩解锁成功，班级ID：{}", classId);
        }

        return success;
    }

    /**
     * 获取计算结果
     */
    @Override
    public AchievementCalculationResultVO getCalculationResult(Long classId) {
        if (classId == null || classId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        TeachingClass teachingClass = teachingClassService.getById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        Course course = courseMapper.selectById(teachingClass.getCourseId());

        AchievementCalculationResultVO result = new AchievementCalculationResultVO();
        result.setClassId(classId);
        result.setClassName(teachingClass.getClassName());
        result.setCourseName(course != null ? course.getCourseName() : "");
        result.setCalculatedStatus(teachingClass.getCalculatedStatus());
        result.setLockedStatus(teachingClass.getLockedStatus());
        result.setCalculateTime(teachingClass.getCalculateTime());

        // 获取一级达成度
        QueryWrapper<StudentObjectiveAchievement> firstLevelWrapper = new QueryWrapper<>();
        firstLevelWrapper.eq("teaching_class_id", classId);
        List<StudentObjectiveAchievement> firstLevelList = studentObjectiveAchievementMapper.selectList(firstLevelWrapper);

        List<AchievementCalculationResultVO.StudentObjectiveAchievementVO> firstLevelVOs = firstLevelList.stream()
                .map(achievement -> {
                    AchievementCalculationResultVO.StudentObjectiveAchievementVO vo =
                            new AchievementCalculationResultVO.StudentObjectiveAchievementVO();
                    BeanUtils.copyProperties(achievement, vo);
                    Student student = studentMapper.selectById(achievement.getStudentId());
                    if (student != null) {
                        vo.setStudentNo(student.getStudentNo());
                        vo.setStudentName(student.getName());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        result.setFirstLevelAchievements(firstLevelVOs);

        // 获取二级达成度
        QueryWrapper<CourseIndicatorAchievement> secondLevelWrapper = new QueryWrapper<>();
        secondLevelWrapper.eq("teaching_class_id", classId);
        List<CourseIndicatorAchievement> secondLevelList = courseIndicatorAchievementMapper.selectList(secondLevelWrapper);

        List<AchievementCalculationResultVO.CourseIndicatorAchievementVO> secondLevelVOs = secondLevelList.stream()
                .map(achievement -> {
                    AchievementCalculationResultVO.CourseIndicatorAchievementVO vo =
                            new AchievementCalculationResultVO.CourseIndicatorAchievementVO();
                    BeanUtils.copyProperties(achievement, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        result.setSecondLevelAchievements(secondLevelVOs);

        result.setSuccess(teachingClass.getCalculatedStatus() != null && teachingClass.getCalculatedStatus() == 1);

        return result;
    }

    /**
     * 获取一级达成度
     */
    @Override
    public AchievementCalculationResultVO getFirstLevelAchievements(Long classId) {
        AchievementCalculationResultVO result = getCalculationResult(classId);
        result.setSecondLevelAchievements(null);
        return result;
    }

    /**
     * 获取二级达成度
     */
    @Override
    public AchievementCalculationResultVO getSecondLevelAchievements(Long classId) {
        AchievementCalculationResultVO result = getCalculationResult(classId);
        result.setFirstLevelAchievements(null);
        return result;
    }
}
