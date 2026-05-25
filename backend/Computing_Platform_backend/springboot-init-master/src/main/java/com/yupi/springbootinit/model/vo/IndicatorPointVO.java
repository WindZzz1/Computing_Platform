package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 二级指标点视图对象
 *
 * @author YU
 */
@Data
public class IndicatorPointVO implements Serializable {

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

    /**
     * 所属毕业要求编号
     */
    private String requirementCode;

    /**
     * 所属毕业要求名称
     */
    private String requirementName;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}