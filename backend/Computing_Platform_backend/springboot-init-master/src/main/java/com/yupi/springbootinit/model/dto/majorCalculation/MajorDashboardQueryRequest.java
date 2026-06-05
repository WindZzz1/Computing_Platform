package com.yupi.springbootinit.model.dto.majorCalculation;

import lombok.Data;

import java.io.Serializable;

/**
 * 专业级监控看板查询请求
 *
 * @author YU
 */
@Data
public class MajorDashboardQueryRequest implements Serializable {

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
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long pageSize;
}
