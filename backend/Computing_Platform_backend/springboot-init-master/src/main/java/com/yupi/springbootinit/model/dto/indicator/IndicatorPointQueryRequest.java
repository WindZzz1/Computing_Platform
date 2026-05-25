package com.yupi.springbootinit.model.dto.indicator;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 二级指标点查询请求
 *
 * @author YU
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IndicatorPointQueryRequest extends PageRequest {

    /**
     * 指标点编号
     */
    private String indicatorCode;

    /**
     * 指标点名称
     */
    private String indicatorName;

    /**
     * 所属毕业要求ID
     */
    private Long requirementId;

    /**
     * 创建时间开始
     */
    private Date createTimeStart;

    /**
     * 创建时间结束
     */
    private Date createTimeEnd;
}