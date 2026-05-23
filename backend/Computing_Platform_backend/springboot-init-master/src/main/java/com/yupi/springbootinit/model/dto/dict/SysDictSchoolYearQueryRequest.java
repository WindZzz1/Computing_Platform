package com.yupi.springbootinit.model.dto.dict;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学年学期字典查询请求
 *
 * @author YU
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictSchoolYearQueryRequest extends PageRequest {

    /**
     * 学年名称
     */
    private String yearName;

    /**
     * 学期名称
     */
    private String semesterName;
}