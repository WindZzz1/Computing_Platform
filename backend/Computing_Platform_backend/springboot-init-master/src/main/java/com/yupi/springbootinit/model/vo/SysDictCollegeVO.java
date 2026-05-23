package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学院字典视图对象
 *
 * @author YU
 */
@Data
public class SysDictCollegeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 学院名称
     */
    private String collegeName;

    /**
     * 学院编码
     */
    private String collegeCode;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}