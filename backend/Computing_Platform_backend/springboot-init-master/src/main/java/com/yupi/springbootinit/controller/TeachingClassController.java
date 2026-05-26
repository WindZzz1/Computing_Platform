package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.student.StudentImportRequest;
import com.yupi.springbootinit.model.dto.teachingClass.ClassStudentBindRequest;
import com.yupi.springbootinit.model.dto.teachingClass.TeachingClassAddRequest;
import com.yupi.springbootinit.model.dto.teachingClass.TeachingClassQueryRequest;
import com.yupi.springbootinit.model.dto.teachingClass.TeachingClassUpdateRequest;
import com.yupi.springbootinit.model.vo.PageResultVO;
import com.yupi.springbootinit.model.vo.StudentVO;
import com.yupi.springbootinit.model.vo.TeachingClassVO;
import com.yupi.springbootinit.service.StudentService;
import com.yupi.springbootinit.service.TeachingClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 教学班级接口
 *
 * @author YU
 */
@RestController
@RequestMapping("/teaching-class")
@Slf4j
public class TeachingClassController {

    @Resource
    private TeachingClassService teachingClassService;

    @Resource
    private StudentService studentService;

    /**
     * 创建教学班级
     *
     * @param teachingClassAddRequest 新增请求
     * @return 教学班级ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Long> addTeachingClass(@RequestBody TeachingClassAddRequest teachingClassAddRequest) {
        Long classId = teachingClassService.createTeachingClass(teachingClassAddRequest);
        return ResultUtils.success(classId);
    }

    /**
     * 更新教学班级
     *
     * @param teachingClassUpdateRequest 更新请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Boolean> updateTeachingClass(@RequestBody TeachingClassUpdateRequest teachingClassUpdateRequest) {
        Boolean result = teachingClassService.updateTeachingClass(teachingClassUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除教学班级
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Boolean> deleteTeachingClass(@RequestBody DeleteRequest deleteRequest) {
        Boolean result = teachingClassService.deleteTeachingClass(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取教学班级
     *
     * @param deleteRequest ID请求
     * @return 教学班级信息
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<TeachingClassVO> getTeachingClassById(@RequestBody DeleteRequest deleteRequest) {
        TeachingClassVO classVO = teachingClassService.getTeachingClassById(deleteRequest.getId());
        return ResultUtils.success(classVO);
    }

    /**
     * 分页查询教学班级
     *
     * @param teachingClassQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<PageResultVO<TeachingClassVO>> pageTeachingClass(@RequestBody TeachingClassQueryRequest teachingClassQueryRequest) {
        Page<TeachingClassVO> classPage = teachingClassService.pageTeachingClass(teachingClassQueryRequest);
        return ResultUtils.success(PageResultVO.from(classPage));
    }

    /**
     * 绑定学生到教学班级
     *
     * @param classStudentBindRequest 绑定请求
     * @return 绑定的学生数量
     */
    @PostMapping("/bind-students")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Integer> bindStudents(@RequestBody ClassStudentBindRequest classStudentBindRequest) {
        Integer count = teachingClassService.bindStudents(classStudentBindRequest);
        return ResultUtils.success(count);
    }

    /**
     * 解绑学生
     *
     * @param classId 教学班级ID
     * @param studentId 学生ID
     * @return 是否成功
     */
    @PostMapping("/unbind-student")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Boolean> unbindStudent(@RequestParam Long classId, @RequestParam Long studentId) {
        Boolean result = teachingClassService.unbindStudent(classId, studentId);
        return ResultUtils.success(result);
    }

    /**
     * 获取教学班级的学生列表
     *
     * @param deleteRequest ID请求
     * @return 学生列表
     */
    @PostMapping("/students")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<List<StudentVO>> getClassStudents(@RequestBody DeleteRequest deleteRequest) {
        List<StudentVO> students = teachingClassService.getClassStudents(deleteRequest.getId());
        return ResultUtils.success(students);
    }

    /**
     * 批量导入学生
     *
     * @param studentImportRequest 导入请求
     * @return 导入结果
     */
    @PostMapping("/import-students")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Map<String, Object>> importStudents(@RequestBody StudentImportRequest studentImportRequest) {
        Map<String, Object> result = studentService.importStudents(studentImportRequest);
        return ResultUtils.success(result);
    }
}
