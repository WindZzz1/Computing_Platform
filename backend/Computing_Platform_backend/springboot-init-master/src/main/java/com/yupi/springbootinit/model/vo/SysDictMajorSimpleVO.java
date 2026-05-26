package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 专业字典简化视图对象
 *
 * @author YU
 */
@Data
public class SysDictMajorSimpleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 专业名称
     */
    private String majorName;
}
