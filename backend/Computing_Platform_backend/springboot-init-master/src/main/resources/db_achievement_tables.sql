-- 学生课程目标达成度表（一级达成度）
CREATE TABLE IF NOT EXISTS student_objective_achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    teaching_class_id BIGINT NOT NULL COMMENT '教学班级ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    objective_id BIGINT NOT NULL COMMENT '课程目标ID',
    objective_code VARCHAR(50) COMMENT '课程目标编号',
    objective_name VARCHAR(255) COMMENT '课程目标名称',
    achievement DECIMAL(10,4) COMMENT '一级达成度值',
    calculate_time DATETIME COMMENT '计算时间',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_teaching_class_id (teaching_class_id),
    INDEX idx_student_id (student_id),
    INDEX idx_objective_id (objective_id),
    INDEX idx_class_student_objective (teaching_class_id, student_id, objective_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生课程目标达成度表';

-- 课程指标点达成度表（二级达成度）
CREATE TABLE IF NOT EXISTS course_indicator_achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    teaching_class_id BIGINT NOT NULL COMMENT '教学班级ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    indicator_id BIGINT NOT NULL COMMENT '指标点ID',
    indicator_code VARCHAR(50) COMMENT '指标点编号',
    indicator_name VARCHAR(255) COMMENT '指标点名称',
    achievement DECIMAL(10,4) COMMENT '二级达成度值',
    calculate_time DATETIME COMMENT '计算时间',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_teaching_class_id (teaching_class_id),
    INDEX idx_course_id (course_id),
    INDEX idx_indicator_id (indicator_id),
    INDEX idx_class_indicator (teaching_class_id, indicator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程指标点达成度表';

-- 成绩计算状态表
CREATE TABLE IF NOT EXISTS grade_calculation_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    teaching_class_id BIGINT NOT NULL COMMENT '教学班级ID',
    is_locked TINYINT DEFAULT 0 COMMENT '是否已锁定：0-未锁定，1-已锁定',
    calc_status TINYINT DEFAULT 0 COMMENT '计算状态：0-未计算，1-计算中，2-计算完成，3-计算失败',
    calc_start_time DATETIME COMMENT '计算开始时间',
    calc_end_time DATETIME COMMENT '计算完成时间',
    lock_time DATETIME COMMENT '锁定时间',
    locked_by BIGINT COMMENT '锁定人ID',
    lock_reason VARCHAR(500) COMMENT '锁定原因',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    UNIQUE INDEX uk_teaching_class_id (teaching_class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩计算状态表';
