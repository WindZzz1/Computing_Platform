package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

// 指标点视图对象

@Data
public class IndicatorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 指标点ID
     */
    private Long id;

    /**
     * 毕业要求ID
     */
    private Long reqId;

    /**
     * 指标点编号，例如 1.1、1.2
     */
    private String indicatorCode;

    /**
     * 指标点名称
     */
    private String indicatorName;

    /**
     * 指标点描述
     */
    private String indicatorDesc;
}
