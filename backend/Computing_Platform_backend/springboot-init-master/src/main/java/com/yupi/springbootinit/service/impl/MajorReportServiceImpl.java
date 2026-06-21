package com.yupi.springbootinit.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.MajorScopeHelper;
import com.yupi.springbootinit.mapper.AssessmentPointMapper;
import com.yupi.springbootinit.mapper.ClassStudentMapper;
import com.yupi.springbootinit.mapper.CourseIndicatorAchievementMapper;
import com.yupi.springbootinit.mapper.CourseMapper;
import com.yupi.springbootinit.mapper.CourseObjectiveMapper;
import com.yupi.springbootinit.mapper.MajorIndicatorAchievementMapper;
import com.yupi.springbootinit.mapper.RelPointObjectiveMapper;
import com.yupi.springbootinit.mapper.StudentMapper;
import com.yupi.springbootinit.mapper.StudentObjectiveAchievementMapper;
import com.yupi.springbootinit.mapper.StudentScoreMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.mapper.SysDictSchoolYearMapper;
import com.yupi.springbootinit.mapper.SysUserMapper;
import com.yupi.springbootinit.mapper.TeachingClassMapper;
import com.yupi.springbootinit.model.dto.report.MajorReportRequest;
import com.yupi.springbootinit.model.entity.AssessmentPoint;
import com.yupi.springbootinit.model.entity.ClassStudent;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.entity.CourseIndicatorAchievement;
import com.yupi.springbootinit.model.entity.CourseObjective;
import com.yupi.springbootinit.model.entity.MajorIndicatorAchievement;
import com.yupi.springbootinit.model.entity.RelPointObjective;
import com.yupi.springbootinit.model.entity.Student;
import com.yupi.springbootinit.model.entity.StudentObjectiveAchievement;
import com.yupi.springbootinit.model.entity.StudentScore;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.entity.SysDictSchoolYear;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.model.entity.TeachingClass;
import com.yupi.springbootinit.model.vo.report.AssessmentPointAccount;
import com.yupi.springbootinit.model.vo.report.CourseAccountInfo;
import com.yupi.springbootinit.model.vo.report.IndicatorPointAchievementVO;
import com.yupi.springbootinit.model.vo.report.MajorAccountInfo;
import com.yupi.springbootinit.model.vo.report.MajorAchievementRadarVO;
import com.yupi.springbootinit.model.vo.report.PenetrationAccountVO;
import com.yupi.springbootinit.model.vo.report.StudentObjectiveAccount;
import com.yupi.springbootinit.model.vo.report.StudentScoreAccount;
import com.yupi.springbootinit.service.MajorReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 专业报告服务实现（模块 D-2）。
 * <p>
 * 雷达图（三级达成度可视化）、穿透式台账（五层追溯）已实现；
 * 台账 Excel 导出在后续 PR 补全。
 *
 * @author YU
 */
@Service
@Slf4j
public class MajorReportServiceImpl implements MajorReportService {

    private static final int SCALE = 4;

    @Resource
    private MajorIndicatorAchievementMapper majorIndicatorAchievementMapper;

    @Resource
    private SysDictMajorMapper sysDictMajorMapper;

    @Resource
    private SysDictSchoolYearMapper sysDictSchoolYearMapper;

    @Resource
    private MajorScopeHelper majorScopeHelper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private TeachingClassMapper teachingClassMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private ClassStudentMapper classStudentMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private CourseIndicatorAchievementMapper courseIndicatorAchievementMapper;

    @Resource
    private StudentObjectiveAchievementMapper studentObjectiveAchievementMapper;

    @Resource
    private StudentScoreMapper studentScoreMapper;

    @Resource
    private AssessmentPointMapper assessmentPointMapper;

    @Resource
    private RelPointObjectiveMapper relPointObjectiveMapper;

    @Resource
    private CourseObjectiveMapper courseObjectiveMapper;

    // ==================== 雷达图（PR-1） ====================

    @Override
    public MajorAchievementRadarVO getRadarChartData(MajorReportRequest request) {
        Long majorId = request.getMajorId();
        Long termId = request.getTermId();
        String grade = request.getGrade();

        // 前置：三级达成度必须已计算（雷达图直接消费三级结果）
        List<MajorIndicatorAchievement> achievements =
                majorIndicatorAchievementMapper.selectByMajorTermGrade(majorId, termId, grade);
        if (achievements == null || achievements.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "该专业本学期本年级尚未计算三级达成度，请先执行专业级计算");
        }

        SysDictMajor major = sysDictMajorMapper.selectById(majorId);
        SysDictSchoolYear term = sysDictSchoolYearMapper.selectById(termId);

        MajorAchievementRadarVO vo = new MajorAchievementRadarVO();
        vo.setMajorId(majorId);
        vo.setMajorName(major != null ? major.getMajorName() : null);
        vo.setMajorCode(major != null ? major.getMajorCode() : null);
        vo.setYearName(term != null ? term.getYearName() : null);
        vo.setSemesterName(term != null ? term.getSemesterName() : null);
        vo.setGrade(grade);
        vo.setGeneratedTime(new Date());

        List<IndicatorPointAchievementVO> points = achievements.stream().map(a -> {
            IndicatorPointAchievementVO p = new IndicatorPointAchievementVO();
            p.setIndicatorId(a.getIndicatorId());
            p.setIndicatorCode(a.getIndicatorCode());
            p.setIndicatorName(a.getIndicatorName());
            p.setAchievement(a.getAchievement());
            p.setRequirementId(a.getRequirementId());
            p.setRequirementCode(a.getRequirementCode());
            p.setRequirementName(a.getRequirementName());
            return p;
        }).collect(Collectors.toList());
        vo.setIndicatorPoints(points);
        return vo;
    }

    // ==================== 穿透式台账（PR-2） ====================

    @Override
    public PenetrationAccountVO getPenetrationAccount(MajorReportRequest request) {
        Long majorId = request.getMajorId();
        Long termId = request.getTermId();
        String grade = request.getGrade();

        // 前置：三级达成度必须已计算
        List<MajorIndicatorAchievement> majorAchievements =
                majorIndicatorAchievementMapper.selectByMajorTermGrade(majorId, termId, grade);
        if (majorAchievements == null || majorAchievements.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "该专业本学期本年级尚未计算三级达成度，请先执行专业级计算");
        }

        // 圈定 scope 教学班（已含 majorId/termId/grade 过滤）
        List<TeachingClass> classes = majorScopeHelper.getTeachingClasses(majorId, termId, grade);
        if (classes.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该专业本学期本年级无教学班级数据");
        }

        List<Long> classIds = classes.stream().map(TeachingClass::getId).collect(Collectors.toList());
        List<Long> distinctCourseIds = classes.stream().map(TeachingClass::getCourseId)
                .distinct().collect(Collectors.toList());

        // ---------- 批量预取（避免 N+1） ----------
        Map<Long, Course> courseMap = batchToMap(courseMapper.selectBatchIds(distinctCourseIds), Course::getId);
        Set<Long> teacherIds = classes.stream().map(TeachingClass::getTeacherId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = teacherIds.isEmpty() ? Collections.emptyMap()
                : batchToMap(sysUserMapper.selectBatchIds(teacherIds), SysUser::getId);

        List<StudentObjectiveAchievement> soaAll = studentObjectiveAchievementMapper.selectList(
                new QueryWrapper<StudentObjectiveAchievement>().in("teaching_class_id", classIds));
        List<CourseIndicatorAchievement> ciaAll = courseIndicatorAchievementMapper.selectList(
                new QueryWrapper<CourseIndicatorAchievement>().in("teaching_class_id", classIds));
        List<StudentScore> scoreAll = studentScoreMapper.selectList(
                new QueryWrapper<StudentScore>().in("teaching_class_id", classIds));

        Set<Long> studentIds = soaAll.stream().map(StudentObjectiveAchievement::getStudentId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        studentIds.addAll(scoreAll.stream().map(StudentScore::getStudentId)
                .filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, Student> studentMap = studentIds.isEmpty() ? Collections.emptyMap()
                : batchToMap(studentMapper.selectBatchIds(studentIds), Student::getId);

        Map<Long, List<AssessmentPoint>> apByCourse = distinctCourseIds.isEmpty()
                ? Collections.emptyMap()
                : assessmentPointMapper.selectList(
                        new QueryWrapper<AssessmentPoint>().in("course_id", distinctCourseIds)).stream()
                .collect(Collectors.groupingBy(AssessmentPoint::getCourseId));
        Map<Long, AssessmentPoint> apById = apByCourse.values().stream().flatMap(List::stream)
                .collect(Collectors.toMap(AssessmentPoint::getId, a -> a, (a, b) -> a));
        Set<Long> pointIds = apById.keySet();
        Map<Long, List<RelPointObjective>> relByPoint = pointIds.isEmpty() ? Collections.emptyMap()
                : relPointObjectiveMapper.selectList(
                        new QueryWrapper<RelPointObjective>().in("point_id", pointIds)).stream()
                .collect(Collectors.groupingBy(RelPointObjective::getPointId));
        Set<Long> objectiveIds = relByPoint.values().stream().flatMap(List::stream)
                .map(RelPointObjective::getObjectiveId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, CourseObjective> coMap = objectiveIds.isEmpty() ? Collections.emptyMap()
                : batchToMap(courseObjectiveMapper.selectBatchIds(objectiveIds), CourseObjective::getId);

        // 班级人数
        Map<Long, Long> studentCountByClass = classStudentMapper.selectList(
                new QueryWrapper<ClassStudent>().in("teaching_class_id", classIds)).stream()
                .collect(Collectors.groupingBy(ClassStudent::getClassId, Collectors.counting()));

        // 考核点班级平均分（跨该课所有班，actual_score 平均）
        Map<Long, BigDecimal> avgScoreByPoint = scoreAll.stream()
                .filter(s -> s.getPointId() != null && s.getActualScore() != null)
                .collect(Collectors.groupingBy(StudentScore::getPointId))
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                        e -> average(e.getValue().stream()
                                .map(StudentScore::getActualScore).collect(Collectors.toList()))));

        // ---------- 组装五层 VO ----------
        PenetrationAccountVO vo = new PenetrationAccountVO();
        vo.setMajorInfo(buildMajorInfo(majorId, termId, grade, majorAchievements, classes, studentMap));
        vo.setCourses(buildCourses(classes, courseMap, userMap, ciaAll, studentCountByClass));
        vo.setStudentObjectives(buildStudentObjectives(soaAll, studentMap, courseMap, classes));
        vo.setAssessmentPoints(buildAssessmentPoints(distinctCourseIds, courseMap, apByCourse,
                relByPoint, coMap, avgScoreByPoint));
        vo.setStudentScores(buildStudentScores(scoreAll, studentMap, courseMap, classes, apById));
        return vo;
    }

    // ---------- 第 1 层：专业概况 ----------
    private MajorAccountInfo buildMajorInfo(Long majorId, Long termId, String grade,
                                            List<MajorIndicatorAchievement> majorAchievements,
                                            List<TeachingClass> classes, Map<Long, Student> studentMap) {
        MajorAccountInfo info = new MajorAccountInfo();
        info.setMajorId(majorId);
        SysDictMajor major = sysDictMajorMapper.selectById(majorId);
        if (major != null) {
            info.setMajorName(major.getMajorName());
            info.setMajorCode(major.getMajorCode());
        }
        info.setTermId(termId);
        SysDictSchoolYear term = sysDictSchoolYearMapper.selectById(termId);
        if (term != null) {
            info.setYearName(term.getYearName());
            info.setSemesterName(term.getSemesterName());
        }
        info.setGrade(grade);
        info.setTotalCourses((int) classes.stream().map(TeachingClass::getCourseId).distinct().count());
        info.setTotalStudents(studentMap.size());
        BigDecimal overall = average(majorAchievements.stream()
                .map(MajorIndicatorAchievement::getAchievement).filter(Objects::nonNull)
                .collect(Collectors.toList()));
        info.setOverallAchievement(overall == null ? null : overall.doubleValue());
        return info;
    }

    // ---------- 第 2 层：课程（教学班粒度） ----------
    private List<CourseAccountInfo> buildCourses(List<TeachingClass> classes, Map<Long, Course> courseMap,
                                                 Map<Long, SysUser> userMap, List<CourseIndicatorAchievement> ciaAll,
                                                 Map<Long, Long> studentCountByClass) {
        Map<Long, List<CourseIndicatorAchievement>> ciaByClass = ciaAll.stream()
                .collect(Collectors.groupingBy(CourseIndicatorAchievement::getClassId));
        List<CourseAccountInfo> list = new ArrayList<>();
        for (TeachingClass tc : classes) {
            CourseAccountInfo c = new CourseAccountInfo();
            c.setClassId(tc.getId());
            c.setClassName(tc.getClassName());
            Course course = courseMap.get(tc.getCourseId());
            if (course != null) {
                c.setCourseId(course.getId());
                c.setCourseCode(course.getCourseCode());
                c.setCourseName(course.getCourseName());
            }
            SysUser teacher = userMap.get(tc.getTeacherId());
            if (teacher != null) {
                c.setTeacherName(teacher.getUsername());
            }
            c.setStudentCount(studentCountByClass.getOrDefault(tc.getId(), 0L).intValue());
            // 课程指标点达成度（单值）：取该班各指标点二级达成度算术平均
            List<CourseIndicatorAchievement> cias = ciaByClass.get(tc.getId());
            c.setCourseIndicatorAchievement(cias == null ? null
                    : average(cias.stream().map(CourseIndicatorAchievement::getAchievement)
                            .collect(Collectors.toList())));
            // 课程目标详情前端不直接消费，置空避免重复查询
            c.setObjectives(Collections.emptyList());
            list.add(c);
        }
        return list;
    }

    // ---------- 第 3 层：学生课程目标达成度 ----------
    private List<StudentObjectiveAccount> buildStudentObjectives(List<StudentObjectiveAchievement> soaAll,
                                                                 Map<Long, Student> studentMap,
                                                                 Map<Long, Course> courseMap,
                                                                 List<TeachingClass> classes) {
        Map<Long, TeachingClass> classById = classes.stream()
                .collect(Collectors.toMap(TeachingClass::getId, t -> t, (a, b) -> a));
        // 按 (studentId, classId) 聚合
        Map<Long, Map<Long, List<StudentObjectiveAchievement>>> grouped = soaAll.stream()
                .filter(s -> s.getStudentId() != null)
                .collect(Collectors.groupingBy(StudentObjectiveAchievement::getStudentId,
                        Collectors.groupingBy(StudentObjectiveAchievement::getClassId)));
        List<StudentObjectiveAccount> list = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, List<StudentObjectiveAchievement>>> byStudent : grouped.entrySet()) {
            Long studentId = byStudent.getKey();
            Student stu = studentMap.get(studentId);
            for (Map.Entry<Long, List<StudentObjectiveAchievement>> byClass : byStudent.getValue().entrySet()) {
                List<StudentObjectiveAchievement> rows = byClass.getValue();
                Long classId = byClass.getKey();
                StudentObjectiveAccount a = new StudentObjectiveAccount();
                a.setStudentId(studentId);
                if (stu != null) {
                    a.setStudentNo(stu.getStudentNo());
                    a.setStudentName(stu.getName());
                }
                a.setClassId(classId);
                TeachingClass tc = classById.get(classId);
                if (tc != null) {
                    a.setClassName(tc.getClassName());
                    Course course = courseMap.get(tc.getCourseId());
                    if (course != null) {
                        a.setCourseCode(course.getCourseCode());
                        a.setCourseName(course.getCourseName());
                    }
                }
                Map<String, BigDecimal> objMap = new LinkedHashMap<>();
                for (StudentObjectiveAchievement r : rows) {
                    if (r.getObjectiveCode() != null) {
                        objMap.put(r.getObjectiveCode(), r.getAchievement());
                    }
                }
                a.setObjectiveAchievements(objMap);
                a.setAverageAchievement(average(rows.stream()
                        .map(StudentObjectiveAchievement::getAchievement).collect(Collectors.toList())));
                list.add(a);
            }
        }
        return list;
    }

    // ---------- 第 4 层：考核点（考核点 × 目标多对多展开） ----------
    private List<AssessmentPointAccount> buildAssessmentPoints(List<Long> courseIds, Map<Long, Course> courseMap,
                                                               Map<Long, List<AssessmentPoint>> apByCourse,
                                                               Map<Long, List<RelPointObjective>> relByPoint,
                                                               Map<Long, CourseObjective> coMap,
                                                               Map<Long, BigDecimal> avgScoreByPoint) {
        List<AssessmentPointAccount> list = new ArrayList<>();
        for (Long courseId : courseIds) {
            Course course = courseMap.get(courseId);
            String courseName = course != null ? course.getCourseName() : null;
            for (AssessmentPoint pt : apByCourse.getOrDefault(courseId, Collections.emptyList())) {
                List<RelPointObjective> rels = relByPoint.getOrDefault(pt.getId(), Collections.emptyList());
                BigDecimal avgScore = avgScoreByPoint.get(pt.getId());
                if (rels.isEmpty()) {
                    // 未关联任何课程目标：仍输出一行，objectiveCode/weight 为 null
                    list.add(buildApAccount(pt, courseName, null, null, avgScore));
                } else {
                    // 多对多：每个 (考核点, 目标) 组合一行
                    for (RelPointObjective rel : rels) {
                        CourseObjective co = rel.getObjectiveId() == null ? null : coMap.get(rel.getObjectiveId());
                        list.add(buildApAccount(pt, courseName,
                                co != null ? co.getObjCode() : null, rel.getWeight(), avgScore));
                    }
                }
            }
        }
        return list;
    }

    private AssessmentPointAccount buildApAccount(AssessmentPoint pt, String courseName,
                                                  String objectiveCode, BigDecimal weight, BigDecimal classAverageScore) {
        AssessmentPointAccount a = new AssessmentPointAccount();
        a.setPointId(pt.getId());
        a.setPointCode(pt.getPointCode());
        a.setPointName(pt.getPointName());
        // 前端契约字段：与 pointCode/pointName 同值
        a.setAssessmentPointCode(pt.getPointCode());
        a.setAssessmentPointName(pt.getPointName());
        a.setFullScore(pt.getFullScore());
        a.setCourseName(courseName);
        a.setObjectiveCode(objectiveCode);
        a.setWeight(weight);
        a.setClassAverageScore(classAverageScore);
        return a;
    }

    // ---------- 第 5 层：学生原始得分 ----------
    private List<StudentScoreAccount> buildStudentScores(List<StudentScore> scoreAll, Map<Long, Student> studentMap,
                                                         Map<Long, Course> courseMap, List<TeachingClass> classes,
                                                         Map<Long, AssessmentPoint> apById) {
        Map<Long, TeachingClass> classById = classes.stream()
                .collect(Collectors.toMap(TeachingClass::getId, t -> t, (a, b) -> a));
        List<StudentScoreAccount> list = new ArrayList<>();
        for (StudentScore s : scoreAll) {
            StudentScoreAccount a = new StudentScoreAccount();
            a.setStudentId(s.getStudentId());
            Student stu = studentMap.get(s.getStudentId());
            if (stu != null) {
                a.setStudentNo(stu.getStudentNo());
                a.setStudentName(stu.getName());
            }
            TeachingClass tc = classById.get(s.getClassId());
            if (tc != null) {
                Course course = courseMap.get(tc.getCourseId());
                if (course != null) {
                    a.setCourseCode(course.getCourseCode());
                    a.setCourseName(course.getCourseName());
                }
            }
            AssessmentPoint pt = s.getPointId() == null ? null : apById.get(s.getPointId());
            if (pt != null) {
                a.setAssessmentPointCode(pt.getPointCode());
                a.setAssessmentPointName(pt.getPointName());
            }
            a.setFullScore(s.getFullScore());
            a.setScore(s.getActualScore());
            a.setAchievement(achievementRatio(s.getActualScore(), s.getFullScore()));
            list.add(a);
        }
        return list;
    }

    @Override
    public byte[] exportPenetrationAccountExcel(MajorReportRequest request) {
        PenetrationAccountVO vo = getPenetrationAccount(request);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExcelWriter writer = EasyExcel.write(outputStream).build();
        try {
            // Sheet 1：专业信息
            MajorAccountInfo info = vo.getMajorInfo();
            List<List<Object>> data1 = new ArrayList<>();
            data1.add(rowOf("专业", info == null ? null : info.getMajorName()));
            data1.add(rowOf("学年学期", info == null ? null : joinNonEmpty(info.getYearName(), info.getSemesterName())));
            data1.add(rowOf("年级", info == null ? null : info.getGrade()));
            data1.add(rowOf("涉及课程数", info == null ? null : info.getTotalCourses()));
            data1.add(rowOf("学生数", info == null ? null : info.getTotalStudents()));
            data1.add(rowOf("整体达成度", info == null ? null : info.getOverallAchievement()));
            writer.write(data1, EasyExcel.writerSheet(0, "专业信息")
                    .head(headOf("项目", "内容")).build());

            // Sheet 2：课程达成度（教学班粒度）
            List<List<Object>> data2 = (vo.getCourses() == null ? Collections.<CourseAccountInfo>emptyList()
                    : vo.getCourses()).stream()
                    .map(c -> rowOf(c.getCourseCode(), c.getCourseName(), c.getClassName(),
                            c.getTeacherName(), c.getStudentCount(), c.getCourseIndicatorAchievement()))
                    .collect(Collectors.toList());
            writer.write(data2, EasyExcel.writerSheet(1, "课程达成度")
                    .head(headOf("课程编号", "课程名称", "教学班级", "主讲教师", "学生数", "课程指标点达成度")).build());

            // Sheet 3：学生课程目标达成度
            List<List<Object>> data3 = (vo.getStudentObjectives() == null ? Collections.<StudentObjectiveAccount>emptyList()
                    : vo.getStudentObjectives()).stream()
                    .map(s -> rowOf(s.getStudentNo(), s.getStudentName(), s.getCourseName(),
                            formatObjectiveAchievements(s.getObjectiveAchievements()), s.getAverageAchievement()))
                    .collect(Collectors.toList());
            writer.write(data3, EasyExcel.writerSheet(2, "学生课程目标")
                    .head(headOf("学号", "姓名", "课程", "各课程目标达成度", "平均达成度")).build());

            // Sheet 4：考核点（含多对多展开）
            List<List<Object>> data4 = (vo.getAssessmentPoints() == null ? Collections.<AssessmentPointAccount>emptyList()
                    : vo.getAssessmentPoints()).stream()
                    .map(a -> rowOf(a.getCourseName(), a.getAssessmentPointCode(), a.getAssessmentPointName(),
                            a.getObjectiveCode(), a.getFullScore(), a.getWeight(), a.getClassAverageScore()))
                    .collect(Collectors.toList());
            writer.write(data4, EasyExcel.writerSheet(3, "考核点")
                    .head(headOf("课程", "考核点编号", "考核点名称", "关联课程目标", "满分", "支撑权重", "班级平均分")).build());

            // Sheet 5：学生原始成绩
            List<List<Object>> data5 = (vo.getStudentScores() == null ? Collections.<StudentScoreAccount>emptyList()
                    : vo.getStudentScores()).stream()
                    .map(s -> rowOf(s.getStudentNo(), s.getStudentName(), s.getCourseName(),
                            s.getAssessmentPointCode(), s.getAssessmentPointName(),
                            s.getFullScore(), s.getScore(), s.getAchievement()))
                    .collect(Collectors.toList());
            writer.write(data5, EasyExcel.writerSheet(4, "学生原始成绩")
                    .head(headOf("学号", "姓名", "课程", "考核点编号", "考核点名称", "满分", "得分", "达成度")).build());
        } finally {
            writer.finish();
        }
        return outputStream.toByteArray();
    }

    @Override
    public boolean validateMajorPermission(Long majorId, Long userId, String userRole) {
        // 教务管理员可以查看所有专业
        if (SysUserConstant.ROLE_EDU.equals(userRole)) {
            return true;
        }
        // 专业负责人可以查看所有专业（只读权限）
        // 已知遗留：此处未按 user.major_id 做专业归属校验，属跨模块权限模型调整，后续单独收紧。
        if (SysUserConstant.ROLE_LEADER.equals(userRole)) {
            return true;
        }
        return false;
    }

    // ==================== 私有工具 ====================

    private List<List<String>> headOf(String... columns) {
        List<List<String>> head = new ArrayList<>();
        for (String column : columns) {
            head.add(Collections.singletonList(column));
        }
        return head;
    }

    private List<Object> rowOf(Object... values) {
        List<Object> row = new ArrayList<>();
        Collections.addAll(row, values);
        return row;
    }

    private String formatObjectiveAchievements(Map<String, BigDecimal> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        return map.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(", "));
    }

    private String joinNonEmpty(String... parts) {
        return Arrays.stream(parts).filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (BigDecimal v : values) {
            if (v != null) {
                sum = sum.add(v);
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return sum.divide(new BigDecimal(count), SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal achievementRatio(BigDecimal actual, BigDecimal full) {
        if (actual == null || full == null || full.signum() == 0) {
            return null;
        }
        return actual.divide(full, SCALE, RoundingMode.HALF_UP);
    }

    private <T> Map<Long, T> batchToMap(List<T> list, Function<T, Long> keyFn) {
        return list.stream().collect(Collectors.toMap(keyFn, t -> t, (a, b) -> a));
    }
}
