package com.yupi.springbootinit.model.dto.dict;

import lombok.Data;

import java.io.Serializable;

/**
 * 学院字典新增请求
 *
 * @author YU
 */
@Data
public class SysDictCollegeAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学院名称
     */
    private String collegeName;

}