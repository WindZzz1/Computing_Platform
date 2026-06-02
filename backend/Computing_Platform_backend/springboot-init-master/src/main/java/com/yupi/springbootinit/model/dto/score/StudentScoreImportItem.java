package com.yupi.springbootinit.model.dto.score;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 学生成绩导入项（Excel行数据）
 *
 * @author YU
 */
@Data
public class StudentScoreImportItem {

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 姓名
     */
    private String studentName;

    /**
     * 考核点成绩映射（考核点ID -> 成绩）
     * 用于存储动态数量的考核点成绩
     */
    private Map<String, BigDecimal> scores;

    /**
     * 行号（用于错误定位）
     */
    private Integer rowNum;

    /**
     * 是否有效（通过校验）
     */
    private Boolean valid;

    /**
     * 错误信息
     */
    private String errorMessage;
}
