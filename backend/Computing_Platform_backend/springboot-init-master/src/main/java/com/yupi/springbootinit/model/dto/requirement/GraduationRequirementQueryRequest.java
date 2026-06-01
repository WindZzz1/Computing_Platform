package com.yupi.springbootinit.model.dto.requirement;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 毕业要求查询请求
 *
 * @author YU
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GraduationRequirementQueryRequest extends PageRequest {

    /**
     * 毕业要求编号
     */
    private String requirementCode;

    /**
     * 毕业要求名称
     */
    private String requirementName;

    /**
     * 所属专业ID
     */
    private Long majorId;

    /**
     * 创建时间开始
     */
    private Date createTimeStart;

    /**
     * 创建时间结束
     */
    private Date createTimeEnd;
}