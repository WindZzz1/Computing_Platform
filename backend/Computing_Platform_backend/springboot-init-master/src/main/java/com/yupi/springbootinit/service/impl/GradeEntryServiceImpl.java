package com.yupi.springbootinit.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.OwnershipHelper;
import com.yupi.springbootinit.mapper.*;
import com.yupi.springbootinit.model.dto.gradeEntry.*;
import com.yupi.springbootinit.model.entity.*;
import com.yupi.springbootinit.model.vo.gradeEntry.GradeEntryExcelData;
import com.yupi.springbootinit.model.vo.gradeEntry.GradeImportResultVO;
import com.yupi.springbootinit.model.vo.gradeEntry.StudentScoreVO;
import com.yupi.springbootinit.service.GradeEntryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 成绩录入服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class GradeEntryServiceImpl extends ServiceImpl<TeachingClassMapper, TeachingClass> implements GradeEntryService {

    @Resource
    private TeachingClassMapper teachingClassMapper;

    @Resource
    private ClassStudentMapper classStudentMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private OwnershipHelper ownershipHelper;

    @Resource
    private AssessmentPointMapper assessmentPointMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysDictSchoolYearMapper sysDictSchoolYearMapper;

    @Resource
    private StudentScoreMapper studentScoreMapper;

    @Override
    public void generateAndDownloadTemplate(GradeEntryTemplateRequest request, HttpServletResponse response) {
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

        // 查询课程信息
        Course course = courseMapper.selectById(teachingClass.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }

        // 查询主讲教师信息
        SysUser teacher = sysUserMapper.selectById(teachingClass.getTeacherId());
        if (teacher == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "主讲教师不存在");
        }

        // 查询学年学期信息
        SysDictSchoolYear schoolYear = sysDictSchoolYearMapper.selectById(teachingClass.getTermId());
        if (schoolYear == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学年学期不存在");
        }

        // 查询班级的所有学生
        QueryWrapper<ClassStudent> classStudentQuery = new QueryWrapper<>();
        classStudentQuery.eq("teaching_class_id", classId);
        classStudentQuery.orderByAsc("id");
        List<ClassStudent> classStudents = classStudentMapper.selectList(classStudentQuery);

        if (classStudents.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该班级暂无学生，请先添加学生");
        }

        // 获取学生ID列表
        List<Long> studentIds = classStudents.stream()
                .map(ClassStudent::getStudentId)
                .collect(Collectors.toList());

        // 查询学生详细信息
        List<Student> students = studentMapper.selectBatchIds(studentIds);

        // 按照班级学生的顺序排序学生信息
        students.sort(Comparator.comparing(student ->
            studentIds.indexOf(student.getId())));

        // 查询课程的所有考核点，按考核点编号排序
        QueryWrapper<AssessmentPoint> assessmentPointQuery = new QueryWrapper<>();
        assessmentPointQuery.eq("course_id", teachingClass.getCourseId());
        assessmentPointQuery.orderByAsc("point_code");
        List<AssessmentPoint> assessmentPoints = assessmentPointMapper.selectList(assessmentPointQuery);

        if (assessmentPoints.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该课程暂无考核点，请先配置考核点");
        }

        // 构建Excel数据
        List<GradeEntryExcelData> excelDataList = new ArrayList<>();
        for (Student student : students) {
            // 创建成绩数组，长度为考核点数量，初始值为null
            Double[] scores = new Double[assessmentPoints.size()];
            // 保持数组为null，表示待填写

            GradeEntryExcelData excelData = new GradeEntryExcelData(
                    student.getStudentNo(),
                    student.getName(),
                    scores
            );
            excelDataList.add(excelData);
        }

        // 生成Excel文件名
        String fileName = generateFileName(course, teachingClass, schoolYear, assessmentPoints.size());

        // 设置响应头
        setResponseHeaders(response, fileName);

        // 使用自定义表头写入Excel
        writeExcelWithCustomHeaders(response, excelDataList, assessmentPoints, fileName);

        log.info("成功生成成绩录入模板：班级ID={}, 课程名称={}, 学生数={}, 考核点数={}",
                classId, course.getCourseName(), students.size(), assessmentPoints.size());
    }

    /**
     * 生成Excel文件名
     */
    private String generateFileName(Course course, TeachingClass teachingClass,
                                    SysDictSchoolYear schoolYear, int pointCount) {
        String fileName = String.format("%s_%s_%s_成绩录入模板_%d个考核点.xlsx",
                schoolYear.getYearName(),
                course.getCourseName(),
                teachingClass.getClassName(),
                pointCount);
        return fileName;
    }

    /**
     * 设置响应头
     */
    private void setResponseHeaders(HttpServletResponse response, String fileName) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // URL编码文件名，支持中文
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);
    }

    /**
     * 使用自定义表头写入Excel
     */
    private void writeExcelWithCustomHeaders(HttpServletResponse response,
                                             List<GradeEntryExcelData> excelDataList,
                                             List<AssessmentPoint> assessmentPoints,
                                             String fileName) {
        try (OutputStream out = response.getOutputStream()) {
            // 创建动态表头
            List<List<String>> headers = new ArrayList<>();

            // 添加固定列头
            headers.add(List.of("学号"));
            headers.add(List.of("姓名"));

            // 添加考核点列头
            for (AssessmentPoint point : assessmentPoints) {
                String headerText = String.format("%s-%s\n满分:%s",
                        point.getPointCode(),
                        point.getPointName(),
                        point.getFullScore().toString());
                headers.add(List.of(headerText));
            }

            // 转换数据为List<List<Object>>格式
            List<List<Object>> dataList = new ArrayList<>();
            for (GradeEntryExcelData excelData : excelDataList) {
                List<Object> row = new ArrayList<>();
                row.add(excelData.getStudentNo());
                row.add(excelData.getName());

                // 添加考核点成绩（初始为空）
                if (excelData.getAssessmentScores() != null) {
                    for (Double score : excelData.getAssessmentScores()) {
                        row.add(score); // 可以为null
                    }
                } else {
                    for (int i = 0; i < assessmentPoints.size(); i++) {
                        row.add(null);
                    }
                }

                dataList.add(row);
            }

            // 使用EasyExcel写入
            EasyExcel.write(out)
                    .head(headers)
                    .sheet("成绩录入")
                    .doWrite(dataList);

        } catch (IOException e) {
            log.error("生成Excel文件失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成Excel文件失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GradeImportResultVO importGrades(GradeEntryImportRequest request) {
        GradeImportResultVO result = new GradeImportResultVO();
        List<String> errorMessages = new ArrayList<>();
        List<String> warningMessages = new ArrayList<>();

        try {
            // 参数校验
            if (request == null || request.getClassId() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
            }

            if (!StringUtils.hasText(request.getExcelFile())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel文件不能为空");
            }

            Long classId = request.getClassId();

            // 归属校验：仅本班主讲教师（admin 放行）可导入成绩
            ownershipHelper.checkClassOwnership(classId);

            // 查询教学班级信息
            TeachingClass teachingClass = teachingClassMapper.selectById(classId);
            if (teachingClass == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
            }

            // 查询课程信息
            Course course = courseMapper.selectById(teachingClass.getCourseId());
            if (course == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
            }

            // 查询班级的所有学生
            QueryWrapper<ClassStudent> classStudentQuery = new QueryWrapper<>();
            classStudentQuery.eq("teaching_class_id", classId);
            List<ClassStudent> classStudents = classStudentMapper.selectList(classStudentQuery);

            if (classStudents.isEmpty()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "该班级暂无学生");
            }

            // 创建学号到学生的映射
            Map<String, Student> studentMap = new HashMap<>();
            List<Long> studentIds = classStudents.stream()
                    .map(ClassStudent::getStudentId)
                    .collect(Collectors.toList());
            List<Student> students = studentMapper.selectBatchIds(studentIds);
            for (Student student : students) {
                studentMap.put(student.getStudentNo(), student);
            }

            // 查询课程的所有考核点
            QueryWrapper<AssessmentPoint> assessmentPointQuery = new QueryWrapper<>();
            assessmentPointQuery.eq("course_id", teachingClass.getCourseId());
            assessmentPointQuery.orderByAsc("point_code");
            List<AssessmentPoint> assessmentPoints = assessmentPointMapper.selectList(assessmentPointQuery);

            if (assessmentPoints.isEmpty()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "该课程暂无考核点");
            }

            // 解析Base64编码的Excel文件
            byte[] fileBytes = Base64.getDecoder().decode(request.getExcelFile());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);

            // 存储解析结果
            List<StudentScore> scoresToSave = new ArrayList<>();
            Set<String> processedStudents = new HashSet<>();

            // 解析Excel文件
            EasyExcel.read(inputStream, new PageReadListener<Map<Integer, String>>(dataList -> {
                for (Map<Integer, String> data : dataList) {
                    try {
                        // 解析学号和姓名
                        String studentNo = data.get(0);
                        String studentName = data.get(1);

                        if (!StringUtils.hasText(studentNo)) {
                            errorMessages.add("第" + (dataList.indexOf(data) + 1) + "行：学号为空");
                            continue;
                        }

                        // 验证学生是否存在
                        Student student = studentMap.get(studentNo.trim());
                        if (student == null) {
                            errorMessages.add("学号" + studentNo + "不存在于该班级");
                            continue;
                        }

                        // 验证姓名是否匹配
                        if (!student.getName().equals(studentName.trim())) {
                            warningMessages.add("学号" + studentNo + "的姓名不匹配，期望：" + student.getName() + "，实际：" + studentName);
                        }

                        // 解析考核点成绩
                        for (int i = 0; i < assessmentPoints.size(); i++) {
                            AssessmentPoint point = assessmentPoints.get(i);
                            String scoreStr = data.get(i + 2); // 前两列是学号和姓名

                            if (StringUtils.hasText(scoreStr)) {
                                try {
                                    BigDecimal score = new BigDecimal(scoreStr.trim());

                                    // 验证分数是否超过满分
                                    if (score.compareTo(point.getFullScore()) > 0) {
                                        errorMessages.add("学号" + studentNo + "在考核点" + point.getPointCode() + "的得分" + score + "超过满分" + point.getFullScore());
                                        continue;
                                    }

                                    if (score.compareTo(BigDecimal.ZERO) < 0) {
                                        errorMessages.add("学号" + studentNo + "在考核点" + point.getPointCode() + "的得分不能为负数");
                                        continue;
                                    }

                                    // 创建成绩记录
                                    StudentScore studentScore = new StudentScore();
                                    studentScore.setClassId(classId);
                                    studentScore.setStudentId(student.getId());
                                    studentScore.setPointId(point.getId());
                                    studentScore.setActualScore(score);

                                    scoresToSave.add(studentScore);

                                } catch (NumberFormatException e) {
                                    errorMessages.add("学号" + studentNo + "在考核点" + point.getPointCode() + "的得分格式错误：" + scoreStr);
                                }
                            }
                        }

                        processedStudents.add(studentNo);

                    } catch (Exception e) {
                        errorMessages.add("第" + (dataList.indexOf(data) + 1) + "行数据解析失败：" + e.getMessage());
                    }
                }
            })).sheet().doRead();

            // 有错误时直接返回，避免清空旧成绩后只写入部分新数据
            if (!errorMessages.isEmpty()) {
                result.setSuccess(false);
                result.setStudentCount(processedStudents.size());
                result.setScoreCount(scoresToSave.size());
                result.setErrorMessages(errorMessages);
                result.setWarningMessages(warningMessages);
                return result;
            }

            // 删除该班级原有的成绩数据
            studentScoreMapper.deleteByClassIdPhysically(classId);

            // 批量保存新成绩
            if (!scoresToSave.isEmpty()) {
                for (StudentScore score : scoresToSave) {
                    studentScoreMapper.insert(score);
                }
            }

            // 检查是否有学生未被导入
            List<String> missingStudents = students.stream()
                    .map(Student::getStudentNo)
                    .filter(no -> !processedStudents.contains(no))
                    .collect(Collectors.toList());

            if (!missingStudents.isEmpty()) {
                warningMessages.add("以下学生未导入成绩：" + String.join("、", missingStudents));
            }

            // 设置返回结果
            result.setSuccess(errorMessages.isEmpty());
            result.setStudentCount(processedStudents.size());
            result.setScoreCount(scoresToSave.size());
            result.setErrorMessages(errorMessages);
            result.setWarningMessages(warningMessages);

            log.info("成绩导入完成：班级ID={}, 导入学生数={}, 导入成绩记录数={}, 错误数={}, 警告数={}",
                    classId, processedStudents.size(), scoresToSave.size(), errorMessages.size(), warningMessages.size());

        } catch (Exception e) {
            log.error("成绩导入失败", e);
            result.setSuccess(false);
            errorMessages.add("成绩导入失败：" + e.getMessage());
            result.setErrorMessages(errorMessages);
        }

        return result;
    }

    @Override
    public Page<StudentScoreVO> queryGrades(GradeEntryQueryRequest request) {
        // 参数校验
        if (request == null || request.getClassId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        Long classId = request.getClassId();
        long current = request.getCurrent() != null ? request.getCurrent() : 1;
        long size = request.getPageSize() != null ? request.getPageSize() : 10;

        // 查询教学班级信息
        TeachingClass teachingClass = teachingClassMapper.selectById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        // 构建查询条件
        QueryWrapper<StudentScore> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teaching_class_id", classId);

        if (request.getStudentId() != null) {
            queryWrapper.eq("student_id", request.getStudentId());
        }

        if (request.getPointId() != null) {
            queryWrapper.eq("point_id", request.getPointId());
        }

        queryWrapper.orderByAsc("student_id", "point_id");

        // 分页查询成绩数据
        Page<StudentScore> page = studentScoreMapper.selectPage(new Page<>(current, size), queryWrapper);

        // 转换为VO
        Page<StudentScoreVO> voPage = new Page<>(current, size, page.getTotal());
        List<StudentScoreVO> voList = new ArrayList<>();

        if (!page.getRecords().isEmpty()) {
            // 获取学生ID和考核点ID
            Set<Long> studentIds = new HashSet<>();
            Set<Long> pointIds = new HashSet<>();

            for (StudentScore score : page.getRecords()) {
                studentIds.add(score.getStudentId());
                pointIds.add(score.getPointId());
            }

            // 批量查询学生和考核点信息
            Map<Long, Student> studentMap = studentMapper.selectBatchIds(studentIds).stream()
                    .collect(Collectors.toMap(Student::getId, s -> s));
            Map<Long, AssessmentPoint> assessmentPointMap = assessmentPointMapper.selectBatchIds(pointIds).stream()
                    .collect(Collectors.toMap(AssessmentPoint::getId, p -> p));

            // 构建VO列表
            for (StudentScore score : page.getRecords()) {
                StudentScoreVO vo = new StudentScoreVO();
                BeanUtils.copyProperties(score, vo);
                vo.setScore(score.getActualScore());

                Student student = studentMap.get(score.getStudentId());
                if (student != null) {
                    vo.setStudentNo(student.getStudentNo());
                    vo.setName(student.getName());
                }

                AssessmentPoint point = assessmentPointMap.get(score.getPointId());
                if (point != null) {
                    vo.setPointCode(point.getPointCode());
                    vo.setPointName(point.getPointName());
                    vo.setFullScore(point.getFullScore());
                }

                voList.add(vo);
            }
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateGrades(StudentScoreUpdateRequest request) {
        // 参数校验
        if (request == null || request.getClassId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        if (request.getScores() == null || request.getScores().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "成绩数据不能为空");
        }

        Long classId = request.getClassId();

        // 归属校验：仅本班主讲教师（admin 放行）可修改成绩
        ownershipHelper.checkClassOwnership(classId);

        // 查询教学班级和课程信息
        TeachingClass teachingClass = teachingClassMapper.selectById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        Course course = courseMapper.selectById(teachingClass.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }

        // 查询所有考核点
        QueryWrapper<AssessmentPoint> assessmentPointQuery = new QueryWrapper<>();
        assessmentPointQuery.eq("course_id", teachingClass.getCourseId());
        List<AssessmentPoint> assessmentPoints = assessmentPointMapper.selectList(assessmentPointQuery);

        // 创建考核点映射
        Map<Long, AssessmentPoint> assessmentPointMap = assessmentPoints.stream()
                .collect(Collectors.toMap(AssessmentPoint::getId, p -> p));

        // 查询当前教学班已绑定的学生，用于校验手工录分归属
        QueryWrapper<ClassStudent> classStudentQuery = new QueryWrapper<>();
        classStudentQuery.eq("teaching_class_id", classId);
        Set<Long> classStudentIds = classStudentMapper.selectList(classStudentQuery).stream()
                .map(ClassStudent::getStudentId)
                .collect(Collectors.toSet());

        // 处理每个成绩项
        for (StudentScoreUpdateRequest.ScoreItem scoreItem : request.getScores()) {
            // 参数校验
            if (scoreItem.getStudentId() == null || scoreItem.getPointId() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "学生ID和考核点ID不能为空");
            }

            // 校验学生是否属于当前教学班
            if (!classStudentIds.contains(scoreItem.getStudentId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "学生不属于当前教学班");
            }

            // 验证考核点是否存在
            AssessmentPoint point = assessmentPointMap.get(scoreItem.getPointId());
            if (point == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "考核点不存在");
            }

            // 验证分数
            if (scoreItem.getScore() != null) {
                if (scoreItem.getScore().compareTo(point.getFullScore()) > 0) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "得分不能超过满分");
                }
                if (scoreItem.getScore().compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "得分不能为负数");
                }
            }

            // 查询是否已存在成绩记录
            QueryWrapper<StudentScore> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("teaching_class_id", classId);
            queryWrapper.eq("student_id", scoreItem.getStudentId());
            queryWrapper.eq("point_id", scoreItem.getPointId());
            StudentScore existingScore = studentScoreMapper.selectOne(queryWrapper);

            if (existingScore != null) {
                // 更新现有记录
                if (scoreItem.getScore() != null) {
                    existingScore.setActualScore(scoreItem.getScore());
                    studentScoreMapper.updateById(existingScore);
                }
            } else {
                // 插入新记录
                if (scoreItem.getScore() != null) {
                    StudentScore newScore = new StudentScore();
                    newScore.setClassId(classId);
                    newScore.setStudentId(scoreItem.getStudentId());
                    newScore.setPointId(scoreItem.getPointId());
                    newScore.setActualScore(scoreItem.getScore());
                    studentScoreMapper.insert(newScore);
                }
            }
        }

        log.info("成绩更新完成：班级ID={}, 更新成绩数={}", classId, request.getScores().size());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteClassGrades(Long classId) {
        if (classId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        // 归属校验：仅本班主讲教师（admin 放行）可清空成绩
        ownershipHelper.checkClassOwnership(classId);

        TeachingClass teachingClass = teachingClassMapper.selectById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        int deletedCount = studentScoreMapper.deleteByClassIdPhysically(classId);
        log.info("删除班级成绩：班级ID={}, 删除记录数={}", classId, deletedCount);

        return true;
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                javax.servlet.http.HttpServletRequest request = attributes.getRequest();
                Object userObj = request.getAttribute("currentUser");
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
