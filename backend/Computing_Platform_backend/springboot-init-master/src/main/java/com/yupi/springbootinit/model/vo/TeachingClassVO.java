package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 教学班级视图对象
 *
 * @author YU
 */
@Data
public class TeachingClassVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程代码
     */
    private String courseCode;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 主讲教师ID
     */
    private Long teacherId;

    /**
     * 主讲教师名称
     */
    private String teacherName;

    /**
     * 学年ID
     */
    private Long schoolYearId;

    /**
     * 学年名称
     */
    private String schoolYearName;

    /**
     * 学期（1/2）
     */
    private Integer semester;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 所属专业ID
     */
    private Long majorId;

    /**
     * 所属专业名称
     */
    private String majorName;

    /**
     * 学生数量
     */
    private Integer studentCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}