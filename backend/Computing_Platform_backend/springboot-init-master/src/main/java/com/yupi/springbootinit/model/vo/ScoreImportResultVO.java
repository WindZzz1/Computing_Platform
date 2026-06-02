package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 成绩导入结果VO
 *
 * @author YU
 */
@Data
public class ScoreImportResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Integer total;

    /**
     * 成功导入数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failedCount;

    /**
     * 是否全部成功
     */
    private Boolean allSuccess;

    /**
     * 错误信息列表
     */
    private List<String> errorMessages;

    /**
     * 教学班级ID
     */
    private Long classId;
}
