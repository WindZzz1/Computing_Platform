package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.teachingClass.ClassStudentBindRequest;
import com.yupi.springbootinit.model.dto.teachingClass.ClassStudentImportRequest;
import com.yupi.springbootinit.model.dto.teachingClass.TeachingClassAddRequest;
import com.yupi.springbootinit.model.dto.teachingClass.TeachingClassQueryRequest;
import com.yupi.springbootinit.model.dto.teachingClass.TeachingClassUpdateRequest;
import com.yupi.springbootinit.model.vo.PageResultVO;
import com.yupi.springbootinit.model.vo.StudentVO;
import com.yupi.springbootinit.model.vo.TeachingClassVO;
import com.yupi.springbootinit.service.AchievementCalculationService;
import com.yupi.springbootinit.service.TeachingClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
    private AchievementCalculationService achievementCalculationService;

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
     * 批量导入学生到教学班级
     *
     * @param classStudentImportRequest 导入请求
     * @return 导入结果
     */
    @PostMapping("/import-students")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Map<String, Object>> importStudents(@RequestBody ClassStudentImportRequest classStudentImportRequest) {
        Map<String, Object> result = teachingClassService.importStudents(classStudentImportRequest);
        return ResultUtils.success(result);
    }

    /**
     * 锁定教学班级成绩
     * 锁定后无法修改成绩和课程配置
     *
     * @param id 教学班级ID
     * @return 是否成功
     */
    @PostMapping("/lock/{id}")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Boolean> lockScores(@PathVariable Long id) {

        log.info("锁定教学班级成绩，班级ID：{}", id);

        Boolean success = achievementCalculationService.lockScores(id);

        log.info("锁定教学班级成绩{}", success ? "成功" : "失败");

        return ResultUtils.success(success);
    }

    /**
     * 解锁教学班级成绩
     * 只有教务管理员可以解锁
     *
     * @param id 教学班级ID
     * @return 是否成功
     */
    @PostMapping("/unlock/{id}")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Boolean> unlockScores(@PathVariable Long id) {

        log.info("解锁教学班级成绩，班级ID：{}", id);

        Boolean success = achievementCalculationService.unlockScores(id);

        log.info("解锁教学班级成绩{}", success ? "成功" : "失败");

        return ResultUtils.success(success);
    }
}
