package com.yupi.springbootinit.model.dto.indicator;

import lombok.Data;

import java.io.Serializable;

/**
 * 二级指标点更新请求
 *
 * @author YU
 */
@Data
public class IndicatorPointUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

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