-- 学生成绩表
CREATE TABLE IF NOT EXISTS student_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    teaching_class_id BIGINT NOT NULL COMMENT '教学班级ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    assessment_point_id BIGINT NOT NULL COMMENT '考核点ID',
    score DECIMAL(10,2) COMMENT '得分',
    is_locked TINYINT DEFAULT 0 COMMENT '是否锁定：0-未锁定，1-已锁定',
    entered_by BIGINT COMMENT '录入人ID',
    enter_time DATETIME COMMENT '录入时间',
    update_time DATETIME COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_teaching_class_id (teaching_class_id),
    INDEX idx_student_id (student_id),
    INDEX idx_assessment_point_id (assessment_point_id),
    INDEX idx_class_student_point (teaching_class_id, student_id, assessment_point_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生成绩表';
