-- 专业级指标点达成度表（三级达成度）
CREATE TABLE IF NOT EXISTS major_indicator_achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    major_id BIGINT NOT NULL COMMENT '专业ID',
    term_id BIGINT COMMENT '学年学期ID',
    grade VARCHAR(20) COMMENT '年级',
    indicator_id BIGINT NOT NULL COMMENT '指标点ID',
    indicator_code VARCHAR(50) COMMENT '指标点编号',
    indicator_name VARCHAR(255) COMMENT '指标点名称',
    requirement_id BIGINT COMMENT '毕业要求ID',
    requirement_code VARCHAR(50) COMMENT '毕业要求编号',
    requirement_name VARCHAR(255) COMMENT '毕业要求名称',
    achievement DECIMAL(10,4) COMMENT '三级达成度值',
    calculate_time DATETIME COMMENT '计算时间',
    calc_status TINYINT DEFAULT 0 COMMENT '计算状态：0-未计算，1-计算中，2-计算完成，3-计算失败',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_major_term_grade (major_id, term_id, grade),
    INDEX idx_indicator_id (indicator_id),
    INDEX idx_requirement_id (requirement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业级指标点达成度表';

-- 专业级计算汇总表
CREATE TABLE IF NOT EXISTS major_calculation_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    major_id BIGINT NOT NULL COMMENT '专业ID',
    term_id BIGINT COMMENT '学年学期ID',
    grade VARCHAR(20) COMMENT '年级',
    total_courses INT DEFAULT 0 COMMENT '涉及课程总数',
    calculated_courses INT DEFAULT 0 COMMENT '已计算课程数',
    locked_courses INT DEFAULT 0 COMMENT '已锁定课程数',
    calc_status TINYINT DEFAULT 0 COMMENT '计算状态：0-未计算，1-计算中，2-计算完成，3-计算失败',
    calc_start_time DATETIME COMMENT '计算开始时间',
    calc_end_time DATETIME COMMENT '计算完成时间',
    calculated_by BIGINT COMMENT '计算人ID',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    UNIQUE INDEX uk_major_term_grade (major_id, term_id, grade)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业级计算汇总表';