package com.yupi.springbootinit.model.dto.weight;

import lombok.Data;

import java.io.Serializable;

// 可配置指标点查询请求

@Data
public class AvailableIndicatorRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID
     */
    private Long courseId;
}
