package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 学院字典简化视图对象
 *
 * @author YU
 */
@Data
public class SysDictCollegeSimpleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 学院名称
     */
    private String collegeName;
}
