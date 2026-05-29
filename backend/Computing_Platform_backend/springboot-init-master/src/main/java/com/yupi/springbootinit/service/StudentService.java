package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.student.StudentImportRequest;
import com.yupi.springbootinit.model.entity.Student;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 通过Excel批量导入学生
     *
     * @param file Excel文件
     * @return 导入结果
     */
    Map<String, Object> importStudentsFromExcel(MultipartFile file);

    /**
     * 生成学生导入模板
     *
     * @return Excel文件字节数组
     */
    byte[] generateStudentTemplate();
}
