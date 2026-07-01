package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.model.dto.student.StudentAddRequest;
import com.yupi.springbootinit.model.dto.student.StudentImportRequest;
import com.yupi.springbootinit.model.dto.student.StudentQueryRequest;
import com.yupi.springbootinit.model.dto.student.StudentUpdateRequest;
import com.yupi.springbootinit.model.entity.Student;
import com.yupi.springbootinit.model.vo.StudentVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 学生服务
 *
 * @author YU
 */
public interface StudentService extends IService<Student> {

    /**
     * 分页查询学生
     *
     * @param studentQueryRequest 查询条件
     * @return 学生分页结果
     */
    Page<StudentVO> pageStudents(StudentQueryRequest studentQueryRequest);

    /**
     * 新增学生
     *
     * @param studentAddRequest 新增请求
     * @return 新增学生ID
     */
    Long addStudent(StudentAddRequest studentAddRequest);

    /**
     * 更新学生
     *
     * @param studentUpdateRequest 更新请求
     * @return 是否更新成功
     */
    Boolean updateStudent(StudentUpdateRequest studentUpdateRequest);

    /**
     * 删除学生（已被教学班绑定的学生不允许删除）
     *
     * @param id 学生ID
     * @return 是否删除成功
     */
    Boolean deleteStudent(Long id);

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
