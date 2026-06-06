package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 穿透式台账数据VO
 * 包含完整的五层数据追溯：专业指标点 -> 课程 -> 课程目标 -> 考核点 -> 学生原始得分
 *
 * @author YU
 */
@Data
public class PenetrationAccountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 第一层：专业基本信息
     */
    private MajorAccountInfo majorInfo;

    /**
     * 第二层：课程级数据
     */
    private List<CourseAccountInfo> courses;

    /**
     * 第三层：学生课程目标达成度
     */
    private List<StudentObjectiveAccount> studentObjectives;

    /**
     * 第四层：考核点明细
     */
    private List<AssessmentPointAccount> assessmentPoints;

    /**
     * 第五层：学生原始得分
     */
    private List<StudentScoreAccount> studentScores;
}