package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.*;
import com.yupi.springbootinit.model.dto.majorCalculation.MajorCalculationRequest;
import com.yupi.springbootinit.model.dto.majorCalculation.MajorDashboardQueryRequest;
import com.yupi.springbootinit.model.entity.*;
import com.yupi.springbootinit.model.vo.majorCalculation.CourseCalculationStatusVO;
import com.yupi.springbootinit.model.vo.majorCalculation.MajorCalculationResultVO;
import com.yupi.springbootinit.service.MajorCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 专业级达成度计算服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class MajorCalculationServiceImpl implements MajorCalculationService {

    @Resource
    private TeachingClassMapper teachingClassMapper;

    @Resource
    private GradeCalculationStatusMapper gradeCalculationStatusMapper;

    @Resource
    private CourseIndicatorAchievementMapper courseIndicatorAchievementMapper;

    @Resource
    private MatrixCourseIndicatorMapper matrixCourseIndicatorMapper;

    @Resource
    private IndicatorPointMapper indicatorPointMapper;

    @Resource
    private GraduationRequirementMapper graduationRequirementMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysDictSchoolYearMapper sysDictSchoolYearMapper;

    @Resource
    private SysDictMajorMapper sysDictMajorMapper;

    @Resource
    private ClassStudentMapper classStudentMapper;

    @Resource
    private MajorIndicatorAchievementMapper majorIndicatorAchievementMapper;

    @Resource
    private MajorCalculationSummaryMapper majorCalculationSummaryMapper;

    private static final int SCALE = 4; // 计算精度：4位小数
    private static final BigDecimal THRESHOLD = new BigDecimal("0.7"); // 达成度阈值

    @Override
    public MajorCalculationResultVO getDashboardOverview(MajorDashboardQueryRequest request) {
        // 参数校验
        if (request == null || request.getMajorId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业ID不能为空");
        }

        Long majorId = request.getMajorId();
        Long termId = request.getTermId();
        String grade = request.getGrade();

        MajorCalculationResultVO result = new MajorCalculationResultVO();
        result.setMajorId(majorId);
        result.setTermId(termId);
        result.setGrade(grade);

        // 查询专业信息
        SysDictMajor major = sysDictMajorMapper.selectById(majorId);
        if (major != null) {
            result.setMajorName(major.getMajorName());
        }

        // 查询学年学期信息
        if (termId != null) {
            SysDictSchoolYear term = sysDictSchoolYearMapper.selectById(termId);
            if (term != null) {
                result.setTermName(term.getYearName());
            }
        }

        // 获取涉及的教学班级
        List<TeachingClass> teachingClasses = getTeachingClasses(majorId, termId, grade);
        result.setTotalCourses(teachingClasses.size());

        // 统计计算状态
        int calculatedCount = 0;
        int lockedCount = 0;
        List<CourseCalculationStatusVO> courseStatusList = new ArrayList<>();

        for (TeachingClass teachingClass : teachingClasses) {
            CourseCalculationStatusVO statusVO = getCourseStatus(teachingClass);
            courseStatusList.add(statusVO);

            if (statusVO.getCalcStatus() == 2) { // 计算完成
                calculatedCount++;
            }
            if (statusVO.getIsLocked()) {
                lockedCount++;
            }
        }

        result.setCalculatedCourses(calculatedCount);
        result.setLockedCourses(lockedCount);
        result.setCourseStatusList(courseStatusList);

        // 检查是否可以计算
        boolean canCalculate = (teachingClasses.size() > 0) && (calculatedCount == teachingClasses.size()) && (lockedCount == teachingClasses.size());
        result.setCalcStatus(canCalculate ? 0 : 3); // 0-可以计算，3-不满足计算条件

        if (!canCalculate) {
            result.setErrorMessage("还有课程未完成计算或未锁定，无法进行专业级计算");
        }

        return result;
    }

    @Override
    public Page<CourseCalculationStatusVO> getCourseCalculationStatus(MajorDashboardQueryRequest request) {
        // 参数校验
        if (request == null || request.getMajorId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业ID不能为空");
        }

        Long majorId = request.getMajorId();
        Long termId = request.getTermId();
        String grade = request.getGrade();
        long current = request.getCurrent() != null ? request.getCurrent() : 1;
        long size = request.getPageSize() != null ? request.getPageSize() : 10;

        // 获取涉及的教学班级
        List<TeachingClass> teachingClasses = getTeachingClasses(majorId, termId, grade);

        // 构建课程状态VO列表
        List<CourseCalculationStatusVO> statusList = new ArrayList<>();
        for (TeachingClass teachingClass : teachingClasses) {
            statusList.add(getCourseStatus(teachingClass));
        }

        // 分页
        Page<CourseCalculationStatusVO> page = new Page<>(current, size, statusList.size());
        int start = (int) ((current - 1) * size);
        int end = Math.min(start + (int) size, statusList.size());

        if (start < statusList.size()) {
            page.setRecords(statusList.subList(start, end));
        } else {
            page.setRecords(new ArrayList<>());
        }

        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MajorCalculationResultVO calculateMajorAchievement(MajorCalculationRequest request) {
        MajorCalculationResultVO result = new MajorCalculationResultVO();

        try {
            // 参数校验
            if (request == null || request.getMajorId() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业ID不能为空");
            }

            Long majorId = request.getMajorId();
            Long termId = request.getTermId();
            String grade = request.getGrade();

            result.setMajorId(majorId);
            result.setTermId(termId);
            result.setGrade(grade);

            // 查询专业信息
            SysDictMajor major = sysDictMajorMapper.selectById(majorId);
            if (major == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "专业不存在");
            }
            result.setMajorName(major.getMajorName());

            // 查询学年学期信息
            if (termId != null) {
                SysDictSchoolYear term = sysDictSchoolYearMapper.selectById(termId);
                if (term == null) {
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学年学期不存在");
                }
                result.setTermName(term.getYearName());
            }

            // 获取涉及的教学班级
            List<TeachingClass> teachingClasses = getTeachingClasses(majorId, termId, grade);
            if (teachingClasses.isEmpty()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "该专业暂无相关教学班级");
            }

            result.setTotalCourses(teachingClasses.size());

            // 检查是否满足计算条件
            MajorCalculationSummary existingSummary = getMajorCalculationSummary(majorId, termId, grade);
            if (existingSummary != null && existingSummary.getCalcStatus() == 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "专业级计算正在进行中，请勿重复触发");
            }

            // 验证所有课程都已计算并锁定
            validateAllCoursesCalculatedAndLocked(teachingClasses);

            // 创建或更新计算汇总状态
            MajorCalculationSummary summary = createOrUpdateCalculationSummary(majorId, termId, grade, getCurrentUserId());
            result.setCalcStatus(summary.getCalcStatus());
            result.setCalcStartTime(summary.getCalcStartTime());

            // 计算三级达成度
            MajorCalculationResultVO.LevelThreeAchievementStats achievementStats =
                    calculateLevelThreeAchievement(majorId, termId, grade, teachingClasses);
            result.setAchievementStats(achievementStats);

            // 更新计算状态为完成
            updateCalculationSummary(majorId, termId, grade, 2, null); // 2-计算完成

            result.setSuccess(true);
            result.setCalcStatus(2);
            result.setCalcEndTime(new Date());

            log.info("专业级达成度计算完成：专业ID={}, 学年学期ID={}, 年级={}, 指标点数={}",
                    majorId, termId, grade, achievementStats.getTotalIndicators());

        } catch (Exception e) {
            log.error("专业级达成度计算失败：专业ID=" + request.getMajorId(), e);
            updateCalculationSummary(request.getMajorId(), request.getTermId(), request.getGrade(), 3, e.getMessage());

            result.setSuccess(false);
            result.setCalcStatus(3);
            result.setErrorMessage("计算失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public MajorCalculationResultVO getMajorCalculationResult(MajorCalculationRequest request) {
        if (request == null || request.getMajorId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业ID不能为空");
        }

        Long majorId = request.getMajorId();
        Long termId = request.getTermId();
        String grade = request.getGrade();

        MajorCalculationResultVO result = new MajorCalculationResultVO();
        result.setMajorId(majorId);
        result.setTermId(termId);
        result.setGrade(grade);

        // 查询专业信息
        SysDictMajor major = sysDictMajorMapper.selectById(majorId);
        if (major != null) {
            result.setMajorName(major.getMajorName());
        }

        // 查询学年学期信息
        if (termId != null) {
            SysDictSchoolYear term = sysDictSchoolYearMapper.selectById(termId);
            if (term != null) {
                result.setTermName(term.getYearName());
            }
        }

        // 查询计算汇总
        MajorCalculationSummary summary = getMajorCalculationSummary(majorId, termId, grade);
        if (summary != null) {
            result.setCalcStatus(summary.getCalcStatus());
            result.setTotalCourses(summary.getTotalCourses());
            result.setCalculatedCourses(summary.getCalculatedCourses());
            result.setLockedCourses(summary.getLockedCourses());
            result.setCalcStartTime(summary.getCalcStartTime());
            result.setCalcEndTime(summary.getCalcEndTime());
            result.setCalculatedBy(summary.getCalculatedBy());
            result.setErrorMessage(summary.getErrorMessage());
        } else {
            result.setCalcStatus(0); // 未计算
        }

        // 查询三级达成度结果
        if (summary != null && summary.getCalcStatus() == 2) {
            List<MajorIndicatorAchievement> achievements = getMajorAchievements(majorId, termId, grade);
            result.setAchievementStats(buildAchievementStats(achievements));
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteMajorCalculationResult(MajorCalculationRequest request) {
        if (request == null || request.getMajorId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业ID不能为空");
        }

        Long majorId = request.getMajorId();
        Long termId = request.getTermId();
        String grade = request.getGrade();

        // 删除三级达成度数据
        majorIndicatorAchievementMapper.deleteByMajorTermGradePhysically(majorId, termId, grade);

        // 删除计算汇总
        MajorCalculationSummary summary = getMajorCalculationSummary(majorId, termId, grade);
        if (summary != null) {
            majorCalculationSummaryMapper.deleteById(summary.getId());
        }

        log.info("删除专业级计算结果：专业ID={}, 学年学期ID={}, 年级={}", majorId, termId, grade);

        return true;
    }

    /**
     * 计算三级达成度（专业级指标点达成度）
     * 公式：专业级指标点达成度 = Σ(课程级指标点达成度 × 宏观总支撑权重Wc) / Σ(宏观总支撑权重Wc)
     */
    private MajorCalculationResultVO.LevelThreeAchievementStats calculateLevelThreeAchievement(
            Long majorId, Long termId, String grade, List<TeachingClass> teachingClasses) {

        log.info("开始计算三级达成度：专业ID={}, 学年学期ID={}, 年级={}", majorId, termId, grade);

        // 获取所有课程ID
        List<Long> courseIds = teachingClasses.stream()
                .map(TeachingClass::getCourseId)
                .distinct()
                .collect(Collectors.toList());

        // 查询宏观支撑矩阵
        QueryWrapper<MatrixCourseIndicator> matrixQuery = new QueryWrapper<>();
        matrixQuery.eq("major_id", majorId);
        matrixQuery.in("course_id", courseIds);
        List<MatrixCourseIndicator> matrices = matrixCourseIndicatorMapper.selectList(matrixQuery);

        if (matrices.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该专业暂无宏观支撑矩阵配置");
        }

        // 按指标点分组宏观支撑矩阵
        Map<Long, List<MatrixCourseIndicator>> indicatorMatricesMap = matrices.stream()
                .collect(Collectors.groupingBy(MatrixCourseIndicator::getIndicatorId));

        // 获取指标点信息
        List<Long> indicatorIds = new ArrayList<>(indicatorMatricesMap.keySet());
        Map<Long, IndicatorPoint> indicatorMap = indicatorPointMapper.selectBatchIds(indicatorIds).stream()
                .collect(Collectors.toMap(IndicatorPoint::getId, i -> i));

        // 获取毕业要求信息
        List<Long> requirementIds = indicatorMap.values().stream()
                .map(IndicatorPoint::getRequirementId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, GraduationRequirement> requirementMap = graduationRequirementMapper.selectBatchIds(requirementIds).stream()
                .collect(Collectors.toMap(GraduationRequirement::getId, r -> r));

        // 查询所有课程的二级达成度
        List<Long> classIds = teachingClasses.stream()
                .map(TeachingClass::getId)
                .collect(Collectors.toList());

        QueryWrapper<CourseIndicatorAchievement> courseAchievementQuery = new QueryWrapper<>();
        courseAchievementQuery.in("teaching_class_id", classIds);
        List<CourseIndicatorAchievement> courseAchievements = courseIndicatorAchievementMapper.selectList(courseAchievementQuery);

        if (courseAchievements.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "课程级达成度数据为空");
        }

        // 按课程ID和指标点ID分组二级达成度
        Map<Long, Map<Long, List<CourseIndicatorAchievement>>> courseIndicatorAchievementsMap = courseAchievements.stream()
                .collect(Collectors.groupingBy(
                        a -> {
                            // 从teaching_class_id找到对应的course_id
                            for (TeachingClass tc : teachingClasses) {
                                if (tc.getId().equals(a.getClassId())) {
                                    return tc.getCourseId();
                                }
                            }
                            return -1L;
                        },
                        Collectors.groupingBy(CourseIndicatorAchievement::getIndicatorId)
                ));

        // 删除已有的三级达成度数据
        majorIndicatorAchievementMapper.deleteByMajorTermGradePhysically(majorId, termId, grade);

        // 计算每个指标点的三级达成度
        List<BigDecimal> allAchievements = new ArrayList<>();
        List<MajorCalculationResultVO.IndicatorAchievementDetail> details = new ArrayList<>();

        for (Map.Entry<Long, List<MatrixCourseIndicator>> entry : indicatorMatricesMap.entrySet()) {
            Long indicatorId = entry.getKey();
            List<MatrixCourseIndicator> indicatorMatrices = entry.getValue();
            IndicatorPoint indicator = indicatorMap.get(indicatorId);

            if (indicator == null) {
                continue;
            }

            BigDecimal numerator = BigDecimal.ZERO; // 分子
            BigDecimal denominator = BigDecimal.ZERO; // 分母

            for (MatrixCourseIndicator matrix : indicatorMatrices) {
                // 获取该课程的二级达成度
                Map<Long, List<CourseIndicatorAchievement>> courseAchievsMap =
                        courseIndicatorAchievementsMap.get(matrix.getCourseId());

                if (courseAchievsMap == null || courseAchievsMap.isEmpty()) {
                    continue;
                }

                List<CourseIndicatorAchievement> indicatorAchievements = courseAchievsMap.get(indicatorId);
                if (indicatorAchievements == null || indicatorAchievements.isEmpty()) {
                    continue;
                }

                // 计算该课程该指标点的平均二级达成度
                BigDecimal sumAchievement = indicatorAchievements.stream()
                        .map(CourseIndicatorAchievement::getAchievement)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal avgAchievement = sumAchievement.divide(
                        new BigDecimal(indicatorAchievements.size()), SCALE, RoundingMode.HALF_UP);

                // 平均二级达成度 × 宏观总支撑权重
                numerator = numerator.add(avgAchievement.multiply(matrix.getTotalWeight()));
                denominator = denominator.add(matrix.getTotalWeight());
            }

            // 计算指标点达成度
            BigDecimal achievement = BigDecimal.ZERO;
            if (denominator.compareTo(BigDecimal.ZERO) > 0) {
                achievement = numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
                allAchievements.add(achievement);
            }

            // 保存三级达成度
            MajorIndicatorAchievement majorAchievement = new MajorIndicatorAchievement();
            majorAchievement.setMajorId(majorId);
            majorAchievement.setTermId(termId);
            majorAchievement.setGrade(grade);
            majorAchievement.setIndicatorId(indicatorId);
            majorAchievement.setIndicatorCode(indicator.getIndicatorCode());
            majorAchievement.setIndicatorName(indicator.getIndicatorName());
            majorAchievement.setRequirementId(indicator.getRequirementId());

            GraduationRequirement requirement = requirementMap.get(indicator.getRequirementId());
            if (requirement != null) {
                majorAchievement.setRequirementCode(requirement.getRequirementCode());
                majorAchievement.setRequirementName(requirement.getRequirementName());
            }

            majorAchievement.setAchievement(achievement);
            majorAchievement.setCalculateTime(new Date());
            majorAchievement.setCalcStatus(2); // 2-计算完成

            majorIndicatorAchievementMapper.insert(majorAchievement);

            // 添加到详情列表
            MajorCalculationResultVO.IndicatorAchievementDetail detail =
                    new MajorCalculationResultVO.IndicatorAchievementDetail();
            detail.setIndicatorId(indicatorId);
            detail.setIndicatorCode(indicator.getIndicatorCode());
            detail.setIndicatorName(indicator.getIndicatorName());
            detail.setRequirementId(indicator.getRequirementId());
            detail.setRequirementCode(requirement != null ? requirement.getRequirementCode() : "");
            detail.setRequirementName(requirement != null ? requirement.getRequirementName() : "");
            detail.setAchievement(achievement);
            detail.setMeetsThreshold(achievement.compareTo(THRESHOLD) >= 0);
            detail.setSupportingCourseCount(indicatorMatrices.size());
            details.add(detail);
        }

        // 计算统计信息
        MajorCalculationResultVO.LevelThreeAchievementStats stats = new MajorCalculationResultVO.LevelThreeAchievementStats();
        stats.setTotalIndicators(indicatorIds.size());
        stats.setTotalRecords(details.size());
        stats.setAchievements(details);
        stats.setThreshold(THRESHOLD);

        if (!allAchievements.isEmpty()) {
            stats.setAverageAchievement(calculateAverage(allAchievements));
            stats.setMinAchievement(Collections.min(allAchievements));
            stats.setMaxAchievement(Collections.max(allAchievements));

            // 检查是否满足毕业要求
            boolean meetsRequirement = allAchievements.stream()
                    .allMatch(achievement -> achievement.compareTo(THRESHOLD) >= 0);
            stats.setMeetsGraduationRequirement(meetsRequirement);
        }

        log.info("三级达成度计算完成：专业ID={}, 指标点数={}, 平均达成度={}, 是否满足毕业要求={}",
                majorId, stats.getTotalIndicators(), stats.getAverageAchievement(), stats.getMeetsGraduationRequirement());

        return stats;
    }

    /**
     * 获取教学班级列表
     */
    private List<TeachingClass> getTeachingClasses(Long majorId, Long termId, String grade) {
        QueryWrapper<TeachingClass> query = new QueryWrapper<>();

        // 通过学生信息筛选专业和年级
        if (majorId != null || grade != null) {
            // 查询该专业该年级的学生班级
            QueryWrapper<ClassStudent> studentQuery = new QueryWrapper<>();
            // 这里需要根据实际的业务逻辑来筛选
            // 暂时返回所有教学班级
        }

        if (termId != null) {
            query.eq("term_id", termId);
        }

        query.orderByAsc("id");
        return teachingClassMapper.selectList(query);
    }

    /**
     * 获取课程计算状态
     */
    private CourseCalculationStatusVO getCourseStatus(TeachingClass teachingClass) {
        CourseCalculationStatusVO statusVO = new CourseCalculationStatusVO();
        statusVO.setClassId(teachingClass.getId());
        statusVO.setClassName(teachingClass.getClassName());
        statusVO.setCourseId(teachingClass.getCourseId());
        statusVO.setTeacherId(teachingClass.getTeacherId());

        // 查询课程信息
        Course course = courseMapper.selectById(teachingClass.getCourseId());
        if (course != null) {
            statusVO.setCourseName(course.getCourseName());
            statusVO.setCourseCode(course.getCourseCode());
        }

        // 查询教师信息
        SysUser teacher = sysUserMapper.selectById(teachingClass.getTeacherId());
        if (teacher != null) {
            statusVO.setTeacherName(teacher.getUsername()); // 使用username
        }

        // 查询计算状态
        QueryWrapper<GradeCalculationStatus> calcStatusQuery = new QueryWrapper<>();
        calcStatusQuery.eq("teaching_class_id", teachingClass.getId());
        GradeCalculationStatus calcStatus = gradeCalculationStatusMapper.selectOne(calcStatusQuery);

        if (calcStatus != null) {
            statusVO.setCalcStatus(calcStatus.getCalcStatus());
            statusVO.setIsLocked(calcStatus.getIsLocked() == 1);
            statusVO.setCalcEndTime(calcStatus.getCalcEndTime());
            statusVO.setLockTime(calcStatus.getLockTime());
        } else {
            statusVO.setCalcStatus(0); // 未计算
            statusVO.setIsLocked(false);
        }

        // 查询学生人数
        QueryWrapper<ClassStudent> studentQuery = new QueryWrapper<>();
        studentQuery.eq("teaching_class_id", teachingClass.getId());
        Long studentCount = classStudentMapper.selectCount(studentQuery);
        statusVO.setStudentCount(studentCount.intValue());

        // 设置状态描述
        statusVO.setStatusDescription(getStatusDescription(statusVO.getCalcStatus(), statusVO.getIsLocked()));

        return statusVO;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDescription(Integer calcStatus, Boolean isLocked) {
        if (calcStatus == null) calcStatus = 0;
        if (isLocked == null) isLocked = false;

        switch (calcStatus) {
            case 0:
                return "未计算";
            case 1:
                return "计算中";
            case 2:
                return isLocked ? "已完成并锁定" : "已完成未锁定";
            case 3:
                return "计算失败";
            default:
                return "未知状态";
        }
    }

    /**
     * 验证所有课程都已计算并锁定
     */
    private void validateAllCoursesCalculatedAndLocked(List<TeachingClass> teachingClasses) {
        List<Long> classIds = teachingClasses.stream()
                .map(TeachingClass::getId)
                .collect(Collectors.toList());

        QueryWrapper<GradeCalculationStatus> query = new QueryWrapper<>();
        query.in("teaching_class_id", classIds);
        List<GradeCalculationStatus> statuses = gradeCalculationStatusMapper.selectList(query);

        for (TeachingClass teachingClass : teachingClasses) {
            GradeCalculationStatus status = statuses.stream()
                    .filter(s -> s.getClassId().equals(teachingClass.getId()))
                    .findFirst()
                    .orElse(null);

            if (status == null || status.getCalcStatus() != 2 || status.getIsLocked() != 1) {
                Course course = courseMapper.selectById(teachingClass.getCourseId());
                String courseName = course != null ? course.getCourseName() : teachingClass.getClassName();
                throw new BusinessException(ErrorCode.OPERATION_ERROR,
                        "课程 " + courseName + " 未完成计算或未锁定，无法进行专业级计算");
            }
        }
    }

    /**
     * 获取专业级计算汇总
     */
    private MajorCalculationSummary getMajorCalculationSummary(Long majorId, Long termId, String grade) {
        QueryWrapper<MajorCalculationSummary> query = new QueryWrapper<>();
        query.eq("major_id", majorId);
        if (termId != null) {
            query.eq("term_id", termId);
        }
        if (grade != null) {
            query.eq("grade", grade);
        }
        return majorCalculationSummaryMapper.selectOne(query);
    }

    /**
     * 创建或更新计算汇总
     */
    private MajorCalculationSummary createOrUpdateCalculationSummary(Long majorId, Long termId, String grade, Long userId) {
        MajorCalculationSummary summary = getMajorCalculationSummary(majorId, termId, grade);

        if (summary == null) {
            summary = new MajorCalculationSummary();
            summary.setMajorId(majorId);
            summary.setTermId(termId);
            summary.setGrade(grade);
            summary.setCalcStatus(1); // 1-计算中
            summary.setCalcStartTime(new Date());
            summary.setCalculatedBy(userId);
            majorCalculationSummaryMapper.insert(summary);
        } else {
            summary.setCalcStatus(1); // 1-计算中
            summary.setCalcStartTime(new Date());
            summary.setCalculatedBy(userId);
            summary.setErrorMessage(null);
            majorCalculationSummaryMapper.updateById(summary);
        }

        return summary;
    }

    /**
     * 更新计算汇总
     */
    private void updateCalculationSummary(Long majorId, Long termId, String grade, Integer calcStatus, String errorMessage) {
        MajorCalculationSummary summary = getMajorCalculationSummary(majorId, termId, grade);
        if (summary != null) {
            summary.setCalcStatus(calcStatus);
            if (calcStatus == 2) { // 计算完成
                summary.setCalcEndTime(new Date());
            }
            if (errorMessage != null) {
                summary.setErrorMessage(errorMessage);
            }
            majorCalculationSummaryMapper.updateById(summary);
        }
    }

    /**
     * 获取专业级达成度结果
     */
    private List<MajorIndicatorAchievement> getMajorAchievements(Long majorId, Long termId, String grade) {
        QueryWrapper<MajorIndicatorAchievement> query = new QueryWrapper<>();
        query.eq("major_id", majorId);
        if (termId != null) {
            query.eq("term_id", termId);
        }
        if (grade != null) {
            query.eq("grade", grade);
        }
        query.orderByAsc("indicator_id");
        return majorIndicatorAchievementMapper.selectList(query);
    }

    /**
     * 构建达成度统计信息
     */
    private MajorCalculationResultVO.LevelThreeAchievementStats buildAchievementStats(
            List<MajorIndicatorAchievement> achievements) {

        MajorCalculationResultVO.LevelThreeAchievementStats stats = new MajorCalculationResultVO.LevelThreeAchievementStats();
        stats.setTotalIndicators(achievements.size());
        stats.setThreshold(THRESHOLD);

        if (achievements.isEmpty()) {
            return stats;
        }

        List<BigDecimal> allAchievements = new ArrayList<>();
        List<MajorCalculationResultVO.IndicatorAchievementDetail> details = new ArrayList<>();

        for (MajorIndicatorAchievement achievement : achievements) {
            if (achievement.getAchievement() != null) {
                allAchievements.add(achievement.getAchievement());
            }

            MajorCalculationResultVO.IndicatorAchievementDetail detail =
                    new MajorCalculationResultVO.IndicatorAchievementDetail();
            detail.setIndicatorId(achievement.getIndicatorId());
            detail.setIndicatorCode(achievement.getIndicatorCode());
            detail.setIndicatorName(achievement.getIndicatorName());
            detail.setRequirementId(achievement.getRequirementId());
            detail.setRequirementCode(achievement.getRequirementCode());
            detail.setRequirementName(achievement.getRequirementName());
            detail.setAchievement(achievement.getAchievement());
            detail.setMeetsThreshold(achievement.getAchievement() != null &&
                    achievement.getAchievement().compareTo(THRESHOLD) >= 0);

            details.add(detail);
        }

        stats.setAchievements(details);
        stats.setTotalRecords(details.size());

        if (!allAchievements.isEmpty()) {
            stats.setAverageAchievement(calculateAverage(allAchievements));
            stats.setMinAchievement(Collections.min(allAchievements));
            stats.setMaxAchievement(Collections.max(allAchievements));

            boolean meetsRequirement = allAchievements.stream()
                    .allMatch(achievement -> achievement.compareTo(THRESHOLD) >= 0);
            stats.setMeetsGraduationRequirement(meetsRequirement);
        }

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
}