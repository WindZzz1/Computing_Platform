package com.yupi.springbootinit.model.dto.course;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 课程批量导入请求
 *
 * @author YU
 */
@Data
public class CourseImportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程列表
     */
    private List<CourseAddRequest> courses;
}