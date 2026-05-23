package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学年学期字典视图对象
 *
 * @author YU
 */
@Data
public class SysDictSchoolYearVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 学年名称（如2023-2024学年）
     */
    private String yearName;

    /**
     * 学期名称（如第一学期）
     */
    private String semesterName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}