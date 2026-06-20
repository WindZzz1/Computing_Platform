package com.yupi.springbootinit.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.CourseIndicatorAchievementMapper;
import com.yupi.springbootinit.mapper.CourseMapper;
import com.yupi.springbootinit.mapper.StudentMapper;
import com.yupi.springbootinit.mapper.StudentObjectiveAchievementMapper;
import com.yupi.springbootinit.mapper.SysDictSchoolYearMapper;
import com.yupi.springbootinit.mapper.SysUserMapper;
import com.yupi.springbootinit.mapper.TeachingClassMapper;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.entity.CourseIndicatorAchievement;
import com.yupi.springbootinit.model.entity.Student;
import com.yupi.springbootinit.model.entity.StudentObjectiveAchievement;
import com.yupi.springbootinit.model.entity.SysDictSchoolYear;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.model.entity.TeachingClass;
import com.yupi.springbootinit.model.vo.report.CourseAchievementReportVO;
import com.yupi.springbootinit.model.vo.report.CourseIndicatorAchievementVO;
import com.yupi.springbootinit.model.vo.report.ObjectiveAchievementSummaryVO;
import com.yupi.springbootinit.model.vo.report.StudentAchievementDetailVO;
import com.yupi.springbootinit.service.CourseAchievementReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 课程达成度报表服务实现
 * <p>
 * 实现 D-1《课程目标达成情况评价表》数据查询与 Excel 导出。
 * （PDF 导出因 CJK 字体嵌入问题，独立 PR 处理。）
 *
 * @author YU
 */
@Service
@Slf4j
public class CourseAchievementReportServiceImpl
        extends ServiceImpl<TeachingClassMapper, TeachingClass>
        implements CourseAchievementReportService {

    private static final int SCALE = 4;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysDictSchoolYearMapper sysDictSchoolYearMapper;

    @Resource
    private StudentObjectiveAchievementMapper studentObjectiveAchievementMapper;

    @Resource
    private CourseIndicatorAchievementMapper courseIndicatorAchievementMapper;

    @Resource
    private StudentMapper studentMapper;

    @Override
    public CourseAchievementReportVO generateReportData(Long classId) {
        if (classId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }
        TeachingClass teachingClass = this.getById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        CourseAchievementReportVO vo = new CourseAchievementReportVO();
        vo.setClassId(classId);
        vo.setClassName(teachingClass.getClassName());
        vo.setReportGeneratedTime(new Date());

        // 课程信息
        Course course = courseMapper.selectById(teachingClass.getCourseId());
        if (course != null) {
            vo.setCourseCode(course.getCourseCode());
            vo.setCourseName(course.getCourseName());
        }
        // 主讲教师
        SysUser teacher = sysUserMapper.selectById(teachingClass.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getUsername());
        }
        // 学年学期
        if (teachingClass.getTermId() != null) {
            SysDictSchoolYear term = sysDictSchoolYearMapper.selectById(teachingClass.getTermId());
            if (term != null) {
                vo.setYearName(term.getYearName());
                vo.setSemesterName(term.getSemesterName());
            }
        }

        // 课程目标达成度汇总（班级平均/最高/最低/达成率）
        List<ObjectiveAchievementSummaryVO> summaries =
                studentObjectiveAchievementMapper.selectObjectiveSummaries(classId);
        vo.setObjectiveSummaries(summaries);

        // 学生×课程目标 明细行，据此组装学生明细并取计算时间
        List<StudentObjectiveAchievement> rows = studentObjectiveAchievementMapper.selectByClassId(classId);
        Date calculationTime = null;
        for (StudentObjectiveAchievement row : rows) {
            Date t = row.getCalculateTime();
            if (t != null && (calculationTime == null || t.after(calculationTime))) {
                calculationTime = t;
            }
        }
        vo.setCalculationTime(calculationTime);

        vo.setStudentDetails(buildStudentDetails(rows));
        vo.setStudentCount(vo.getStudentDetails().size());

        // 课程指标点达成度（二级）
        List<CourseIndicatorAchievement> indicators = courseIndicatorAchievementMapper.selectByClassId(classId);
        vo.setIndicatorAchievements(buildIndicatorAchievementVOs(indicators));

        return vo;
    }

    @Override
    public byte[] exportExcelReport(Long classId) {
        CourseAchievementReportVO vo = generateReportData(classId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExcelWriter writer = EasyExcel.write(outputStream).build();
        try {
            // Sheet 1：课程目标达成度汇总
            List<List<String>> head1 = headOf(
                    "课程目标编号", "课程目标名称", "班级平均达成度", "最高达成度", "最低达成度", "达成率", "学生数");
            List<List<Object>> data1 = (vo.getObjectiveSummaries() == null ? Collections.<ObjectiveAchievementSummaryVO>emptyList()
                    : vo.getObjectiveSummaries()).stream().map(s -> rowOf(
                    s.getObjectiveCode(), s.getObjectiveName(), s.getClassAverage(),
                    s.getMaxScore(), s.getMinScore(), s.getPassRate(), s.getStudentCount()))
                    .collect(Collectors.toList());
            WriteSheet sheet1 = EasyExcel.writerSheet(0, "课程目标达成度汇总").head(head1).build();
            writer.write(data1, sheet1);

            // Sheet 2：课程指标点达成度
            List<List<String>> head2 = headOf("指标点编号", "指标点名称", "课程级达成度");
            List<List<Object>> data2 = (vo.getIndicatorAchievements() == null ? Collections.<CourseIndicatorAchievementVO>emptyList()
                    : vo.getIndicatorAchievements()).stream().map(i -> rowOf(
                    i.getIndicatorCode(), i.getIndicatorName(), i.getAchievement()))
                    .collect(Collectors.toList());
            WriteSheet sheet2 = EasyExcel.writerSheet(1, "课程指标点达成度").head(head2).build();
            writer.write(data2, sheet2);

            // Sheet 3：学生达成度明细（按课程目标编号动态列）
            List<String> objectiveCodes = (vo.getObjectiveSummaries() == null ? Collections.<ObjectiveAchievementSummaryVO>emptyList()
                    : vo.getObjectiveSummaries()).stream()
                    .map(ObjectiveAchievementSummaryVO::getObjectiveCode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            List<List<String>> head3 = new ArrayList<>();
            head3.add(Collections.singletonList("学号"));
            head3.add(Collections.singletonList("姓名"));
            for (String code : objectiveCodes) {
                head3.add(Collections.singletonList(code));
            }
            head3.add(Collections.singletonList("平均达成度"));
            List<List<Object>> data3 = (vo.getStudentDetails() == null ? Collections.<StudentAchievementDetailVO>emptyList()
                    : vo.getStudentDetails()).stream().map(d -> {
                List<Object> row = new ArrayList<>();
                row.add(d.getStudentNo());
                row.add(d.getStudentName());
                Map<String, BigDecimal> map = d.getObjectiveAchievements();
                for (String code : objectiveCodes) {
                    row.add(map == null ? null : map.get(code));
                }
                row.add(d.getAverageAchievement());
                return row;
            }).collect(Collectors.toList());
            WriteSheet sheet3 = EasyExcel.writerSheet(2, "学生达成度明细").head(head3).build();
            writer.write(data3, sheet3);
        } finally {
            writer.finish();
        }
        return outputStream.toByteArray();
    }

    @Override
    public byte[] exportPdfReport(Long classId) {
        // PDF 导出需嵌入 CJK 字体（PDFBox 内置字体不支持中文），独立 PR 处理。
        // 在此之前引导用户使用 Excel 导出。
        throw new BusinessException(ErrorCode.OPERATION_ERROR,
                "PDF导出暂未实现，请使用Excel导出（PDF将在后续PR支持中文字体嵌入）");
    }

    @Override
    public boolean validateReportPermission(Long classId, Long userId) {
        if (classId == null || userId == null) {
            return false;
        }
        TeachingClass teachingClass = this.getById(classId);
        if (teachingClass == null) {
            return false;
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        // 管理员放行
        if (SysUserConstant.ROLE_ADMIN.equals(user.getRoleCode())) {
            return true;
        }
        // 仅本课程主讲教师可访问其班级报表
        return userId.equals(teachingClass.getTeacherId());
    }

    // ==================== 私有辅助 ====================

    /**
     * 将"学生×课程目标"明细行按学生聚合：每个学生一个 objectiveCode -> 达成度 映射 + 平均达成度。
     */
    private List<StudentAchievementDetailVO> buildStudentDetails(List<StudentObjectiveAchievement> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, StudentAchievementDetailVO> detailMap = new LinkedHashMap<>();
        Map<Long, List<BigDecimal>> valuesByStudent = new HashMap<>();
        Set<Long> studentIds = new LinkedHashSet<>();
        for (StudentObjectiveAchievement row : rows) {
            Long studentId = row.getStudentId();
            if (studentId == null) {
                continue;
            }
            studentIds.add(studentId);
            StudentAchievementDetailVO detail = detailMap.computeIfAbsent(studentId, k -> {
                StudentAchievementDetailVO d = new StudentAchievementDetailVO();
                d.setStudentId(k);
                d.setObjectiveAchievements(new LinkedHashMap<>());
                return d;
            });
            if (row.getObjectiveCode() != null) {
                detail.getObjectiveAchievements().put(row.getObjectiveCode(), row.getAchievement());
            }
            valuesByStudent.computeIfAbsent(studentId, k -> new ArrayList<>()).add(row.getAchievement());
        }

        Map<Long, Student> studentMap = studentIds.isEmpty() ? Collections.emptyMap()
                : studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        for (StudentAchievementDetailVO detail : detailMap.values()) {
            Student student = studentMap.get(detail.getStudentId());
            if (student != null) {
                detail.setStudentNo(student.getStudentNo());
                detail.setStudentName(student.getName());
            }
            detail.setAverageAchievement(average(valuesByStudent.get(detail.getStudentId())));
        }
        return new ArrayList<>(detailMap.values());
    }

    private List<CourseIndicatorAchievementVO> buildIndicatorAchievementVOs(List<CourseIndicatorAchievement> indicators) {
        if (indicators == null || indicators.isEmpty()) {
            return Collections.emptyList();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return indicators.stream().map(e -> {
            CourseIndicatorAchievementVO vo = new CourseIndicatorAchievementVO();
            vo.setIndicatorId(e.getIndicatorId());
            vo.setIndicatorCode(e.getIndicatorCode());
            vo.setIndicatorName(e.getIndicatorName());
            vo.setAchievement(e.getAchievement());
            vo.setCalculationTime(e.getCalculateTime() == null ? null : dateFormat.format(e.getCalculateTime()));
            return vo;
        }).collect(Collectors.toList());
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            if (v != null) {
                sum = sum.add(v);
            }
        }
        return sum.divide(new BigDecimal(values.size()), SCALE, RoundingMode.HALF_UP);
    }

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
}
