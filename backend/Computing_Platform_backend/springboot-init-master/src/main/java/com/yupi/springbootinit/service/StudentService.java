package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.student.StudentImportRequest;
import com.yupi.springbootinit.model.entity.Student;

import java.util.Map;

/**
 * 学生服务
 *
 * @author YU
 */
public interface StudentService extends IService<Student> {

    /**
     * 批量导入学生
     *
     * @param studentImportRequest 导入请求
     * @return 导入结果（成功数量，失败详情）
     */
    Map<String, Object> importStudents(StudentImportRequest studentImportRequest);
}
