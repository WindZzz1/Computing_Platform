package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.student.StudentImportRequest;
import com.yupi.springbootinit.service.StudentService;
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
