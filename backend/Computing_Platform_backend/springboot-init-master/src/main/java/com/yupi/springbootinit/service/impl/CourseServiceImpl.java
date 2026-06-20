package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.CourseMapper;
import com.yupi.springbootinit.mapper.SysDictCollegeMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.model.dto.course.CourseAddRequest;
import com.yupi.springbootinit.model.dto.course.CourseImportRequest;
import com.yupi.springbootinit.model.dto.course.CourseQueryRequest;
import com.yupi.springbootinit.model.dto.course.CourseUpdateRequest;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.entity.SysDictCollege;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.excel.CourseExcel;
import com.yupi.springbootinit.model.vo.CourseSimpleVO;
import com.yupi.springbootinit.model.vo.CourseVO;
import com.yupi.springbootinit.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

/**
 * 课程服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private SysDictMajorMapper sysDictMajorMapper;

    @Resource
    private SysDictCollegeMapper sysDictCollegeMapper;

    @Override
    public Long createCourse(CourseAddRequest courseAddRequest) {
        if (courseAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String courseCode = courseAddRequest.getCourseCode();
        String courseName = courseAddRequest.getCourseName();
        String courseNature = courseAddRequest.getCourseNature();
        Double credit = courseAddRequest.getCredit();
        if (StringUtils.isAnyBlank(courseCode, courseName, courseNature) || credit == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程代码、名称、性质或学分不能为空");
        }
        Long majorId = courseAddRequest.getMajorId();
        if (majorId != null) {
            SysDictMajor major = sysDictMajorMapper.selectById(majorId);
            if (major == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "专业不存在");
            }
        }
        synchronized (courseCode.intern()) {
            QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_code", courseCode);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程代码已存在");
            }
            Course course = new Course();
            BeanUtils.copyProperties(courseAddRequest, course);
            boolean saveResult = this.save(course);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建课程失败");
            }
            return course.getId();
        }
    }

    @Override
    public Boolean updateCourse(CourseUpdateRequest courseUpdateRequest) {
        if (courseUpdateRequest == null || courseUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Course existCourse = this.getById(courseUpdateRequest.getId());
        if (existCourse == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }
        Long majorId = courseUpdateRequest.getMajorId();
        if (majorId != null) {
            SysDictMajor major = sysDictMajorMapper.selectById(majorId);
            if (major == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "专业不存在");
            }
        }
        String courseCode = courseUpdateRequest.getCourseCode();
        if (StringUtils.isNotBlank(courseCode) && !courseCode.equals(existCourse.getCourseCode())) {
            QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_code", courseCode);
            queryWrapper.ne("id", courseUpdateRequest.getId());
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程代码已存在");
            }
        }
        Course course = new Course();
        BeanUtils.copyProperties(courseUpdateRequest, course);
        return this.updateById(course);
    }

    @Override
    public Boolean deleteCourse(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID不能为空");
        }
        Course course = this.getById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }
        validateCourseNotReferenced(id);
        boolean result = this.removeById(id);
        return result;
    }

    private void validateCourseNotReferenced(Long courseId) {
        Long teachingClassCount = courseMapper.countTeachingClassByCourseId(courseId);
        if (teachingClassCount != null && teachingClassCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "课程已被教学班级引用，不能删除");
        }
        Long objectiveCount = courseMapper.countObjectiveByCourseId(courseId);
        if (objectiveCount != null && objectiveCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "课程已被课程目标引用，不能删除");
        }
        Long assessmentPointCount = courseMapper.countAssessmentPointByCourseId(courseId);
        if (assessmentPointCount != null && assessmentPointCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "课程已被考核点引用，不能删除");
        }
        Long matrixCount = courseMapper.countMatrixByCourseId(courseId);
        if (matrixCount != null && matrixCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "课程已被矩阵配置引用，不能删除");
        }
        Long weightCount = courseMapper.countWeightByCourseId(courseId);
        if (weightCount != null && weightCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "课程已被内部权重配置引用，不能删除");
        }
        Long courseResultCount = courseMapper.countCourseResultByCourseId(courseId);
        if (courseResultCount != null && courseResultCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "课程已被达成度结果引用，不能删除");
        }
    }

    @Override
    public CourseVO getCourseById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID不能为空");
        }
        Course course = this.getById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }
        return this.getCourseVO(course);
    }

    @Override
    public Page<CourseVO> pageCourse(CourseQueryRequest courseQueryRequest) {
        long current = courseQueryRequest.getCurrent();
        long size = courseQueryRequest.getPageSize();
        QueryWrapper<Course> queryWrapper = this.getQueryWrapper(courseQueryRequest);
        Page<Course> coursePage = this.page(new Page<>(current, size), queryWrapper);
        Page<CourseVO> courseVOPage = new Page<>(current, size, coursePage.getTotal());
        courseVOPage.setRecords(coursePage.getRecords().stream().map(this::getCourseVO).collect(java.util.stream.Collectors.toList()));
        return courseVOPage;
    }

    @Override
    public QueryWrapper<Course> getQueryWrapper(CourseQueryRequest courseQueryRequest) {
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        if (courseQueryRequest == null) {
            return queryWrapper;
        }
        String courseCode = courseQueryRequest.getCourseCode();
        String courseName = courseQueryRequest.getCourseName();
        String courseNature = courseQueryRequest.getCourseNature();
        Long majorId = courseQueryRequest.getMajorId();
        java.util.Date createTimeStart = courseQueryRequest.getCreateTimeStart();
        java.util.Date createTimeEnd = courseQueryRequest.getCreateTimeEnd();
        queryWrapper.like(StringUtils.isNotBlank(courseCode), "course_code", courseCode);
        queryWrapper.like(StringUtils.isNotBlank(courseName), "course_name", courseName);
        queryWrapper.eq(StringUtils.isNotBlank(courseNature), "course_nature", courseNature);
        queryWrapper.eq(majorId != null, "major_id", majorId);
        queryWrapper.ge(createTimeStart != null, "create_time", createTimeStart);
        queryWrapper.le(createTimeEnd != null, "create_time", createTimeEnd);
        queryWrapper.orderByAsc("course_code");
        return queryWrapper;
    }

    @Override
    public CourseVO getCourseVO(Course course) {
        if (course == null) {
            return null;
        }
        CourseVO courseVO = new CourseVO();
        BeanUtils.copyProperties(course, courseVO);
        if (course.getMajorId() != null) {
            SysDictMajor major = sysDictMajorMapper.selectById(course.getMajorId());
            if (major != null) {
                courseVO.setMajorName(major.getMajorName());
                courseVO.setCollegeId(major.getCollegeId());
                if (major.getCollegeId() != null) {
                    SysDictCollege college = sysDictCollegeMapper.selectById(major.getCollegeId());
                    if (college != null) {
                        courseVO.setCollegeName(college.getCollegeName());
                    }
                }
            }
        }
        return courseVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importCourses(CourseImportRequest courseImportRequest) {
        if (courseImportRequest == null || courseImportRequest.getCourses() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "导入数据不能为空");
        }

        List<CourseAddRequest> courses = courseImportRequest.getCourses();
        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failDetails = new ArrayList<>();

        for (int i = 0; i < courses.size(); i++) {
            CourseAddRequest request = courses.get(i);
            try {
                // 检查必填字段
                if (StringUtils.isAnyBlank(request.getCourseCode(), request.getCourseName(), request.getCourseNature())) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 1));
                    detail.put("courseCode", request.getCourseCode() != null ? request.getCourseCode() : "");
                    detail.put("reason", "必填字段为空");
                    failDetails.add(detail);
                    continue;
                }

                // 检查课程代码是否已存在
                QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("course_code", request.getCourseCode());
                long count = this.baseMapper.selectCount(queryWrapper);
                if (count > 0) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 1));
                    detail.put("courseCode", request.getCourseCode());
                    detail.put("reason", "课程代码已存在");
                    failDetails.add(detail);
                    continue;
                }

                // 创建课程
                Course course = new Course();
                BeanUtils.copyProperties(request, course);
                if (this.save(course)) {
                    successCount++;
                } else {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 1));
                    detail.put("courseCode", request.getCourseCode());
                    detail.put("reason", "保存失败");
                    failDetails.add(detail);
                }
            } catch (Exception e) {
                failCount++;
                Map<String, String> detail = new HashMap<>();
                detail.put("row", String.valueOf(i + 1));
                detail.put("courseCode", request.getCourseCode() != null ? request.getCourseCode() : "");
                detail.put("reason", e.getMessage());
                failDetails.add(detail);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", courses.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failDetails", failDetails);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importCoursesFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        // 验证文件类型
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式不正确，请上传Excel文件");
        }

        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failDetails = new ArrayList<>();

        try {
            // 读取Excel数据
            List<CourseExcel> courseExcels = com.alibaba.excel.EasyExcel.read(file.getInputStream())
                    .head(CourseExcel.class)
                    .sheet(0)
                    .doReadSync();

            if (courseExcels == null || courseExcels.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel中没有数据");
            }

            for (int i = 0; i < courseExcels.size(); i++) {
                CourseExcel excel = courseExcels.get(i);
                try {
                    // 检查必填字段
                    if (StringUtils.isAnyBlank(excel.getCourseCode(), excel.getCourseName(), excel.getCourseNature()) || excel.getCredit() == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2)); // Excel行号从2开始（第1行是表头）
                        detail.put("courseCode", excel.getCourseCode() != null ? excel.getCourseCode() : "");
                        detail.put("reason", "必填字段为空");
                        failDetails.add(detail);
                        continue;
                    }

                    // 校验课程性质
                    if (!"必修".equals(excel.getCourseNature()) && !"选修".equals(excel.getCourseNature())) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "课程性质只能是'必修'或'选修'");
                        failDetails.add(detail);
                        continue;
                    }

                    // 检查课程代码是否已存在
                    QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("course_code", excel.getCourseCode());
                    long count = this.baseMapper.selectCount(queryWrapper);
                    if (count > 0) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "课程代码已存在");
                        failDetails.add(detail);
                        continue;
                    }

                    // 处理专业代码
                    Long majorId = null;
                    if (StringUtils.isNotBlank(excel.getMajorCode())) {
                        QueryWrapper<com.yupi.springbootinit.model.entity.SysDictMajor> majorQueryWrapper = new QueryWrapper<>();
                        majorQueryWrapper.eq("major_code", excel.getMajorCode());
                        com.yupi.springbootinit.model.entity.SysDictMajor major = sysDictMajorMapper.selectOne(majorQueryWrapper);
                        if (major != null) {
                            majorId = major.getId();
                        } else {
                            failCount++;
                            Map<String, String> detail = new HashMap<>();
                            detail.put("row", String.valueOf(i + 2));
                            detail.put("courseCode", excel.getCourseCode());
                            detail.put("reason", "专业代码不存在");
                            failDetails.add(detail);
                            continue;
                        }
                    }

                    // 创建课程
                    Course course = new Course();
                    course.setCourseCode(excel.getCourseCode());
                    course.setCourseName(excel.getCourseName());
                    course.setCourseNature(excel.getCourseNature());
                    course.setCredit(excel.getCredit());
                    course.setMajorId(majorId);

                    if (this.save(course)) {
                        successCount++;
                    } else {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "保存失败");
                        failDetails.add(detail);
                    }
                } catch (Exception e) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 2));
                    detail.put("courseCode", excel.getCourseCode() != null ? excel.getCourseCode() : "");
                    detail.put("reason", e.getMessage());
                    failDetails.add(detail);
                }
            }

            // 原子导入：任一行失败则整批回滚，避免部分成功导致 successCount/failCount 与真实库不一致
            if (failCount > 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,
                        "Excel导入存在 " + failCount + " 条失败，已整体回滚，请修正后重新导入（详见返回的错误明细）");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", courseExcels.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("failDetails", failDetails);
            return result;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateCourseTemplate() {
        String templatePath = "/templates/course_template.xlsx";

        // 方式1: 使用 ClassPathResource
        try {
            ClassPathResource resource = new ClassPathResource("templates/course_template.xlsx");
            log.info("尝试读取静态模板文件: {}, 存在: {}", templatePath, resource.exists());

            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, bytesRead);
                    }
                    byte[] templateBytes = buffer.toByteArray();

                    // 验证文件魔数（xlsx本质是zip文件，前两字节应该是0x50 0x4B即"PK"）
                    if (templateBytes.length >= 2) {
                        String magicNumber = String.format("%02X%02X", templateBytes[0], templateBytes[1]);
                        log.info("静态模板读取成功，大小: {} bytes, 魔数: {}", templateBytes.length, magicNumber);
                        if (!"504B".equals(magicNumber)) {
                            log.error("文件魔数不正确，期望504B，实际: {}", magicNumber);
                        }
                    }
                    return templateBytes;
                }
            }
        } catch (IOException e) {
            log.warn("使用ClassPathResource读取模板失败: {}", e.getMessage());
        }

        // 方式2: 尝试使用 ClassLoader.getResourceAsStream
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/course_template.xlsx")) {
            if (inputStream != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                byte[] templateBytes = buffer.toByteArray();

                // 验证文件魔数
                if (templateBytes.length >= 2) {
                    String magicNumber = String.format("%02X%02X", templateBytes[0], templateBytes[1]);
                    log.info("使用ClassLoader读取静态模板成功，大小: {} bytes, 魔数: {}", templateBytes.length, magicNumber);
                }
                return templateBytes;
            }
        } catch (IOException e) {
            log.warn("使用ClassLoader读取模板失败: {}", e.getMessage());
        }

        // 静态模板不存在时，动态生成（只包含表头，不含示例数据）
        log.info("静态模板不存在，使用动态生成");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            com.alibaba.excel.EasyExcel.write(outputStream, CourseExcel.class)
                    .sheet("课程导入模板")
                    .doWrite(new ArrayList<>());
            byte[] result = outputStream.toByteArray();
            log.info("动态生成模板成功，大小: {} bytes", result.length);
            return result;
        } catch (Exception e) {
            log.error("动态生成模板失败", e);
            throw new com.yupi.springbootinit.exception.BusinessException(com.yupi.springbootinit.common.ErrorCode.SYSTEM_ERROR, "生成模板失败");
        }
    }

    @Override
    public List<CourseSimpleVO> listCourseSimple() {
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "course_code", "course_name");
        queryWrapper.orderByAsc("course_code");
        return this.list(queryWrapper).stream().map(course -> {
            CourseSimpleVO simpleVO = new CourseSimpleVO();
            simpleVO.setId(course.getId());
            simpleVO.setCourseCode(course.getCourseCode());
            simpleVO.setCourseName(course.getCourseName());
            return simpleVO;
        }).collect(java.util.stream.Collectors.toList());
    }
}
