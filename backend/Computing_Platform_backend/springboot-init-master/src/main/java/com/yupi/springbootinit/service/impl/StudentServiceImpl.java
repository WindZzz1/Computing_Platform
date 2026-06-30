package com.yupi.springbootinit.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.StudentMapper;
import com.yupi.springbootinit.mapper.SysDictCollegeMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.model.dto.student.StudentImportRequest;
import com.yupi.springbootinit.model.dto.student.StudentQueryRequest;
import com.yupi.springbootinit.model.entity.Student;
import com.yupi.springbootinit.model.entity.SysDictCollege;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.excel.StudentExcel;
import com.yupi.springbootinit.model.vo.StudentVO;
import com.yupi.springbootinit.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private SysDictMajorMapper sysDictMajorMapper;

    @Resource
    private SysDictCollegeMapper sysDictCollegeMapper;

    @Override
    public Page<StudentVO> pageStudents(StudentQueryRequest studentQueryRequest) {
        long current = studentQueryRequest == null ? 1 : studentQueryRequest.getCurrent();
        long size = studentQueryRequest == null ? 10 : studentQueryRequest.getPageSize();

        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        if (studentQueryRequest != null) {
            queryWrapper.like(StringUtils.isNotBlank(studentQueryRequest.getStudentNo()), "student_no", studentQueryRequest.getStudentNo());
            queryWrapper.like(StringUtils.isNotBlank(studentQueryRequest.getStudentName()), "student_name", studentQueryRequest.getStudentName());
            queryWrapper.eq(studentQueryRequest.getMajorId() != null, "major_id", studentQueryRequest.getMajorId());
            queryWrapper.like(StringUtils.isNotBlank(studentQueryRequest.getClassName()), "class_name", studentQueryRequest.getClassName());
        }
        queryWrapper.orderByDesc("id");

        Page<Student> studentPage = this.page(new Page<>(current, size), queryWrapper);
        Page<StudentVO> studentVOPage = new Page<>(current, size, studentPage.getTotal());
        studentVOPage.setRecords(studentPage.getRecords().stream().map(this::getStudentVO).collect(java.util.stream.Collectors.toList()));
        return studentVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importStudents(StudentImportRequest studentImportRequest) {
        if (studentImportRequest == null || studentImportRequest.getStudents() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "导入数据不能为空");
        }

        List<StudentImportRequest.StudentItem> students = studentImportRequest.getStudents();
        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failDetails = new ArrayList<>();

        for (int i = 0; i < students.size(); i++) {
            StudentImportRequest.StudentItem item = students.get(i);
            try {
                // 检查必填字段
                if (StringUtils.isAnyBlank(item.getStudentNo(), item.getStudentName(), item.getMajorCode())) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 1));
                    detail.put("studentNo", item.getStudentNo() != null ? item.getStudentNo() : "");
                    detail.put("reason", "必填字段为空");
                    failDetails.add(detail);
                    continue;
                }

                // 检查学号是否已存在
                QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("student_no", item.getStudentNo());
                long count = this.baseMapper.selectCount(queryWrapper);
                if (count > 0) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 1));
                    detail.put("studentNo", item.getStudentNo());
                    detail.put("reason", "学号已存在");
                    failDetails.add(detail);
                    continue;
                }

                // 根据专业代码查询专业ID
                QueryWrapper<SysDictMajor> majorQueryWrapper = new QueryWrapper<>();
                majorQueryWrapper.eq("major_code", item.getMajorCode());
                SysDictMajor major = sysDictMajorMapper.selectOne(majorQueryWrapper);
                if (major == null) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 1));
                    detail.put("studentNo", item.getStudentNo());
                    detail.put("reason", "专业代码不存在");
                    failDetails.add(detail);
                    continue;
                }

                // 创建学生
                Student student = new Student();
                student.setStudentNo(item.getStudentNo());
                student.setName(item.getStudentName());
                student.setGrade(item.getGrade());
                student.setMajorId(major.getId());
                student.setClassName(item.getClassName());

                if (this.save(student)) {
                    successCount++;
                } else {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 1));
                    detail.put("studentNo", item.getStudentNo());
                    detail.put("reason", "保存失败");
                    failDetails.add(detail);
                }
            } catch (Exception e) {
                failCount++;
                Map<String, String> detail = new HashMap<>();
                detail.put("row", String.valueOf(i + 1));
                detail.put("studentNo", item.getStudentNo() != null ? item.getStudentNo() : "");
                detail.put("reason", e.getMessage());
                failDetails.add(detail);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", students.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failDetails", failDetails);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importStudentsFromExcel(MultipartFile file) {
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
            List<StudentExcel> studentExcels = EasyExcel.read(file.getInputStream())
                    .head(StudentExcel.class)
                    .sheet(0)
                    .doReadSync();

            if (studentExcels == null || studentExcels.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel中没有数据");
            }

            for (int i = 0; i < studentExcels.size(); i++) {
                StudentExcel excel = studentExcels.get(i);
                try {
                    // 检查必填字段
                    if (StringUtils.isAnyBlank(excel.getStudentNo(), excel.getStudentName(), excel.getMajorCode())) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("studentNo", excel.getStudentNo() != null ? excel.getStudentNo() : "");
                        detail.put("reason", "必填字段为空");
                        failDetails.add(detail);
                        continue;
                    }

                    // 检查学号是否已存在
                    QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("student_no", excel.getStudentNo());
                    long count = this.baseMapper.selectCount(queryWrapper);
                    if (count > 0) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("studentNo", excel.getStudentNo());
                        detail.put("reason", "学号已存在");
                        failDetails.add(detail);
                        continue;
                    }

                    // 根据专业代码查询专业ID
                    QueryWrapper<SysDictMajor> majorQueryWrapper = new QueryWrapper<>();
                    majorQueryWrapper.eq("major_code", excel.getMajorCode());
                    SysDictMajor major = sysDictMajorMapper.selectOne(majorQueryWrapper);
                    if (major == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("studentNo", excel.getStudentNo());
                        detail.put("reason", "专业代码不存在");
                        failDetails.add(detail);
                        continue;
                    }

                    // 创建学生
                    Student student = new Student();
                    student.setStudentNo(excel.getStudentNo());
                    student.setName(excel.getStudentName());
                    student.setGrade(excel.getGrade());
                    student.setMajorId(major.getId());
                    student.setClassName(excel.getClassName());

                    if (this.save(student)) {
                        successCount++;
                    } else {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("studentNo", excel.getStudentNo());
                        detail.put("reason", "保存失败");
                        failDetails.add(detail);
                    }
                } catch (Exception e) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 2));
                    detail.put("studentNo", excel.getStudentNo() != null ? excel.getStudentNo() : "");
                    detail.put("reason", e.getMessage());
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

        } catch (BusinessException e) {
            // 业务异常（含原子导入的整批回滚）原样抛出，避免被下面的"文件读取失败"误包装
            throw e;
        } catch (Exception e) {
            log.error("文件读取失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateStudentTemplate() {
        String templatePath = "/templates/student_template.xlsx";

        // 方式1: 使用 ClassPathResource
        try {
            ClassPathResource resource = new ClassPathResource("templates/student_template.xlsx");
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

                    // 验证文件魔数
                    if (templateBytes.length >= 2) {
                        String magicNumber = String.format("%02X%02X", templateBytes[0], templateBytes[1]);
                        log.info("静态模板读取成功，大小: {} bytes, 魔数: {}", templateBytes.length, magicNumber);
                    }
                    return templateBytes;
                }
            }
        } catch (Exception e) {
            log.warn("使用ClassPathResource读取模板失败: {}", e.getMessage());
        }

        // 方式2: 尝试使用 ClassLoader.getResourceAsStream
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/student_template.xlsx")) {
            if (inputStream != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                byte[] templateBytes = buffer.toByteArray();

                if (templateBytes.length >= 2) {
                    String magicNumber = String.format("%02X%02X", templateBytes[0], templateBytes[1]);
                    log.info("使用ClassLoader读取静态模板成功，大小: {} bytes, 魔数: {}", templateBytes.length, magicNumber);
                }
                return templateBytes;
            }
        } catch (Exception e) {
            log.warn("使用ClassLoader读取模板失败: {}", e.getMessage());
        }

        // 静态模板不存在时，动态生成
        log.info("静态模板不存在，使用动态生成");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            EasyExcel.write(outputStream, StudentExcel.class)
                    .sheet("学生导入模板")
                    .doWrite(new ArrayList<>());
            byte[] result = outputStream.toByteArray();
            log.info("动态生成模板成功，大小: {} bytes", result.length);
            return result;
        } catch (Exception e) {
            log.error("动态生成模板失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成模板失败");
        }
    }

    private StudentVO getStudentVO(Student student) {
        if (student == null) {
            return null;
        }

        StudentVO studentVO = new StudentVO();
        BeanUtils.copyProperties(student, studentVO);

        if (student.getMajorId() != null) {
            SysDictMajor major = sysDictMajorMapper.selectById(student.getMajorId());
            if (major != null) {
                studentVO.setMajorName(major.getMajorName());
                studentVO.setMajorId(major.getId());

                if (major.getCollegeId() != null) {
                    SysDictCollege college = sysDictCollegeMapper.selectById(major.getCollegeId());
                    if (college != null) {
                        studentVO.setCollegeId(college.getId());
                        studentVO.setCollegeName(college.getCollegeName());
                    }
                }
            }
        }

        return studentVO;
    }
}
