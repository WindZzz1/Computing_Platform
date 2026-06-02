package com.yupi.springbootinit.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.listener.StudentScoreImportListener;
import com.yupi.springbootinit.mapper.*;
import com.yupi.springbootinit.model.dto.score.StudentScoreImportItem;
import com.yupi.springbootinit.model.entity.*;
import com.yupi.springbootinit.model.vo.ScoreImportResultVO;
import com.yupi.springbootinit.model.vo.ScorePreviewVO;
import com.yupi.springbootinit.model.vo.StudentVO;
import com.yupi.springbootinit.service.StudentScoreService;
import com.yupi.springbootinit.service.TeachingClassService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * 学生考核点成绩服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class StudentScoreServiceImpl extends ServiceImpl<StudentScoreMapper, StudentScore>
        implements StudentScoreService {

    @Resource
    private StudentScoreMapper studentScoreMapper;

    @Resource
    private TeachingClassService teachingClassService;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private AssessmentPointMapper assessmentPointMapper;

    @Resource
    private SysDictSchoolYearMapper sysDictSchoolYearMapper;

    @Resource
    private ClassStudentMapper classStudentMapper;

    @Resource
    private StudentMapper studentMapper;

    /**
     * 生成成绩录入模板（动态表头）
     */
    @Override
    public byte[] generateScoreTemplate(Long classId) {
        // 1. 校验教学班级存在性
        if (classId == null || classId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        TeachingClass teachingClass = teachingClassService.getById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        // 2. 获取课程信息
        Course course = courseMapper.selectById(teachingClass.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }

        // 3. 获取学期信息
        String semesterInfo = "";
        if (teachingClass.getTermId() != null) {
            SysDictSchoolYear schoolYear = sysDictSchoolYearMapper.selectById(teachingClass.getTermId());
            if (schoolYear != null) {
                semesterInfo = schoolYear.getYearName() + " " + schoolYear.getSemesterName();
            }
        }

        // 4. 获取该课程的所有考核点（按point_code排序）
        QueryWrapper<AssessmentPoint> pointQueryWrapper = new QueryWrapper<>();
        pointQueryWrapper.eq("course_id", teachingClass.getCourseId());
        pointQueryWrapper.orderByAsc("point_code");
        List<AssessmentPoint> assessmentPoints = assessmentPointMapper.selectList(pointQueryWrapper);

        if (assessmentPoints == null || assessmentPoints.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该课程暂无考核点，请先配置考核点");
        }

        // 5. 获取班级学生列表
        QueryWrapper<ClassStudent> classStudentQueryWrapper = new QueryWrapper<>();
        classStudentQueryWrapper.eq("teaching_class_id", classId);
        List<ClassStudent> classStudents = classStudentMapper.selectList(classStudentQueryWrapper);

        if (classStudents == null || classStudents.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该班级暂无学生，请先添加学生");
        }

        List<StudentVO> students = new ArrayList<>();
        for (ClassStudent classStudent : classStudents) {
            Student student = studentMapper.selectById(classStudent.getStudentId());
            if (student != null) {
                StudentVO studentVO = new StudentVO();
                BeanUtils.copyProperties(student, studentVO);
                students.add(studentVO);
            }
        }

        // 按学号排序
        students.sort(Comparator.comparing(StudentVO::getStudentNo));

        // 6. 构建两行复合表头
        List<List<String>> headers = new ArrayList<>();

        // 构建课程信息字符串（第一行所有列合并显示）
        String courseInfo = String.format("课程：%s | 班级：%s | 学期：%s",
                course.getCourseName(),
                teachingClass.getClassName(),
                StringUtils.isNotBlank(semesterInfo) ? semesterInfo : "未设置");

        // 固定列：学号、姓名
        List<String> studentNoHeader = new ArrayList<>();
        studentNoHeader.add(courseInfo);
        studentNoHeader.add("学号");
        headers.add(studentNoHeader);

        List<String> studentNameHeader = new ArrayList<>();
        studentNameHeader.add(courseInfo);
        studentNameHeader.add("姓名");
        headers.add(studentNameHeader);

        // 动态考核点列
        for (AssessmentPoint point : assessmentPoints) {
            List<String> pointHeader = new ArrayList<>();
            pointHeader.add(courseInfo);
            String columnHeader = String.format("%s %s(满分%s)",
                    point.getPointCode(),
                    point.getPointName(),
                    point.getFullScore() != null ? point.getFullScore().toString() : "0");
            pointHeader.add(columnHeader);
            headers.add(pointHeader);
        }

        // 7. 预填充学生数据
        List<List<Object>> dataList = new ArrayList<>();

        for (StudentVO student : students) {
            List<Object> row = new ArrayList<>();
            row.add(student.getStudentNo());
            row.add(student.getName());

            // 考核点列留空（待教师填写）
            for (int i = 0; i < assessmentPoints.size(); i++) {
                row.add("");
            }

            dataList.add(row);
        }

        // 8. 使用EasyExcel动态生成并返回字节数组
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            EasyExcel.write(outputStream)
                    .head(headers)
                    .sheet("成绩录入")
                    .doWrite(dataList);

            byte[] result = outputStream.toByteArray();
            log.info("生成成绩录入模板成功，班级ID：{}，文件大小：{} bytes", classId, result.length);
            return result;
        } catch (Exception e) {
            log.error("生成成绩录入模板失败，班级ID：{}", classId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成模板失败： " + e.getMessage());
        }
    }

    /**
     * 导入学生成绩
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScoreImportResultVO importScores(Long classId, MultipartFile file) {
        // 1. 校验教学班级存在性
        if (classId == null || classId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }

        TeachingClass teachingClass = teachingClassService.getById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        // 1.5 检查是否已锁定
        if (teachingClass.getLockedStatus() != null && teachingClass.getLockedStatus() == 1) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "成绩已锁定，无法导入。如需重新导入，请先联系教务管理员解锁");
        }

        // 2. 获取课程的考核点信息（按point_code排序）
        QueryWrapper<AssessmentPoint> pointQueryWrapper = new QueryWrapper<>();
        pointQueryWrapper.eq("course_id", teachingClass.getCourseId());
        pointQueryWrapper.orderByAsc("point_code");
        List<AssessmentPoint> assessmentPoints = assessmentPointMapper.selectList(pointQueryWrapper);

        if (assessmentPoints == null || assessmentPoints.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该课程暂无考核点，请先配置考核点");
        }

        // 3. 获取班级学生信息（用于校验）
        QueryWrapper<ClassStudent> classStudentQueryWrapper = new QueryWrapper<>();
        classStudentQueryWrapper.eq("teaching_class_id", classId);
        List<ClassStudent> classStudents = classStudentMapper.selectList(classStudentQueryWrapper);

        // 构建学生信息映射（学号 -> 学生信息）
        Map<String, Student> studentMap = new HashMap<>();
        for (ClassStudent classStudent : classStudents) {
            Student student = studentMapper.selectById(classStudent.getStudentId());
            if (student != null) {
                studentMap.put(student.getStudentNo(), student);
            }
        }

        // 4. 构建考核点列映射（列索引 -> 考核点ID）
        // 列索引：0=学号, 1=姓名, 2=第一个考核点, 3=第二个考核点...
        Map<Integer, String> pointColumnMapping = new HashMap<>();
        for (int i = 0; i < assessmentPoints.size(); i++) {
            int columnIndex = 2 + i; // 从第3列开始
            pointColumnMapping.put(columnIndex, assessmentPoints.get(i).getId().toString());
        }

        // 5. 解析Excel文件
        StudentScoreImportListener listener = new StudentScoreImportListener();
        listener.setPointColumnMapping(pointColumnMapping);

        try {
            EasyExcel.read(file.getInputStream(), listener).sheet().doRead();
        } catch (Exception e) {
            log.error("读取Excel文件失败，班级ID：{}", classId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取Excel文件失败： " + e.getMessage());
        }

        List<com.yupi.springbootinit.model.dto.score.StudentScoreImportItem> importItems = listener.getDataList();

        // 6. 校验并保存数据
        ScoreImportResultVO result = new ScoreImportResultVO();
        result.setClassId(classId);
        result.setTotal(importItems.size());
        result.setSuccessCount(0);
        result.setFailedCount(0);
        List<String> errorMessages = new ArrayList<>();

        // 先删除该班级的旧成绩数据
        studentScoreMapper.deleteByClassId(classId);

        for (com.yupi.springbootinit.model.dto.score.StudentScoreImportItem item : importItems) {
            try {
                // 校验学号和姓名
                String studentNo = item.getStudentNo();
                Student student = studentMap.get(studentNo);

                if (student == null) {
                    result.setFailedCount(result.getFailedCount() + 1);
                    errorMessages.add(String.format("第%d行：学号%s不存在或不在该班级", item.getRowNum(), studentNo));
                    continue;
                }

                // 校验姓名（可选，如果Excel中填了姓名则校验）
                String excelName = item.getStudentName();
                if (StringUtils.isNotBlank(excelName) && !excelName.equals(student.getName())) {
                    result.setFailedCount(result.getFailedCount() + 1);
                    errorMessages.add(String.format("第%d行：姓名不匹配，期望'%s'，实际'%s'",
                            item.getRowNum(), student.getName(), excelName));
                    continue;
                }

                // 保存考核点成绩
                Map<String, BigDecimal> scores = item.getScores();
                int savedCount = 0;

                for (Map.Entry<String, BigDecimal> entry : scores.entrySet()) {
                    String pointIdStr = entry.getKey();
                    BigDecimal actualScore = entry.getValue();

                    if (actualScore == null) {
                        continue;
                    }

                    // 查找对应的考核点
                    Long pointId = Long.parseLong(pointIdStr);
                    AssessmentPoint point = assessmentPoints.stream()
                            .filter(p -> p.getId().equals(pointId))
                            .findFirst()
                            .orElse(null);

                    if (point == null) {
                        continue;
                    }

                    // 校验得分是否超过满分
                    if (actualScore.compareTo(point.getFullScore()) > 0) {
                        result.setFailedCount(result.getFailedCount() + 1);
                        errorMessages.add(String.format("第%d行：%s %s得分%.1f超过满分%.1f",
                                item.getRowNum(), student.getStudentNo(), student.getName(),
                                actualScore, point.getFullScore()));
                        continue;
                    }

                    // 保存成绩
                    StudentScore studentScore = new StudentScore();
                    studentScore.setTeachingClassId(classId);
                    studentScore.setStudentId(student.getId());
                    studentScore.setPointId(pointId);
                    studentScore.setActualScore(actualScore);
                    studentScore.setFullScore(point.getFullScore());

                    if (studentScoreMapper.insert(studentScore) > 0) {
                        savedCount++;
                    }
                }

                if (savedCount > 0) {
                    result.setSuccessCount(result.getSuccessCount() + 1);
                }

            } catch (Exception e) {
                log.error("保存第{}行成绩失败", item.getRowNum(), e);
                result.setFailedCount(result.getFailedCount() + 1);
                errorMessages.add(String.format("第%d行：保存失败 - %s", item.getRowNum(), e.getMessage()));
            }
        }

        result.setErrorMessages(errorMessages);
        result.setAllSuccess(result.getFailedCount() == 0);

        log.info("成绩导入完成，班级ID：{}，总数：{}，成功：{}，失败：{}",
                classId, result.getTotal(), result.getSuccessCount(), result.getFailedCount());

        return result;
    }

    /**
     * 获取教学班级的成绩预览数据
     */
    @Override
    public List<ScorePreviewVO> getScoresByClass(Long classId) {
        // 1. 校验教学班级存在性
        if (classId == null || classId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        TeachingClass teachingClass = teachingClassService.getById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        // 2. 获取班级的所有学生
        QueryWrapper<ClassStudent> classStudentQueryWrapper = new QueryWrapper<>();
        classStudentQueryWrapper.eq("teaching_class_id", classId);
        List<ClassStudent> classStudents = classStudentMapper.selectList(classStudentQueryWrapper);

        if (classStudents == null || classStudents.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 获取课程的所有考核点
        QueryWrapper<AssessmentPoint> pointQueryWrapper = new QueryWrapper<>();
        pointQueryWrapper.eq("course_id", teachingClass.getCourseId());
        pointQueryWrapper.orderByAsc("point_code");
        List<AssessmentPoint> assessmentPoints = assessmentPointMapper.selectList(pointQueryWrapper);

        // 4. 构建成绩预览数据
        List<ScorePreviewVO> result = new ArrayList<>();

        for (ClassStudent classStudent : classStudents) {
            Student student = studentMapper.selectById(classStudent.getStudentId());
            if (student == null) {
                continue;
            }

            // 为每个学生的每个考核点创建一条记录
            for (AssessmentPoint point : assessmentPoints) {
                ScorePreviewVO vo = new ScorePreviewVO();
                vo.setStudentId(student.getId());
                vo.setStudentNo(student.getStudentNo());
                vo.setStudentName(student.getName());
                vo.setPointId(point.getId());
                vo.setPointCode(point.getPointCode());
                vo.setPointName(point.getPointName());
                vo.setFullScore(point.getFullScore());

                // 查询该学生该考核点的成绩
                QueryWrapper<StudentScore> scoreQueryWrapper = new QueryWrapper<>();
                scoreQueryWrapper.eq("teaching_class_id", classId);
                scoreQueryWrapper.eq("student_id", student.getId());
                scoreQueryWrapper.eq("point_id", point.getId());
                StudentScore studentScore = studentScoreMapper.selectOne(scoreQueryWrapper);

                if (studentScore != null) {
                    vo.setId(studentScore.getId());
                    vo.setActualScore(studentScore.getActualScore());
                    vo.setSubmitted(true);
                    vo.setCreateTime(studentScore.getCreateTime());
                    vo.setUpdateTime(studentScore.getUpdateTime());
                } else {
                    vo.setSubmitted(false);
                }

                result.add(vo);
            }
        }

        log.info("获取班级成绩预览成功，班级ID：{}，记录数：{}", classId, result.size());
        return result;
    }

    /**
     * 更新单条成绩记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateScore(Long id, BigDecimal actualScore) {
        // 1. 校验参数
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "成绩记录ID不能为空");
        }

        if (actualScore == null || actualScore.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "得分不能为空且不能为负数");
        }

        // 2. 查询成绩记录
        StudentScore studentScore = studentScoreMapper.selectById(id);
        if (studentScore == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "成绩记录不存在");
        }

        // 2.5 检查是否已锁定
        TeachingClass teachingClass = teachingClassService.getById(studentScore.getTeachingClassId());
        if (teachingClass != null && teachingClass.getLockedStatus() != null && teachingClass.getLockedStatus() == 1) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "成绩已锁定，无法修改。如需修改，请先联系教务管理员解锁");
        }

        // 3. 查询考核点信息，校验得分是否超过满分
        AssessmentPoint point = assessmentPointMapper.selectById(studentScore.getPointId());
        if (point == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "考核点不存在");
        }

        if (actualScore.compareTo(point.getFullScore()) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    String.format("得分%.1f超过满分%.1f", actualScore, point.getFullScore()));
        }

        // 4. 更新成绩
        studentScore.setActualScore(actualScore);

        boolean success = studentScoreMapper.updateById(studentScore) > 0;

        if (success) {
            log.info("更新成绩成功，记录ID：{}，新得分：{}", id, actualScore);
        }

        return success;
    }

    /**
     * 删除教学班级的所有成绩
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteScoresByClassId(Long classId) {
        if (classId == null || classId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        int count = studentScoreMapper.deleteByClassId(classId);

        log.info("删除教学班级成绩成功，班级ID：{}，删除记录数：{}", classId, count);
        return count >= 0;
    }
}
