package com.yupi.springbootinit.model.dto.course;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 课程查询请求
 *
 * @author YU
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseQueryRequest extends PageRequest {

    /**
     * 课程代码
     */
    private String courseCode;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程性质
     */
    private String courseNature;

    /**
     * 所属专业ID
     */
    private Long majorId;

    /**
     * 创建时间开始
     */
    private Date createTimeStart;

    /**
     * 创建时间结束
     */
    private Date createTimeEnd;
}