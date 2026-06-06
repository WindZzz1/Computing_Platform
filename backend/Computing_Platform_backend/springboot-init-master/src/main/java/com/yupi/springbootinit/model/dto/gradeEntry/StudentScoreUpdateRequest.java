package com.yupi.springbootinit.model.dto.gradeEntry;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 学生成绩更新请求
 *
 * @author YU
 */
@Data
public class StudentScoreUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教学班级ID
     */
    private Long classId;

    /**
     * 成绩列表
     */
    private List<ScoreItem> scores;

    @Data
    public static class ScoreItem implements Serializable {
        /**
         * 成绩ID（可选，存在则更新，不存在则新增）
         */
        private Long id;

        /**
         * 学生ID
         */
        private Long studentId;

        /**
         * 考核点ID
         */
        private Long pointId;

        /**
         * 得分
         */
        private BigDecimal score;
    }
}
