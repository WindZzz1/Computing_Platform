package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 专业字典视图对象
 *
 * @author YU
 */
@Data
public class SysDictMajorVO implements Serializable {

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

    /**
     * 所属学院名称
     */
    private String collegeName;

}