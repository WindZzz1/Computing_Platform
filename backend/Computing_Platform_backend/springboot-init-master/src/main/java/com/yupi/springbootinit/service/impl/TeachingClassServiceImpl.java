package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.OwnershipHelper;
import com.yupi.springbootinit.mapper.*;
import com.yupi.springbootinit.model.dto.teachingClass.ClassStudentBindRequest;
import com.yupi.springbootinit.model.dto.teachingClass.TeachingClassAddRequest;
import com.yupi.springbootinit.model.dto.teachingClass.TeachingClassQueryRequest;
import com.yupi.springbootinit.model.dto.teachingClass.TeachingClassUpdateRequest;
import com.yupi.springbootinit.model.entity.*;
import com.yupi.springbootinit.model.excel.ClassStudentExcel;
import com.yupi.springbootinit.model.excel.TeachingClassExcel;
import com.yupi.springbootinit.model.vo.StudentVO;
import com.yupi.springbootinit.model.vo.TeachingClassVO;
import com.yupi.springbootinit.service.TeachingClassService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 教学班级服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class TeachingClassServiceImpl extends ServiceImpl<TeachingClassMapper, TeachingClass> implements TeachingClassService {

    @Resource
    private TeachingClassMapper teachingClassMapper;

    @Resource
    private ClassStudentMapper classStudentMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysDictSchoolYearMapper sysDictSchoolYearMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private SysDictMajorMapper sysDictMajorMapper;

    @Resource
    private SysDictCollegeMapper sysDictCollegeMapper;

    @Resource
    private OwnershipHelper ownershipHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTeachingClass(TeachingClassAddRequest teachingClassAddRequest) {
        if (teachingClassAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String className = teachingClassAddRequest.getClassName();
        Long courseId = teachingClassAddRequest.getCourseId();
        Long teacherId = teachingClassAddRequest.getTeacherId();
        Long termId = teachingClassAddRequest.getTermId();

        if (StringUtils.isAnyBlank(className) || courseId == null || teacherId == null || termId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "班级名称、课程、教师、学年学期不能为空");
        }

        // 校验课程存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }

        // 校验教师存在且是教师角色
        SysUser teacher = sysUserMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教师不存在");
        }

        // 校验学年学期存在
        SysDictSchoolYear schoolYear = sysDictSchoolYearMapper.selectById(termId);
        if (schoolYear == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学年学期不存在");
        }

        // 检查是否已存在相同课程、教师、学年学期的班级
        QueryWrapper<TeachingClass> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        queryWrapper.eq("teacher_id", teacherId);
        queryWrapper.eq("term_id", termId);
        Long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该课程、教师、学期组合已存在教学班级");
        }

        TeachingClass teachingClass = new TeachingClass();
        BeanUtils.copyProperties(teachingClassAddRequest, teachingClass);
        boolean saveResult = this.save(teachingClass);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建教学班级失败");
        }
        return teachingClass.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTeachingClass(TeachingClassUpdateRequest teachingClassUpdateRequest) {
        if (teachingClassUpdateRequest == null || teachingClassUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        TeachingClass existClass = this.getById(teachingClassUpdateRequest.getId());
        if (existClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        // 检查是否修改了课程、教师、学年学期
        Long courseId = teachingClassUpdateRequest.getCourseId() != null ? teachingClassUpdateRequest.getCourseId() : existClass.getCourseId();
        Long teacherId = teachingClassUpdateRequest.getTeacherId() != null ? teachingClassUpdateRequest.getTeacherId() : existClass.getTeacherId();
        Long termId = teachingClassUpdateRequest.getTermId() != null ? teachingClassUpdateRequest.getTermId() : existClass.getTermId();

        // 校验课程存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }

        // 校验学年学期存在
        if (teachingClassUpdateRequest.getTermId() != null) {
            SysDictSchoolYear schoolYear = sysDictSchoolYearMapper.selectById(termId);
            if (schoolYear == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学年学期不存在");
            }
        }

        // 检查是否已存在相同课程、教师、学年学期的班级（排除自己）
        QueryWrapper<TeachingClass> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        queryWrapper.eq("teacher_id", teacherId);
        queryWrapper.eq("term_id", termId);
        queryWrapper.ne("id", teachingClassUpdateRequest.getId());
        Long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该课程、教师、学期组合已存在教学班级");
        }

        TeachingClass teachingClass = new TeachingClass();
        BeanUtils.copyProperties(teachingClassUpdateRequest, teachingClass);
        return this.updateById(teachingClass);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTeachingClass(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        // 删除班级学生关联
        QueryWrapper<ClassStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teaching_class_id", id);
        classStudentMapper.delete(queryWrapper);

        boolean result = this.removeById(id);
        return result;
    }

    @Override
    public TeachingClassVO getTeachingClassById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }
        TeachingClass teachingClass = this.getById(id);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }
        return this.getTeachingClassVO(teachingClass);
    }

    @Override
    public Page<TeachingClassVO> pageTeachingClass(TeachingClassQueryRequest teachingClassQueryRequest) {
        long current = teachingClassQueryRequest.getCurrent();
        long size = teachingClassQueryRequest.getPageSize();
        QueryWrapper<TeachingClass> queryWrapper = this.getQueryWrapper(teachingClassQueryRequest);
        Page<TeachingClass> classPage = this.page(new Page<>(current, size), queryWrapper);
        Page<TeachingClassVO> classVOPage = new Page<>(current, size, classPage.getTotal());
        classVOPage.setRecords(classPage.getRecords().stream().map(this::getTeachingClassVO).collect(Collectors.toList()));
        return classVOPage;
    }

    @Override
    public QueryWrapper<TeachingClass> getQueryWrapper(TeachingClassQueryRequest teachingClassQueryRequest) {
        QueryWrapper<TeachingClass> queryWrapper = new QueryWrapper<>();
        if (teachingClassQueryRequest == null) {
            return queryWrapper;
        }
        String className = teachingClassQueryRequest.getClassName();
        Long courseId = teachingClassQueryRequest.getCourseId();
        Long teacherId = teachingClassQueryRequest.getTeacherId();
        Long termId = teachingClassQueryRequest.getTermId();

        queryWrapper.like(StringUtils.isNotBlank(className), "class_name", className);
        queryWrapper.eq(courseId != null, "course_id", courseId);
        queryWrapper.eq(teacherId != null, "teacher_id", teacherId);
        queryWrapper.eq(termId != null, "term_id", termId);
        queryWrapper.orderByDesc("create_time");
        return queryWrapper;
    }

    @Override
    public TeachingClassVO getTeachingClassVO(TeachingClass teachingClass) {
        if (teachingClass == null) {
            return null;
        }
        TeachingClassVO teachingClassVO = new TeachingClassVO();
        BeanUtils.copyProperties(teachingClass, teachingClassVO);

        // 获取课程信息
        if (teachingClass.getCourseId() != null) {
            Course course = courseMapper.selectById(teachingClass.getCourseId());
            if (course != null) {
                teachingClassVO.setCourseCode(course.getCourseCode());
                teachingClassVO.setCourseName(course.getCourseName());
            }
        }

        // 获取教师信息
        if (teachingClass.getTeacherId() != null) {
            SysUser teacher = sysUserMapper.selectById(teachingClass.getTeacherId());
            if (teacher != null) {
                teachingClassVO.setTeacherName(teacher.getUsername());
            }
        }

        // 获取学年学期信息
        if (teachingClass.getTermId() != null) {
            SysDictSchoolYear schoolYear = sysDictSchoolYearMapper.selectById(teachingClass.getTermId());
            if (schoolYear != null) {
                teachingClassVO.setYearName(schoolYear.getYearName());
                teachingClassVO.setSemesterName(schoolYear.getSemesterName());
            }
        }

        // 获取学生数量
        QueryWrapper<ClassStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teaching_class_id", teachingClass.getId());
        Long studentCount = classStudentMapper.selectCount(queryWrapper);
        teachingClassVO.setStudentCount(studentCount.intValue());

        return teachingClassVO;
    }

    @Override
    public List<TeachingClassVO> listMyTeachingClasses() {
        SysUser currentUser = ownershipHelper.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        QueryWrapper<TeachingClass> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_id", currentUser.getId());
        queryWrapper.orderByDesc("create_time");
        return this.list(queryWrapper).stream()
                .map(this::getTeachingClassVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer bindStudents(ClassStudentBindRequest classStudentBindRequest) {
        if (classStudentBindRequest == null || classStudentBindRequest.getClassId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        Long classId = classStudentBindRequest.getClassId();
        TeachingClass teachingClass = this.getById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        List<Long> studentIds = classStudentBindRequest.getStudentIds();
        if (studentIds == null || studentIds.isEmpty()) {
            return 0;
        }

        int bindCount = 0;
        for (Long studentId : studentIds) {
            // 检查学生是否存在
            Student student = studentMapper.selectById(studentId);
            if (student == null) {
                continue;
            }

            // 检查是否已绑定
            if (bindOrRestoreStudent(classId, studentId, teachingClass.getClassName())) {
                bindCount++;
            }
        }

        return bindCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unbindStudent(Long classId, Long studentId) {
        if (classId == null || studentId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID或学生ID不能为空");
        }

        QueryWrapper<ClassStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teaching_class_id", classId);
        queryWrapper.eq("student_id", studentId);

        boolean result = classStudentMapper.delete(queryWrapper) > 0;

        // 解绑后：检查学生是否还绑定在其他教学班，若未绑定则清除 className（使用 UpdateWrapper 强制设 null 避免 NOT_NULL 策略跳过）
        if (result) {
            QueryWrapper<ClassStudent> remainingWrapper = new QueryWrapper<>();
            remainingWrapper.eq("student_id", studentId);
            if (classStudentMapper.selectCount(remainingWrapper) == 0) {
                UpdateWrapper<Student> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", studentId).set("class_name", null);
                studentMapper.update(null, updateWrapper);
            }
        }

        return result;
    }

    @Override
    public List<StudentVO> getClassStudents(Long classId) {
        if (classId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        QueryWrapper<ClassStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teaching_class_id", classId);
        List<ClassStudent> classStudents = classStudentMapper.selectList(queryWrapper);

        List<StudentVO> studentVOList = new ArrayList<>();
        for (ClassStudent classStudent : classStudents) {
            Student student = studentMapper.selectById(classStudent.getStudentId());
            if (student != null) {
                StudentVO studentVO = new StudentVO();
                BeanUtils.copyProperties(student, studentVO);

                // 获取专业信息
                if (student.getMajorId() != null) {
                    SysDictMajor major = sysDictMajorMapper.selectById(student.getMajorId());
                    if (major != null) {
                        studentVO.setMajorName(major.getMajorName());
                        // 获取学院信息
                        if (major.getCollegeId() != null) {
                            SysDictCollege college = sysDictCollegeMapper.selectById(major.getCollegeId());
                            if (college != null) {
                                studentVO.setCollegeName(college.getCollegeName());
                                studentVO.setCollegeId(college.getId());
                            }
                        }
                    }
                }

                studentVOList.add(studentVO);
            }
        }

        return studentVOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.Map<String, Object> importStudents(com.yupi.springbootinit.model.dto.teachingClass.ClassStudentImportRequest classStudentImportRequest) {
        if (classStudentImportRequest == null || classStudentImportRequest.getClassId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        Long classId = classStudentImportRequest.getClassId();
        TeachingClass teachingClass = this.getById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }

        java.util.List<com.yupi.springbootinit.model.dto.teachingClass.ClassStudentImportRequest.StudentItem> students = classStudentImportRequest.getStudents();
        if (students == null || students.isEmpty()) {
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("totalCount", 0);
            result.put("successCount", 0);
            result.put("failCount", 0);
            result.put("failDetails", new java.util.ArrayList<>());
            return result;
        }

        int successCount = 0;
        int failCount = 0;
        java.util.List<java.util.Map<String, String>> failDetails = new java.util.ArrayList<>();

        for (int i = 0; i < students.size(); i++) {
            com.yupi.springbootinit.model.dto.teachingClass.ClassStudentImportRequest.StudentItem item = students.get(i);
            try {
                // 检查必填字段
                if (StringUtils.isAnyBlank(item.getStudentNo())) {
                    failCount++;
                    java.util.Map<String, String> detail = new java.util.HashMap<>();
                    detail.put("row", String.valueOf(i + 1));
                    detail.put("studentNo", item.getStudentNo() != null ? item.getStudentNo() : "");
                    detail.put("reason", "学号不能为空");
                    failDetails.add(detail);
                    continue;
                }

                // 根据学号查询学生
                QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
                studentQueryWrapper.eq("student_no", item.getStudentNo());
                Student student = studentMapper.selectOne(studentQueryWrapper);
                if (student == null) {
                    failCount++;
                    java.util.Map<String, String> detail = new java.util.HashMap<>();
                    detail.put("row", String.valueOf(i + 1));
                    detail.put("studentNo", item.getStudentNo());
                    detail.put("reason", "学生不存在");
                    failDetails.add(detail);
                    continue;
                }

                // 验证姓名（可选）
                if (StringUtils.isNotBlank(item.getStudentName()) && !item.getStudentName().equals(student.getName())) {
                    failCount++;
                    java.util.Map<String, String> detail = new java.util.HashMap<>();
                    detail.put("row", String.valueOf(i + 1));
                    detail.put("studentNo", item.getStudentNo());
                    detail.put("reason", "姓名不匹配，期望：" + student.getName());
                    failDetails.add(detail);
                    continue;
                }

                // 检查是否已绑定
                if (!bindOrRestoreStudent(classId, student.getId(), teachingClass.getClassName())) {
                    failCount++;
                    java.util.Map<String, String> detail = new java.util.HashMap<>();
                    detail.put("row", String.valueOf(i + 1));
                    detail.put("studentNo", item.getStudentNo());
                    detail.put("reason", "学生已在班级中");
                    failDetails.add(detail);
                    continue;
                }
                successCount++;
            } catch (Exception e) {
                failCount++;
                java.util.Map<String, String> detail = new java.util.HashMap<>();
                detail.put("row", String.valueOf(i + 1));
                detail.put("studentNo", item.getStudentNo() != null ? item.getStudentNo() : "");
                detail.put("reason", "系统错误：" + e.getMessage());
                failDetails.add(detail);
            }
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalCount", students.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failDetails", failDetails);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importStudentsFromExcel(Long classId, MultipartFile file) {
        if (classId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "教学班级ID不能为空");
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        // 验证教学班级是否存在
        TeachingClass teachingClass = this.getById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
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
            List<ClassStudentExcel> studentExcels = com.alibaba.excel.EasyExcel.read(file.getInputStream())
                    .head(ClassStudentExcel.class)
                    .sheet(0)
                    .doReadSync();

            if (studentExcels == null || studentExcels.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel中没有数据");
            }

            for (int i = 0; i < studentExcels.size(); i++) {
                ClassStudentExcel excel = studentExcels.get(i);
                try {
                    // 检查必填字段
                    if (StringUtils.isBlank(excel.getStudentNo())) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2)); // Excel行号从2开始（第1行是表头）
                        detail.put("studentNo", "");
                        detail.put("reason", "学号不能为空");
                        failDetails.add(detail);
                        continue;
                    }

                    // 根据学号查询学生
                    QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
                    studentQueryWrapper.eq("student_no", excel.getStudentNo());
                    Student student = studentMapper.selectOne(studentQueryWrapper);
                    if (student == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("studentNo", excel.getStudentNo());
                        detail.put("reason", "学生不存在");
                        failDetails.add(detail);
                        continue;
                    }

                    // 验证姓名（可选）
                    if (StringUtils.isNotBlank(excel.getStudentName()) && !excel.getStudentName().equals(student.getName())) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("studentNo", excel.getStudentNo());
                        detail.put("reason", "姓名不匹配，期望：" + student.getName());
                        failDetails.add(detail);
                        continue;
                    }

                    // 检查是否已绑定
                    if (!bindOrRestoreStudent(classId, student.getId(), teachingClass.getClassName())) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("studentNo", excel.getStudentNo());
                        detail.put("reason", "学生已在班级中");
                        failDetails.add(detail);
                        continue;
                    }
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 2));
                    detail.put("studentNo", excel.getStudentNo() != null ? excel.getStudentNo() : "");
                    detail.put("reason", "系统错误：" + e.getMessage());
                    failDetails.add(detail);
                }
            }

            // 原子导入：任一行失败则整批回滚
            if (failCount > 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,
                        "Excel导入存在 " + failCount + " 条失败，已整体回滚，请修正后重新导入");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", studentExcels.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("failDetails", failDetails);
            return result;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件读取失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importTeachingClassesFromExcel(MultipartFile file) {
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
            List<TeachingClassExcel> classExcels = com.alibaba.excel.EasyExcel.read(file.getInputStream())
                    .head(TeachingClassExcel.class)
                    .sheet(0)
                    .doReadSync();

            if (classExcels == null || classExcels.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel中没有数据");
            }

            for (int i = 0; i < classExcels.size(); i++) {
                TeachingClassExcel excel = classExcels.get(i);
                try {
                    // 检查必填字段
                    if (StringUtils.isAnyBlank(excel.getClassName(), excel.getCourseCode(),
                            excel.getTeacherUsername(), excel.getYearName(), excel.getSemesterName())) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("className", excel.getClassName() != null ? excel.getClassName() : "");
                        detail.put("reason", "必填字段为空（教学班名称、课程代码、教师用户名、学年、学期均必填）");
                        failDetails.add(detail);
                        continue;
                    }

                    // 查找课程（按课程代码）
                    QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
                    courseQueryWrapper.eq("course_code", excel.getCourseCode());
                    Course course = courseMapper.selectOne(courseQueryWrapper);
                    if (course == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("className", excel.getClassName());
                        detail.put("reason", "课程代码不存在：" + excel.getCourseCode());
                        failDetails.add(detail);
                        continue;
                    }

                    // 查找教师（按用户名）
                    QueryWrapper<SysUser> userQueryWrapper = new QueryWrapper<>();
                    userQueryWrapper.eq("username", excel.getTeacherUsername());
                    SysUser teacher = sysUserMapper.selectOne(userQueryWrapper);
                    if (teacher == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("className", excel.getClassName());
                        detail.put("reason", "教师用户名不存在：" + excel.getTeacherUsername());
                        failDetails.add(detail);
                        continue;
                    }

                    // 查找学年学期（按学年名称+学期名称）
                    QueryWrapper<SysDictSchoolYear> termQueryWrapper = new QueryWrapper<>();
                    termQueryWrapper.eq("year_name", excel.getYearName());
                    termQueryWrapper.eq("semester_name", excel.getSemesterName());
                    SysDictSchoolYear schoolYear = sysDictSchoolYearMapper.selectOne(termQueryWrapper);
                    if (schoolYear == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("className", excel.getClassName());
                        detail.put("reason", "学年学期不存在：" + excel.getYearName() + " " + excel.getSemesterName());
                        failDetails.add(detail);
                        continue;
                    }

                    // 检查唯一性约束：课程+教师+学期组合不能重复
                    QueryWrapper<TeachingClass> existQueryWrapper = new QueryWrapper<>();
                    existQueryWrapper.eq("course_id", course.getId());
                    existQueryWrapper.eq("teacher_id", teacher.getId());
                    existQueryWrapper.eq("term_id", schoolYear.getId());
                    Long existCount = this.baseMapper.selectCount(existQueryWrapper);
                    if (existCount > 0) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("className", excel.getClassName());
                        detail.put("reason", "该课程、教师、学期组合已存在教学班");
                        failDetails.add(detail);
                        continue;
                    }

                    // 创建教学班
                    TeachingClass teachingClass = new TeachingClass();
                    teachingClass.setClassName(excel.getClassName());
                    teachingClass.setCourseId(course.getId());
                    teachingClass.setTeacherId(teacher.getId());
                    teachingClass.setTermId(schoolYear.getId());

                    if (this.save(teachingClass)) {
                        successCount++;
                    } else {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("className", excel.getClassName());
                        detail.put("reason", "保存失败");
                        failDetails.add(detail);
                    }
                } catch (Exception e) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 2));
                    detail.put("className", excel.getClassName() != null ? excel.getClassName() : "");
                    detail.put("reason", "系统错误：" + e.getMessage());
                    failDetails.add(detail);
                }
            }

            // 原子导入：任一行失败则整批回滚
            if (failCount > 0) {
                // 拼接失败明细到异常消息中，方便用户定位问题
                StringBuilder msg = new StringBuilder();
                msg.append("Excel导入存在 ").append(failCount).append(" 条失败，已整体回滚，请修正后重新导入");
                msg.append("\n\n失败明细：");
                for (Map<String, String> detail : failDetails) {
                    msg.append("\n第 ").append(detail.get("row")).append(" 行（")
                       .append(detail.get("className")).append("）：")
                       .append(detail.get("reason"));
                }
                throw new BusinessException(ErrorCode.OPERATION_ERROR, msg.toString());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", classExcels.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("failDetails", failDetails);
            return result;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件读取失败: " + e.getMessage());
        }
    }

    private boolean bindOrRestoreStudent(Long classId, Long studentId, String className) {
        ClassStudent existingRelation = classStudentMapper.selectAnyByClassIdAndStudentId(classId, studentId);
        boolean bound;
        if (existingRelation != null) {
            if (existingRelation.getIsDeleted() != null && existingRelation.getIsDeleted() == 0) {
                return false;
            }
            bound = classStudentMapper.restoreById(existingRelation.getId()) > 0;
        } else {
            ClassStudent classStudent = new ClassStudent();
            classStudent.setClassId(classId);
            classStudent.setStudentId(studentId);
            bound = classStudentMapper.insert(classStudent) > 0;
        }
        // 绑定成功后回填学生的班级字段（学生导入时不带班级，绑定教学班时再回填）
        if (bound && StringUtils.isNotBlank(className)) {
            Student updateStudent = new Student();
            updateStudent.setId(studentId);
            updateStudent.setClassName(className);
            studentMapper.updateById(updateStudent);
        }
        return bound;
    }
}
