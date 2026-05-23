package com.yupi.springbootinit.model.dto.dict;

import lombok.Data;

import java.io.Serializable;

/**
 * 学年学期字典新增请求
 *
 * @author YU
 */
@Data
public class SysDictSchoolYearAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学年名称（如2023-2024学年）
     */
    private String yearName;

    /**
     * 学期名称（如第一学期）
     */
    private String semesterName;
}