package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.StudentMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.model.dto.student.StudentImportRequest;
import com.yupi.springbootinit.model.entity.Student;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
                if (StringUtils.isAnyBlank(item.getStudentNo(), item.getName(), item.getMajorCode())) {
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
                student.setName(item.getName());
                student.setMajorId(major.getId());
                student.setCollegeId(major.getCollegeId());
                student.setClassName(item.getClassName());
                student.setPhone(item.getPhone());
                student.setEmail(item.getEmail());

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
}
