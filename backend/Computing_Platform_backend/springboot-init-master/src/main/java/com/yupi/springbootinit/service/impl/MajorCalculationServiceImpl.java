package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.GradesheetStatusHelper;
import com.yupi.springbootinit.manager.MajorScopeHelper;
import com.yupi.springbootinit.mapper.*;
import com.yupi.springbootinit.model.dto.majorCalculation.MajorCalculationRequest;
import com.yupi.springbootinit.model.dto.majorCalculation.MajorDashboardQueryRequest;
import com.yupi.springbootinit.model.entity.*;
import com.yupi.springbootinit.service.MajorCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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
    private MajorIndicatorAchievementMapper majorIndicatorAchievementMapper;

    @Resource
    private MajorScopeHelper majorScopeHelper;

    @Resource
    private GradesheetStatusHelper gradesheetStatusHelper;

    @Resource
    private StudentObjectiveAchievementMapper studentObjectiveAchievementMapper;

    @Resource
    private WeightObjectiveIndicatorMapper weightObjectiveIndicatorMapper;

    @Resource
    private ClassStudentMapper classStudentMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private StudentMajorAchievementMapper studentMajorAchievementMapper;

    private static final int SCALE = 4; // 计算精度：4位小数
    private static final BigDecimal THRESHOLD = new BigDecimal("0.7"); // 达成度阈值

    @Override
    public Map<String, Object> getDashboardOverview(MajorDashboardQueryRequest request) {
        // 参数校验
        if (request == null || request.getMajorId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业ID不能为空");
        }

        Long majorId = request.getMajorId();
        Long termId = request.getTermId();
        String grade = request.getGrade();

        Map<String, Object> result = new HashMap<>();
        result.put("majorId", majorId);
        result.put("termId", termId);
        result.put("grade", grade);

        // 查询专业信息
        SysDictMajor major = sysDictMajorMapper.selectById(majorId);
        if (major != null) {
            result.put("majorName", major.getMajorName());
        }

        // 查询学年学期信息
        if (termId != null) {
            SysDictSchoolYear term = sysDictSchoolYearMapper.selectById(termId);
            if (term != null) {
                result.put("termName", term.getYearName());
            }
        }

        // 获取涉及的教学班级
        List<TeachingClass> teachingClasses = getTeachingClasses(majorId, termId, grade);
        result.put("totalCourses", teachingClasses.size());

        // 统计各课程达成度数据情况
        int hasDataCount = 0;
        List<Map<String, Object>> courseStatusList = new ArrayList<>();

        for (TeachingClass teachingClass : teachingClasses) {
            Map<String, Object> courseStatus = new HashMap<>();
            courseStatus.put("classId", teachingClass.getId());
            courseStatus.put("className", teachingClass.getClassName());
            courseStatus.put("courseId", teachingClass.getCourseId());

            // 检查该课程是否有达成度数据
            QueryWrapper<CourseIndicatorAchievement> achievementQuery = new QueryWrapper<>();
            achievementQuery.eq("teaching_class_id", teachingClass.getId());
            long achievementCount = courseIndicatorAchievementMapper.selectCount(achievementQuery);

            boolean hasData = achievementCount > 0;
            if (hasData) {
                hasDataCount++;
            }

            courseStatus.put("hasAchievementData", hasData);
            courseStatus.put("achievementDataCount", achievementCount);
            // 三态状态（不改表推断）：LOCKED 已锁定 / SUBMITTED 已提交未计算 / NOT_SUBMITTED 未提交，
            // 供 C-4 看板展示"已锁定/未提交"；hasAchievementData 保留以兼容前端旧字段
            courseStatus.put("status", gradesheetStatusHelper.getStatus(teachingClass.getId()).name());
            courseStatusList.add(courseStatus);
        }

        result.put("coursesWithData", hasDataCount);
        result.put("courseStatusList", courseStatusList);

        // 检查是否可以计算（所有课程都有达成度数据）
        boolean canCalculate = !teachingClasses.isEmpty() && hasDataCount == teachingClasses.size();
        result.put("canCalculate", canCalculate);

        if (!canCalculate && !teachingClasses.isEmpty()) {
            result.put("errorMessage", "还有课程未计算达成度，无法进行专业级计算");
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> calculateMajorAchievement(MajorCalculationRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 参数校验
            if (request == null || request.getMajorId() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业ID不能为空");
            }

            Long majorId = request.getMajorId();
            Long termId = request.getTermId();
            String grade = request.getGrade();

            result.put("majorId", majorId);
            result.put("termId", termId);
            result.put("grade", grade);

            // 查询专业信息
            SysDictMajor major = sysDictMajorMapper.selectById(majorId);
            if (major == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "专业不存在");
            }
            result.put("majorName", major.getMajorName());

            // 查询学年学期信息
            if (termId != null) {
                SysDictSchoolYear term = sysDictSchoolYearMapper.selectById(termId);
                if (term == null) {
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学年学期不存在");
                }
                result.put("termName", term.getYearName());
            }

            // 获取涉及的教学班级
            List<TeachingClass> teachingClasses = getTeachingClasses(majorId, termId, grade);
            if (teachingClasses.isEmpty()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "该专业暂无相关教学班级");
            }

            result.put("totalCourses", teachingClasses.size());

            // 检查所有课程是否都有达成度数据
            for (TeachingClass teachingClass : teachingClasses) {
                QueryWrapper<CourseIndicatorAchievement> achievementQuery = new QueryWrapper<>();
                achievementQuery.eq("teaching_class_id", teachingClass.getId());
                long achievementCount = courseIndicatorAchievementMapper.selectCount(achievementQuery);

                if (achievementCount == 0) {
                    Course course = courseMapper.selectById(teachingClass.getCourseId());
                    String courseName = course != null ? course.getCourseName() : teachingClass.getClassName();
                    throw new BusinessException(ErrorCode.OPERATION_ERROR,
                            "课程 " + courseName + " 尚未计算达成度，无法进行专业级计算");
                }
            }

            // 计算三级达成度
            Map<String, Object> achievementStats = calculateLevelThreeAchievement(majorId, termId, grade, teachingClasses);
            result.putAll(achievementStats);

            // 计算每个学生的专业达成度（学生×指标点）
            Map<String, Object> studentStats = calculateStudentMajorAchievement(majorId, termId, grade, teachingClasses);
            result.putAll(studentStats);

            result.put("success", true);
            result.put("calcStatus", 2);
            result.put("calcEndTime", new Date());

            log.info("专业级达成度计算完成：专业ID={}, 学年学期ID={}, 年级={}, 指标点数={}",
                    majorId, termId, grade, achievementStats.get("totalIndicators"));

        } catch (Exception e) {
            log.error("专业级达成度计算失败：专业ID=" + request.getMajorId(), e);
            // 显式标记事务回滚：try-catch 会吞掉异常，导致 @Transactional 无法自动回滚。
            // 三级计算流程为"先物理删除旧三级数据（deleteByMajorTermGradePhysically），
            // 再逐条插入新数据"，若插入阶段失败必须回滚，否则旧数据已被物理删除且无法恢复，
            // 造成专业级达成度永久丢失或残缺，而接口仅返回 success=false，极具破坏性且隐蔽。
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            result.put("success", false);
            result.put("calcStatus", 3);
            result.put("errorMessage", "计算失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> getMajorCalculationResult(MajorCalculationRequest request) {
        if (request == null || request.getMajorId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业ID不能为空");
        }

        Long majorId = request.getMajorId();
        Long termId = request.getTermId();
        String grade = request.getGrade();

        Map<String, Object> result = new HashMap<>();
        result.put("majorId", majorId);
        result.put("termId", termId);
        result.put("grade", grade);

        // 查询专业信息
        SysDictMajor major = sysDictMajorMapper.selectById(majorId);
        if (major != null) {
            result.put("majorName", major.getMajorName());
        }

        // 查询学年学期信息
        if (termId != null) {
            SysDictSchoolYear term = sysDictSchoolYearMapper.selectById(termId);
            if (term != null) {
                result.put("termName", term.getYearName());
            }
        }

        // 查询三级达成度结果
        List<MajorIndicatorAchievement> achievements = getMajorAchievements(majorId, termId, grade);
        if (achievements != null && !achievements.isEmpty()) {
            result.put("calcStatus", 2); // 已计算
            result.putAll(buildAchievementStats(achievements));
        } else {
            result.put("calcStatus", 0); // 未计算
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

        log.info("删除专业级计算结果：专业ID={}, 学年学期ID={}, 年级={}", majorId, termId, grade);

        return true;
    }

    /**
     * 计算三级达成度（专业级指标点达成度）
     * 公式：专业级指标点达成度 = Σ(课程级指标点达成度 × 宏观总支撑权重Wc) / Σ(宏观总支撑权重Wc)
     */
    private Map<String, Object> calculateLevelThreeAchievement(
            Long majorId, Long termId, String grade, List<TeachingClass> teachingClasses) {

        log.info("开始计算三级达成度：专业ID={}, 学年学期ID={}, 年级={}", majorId, termId, grade);

        Map<String, Object> result = new HashMap<>();

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
        List<Map<String, Object>> details = new ArrayList<>();

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

            majorIndicatorAchievementMapper.insert(majorAchievement);

            // 添加到详情列表
            Map<String, Object> detail = new HashMap<>();
            detail.put("indicatorId", indicatorId);
            detail.put("indicatorCode", indicator.getIndicatorCode());
            detail.put("indicatorName", indicator.getIndicatorName());
            detail.put("requirementId", indicator.getRequirementId());
            detail.put("requirementCode", requirement != null ? requirement.getRequirementCode() : "");
            detail.put("requirementName", requirement != null ? requirement.getRequirementName() : "");
            detail.put("achievement", achievement);
            detail.put("meetsThreshold", achievement.compareTo(THRESHOLD) >= 0);
            detail.put("supportingCourseCount", indicatorMatrices.size());
            details.add(detail);
        }

        // 计算统计信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIndicators", indicatorIds.size());
        stats.put("totalRecords", details.size());
        stats.put("achievements", details);
        stats.put("threshold", THRESHOLD);

        if (!allAchievements.isEmpty()) {
            stats.put("averageAchievement", calculateAverage(allAchievements));
            stats.put("minAchievement", Collections.min(allAchievements));
            stats.put("maxAchievement", Collections.max(allAchievements));

            // 检查是否满足毕业要求
            boolean meetsRequirement = allAchievements.stream()
                    .allMatch(achievement -> achievement.compareTo(THRESHOLD) >= 0);
            stats.put("meetsGraduationRequirement", meetsRequirement);
        }

        log.info("三级达成度计算完成：专业ID={}, 指标点数={}, 平均达成度={}, 是否满足毕业要求={}",
                majorId, stats.get("totalIndicators"), stats.get("averageAchievement"), stats.get("meetsGraduationRequirement"));

        return stats;
    }

    /**
     * 计算每个学生的专业达成度（学生×指标点），全程保留学生维度。
     * 公式：
     *   学生 s 在课程 c 指标点 X 达成度 C(s,c,X) = Σ( s 在 c 的目标 j 达成度 × wjk ) / Σ( wjk )
     *   学生 s 专业指标点达成度     M(s,X)   = Σ( C(s,c,X) × Wc ) / Σ( Wc )
     * 整体达成度（各指标点算术平均）不在此落表，导出时从明细聚合。
     */
    private Map<String, Object> calculateStudentMajorAchievement(
            Long majorId, Long termId, String grade, List<TeachingClass> teachingClasses) {

        log.info("开始计算学生专业达成度：专业ID={}, 学年学期ID={}, 年级={}", majorId, termId, grade);

        List<Long> classIds = teachingClasses.stream()
                .map(TeachingClass::getId).collect(Collectors.toList());
        List<Long> courseIds = teachingClasses.stream()
                .map(TeachingClass::getCourseId).distinct().collect(Collectors.toList());

        // 班级 -> 课程
        Map<Long, Long> classToCourse = teachingClasses.stream()
                .collect(Collectors.toMap(TeachingClass::getId, TeachingClass::getCourseId, (a, b) -> a));

        // 宏观支撑矩阵（指标点 -> 支撑课程 + Wc）
        QueryWrapper<MatrixCourseIndicator> matrixQuery = new QueryWrapper<>();
        matrixQuery.eq("major_id", majorId).in("course_id", courseIds);
        Map<Long, List<MatrixCourseIndicator>> indicatorMatricesMap = matrixCourseIndicatorMapper.selectList(matrixQuery)
                .stream().collect(Collectors.groupingBy(MatrixCourseIndicator::getIndicatorId));

        // 指标点 / 毕业要求元信息
        List<Long> indicatorIds = new ArrayList<>(indicatorMatricesMap.keySet());
        Map<Long, IndicatorPoint> indicatorMap = indicatorIds.isEmpty() ? Collections.emptyMap()
                : indicatorPointMapper.selectBatchIds(indicatorIds).stream()
                .collect(Collectors.toMap(IndicatorPoint::getId, i -> i));
        List<Long> requirementIds = indicatorMap.values().stream()
                .map(IndicatorPoint::getRequirementId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, GraduationRequirement> requirementMap = requirementIds.isEmpty() ? Collections.emptyMap()
                : graduationRequirementMapper.selectBatchIds(requirementIds).stream()
                .collect(Collectors.toMap(GraduationRequirement::getId, r -> r));

        // 课程目标-指标点内部权重（课程 -> 权重列表）
        QueryWrapper<WeightObjectiveIndicator> weightQuery = new QueryWrapper<>();
        weightQuery.in("course_id", courseIds);
        Map<Long, List<WeightObjectiveIndicator>> weightByCourse = weightObjectiveIndicatorMapper.selectList(weightQuery)
                .stream().collect(Collectors.groupingBy(WeightObjectiveIndicator::getCourseId));

        // 学生一级达成度：studentId -> courseId -> objectiveId -> achievement
        QueryWrapper<StudentObjectiveAchievement> soaQuery = new QueryWrapper<>();
        soaQuery.in("teaching_class_id", classIds);
        List<StudentObjectiveAchievement> soaList = studentObjectiveAchievementMapper.selectList(soaQuery);

        Map<Long, Map<Long, Map<Long, BigDecimal>>> studentObjectiveMap = new HashMap<>();
        for (StudentObjectiveAchievement soa : soaList) {
            if (soa.getStudentId() == null || soa.getClassId() == null
                    || soa.getObjectiveId() == null || soa.getAchievement() == null) {
                continue;
            }
            Long courseId = classToCourse.get(soa.getClassId());
            if (courseId == null) {
                continue;
            }
            studentObjectiveMap
                    .computeIfAbsent(soa.getStudentId(), k -> new HashMap<>())
                    .computeIfAbsent(courseId, k -> new HashMap<>())
                    .put(soa.getObjectiveId(), soa.getAchievement());
        }

        // 计算每个学生每个指标点
        Date now = new Date();
        List<StudentMajorAchievement> toInsert = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Map<Long, BigDecimal>>> stuEntry : studentObjectiveMap.entrySet()) {
            Long studentId = stuEntry.getKey();
            Map<Long, Map<Long, BigDecimal>> studentByCourse = stuEntry.getValue();

            for (Map.Entry<Long, List<MatrixCourseIndicator>> indEntry : indicatorMatricesMap.entrySet()) {
                Long indicatorId = indEntry.getKey();
                IndicatorPoint indicator = indicatorMap.get(indicatorId);
                if (indicator == null) {
                    continue;
                }

                BigDecimal numerator = BigDecimal.ZERO;
                BigDecimal denominator = BigDecimal.ZERO;

                for (MatrixCourseIndicator matrix : indEntry.getValue()) {
                    Long courseId = matrix.getCourseId();
                    Map<Long, BigDecimal> objAch = studentByCourse.get(courseId);
                    if (objAch == null || objAch.isEmpty()) {
                        continue;
                    }
                    List<WeightObjectiveIndicator> indicatorWeights = weightByCourse
                            .getOrDefault(courseId, Collections.emptyList()).stream()
                            .filter(w -> indicatorId.equals(w.getIndicatorId()))
                            .collect(Collectors.toList());
                    if (indicatorWeights.isEmpty()) {
                        continue;
                    }

                    // C(s,c,X) = Σ(学生该目标达成度 × wjk) / Σ(wjk)
                    BigDecimal cNum = BigDecimal.ZERO;
                    BigDecimal cDen = BigDecimal.ZERO;
                    for (WeightObjectiveIndicator w : indicatorWeights) {
                        BigDecimal ach = objAch.get(w.getObjectiveId());
                        if (ach == null) {
                            continue;
                        }
                        cNum = cNum.add(ach.multiply(w.getInnerWeight()));
                        cDen = cDen.add(w.getInnerWeight());
                    }
                    if (cDen.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    BigDecimal cAchievement = cNum.divide(cDen, SCALE, RoundingMode.HALF_UP);

                    // 按 Wc 聚合到指标点
                    numerator = numerator.add(cAchievement.multiply(matrix.getTotalWeight()));
                    denominator = denominator.add(matrix.getTotalWeight());
                }

                if (denominator.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal achievement = numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);

                StudentMajorAchievement sma = new StudentMajorAchievement();
                sma.setStudentId(studentId);
                sma.setMajorId(majorId);
                sma.setTermId(termId);
                sma.setGrade(grade);
                sma.setIndicatorId(indicatorId);
                sma.setIndicatorCode(indicator.getIndicatorCode());
                sma.setIndicatorName(indicator.getIndicatorName());
                sma.setRequirementId(indicator.getRequirementId());
                GraduationRequirement req = requirementMap.get(indicator.getRequirementId());
                if (req != null) {
                    sma.setRequirementCode(req.getRequirementCode());
                    sma.setRequirementName(req.getRequirementName());
                }
                sma.setAchievement(achievement);
                sma.setCalculateTime(now);
                toInsert.add(sma);
            }
        }

        // 删旧 + 插新
        studentMajorAchievementMapper.deleteByMajorTermGradePhysically(majorId, termId, grade);
        for (StudentMajorAchievement sma : toInsert) {
            studentMajorAchievementMapper.insert(sma);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("studentAchievementCount", toInsert.size());
        stats.put("studentCount", toInsert.stream().map(StudentMajorAchievement::getStudentId).distinct().count());

        log.info("学生专业达成度计算完成：专业ID={}, 学生数={}, 记录数={}",
                majorId, stats.get("studentCount"), stats.get("studentAchievementCount"));

        return stats;
    }

    /**
     * 获取某专业 / 学年学期 / 年级涉及的教学班级。
     * <p>
     * 实现已抽取到 {@link MajorScopeHelper#getTeachingClasses}，供专业级计算与专业级报表共用，
     * 避免两处过滤逻辑不同步。本方法保留为包级委托，调用点（看板 / 专业级计算）无需改动。
     */
    List<TeachingClass> getTeachingClasses(Long majorId, Long termId, String grade) {
        return majorScopeHelper.getTeachingClasses(majorId, termId, grade);
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
    private Map<String, Object> buildAchievementStats(List<MajorIndicatorAchievement> achievements) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIndicators", achievements.size());
        stats.put("threshold", THRESHOLD);

        if (achievements.isEmpty()) {
            return stats;
        }

        List<BigDecimal> allAchievements = new ArrayList<>();
        List<Map<String, Object>> details = new ArrayList<>();

        for (MajorIndicatorAchievement achievement : achievements) {
            if (achievement.getAchievement() != null) {
                allAchievements.add(achievement.getAchievement());
            }

            Map<String, Object> detail = new HashMap<>();
            detail.put("indicatorId", achievement.getIndicatorId());
            detail.put("indicatorCode", achievement.getIndicatorCode());
            detail.put("indicatorName", achievement.getIndicatorName());
            detail.put("requirementId", achievement.getRequirementId());
            detail.put("requirementCode", achievement.getRequirementCode());
            detail.put("requirementName", achievement.getRequirementName());
            detail.put("achievement", achievement.getAchievement());
            detail.put("meetsThreshold", achievement.getAchievement() != null &&
                    achievement.getAchievement().compareTo(THRESHOLD) >= 0);

            details.add(detail);
        }

        stats.put("achievements", details);
        stats.put("totalRecords", details.size());

        if (!allAchievements.isEmpty()) {
            stats.put("averageAchievement", calculateAverage(allAchievements));
            stats.put("minAchievement", Collections.min(allAchievements));
            stats.put("maxAchievement", Collections.max(allAchievements));

            boolean meetsRequirement = allAchievements.stream()
                    .allMatch(achievement -> achievement.compareTo(THRESHOLD) >= 0);
            stats.put("meetsGraduationRequirement", meetsRequirement);
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
}