package com.yupi.springbootinit.model.dto.dict;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 学院字典查询请求
 *
 * @author YU
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictCollegeQueryRequest extends PageRequest {

    /**
     * 学院名称
     */
    private String collegeName;

}