package com.yupi.springbootinit.model.dto.indicator;

import lombok.Data;

import java.io.Serializable;

/**
 * 二级指标点新增请求
 *
 * @author YU
 */
@Data
public class IndicatorPointAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 指标点编号(如1.1、1.2、1.3)
     */
    private String indicatorCode;

    /**
     * 指标点名称
     */
    private String indicatorName;

    /**
     * 指标点描述
     */
    private String description;

    /**
     * 所属毕业要求ID
     */
    private Long requirementId;

}