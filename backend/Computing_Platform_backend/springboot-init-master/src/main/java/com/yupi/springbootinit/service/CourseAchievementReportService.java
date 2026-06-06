package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.TeachingClass;
import com.yupi.springbootinit.model.vo.report.CourseAchievementReportVO;

/**
 * 课程达成度报表服务接口
 *
 * @author YU
 */
public interface CourseAchievementReportService extends IService<TeachingClass> {

    /**
     * 生成课程达成度报表数据
     *
     * @param classId 教学班级ID
     * @return 报表数据
     */
    CourseAchievementReportVO generateReportData(Long classId);

    /**
     * 导出Excel格式报表
     *
     * @param classId 教学班级ID
     * @return Excel文件字节数组
     */
    byte[] exportExcelReport(Long classId);

    /**
     * 导出PDF格式报表
     *
     * @param classId 教学班级ID
     * @return PDF文件字节数组
     */
    byte[] exportPdfReport(Long classId);

    /**
     * 验证报表生成权限
     *
     * @param classId 教学班级ID
     * @param userId  当前用户ID
     * @return 是否有权限
     */
    boolean validateReportPermission(Long classId, Long userId);
}