package com.yupi.springbootinit.model.dto.report;

import lombok.Data;

import java.io.Serializable;

/**
 * 课程达成度报表查询请求
 *
 * @author YU
 */
@Data
public class CourseAchievementReportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教学班级ID
     */
    private Long classId;

    /**
     * 导出格式：EXCEL、PDF
     */
    private String exportFormat = "EXCEL";

    /**
     * 是否包含学生明细
     */
    private Boolean includeStudentDetails = true;

    /**
     * 是否包含指标点达成度
     */
    private Boolean includeIndicatorAchievement = true;
}