package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.student.StudentAddRequest;
import com.yupi.springbootinit.model.dto.student.StudentImportRequest;
import com.yupi.springbootinit.model.dto.student.StudentQueryRequest;
import com.yupi.springbootinit.model.dto.student.StudentUpdateRequest;
import com.yupi.springbootinit.model.vo.PageResultVO;
import com.yupi.springbootinit.model.vo.StudentVO;
import com.yupi.springbootinit.service.StudentService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 学生接口
 *
 * @author YU
 */
@RestController
@RequestMapping("/student")
@Slf4j
public class StudentController {

    @Resource
    private StudentService studentService;

    /**
     * 分页查询系统学生库
     *
     * @param studentQueryRequest 查询条件
     * @return 学生分页结果
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<PageResultVO<StudentVO>> pageStudents(@RequestBody StudentQueryRequest studentQueryRequest) {
        Page<StudentVO> studentPage = studentService.pageStudents(studentQueryRequest);
        return ResultUtils.success(PageResultVO.from(studentPage));
    }

    /**
     * 新增学生
     *
     * @param studentAddRequest 新增请求
     * @return 新增学生ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Long> addStudent(@RequestBody StudentAddRequest studentAddRequest) {
        Long id = studentService.addStudent(studentAddRequest);
        return ResultUtils.success(id);
    }

    /**
     * 更新学生
     *
     * @param studentUpdateRequest 更新请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Boolean> updateStudent(@RequestBody StudentUpdateRequest studentUpdateRequest) {
        Boolean result = studentService.updateStudent(studentUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除学生（已被教学班绑定的学生不允许删除）
     *
     * @param deleteRequest 删除请求（含id）
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Boolean> deleteStudent(@RequestBody DeleteRequest deleteRequest) {
        Boolean result = studentService.deleteStudent(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 通过Excel批量导入学生
     *
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/import/excel")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Map<String, Object>> importStudentsFromExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = studentService.importStudentsFromExcel(file);
        return ResultUtils.success(result);
    }

    /**
     * 下载学生导入模板
     *
     * @return Excel文件
     */
    @GetMapping("/template")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    @com.yupi.springbootinit.annotation.NoLog
    public ResponseEntity<org.springframework.core.io.Resource> downloadStudentTemplate() {
        // 直接返回Resource，让Spring处理
        org.springframework.core.io.ClassPathResource resource =
            new org.springframework.core.io.ClassPathResource("templates/student_template.xlsx");

        String filename = "学生导入模板.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.set("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    /**
     * 批量导入学生（JSON方式）
     *
     * @param studentImportRequest 导入请求
     * @return 导入结果
     */
    @PostMapping("/import")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Map<String, Object>> importStudents(@RequestBody StudentImportRequest studentImportRequest) {
        Map<String, Object> result = studentService.importStudents(studentImportRequest);
        return ResultUtils.success(result);
    }
}
