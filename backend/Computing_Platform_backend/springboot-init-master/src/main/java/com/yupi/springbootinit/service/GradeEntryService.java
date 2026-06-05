package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.model.dto.gradeEntry.GradeEntryImportRequest;
import com.yupi.springbootinit.model.dto.gradeEntry.GradeEntryQueryRequest;
import com.yupi.springbootinit.model.dto.gradeEntry.GradeEntryTemplateRequest;
import com.yupi.springbootinit.model.dto.gradeEntry.StudentScoreUpdateRequest;
import com.yupi.springbootinit.model.vo.gradeEntry.GradeImportResultVO;
import com.yupi.springbootinit.model.vo.gradeEntry.StudentScoreVO;

import javax.servlet.http.HttpServletResponse;

/**
 * 成绩录入服务
 *
 * @author YU
 */
public interface GradeEntryService {

    /**
     * 生成并下载成绩录入模板
     *
     * @param request 请求参数
     * @param response HTTP响应
     */
    void generateAndDownloadTemplate(GradeEntryTemplateRequest request, HttpServletResponse response);

    /**
     * 导入成绩数据
     *
     * @param request 导入请求
     * @return 导入结果
     */
    GradeImportResultVO importGrades(GradeEntryImportRequest request);

    /**
     * 查询成绩数据
     *
     * @param request 查询请求
     * @return 成绩数据分页
     */
    Page<StudentScoreVO> queryGrades(GradeEntryQueryRequest request);

    /**
     * 更新成绩数据
     *
     * @param request 更新请求
     * @return 是否成功
     */
    Boolean updateGrades(StudentScoreUpdateRequest request);

    /**
     * 删除教学班级的所有成绩
     *
     * @param classId 教学班级ID
     * @return 是否成功
     */
    Boolean deleteClassGrades(Long classId);
}
