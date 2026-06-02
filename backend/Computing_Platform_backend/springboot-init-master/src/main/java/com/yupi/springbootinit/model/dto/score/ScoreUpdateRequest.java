package com.yupi.springbootinit.model.dto.score;

import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 成绩更新请求
 *
 * @author YU
 */
@Data
public class ScoreUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成绩记录ID
     */
    @NotNull(message = "成绩记录ID不能为空")
    private Long id;

    /**
     * 实际得分
     */
    @NotNull(message = "得分不能为空")
    @DecimalMin(value = "0.0", message = "得分不能为负数")
    private BigDecimal actualScore;
}
