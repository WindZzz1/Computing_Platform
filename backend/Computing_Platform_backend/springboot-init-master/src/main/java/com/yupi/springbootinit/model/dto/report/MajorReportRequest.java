package com.yupi.springbootinit.model.dto.report;

import lombok.Data;

import java.io.Serializable;

/**
 * 专业报告查询请求
 *
 * @author YU
 */
@Data
public class MajorReportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 学年学期ID
     */
    private Long termId;

    /**
     * 年级
     */
    private String grade;

    /**
     * 报表类型：RADAR（雷达图数据）、ACCOUNT（穿透式台账）
     */
    private String reportType = "ACCOUNT";
}