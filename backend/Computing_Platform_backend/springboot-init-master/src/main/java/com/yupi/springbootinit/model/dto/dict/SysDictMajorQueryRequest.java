package com.yupi.springbootinit.model.dto.dict;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 专业字典查询请求
 *
 * @author YU
 */
@Data
@EqualsAndHashCode(callSuper = true)
//@NoArgsConstructor
public class SysDictMajorQueryRequest extends PageRequest {

    /**
     * 专业代码
     */
    private String majorCode;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 所属学院ID
     */
    private Long collegeId;
}