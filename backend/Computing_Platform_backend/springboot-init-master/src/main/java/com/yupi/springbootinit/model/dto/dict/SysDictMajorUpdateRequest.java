package com.yupi.springbootinit.model.dto.dict;

import lombok.Data;

import java.io.Serializable;

/**
 * 专业字典更新请求
 *
 * @author YU
 */
@Data
public class SysDictMajorUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

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