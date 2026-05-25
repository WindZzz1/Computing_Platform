package com.yupi.springbootinit.model.dto.dict;

import lombok.Data;

import java.io.Serializable;

/**
 * 学院字典更新请求
 *
 * @author YU
 */
@Data
public class SysDictCollegeUpdateRequest implements Serializable {

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